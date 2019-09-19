package org.embest.ali;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.embest.model.SubDeviceInfo;
import org.embest.util.BASE64;
import org.embest.util.FileUtils;
import org.embest.util.LogUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.alink.apiclient.utils.StringUtils;
import com.aliyun.alink.dm.api.DeviceInfo;

public class Config {
	private static final Logger logger = LogUtils.getLogger(Config.class.getName());
	private static final String CONFIG_FILE = System.getProperty("user.dir")+"/device_config.json";
	private static final String SUBDEVICE_CONFIG_FILE = System.getProperty("user.dir")+"/subdevices.json";
	public static final Config instance = new Config();
	private Config(){
		init();
	}
	
	
	private DeviceInfo device = new DeviceInfo();
	private Set<SubDeviceInfo> subDevices;
	private String _region = "cn-shanghai";
	public String _projectKey;
	
	public void init(){
		String data = FileUtils.read(CONFIG_FILE);
		if(StringUtils.isEmptyString(data)){
			logger.severe("Ali device config failed.");
			System.exit(0);
		}
		JSONObject obj = (JSONObject) JSON.parse(data);
		if(!obj.containsKey("gatewayProductKey")){
			logger.severe("Ali device config failed: Need 'gatewayProductKey'");
			System.exit(0);
		}else{
			device.productKey = BASE64.decryptBASE64(obj.getString("gatewayProductKey"));
		}
//		if(!obj.containsKey("gatewayProductSecret")){
//			logger.severe("Ali device config failed: Need 'gatewayProductSecret'");
//			System.exit(0);
//		}else{
//			device.productSecret = obj.getString("gatewayProductSecret");
//		}
		if(!obj.containsKey("gatewayMonitor")){
			logger.severe("Ali device config failed: Need 'gatewayMonitor'");
			System.exit(0);
		}else{
			device.deviceName = obj.getString("gatewayMonitor");
		}
		if(!obj.containsKey("gatewayMonitorSecret")){
			logger.severe("Ali device config failed: Need 'gatewayMonitorSecret'");
			System.exit(0);
		}else{
			device.deviceSecret = BASE64.decryptBASE64(obj.getString("gatewayMonitorSecret"));
		}
		
		if(!obj.containsKey("deviceProductKey")){
			logger.severe("Ali device config failed: Need 'deviceProductKey'");
			System.exit(0);
		}else{
			_projectKey = BASE64.decryptBASE64(obj.getString("deviceProductKey"));
		}
		
		if(obj.containsKey("region")){
			_region = obj.getString("region");
		}
		loadSubDeviceConfig();
	}
	
	public String productKey(){
		return device.productKey;
	}
	
//	public String productSecret(){
//		return device.productSecret;
//	}
	
	public String region(){
		return _region;
	}
	
	public String monitorDevice(){
		return device.deviceName;
	}
	public String monitorDeviceSecret(){
		return device.deviceSecret;
	}
	
	public void setMonitorDeviceSecret(String monitorDeviceSecret){
		device.deviceSecret = monitorDeviceSecret;
	}
	
	public void saveSubDevices(){
		if(subDevices != null && subDevices.size() > 0){ 
			FileUtils.write(SUBDEVICE_CONFIG_FILE, JSON.toJSONString(subDevices));
		}
	}
	
	public void loadSubDeviceConfig(){
		String data = FileUtils.read(SUBDEVICE_CONFIG_FILE);
		if(!StringUtils.isEmptyString(data)){
			List<SubDeviceInfo> subDeviceInfos = JSON.parseArray(data, SubDeviceInfo.class);
			subDevices = new HashSet<>();
			subDevices.addAll(subDeviceInfos);
		}
	}
	
	public SubDeviceInfo getSubDevice(String name){
		if(subDevices == null){
			loadSubDeviceConfig();
		}
		SubDeviceInfo subDeviceInfo = null;
		if(subDevices != null){
			for(SubDeviceInfo info : subDevices){
				if(info.getDeviceName().equals(name)){
					subDeviceInfo = info;
					break;
				}
			}
		}
		return subDeviceInfo;
	}
	
	public List<SubDeviceInfo> getSubDeviceByUnicast(int unicast){
		if(subDevices == null){
			loadSubDeviceConfig();
		}
		List<SubDeviceInfo> subDeviceInfos = new ArrayList<SubDeviceInfo>();
		if(subDevices != null){
			for(SubDeviceInfo info : subDevices){
				if(info.getUnicast() == unicast){
					subDeviceInfos.add(info);
				}
			}
		}
		return subDeviceInfos;
	}
	
	public SubDeviceInfo getSubDevice(int elementAddr,int modelId){
		if(subDevices == null){
			loadSubDeviceConfig();
		}
		SubDeviceInfo subDeviceInfo = null;
		if(subDevices != null){
			for(SubDeviceInfo info : subDevices){
				int _eleAddr = info.getUnicast()+ info.getElement();
				if(_eleAddr == elementAddr && info.getModelId() == modelId){
					subDeviceInfo = info;
					break;
				}
			}
		}
		return subDeviceInfo;
	}
	
	public Set<SubDeviceInfo> getSubDevices(){
		return subDevices;
	}
	
	public void addSubDevices(SubDeviceInfo subDevice){
		if(subDevices == null){
			subDevices = new HashSet<>();
		}
		if(subDevices.add(subDevice)){
			saveSubDevices();
		}
	}
	
	public void setDeviceAddress(String name,int address,int unicast,int element,int modelId,int server){
		SubDeviceInfo info = getSubDevice(name);
		if(info != null){
			info.setAddress(address);
			info.setUnicast(unicast);
			info.setElement(element);
			info.setModelId(modelId);
			info.setServer(server);
			saveSubDevices();
		}
	}
	public int getDeviceAddress(String name){
		SubDeviceInfo info = getSubDevice(name);
		if(info != null){
			return info.getAddress();
		}
		return -1;
	}
	
	public void removeSubDevice(String deviceName){
		if(subDevices !=null && deviceName != null){
			SubDeviceInfo subDevice = null;
			for(SubDeviceInfo d:subDevices){
				if(deviceName.equals(d.getDeviceName())){
					subDevice = d;
				}
			}
			if(subDevice != null){
				subDevices.remove(subDevice);
				saveSubDevices();
			}
		}
	}
	
	public String subProductKey(){
		return _projectKey;
	}
}
