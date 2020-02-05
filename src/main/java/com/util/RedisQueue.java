package com.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.util.SafeEncoder;

public class RedisQueue<E>
{
	public interface ISerializer<E>
	{
		byte[] encode(E e);

		E decode(byte[] buffer);
	}

	public interface Action<T>
	{
		void run(T a);
	}

	private final static Logger LOGGER = LoggerFactory.getLogger(RedisQueue.class);

	private String queueName;

	private byte[] queueKey;

	private int batchCount;

	private long minIdleTimeMs;

	private Action<List<E>> fireEvent;

	private ISerializer<E> serializer;

	private Thread[] monitorThreads;

	private Object syncObject = new Object();

	public RedisQueue(String queueName, int batchCount, long minIdleTimeMs, int monitorThreadCount, ISerializer<E> serializer, final Action<List<E>> fireEvent)
	{
		this.queueName = queueName;
		this.queueKey = SafeEncoder.encode(queueName);

		if (batchCount <= 0)
		{
			batchCount = 10;
		}

		this.batchCount = batchCount;

		if (minIdleTimeMs <= 0)
		{
			minIdleTimeMs = 10;
		}

		this.minIdleTimeMs = minIdleTimeMs;

		this.serializer = serializer;

		this.fireEvent = fireEvent;

		if (monitorThreadCount <= 0)
		{
			monitorThreadCount = 1;
		}

		monitorThreads = new Thread[monitorThreadCount];

		for (int i = 0; i < monitorThreadCount; i++)
		{
			Thread monitorThread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					doRun();
				}
			});

			monitorThread.setName(String.format("%s-monitorThread-%s", queueName, i));
			monitorThread.setDaemon(true);
			monitorThread.start();
			monitorThreads[i] = monitorThread;
		}

	}

	private List<E> pop(int count)
	{
		Jedis jedis = null;
		try
		{
			jedis = RedisUtil.getJedis();

			long beforeLength = 0;
			long afterLength = 0;

			if (LOGGER.isInfoEnabled())
			{
				beforeLength = jedis.llen(this.queueKey);
			}

			List<Response<byte[]>> responses = new ArrayList<Response<byte[]>>(count);
			Pipeline pipeline = jedis.pipelined();
			for (int i = 0; i < count; i++)
			{
				Response<byte[]> response = pipeline.lpop(this.queueKey);
				responses.add(response);
			}

			pipeline.sync();
			// List<Object> resultBuffer = pipeline.syncAndReturnAll();
			// List<byte[]> resultBuffer = jedis.lrange(queueKey, 0, count -
			// 1);
			// jedis.ltrim(queueKey, count, -1);

			if (LOGGER.isInfoEnabled())
			{
				afterLength = jedis.llen(this.queueKey);
			}

			List<E> result = new ArrayList<E>(count);

			for (Response<byte[]> response : responses)
			{
				try
				{
					byte[] buffer = response.get();

					if (buffer != null)
					{
						E e = serializer.decode(buffer);
						if (e != null)
						{
							result.add(e);
						}
					}
				}
				catch (Exception e)
				{
					LOGGER.error("error", e);
				}
			}

			if (LOGGER.isInfoEnabled())
			{
				if(result.size()>0){
					LOGGER.info(String.format("%s pop OK beforeLength=%s afterLength=%s resultSize=%s", this.queueName, beforeLength, afterLength, result.size()));
				}
			}

			return result;
		}
		catch (Throwable t)
		{
			LOGGER.error("pop() error", t);
			if (jedis != null)
			{
				RedisUtil.returnBrokenResource(jedis);
				jedis = null;
			}
			return null;
		}
		finally
		{
			if (jedis != null)
			{
				RedisUtil.returnResource(jedis);
			}
		}
	}

	private void doRun()
	{
		while (true)
		{
			try
			{
				List<E> result = pop(batchCount);

				boolean needWait = result == null || result.size() <= 0;
				try
				{
					if (result != null)
					{
						if (fireEvent != null)
						{
							fireEvent.run(result);
						}
						else
						{
							LOGGER.error(String.format("%s fireEvent NULL", this.queueName));
						}
					}
				}
				catch (Throwable e)
				{
					LOGGER.error("exec fireEvent error", e);
				}

				if (needWait)
				{
					synchronized (syncObject)
					{
						syncObject.wait(minIdleTimeMs);
					}
				}
			}
			catch (Throwable e)
			{
				try
				{
					LOGGER.error("monitorThread.run() error", e);

					Throwable t = e.getCause();
					if (t != null)
					{
						int index = 0;
						while (t != null)
						{
							index++;
							LOGGER.error("monitorThread.run() [" + index + "]error", t);
							t = t.getCause();
						}
					}

				}
				catch (Throwable e2)
				{
					LOGGER.error("", e2);
				}

				// 一旦异常就让一下，避免错误连续
				try
				{
					Thread.sleep(100);
				}
				catch (Throwable e2)
				{
					LOGGER.error("", e2);
				}
			}
		}

	}

	public void add(E value)
	{

		if (value == null )
		{
			return;
		}

		Jedis jedis = null;
		try
		{
			jedis = RedisUtil.getJedis();

			long beforeLength = 0;
			long afterLength = 0;

			if (LOGGER.isInfoEnabled())
			{
				beforeLength = jedis.llen(this.queueKey);
			}

			byte[] buffer = serializer.encode(value);
			if (buffer != null)
			{
				jedis.rpush(this.queueKey, buffer);
			}

			if (LOGGER.isInfoEnabled())
			{
				afterLength = jedis.llen(this.queueKey);
			}

			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info(String.format("%s add OK beforeLength=%s afterLength=%s", this.queueName, beforeLength, afterLength));
			}

			synchronized (syncObject)
			{
				syncObject.notifyAll();
			}
		}
		catch (Throwable t)
		{
			LOGGER.error("add error", t);

			Throwable t1 = t.getCause();
			if (t1 != null)
			{
				int index = 0;
				while (t1 != null)
				{
					index++;
					LOGGER.error("add error [" + index + "]error", t1);
					t1 = t1.getCause();
				}
			}

			if (jedis != null)
			{
				RedisUtil.returnBrokenResource(jedis);
				jedis = null;
			}
		}
		finally
		{
			if (jedis != null)
			{
				RedisUtil.returnResource(jedis);
			}
		}
	}
	
	public void add(List<E> values)
	{
		if (values == null || values.size() <= 0)
		{
			return;
		}

		Jedis jedis = null;
		try
		{
			jedis = RedisUtil.getJedis();

			long beforeLength = 0;
			long afterLength = 0;

			if (LOGGER.isInfoEnabled())
			{
				beforeLength = jedis.llen(this.queueKey);
			}

			Pipeline pipeline = jedis.pipelined();
			for (E bs : values)
			{
				byte[] buffer = serializer.encode(bs);
				if (buffer != null)
				{
					pipeline.rpush(this.queueKey, buffer);
				}
			}

			pipeline.sync();

			if (LOGGER.isInfoEnabled())
			{
				afterLength = jedis.llen(this.queueKey);
			}

			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info(String.format("%s add OK beforeLength=%s afterLength=%s valuesSize=%s", this.queueName, beforeLength, afterLength, values.size()));
			}

			synchronized (syncObject)
			{
				syncObject.notifyAll();
			}
		}
		catch (Throwable t)
		{
			LOGGER.error("add error", t);

			Throwable t1 = t.getCause();
			if (t1 != null)
			{
				int index = 0;
				while (t1 != null)
				{
					index++;
					LOGGER.error("add error [" + index + "]error", t1);
					t1 = t1.getCause();
				}
			}

			if (jedis != null)
			{
				RedisUtil.returnBrokenResource(jedis);
				jedis = null;
			}
		}
		finally
		{
			if (jedis != null)
			{
				RedisUtil.returnResource(jedis);
			}
		}
	}

	public String getQueueName()
	{
		return queueName;
	}

}
