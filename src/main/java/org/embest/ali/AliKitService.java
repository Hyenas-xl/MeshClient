package org.embest.ali;

import java.util.function.Consumer;

import org.embest.ali.Monitor.DeviceNotifyListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class AliKitService implements Alikit{
	private Monitor monitor = new Monitor();
	private static Alikit instance = null;
	
	public synchronized static Alikit kit(){
		if(instance == null){
			instance = new AliKitService();
		}
		return instance;
	}
	private AliKitService(){}
	
	@Override
	public void init() {
		monitor.init();
	}
	
	public void postScanInfo(String name,String uuid,String key){
		String json = String.format("{\"type\":1,\"uuid\":\"%s\",\"name\":\"%s\",\"key\":\"%s\"}", uuid,name,key);
		monitor.postMonitorEvent(json);
	}
	
	@Override
	public void postScanError(String key, String error) {
		String json = String.format("{\"type\":1,\"error\":\"%s\",\"key\":\"%s\"}", error,key);
		monitor.postMonitorEvent(json);
	}
	@Override
	public void postRequestKey(String uuid,String name, String key) {
		JSONObject obj = new JSONObject();
		obj.put("type", 2);
		obj.put("uuid", uuid);
		obj.put("key", key);
		obj.put("name", name);
		obj.put("requestKey", 1);
		monitor.postMonitorEvent(obj.toJSONString());
	}
	@Override
	public void postProvision(String uuid, String unicast, String info, String name,String key) {
		JSONObject obj = new JSONObject();
		obj.put("type", 2);
		obj.put("uuid", uuid);
		obj.put("unicast", unicast);
		obj.put("key", key);
		obj.put("name", name);
		obj.put("info", info);
		monitor.postMonitorEvent(obj.toJSONString());
	}
	
	@Override
	public void postProvisionError(String uuid, String name, String key,String error) {
		JSONObject obj = new JSONObject();
		obj.put("type", 2);
		obj.put("uuid", uuid);
		obj.put("key", key);
		obj.put("name", name);
		obj.put("error", error);
		monitor.postMonitorEvent(obj.toJSONString());
	}
	
	public void listenService(Consumer<String> consumer){
		monitor.listenMonitorService(consumer);
	}
	
	public void addDeviceNotifyListener(DeviceNotifyListener l){
		monitor.addDeviceNotifyListener(l);
	}
	
	@Override
	public void addSubDevice(String deviceName,String deviceSecret) {
		monitor.addSubDevice(deviceName, deviceSecret);
	}
	
	@Override
	public void loginSubDevice(String name) {
		monitor.loginSubDevice(name);
	}
	
	@Override
	public void logoutSubDevice(String name) {
		monitor.logoutSubDevice(name);
	}
	
	@Override
	public void postSubDeviceStatusProperty(String deviceName,String deviceSecret,String property, int status) {
		monitor.postSubDeviceStatusProperty(deviceName,deviceSecret, property,status);
	}
	
	@Override
	public void subscribeSubDeviceStatus(String deviceName) {
		monitor.subscribeSubDeviceStatus(deviceName);
	}
}
