package org.embest.mesh.signal;

import java.util.Map;

import org.embest.model.mesh.Device;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.Variant;

public interface Command {
	
	void startScan(CommandCallBack<Device> callBack);
	
	void stopScan();
	
	void requestkey(CommandRequestCallBack<String> callback);
	
	void provision(String uuid,CommandCallBack<UInt16> callback,CommandCallBack<String> compositionCallBack);
//	/**
//	 * 目前只支持一个网络，
//	 * @param address ： 0
//	 */
//	void connect(UInt16 address);
//	
//	void disconnect();
//	
	void target(UInt16 address,CommandCallBack<Boolean> callBack);
	
	void appkeyAdd(UInt16 inx,CommandCallBack<Boolean> callBack);
	
	void appkeyDel(UInt16 inx,CommandCallBack<Boolean> callBack);
	
	void bind(Map<String,Variant> info,CommandCallBack<Boolean> callBack);
	
	void pubSet(Map<String,Variant> info,CommandCallBack<Boolean> callBack);
	
	void subAdd(Map<String,Variant> info,CommandCallBack<Boolean> callBack);
	
	void composition(CommandCallBack<String> callBack);
	
	void nodeReset(CommandCallBack<Boolean> callBack);
	
	void deviceOnline(DeviceStatusCallBack callback);
	
	void deviceStateListen(ModelSignalListen listen);
	
	void deviceOnOffGet(String address);
	
	void deviceOnOffSet(int address,int status);
}
