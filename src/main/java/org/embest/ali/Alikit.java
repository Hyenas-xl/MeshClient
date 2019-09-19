package org.embest.ali;

import java.util.function.Consumer;

import org.embest.ali.Monitor.DeviceNotifyListener;


public interface Alikit {
	
	void init();
	
	void postScanInfo(String name,String uuid,String key);
	
	void postScanError(String key,String error);
	
	void postRequestKey(String uuid,String name,String key);
	
	void postProvision(String uuid,String unicast,String info,String name,String key);
	
	void postProvisionError(String uuid,String name,String key,String error);
	
	void listenService(Consumer<String> consumer);
	
	void addDeviceNotifyListener(DeviceNotifyListener l);
	
	void addSubDevice(String deviceName,String deviceSecret);
	
	void loginSubDevice(String name);
	
	void logoutSubDevice(String name);
	
	void postSubDeviceStatusProperty(String deviceName,String deviceSecret,String property,int status);
	
	void subscribeSubDeviceStatus(String deviceName);
	
}
