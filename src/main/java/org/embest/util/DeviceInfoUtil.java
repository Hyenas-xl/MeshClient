package org.embest.util;

import java.util.List;

import org.embest.model.SubDeviceInfo;

import com.alibaba.fastjson.JSON;

public class DeviceInfoUtil {
	private static final String CONFIG_FILE = System.getProperty("user.dir")+"/device_info.json";
	private List<SubDeviceInfo> deviceInfoList = null;
	
	private void save(){
		FileUtils.write(CONFIG_FILE, JSON.toJSONString(deviceInfoList));
	}
	
	public List<SubDeviceInfo> load(){
		String value = FileUtils.read(CONFIG_FILE);
		if(value != null){
			deviceInfoList = JSON.parseObject(value, List.class);
		}
		return deviceInfoList;
	}
	
	public void addDeviceInfo(SubDeviceInfo deviceInfo){
		deviceInfoList.add(deviceInfo);
		save();
	}
	
	public List<SubDeviceInfo> getDeviceInfos(){
		if(deviceInfoList == null){
			return load();
		}
		return deviceInfoList;
	}
}
