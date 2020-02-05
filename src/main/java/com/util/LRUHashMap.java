package com.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class LRUHashMap<K, V>
{
	public static interface Action<T1, T2>
	{
		public void invoke(T1 arg1, T2 arg2);
	}

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 8660712027640838753L;
	

	private static ScheduledExecutorService expireExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory(){

		int count = 0;
		@Override
		public Thread newThread(Runnable r) 
		{
			
			Thread th = new Thread(r);
			th.setDaemon(true);
			th.setName("LRU-CHECK-EXPIRE-Thread-" + count);
			count++;
			return th;
		}});
	
	private AtomicBoolean isCleanerRuning = new AtomicBoolean(false);

	private LRUContainerMap<K, TimestampEntryValue<V>> container;

	private Runnable expireRunnable = new Runnable()
	{

		@Override
		public void run()
		{
			long nextInterval = 1000;
			container.lock();
			try
			{
				boolean shouldStopCleaner = true;
				if (container.size() > 0)
				{
					System.out.println((new SimpleDateFormat("HH:mm:ss zzz")).format(new Date(System.currentTimeMillis())) + "\trun clear");
					
					long now = System.currentTimeMillis();
					List<K> toBeRemoved = new ArrayList<K>();
					for (Entry<K, TimestampEntryValue<V>> e : container.entrySet())
					{
						K key = e.getKey();
						TimestampEntryValue<V> tValue = e.getValue();
						long timeLapsed = now - tValue.lastUsedTimestamp;
						long expiredMs = tValue.deadTimestamp > 0 ?  (tValue.deadTimestamp - now) : 0;
						
						if (maxIdleMs > 0 && timeLapsed >= maxIdleMs)
						{
							toBeRemoved.add(key);
						}
						else if (expiredMs <=0)
						{
							toBeRemoved.add(key);
						}
						else
						{
							long delta = Math.max(maxIdleMs, maxExpireMs)  - Math.min(timeLapsed,expiredMs) ;
							if (delta > 1000L)
							{
								nextInterval = delta;
							}
							break;
						}
					}

					if (toBeRemoved.size() > 0)
					{
						for (K key : toBeRemoved)
						{
							container.remove(key);
						}
					}

					if (container.size() > 0)
					{
						shouldStopCleaner = false;
					}
				}

				if (shouldStopCleaner)
				{
					isCleanerRuning.compareAndSet(true, false);
				}
				else
				{
					expireExecutor.schedule(this, nextInterval, TimeUnit.MILLISECONDS);
				}

			}
			finally
			{
				container.unlock();
			}

		}
	};

	private long maxIdleMs = -1;
	
	private long maxExpireMs = -1;

	public LRUHashMap(int maxSize, final Action<K, V> onEvict)
	{
		this(maxSize, onEvict, -1L,-1L);
	}

	public LRUHashMap(int max, final Action<K, V> onEvict, long maxIdleMs,long maxExpireMs)
	{
		Action<K, TimestampEntryValue<V>> doOnEvict = null;

		if (onEvict != null)
		{
			doOnEvict = new Action<K, TimestampEntryValue<V>>()
			{

				@Override
				public void invoke(K key, TimestampEntryValue<V> value)
				{
					if (value != null)
					{
						onEvict.invoke(key, value.value);
					}
				}
			};
		}
		this.maxIdleMs = maxIdleMs;
		this.maxExpireMs = maxExpireMs;
		//LinkedHashMap
		container = new LRUContainerMap<K, TimestampEntryValue<V>>(max, doOnEvict);
	}

	public int size()
	{
		return container.size();
	}

	int getMaxSize()
	{
		return container.getMaxSize();
	}

	public long getDuration()
	{
		return maxIdleMs;
	}
	
	/**设置方法*/
	public V put(K key, V value,long expireAtTimestamp)
	{
		TimestampEntryValue<V> v = new TimestampEntryValue<V>();
		v.lastUsedTimestamp = System.currentTimeMillis();
		v.deadTimestamp = expireAtTimestamp >0 ? expireAtTimestamp : -1;
		v.value = value;
		TimestampEntryValue<V> old = container.put(key, v);
		if (old == null && expireAtTimestamp > 0)
		{
			if (isCleanerRuning.compareAndSet(false, true))
			{
				long e = 0;
				if (maxIdleMs > 0 && this.maxExpireMs > 0)
				{
					e = Math.min(maxIdleMs, maxExpireMs);
				}
				else if (maxIdleMs >0)
				{
					e = maxIdleMs;
				}
				else if (maxExpireMs >0)
				{
					e = maxExpireMs;
				}
				else
				{
					e = 1000L;
				}
				
				expireExecutor.schedule(expireRunnable,e , TimeUnit.MILLISECONDS);
			}

		}
		
		return old == null ? null : old.value;
	}

	public V put(K key, V value)
	{
		TimestampEntryValue<V> v = new TimestampEntryValue<V>();
		v.lastUsedTimestamp = System.currentTimeMillis();
		v.deadTimestamp = ((this.maxExpireMs > 0) ? System.currentTimeMillis() + this.maxExpireMs : -1);
		v.value = value;
		TimestampEntryValue<V> old = container.put(key, v);

		if (old == null)
		{
			if (maxIdleMs > 0 || this.maxExpireMs > 0)
			{
				if (isCleanerRuning.compareAndSet(false, true))
				{
					long e = 0;
					if (maxIdleMs > 0 && this.maxExpireMs > 0)
					{
						e = Math.min(maxIdleMs, maxExpireMs);
					}
					else if (maxIdleMs >0)
					{
						e = maxIdleMs;
					}
					else
					{
						e = maxExpireMs;
					}
							
					expireExecutor.schedule(expireRunnable,e , TimeUnit.MILLISECONDS);
					System.out.println("start clear");
				}
			}
		}
		
		return old == null ? null : old.value;
	}

	public V putIfAbsent(K key, V value,long expireAtTimestamp)
	{
		TimestampEntryValue<V> v = new TimestampEntryValue<V>();
		v.lastUsedTimestamp = System.currentTimeMillis();
		v.deadTimestamp = expireAtTimestamp >0 ?expireAtTimestamp : -1;
		v.value = value;
		TimestampEntryValue<V> old = null;
		
		old = container.putIfAbsent(key, v);
		if (old == null && expireAtTimestamp > 0)
		{
			if (isCleanerRuning.compareAndSet(false, true))
			{
				long e = 0;
				if (maxIdleMs > 0 && this.maxExpireMs > 0)
				{
					e = Math.min(maxIdleMs, maxExpireMs);
				}
				else if (maxIdleMs >0)
				{
					e = maxIdleMs;
				}
				else if (maxExpireMs >0)
				{
					e = maxExpireMs;
				}
				else
				{
					e = 1000L;
				}
				expireExecutor.schedule(expireRunnable, e, TimeUnit.MILLISECONDS);
			}
		}
		
		return old == null ? null : old.value;
	}
	
	public V putIfAbsent(K key, V value)
	{
		TimestampEntryValue<V> v = new TimestampEntryValue<V>();
		v.lastUsedTimestamp = System.currentTimeMillis();
		v.deadTimestamp = ((this.maxExpireMs > 0) ? System.currentTimeMillis() + this.maxExpireMs : -1);
		v.value = value;
		TimestampEntryValue<V> old = container.putIfAbsent(key, v);

		if (old == null)
		{
			if (maxIdleMs > 0 ||  this.maxExpireMs > 0)
			{
				if (isCleanerRuning.compareAndSet(false, true))
				{
					long e = 0;
					if (maxIdleMs > 0 && this.maxExpireMs > 0)
					{
						e = Math.min(maxIdleMs, maxExpireMs);
					}
					else if (maxIdleMs >0)
					{
						e = maxIdleMs;
					}
					else
					{
						e = maxExpireMs;
					}
					expireExecutor.schedule(expireRunnable, e, TimeUnit.MILLISECONDS);
				}
			}
		}

		return old == null ? null : old.value;
	}

	public boolean containsKey(Object key)
	{
		return container.containsKey(key);
	}

	public V get(Object key)
	{
		TimestampEntryValue<V> got = container.get(key);
		V ret = null;
		if (got != null)
		{
			
			if (got.deadTimestamp > 0 && got.deadTimestamp <= System.currentTimeMillis())
			{
				container.remove(key);
				return null;
			}
		
			got.lastUsedTimestamp = System.currentTimeMillis();
			ret = got.value;
		}
		return ret;
	}

	public V remove(Object key, boolean doEvict)
	{
		TimestampEntryValue<V> removed;
		if (doEvict)
		{
			removed = container.remove(key);
		}
		else
		{
			removed = container.removeUnEvict(key);
		}

		V ret = null;
		if (removed != null)
		{
			ret = removed.value;
		}
		return ret;
	}

	public V remove(Object key)
	{
		return remove(key, true);
	}

	public Map<K, Object> clone()
	{
		return container.clone();
	}

	static class TimestampEntryValue<V>
	{
		public V value;
		public long lastUsedTimestamp;
		public long deadTimestamp;
	}

	private static class LRUContainerMap<K, V extends TimestampEntryValue<?>> extends LinkedHashMap<K, V>
	{
		private static ExecutorService pool = Executors.newCachedThreadPool();
		private static final long serialVersionUID = -2108033306317724707L;

		private ReentrantLock lock = new ReentrantLock();

		private int maxSize;

		private Action<K, V> onEvict;

		public LRUContainerMap(int maxSize, Action<K, V> onEvict)
		{
			super(16, 0.75f, true);
			this.maxSize = maxSize;
			this.onEvict = onEvict;
		}

		public int getMaxSize()
		{
			return maxSize;
		}
		
		public void lock()
		{
			lock.lock();
		}

		public void unlock()
		{
			lock.unlock();
		}

		@Override
		public V put(K key, V value)
		{
			lock();
			try
			{
				return super.put(key, value);
			}
			finally
			{
				unlock();
			}
		}

		public V putIfAbsent(K key, V value)
		{
			lock();
			try
			{
				V result = super.get(key);
				if (result != null)
				{
					return result;
				}
				else
				{
					super.put(key, value);
					return null;
				}
			}
			finally
			{
				unlock();
			}
		}

		@Override
		public V get(Object key)
		{
			lock.lock();
			try
			{
				return super.get(key);
			}
			finally
			{
				lock.unlock();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public V remove(final Object key)
		{
			lock();
			try
			{
				final V ret = super.remove(key);
				if (onEvict != null)
				{
					pool.execute(new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								onEvict.invoke((K) key, ret);
							}
							catch (Exception ignore)
							{
							}
						}
					});
				}
				return ret;
			}
			finally
			{
				unlock();
			}
		}

		public V removeUnEvict(final Object key)
		{
			lock();
			try
			{
				final V ret = super.remove(key);

				return ret;
			}
			finally
			{
				unlock();
			}
		}

		@Override
		protected boolean removeEldestEntry(final Entry<K, V> eldest)
		{
			final boolean ret = size() > maxSize;
			if (onEvict != null && ret)
			{
				pool.execute(new Runnable()
				{
					@Override
					public void run()
					{
						onEvict.invoke(eldest.getKey(), eldest.getValue());
					}
				});
			}
			return ret;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Map<K, Object> clone()
		{
			Map<K, V> map;

			lock();
			try
			{
				map = (Map<K, V>) super.clone();
			}
			finally
			{
				unlock();
			}

			Iterator<Entry<K, V>> iter = map.entrySet().iterator();
			Map<K, Object> result = new HashMap<K, Object>();
			while (iter.hasNext())
			{
				Entry<K, V> entry = iter.next();
				result.put(entry.getKey(), entry.getValue().value);
			}

			return result;
		}
	}
}
