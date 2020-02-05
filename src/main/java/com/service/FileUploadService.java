package com.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dao.mapper.FileUploadMapper;
import com.dao.mapper.XaMapper;

/***
 * 2018
 * */
@Transactional
@Service(value = "FileUploadService")
@SuppressWarnings("all")
public class FileUploadService {

	@Resource(name="FileUploadMapper")
	private FileUploadMapper fileUploadMapper;
	
	public List<HashMap<Object, Object>> test(){
		return fileUploadMapper.test();
	}

	/**
	 * 举报事件上传
	 * @throws ParseException 
	 * */
	public String uploadEvent(Map<String,Object> eventParaMap,List<HashMap<String,String>> resourcePathsList) throws ParseException{
		
		List<String> list = new ArrayList<String>();//存储资源表主键
		HashMap<String, String> paraMapNew = new HashMap<String, String>();
		String e_code = "";
		//添加资源		
		for (Iterator iterator = resourcePathsList.iterator(); iterator
				.hasNext();) {
			HashMap<String, String> hashMap = (HashMap<String, String>) iterator
					.next();
			String rt_code = fileUploadMapper.getResourceType(hashMap).get("RT_CODE").toString();//获得资源类型主键
			String re_code = UUID.randomUUID().toString().replaceAll("-", "");
			list.add(re_code);
			hashMap.put("RE_CODE", re_code);
			hashMap.put("RT_CODE", rt_code);
			hashMap.put("RE_DATE", eventParaMap.get("jubaoTime").toString());
			fileUploadMapper.addResource(hashMap);
		}
		
		//添加事件
		HashMap<String,Integer> eventIdMap = fileUploadMapper.searchEventId();
		String seq_eventId = "000000"+eventIdMap.get("NEXTVAL")+"";
		if(seq_eventId.length()>7){
			seq_eventId = seq_eventId.substring(seq_eventId.length()-7);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date dateTime = sdf.parse(eventParaMap.get("jubaoTime").toString());
		String date = sdf.format(dateTime);
		e_code = "GZSJ"+date+seq_eventId;
		
		HashMap<String,String> eventPara = new HashMap<String, String>();
		
		eventPara.put("E_CODE", e_code);
		eventPara.put("US_CODE", "");   //暂时空，
		eventPara.put("ET_CODE", eventParaMap.get("jubaoType").toString());
		eventPara.put("EIL_CODE", "");
		eventPara.put("E_TIME", eventParaMap.get("jubaoTime").toString());
		eventPara.put("E_ADDRESS", eventParaMap.get("eventAddress").toString());
		eventPara.put("E_LON", eventParaMap.get("longitude").toString());
		eventPara.put("E_LAT", eventParaMap.get("latitude").toString());
		eventPara.put("E_DEC", eventParaMap.get("eventDescribe").toString());
		
		fileUploadMapper.addEvent(eventPara);
		
		//添加事件资源关系
		for (String string : list) {
			HashMap<String,String> eveResMap = new HashMap<String, String>();
			eveResMap.put("E_CODE",e_code );
			eveResMap.put("RE_CODE",string );
			fileUploadMapper.addEventResource(eveResMap);
		}
		return e_code;
	}
	
}
