package org.embest.ali;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.embest.util.LogUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.alink.dm.api.DeviceInfo;
import com.aliyun.alink.dm.api.InitResult;
import com.aliyun.alink.linkkit.api.ILinkKitConnectListener;
import com.aliyun.alink.linkkit.api.IoTMqttClientConfig;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linkkit.api.LinkKitInitParams;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener;
import com.aliyun.alink.linksdk.tmp.api.InputParams;
import com.aliyun.alink.linksdk.tmp.api.OutputParams;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper;
import com.aliyun.alink.linksdk.tmp.devicemodel.Service;
import com.aliyun.alink.linksdk.tmp.listener.IPublishResourceListener;
import com.aliyun.alink.linksdk.tmp.listener.ITResRequestHandler;
import com.aliyun.alink.linksdk.tmp.listener.ITResResponseCallback;
import com.aliyun.alink.linksdk.tmp.utils.ErrorInfo;
import com.aliyun.alink.linksdk.tools.AError;

public class Monitor {
	private static final Logger logger = LogUtils.getLogger(Monitor.class.getName());
	private final static String CONNECT_ID = "LINK_PERSISTENT";
	private final static String TOPIC_END_PROPERTY_SET = "/thing/service/property/set";
	boolean connectStatus = false;
	private List<DeviceNotifyListener> listeners = new ArrayList<>();
	
	public synchronized void init(){
		LinkKitInitParams params = new LinkKitInitParams();
		
		IoTMqttClientConfig config = new IoTMqttClientConfig();
		config.productKey = Config.instance.productKey();
		config.deviceName = Config.instance.monitorDevice();
		config.deviceSecret = Config.instance.monitorDeviceSecret();
		config.channelHost = Config.instance.productKey() + ".iot-as-mqtt." + Config.instance.region() + ".aliyuncs.com:1883";
		
		config.receiveOfflineMsg = false;
        params.mqttClientConfig = config;
        
		DeviceInfo deviceInfo = new DeviceInfo();
		deviceInfo.productKey = Config.instance.productKey();
		deviceInfo.deviceName = Config.instance.monitorDevice();
		deviceInfo.deviceSecret = Config.instance.monitorDeviceSecret();
		
		params.deviceInfo = deviceInfo;
		
		Map<String, ValueWrapper> propertyValues = new HashMap<String, ValueWrapper>();
		params.propertyValues = propertyValues;
		LinkKit.getInstance().init(params, new ILinkKitConnectListener() {
            public void onError(AError aError) {
            	logger.severe("Init Error error=" + aError);
            	connectStatus = true;
            }

            public void onInitDone(InitResult initResult) {
//            	logger.info("onInitDone result=" + initResult);
//            	DeviceInfo info = new DeviceInfo();
//            	info.deviceName="Test_Mesh_Device_001";
//            	info.deviceSecret = "ciVPD6GMG92tK0JZmutUBNhalHY6ffO7";
//            	info.productKey = Config.instance.subProductKey();
//            	DeviceThing.instance.addSubDevice(info);
            	connectStatus = true;
//            	listenMonitorService();
            	DeviceThing.instance.subdevice();
            }
        });
		while(!connectStatus){
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void postMonitorEvent(String value){
		HashMap<String, ValueWrapper> valueWrapperMap = new HashMap<>();
		ValueWrapper valueWrapper = new ValueWrapper();
		valueWrapper.setValue(value);
		valueWrapperMap.put("data", valueWrapper);
		OutputParams params = new OutputParams(valueWrapperMap);
		LinkKit.getInstance().getDeviceThing().thingEventPost("meshData",params,new IPublishResourceListener() {
			
			@Override
			public void onSuccess(String paramString, Object paramObject) {
			}
			
			@Override
			public void onError(String paramString, AError aError) {
				logger.severe("monitor post event: " + value);
			}
		});
	}
	
	public void listenMonitorService(Consumer<String> consumer){
		List<Service> serviceList = LinkKit.getInstance().getDeviceThing().getServices();
		if(serviceList != null){
			for(Service service : serviceList){
				if("meshCommand".equals(service.getIdentifier())){
					LinkKit.getInstance().getDeviceThing().setServiceHandler(service.getIdentifier(), new ITResRequestHandler() {
						
						@Override
						public void onSuccess(Object paramObject, OutputParams paramOutputParams) {
						}
						
						@Override
						public void onFail(Object o, ErrorInfo errorInfo) {
							logger.severe("monitor service listen: o = [" + o + "], errorInfo = [" + errorInfo + "]");
						}
						
						@Override
						public void onProcess(String identify, Object result, ITResResponseCallback itResResponseCallback) {
							if (result instanceof InputParams) {
								Map<String, ValueWrapper> data = (Map<String, ValueWrapper>) ((InputParams) result).getData();
								String value = data.get("command").getValue().toString();
								consumer.accept(value);
							}
							itResResponseCallback.onComplete(identify, null, null);
						}
					});
					break;
				}
			}
		}
		
		LinkKit.getInstance().registerOnNotifyListener(new IConnectNotifyListener() {
			
			@Override
			public boolean shouldHandle(String connectId, String topic) {
				return true;
			}
			
			@Override
			public void onNotify(String connectId, String topic, AMessage aMessage) {
				if(CONNECT_ID.equals(connectId) && topic.endsWith(TOPIC_END_PROPERTY_SET)){
					String message = (aMessage == null || aMessage.data == null) ? "" : new String((byte[]) aMessage.data);
					if(message.length() > 0){
						String[] splitsArray = topic.split("/");
						String deviceName = splitsArray[3];
						JSONObject obj = (JSONObject) JSON.parse(message);
						if(obj.containsKey("params")){
							JSONObject valueObj = (JSONObject) obj.get("params");
							if(listeners.size() > 0){
								for( DeviceNotifyListener l : listeners){
									if(l instanceof DeviceNotifyStatusAdapter){
										int status = valueObj.getIntValue("status");
										((DeviceNotifyStatusAdapter)l).deviceStatus(deviceName, status);
									}
								}
							}
						}
					}
				}
			}
			
			@Override
			public void onConnectStateChange(String connectId, ConnectState connectState) {
				
			}
		});
	}
	
	public void addSubDevice(String deviceName,String deviceSecret){
		DeviceInfo info = new DeviceInfo();
		info.deviceName = deviceName;
    	info.deviceSecret = deviceSecret;
    	info.productKey = Config.instance.subProductKey();
		DeviceThing.instance.addSubDevice(info);
	}
	
	public void loginSubDevice(String name){
		DeviceThing.instance.loginSubDevice(name);
	}
	
	public void logoutSubDevice(String name){
		DeviceThing.instance.logoutSubDevice(name);
	}
	
	public void postSubDeviceStatusProperty(String deviceName,String deviceSecret,String property,int status){
		DeviceInfo info = new DeviceInfo();
		info.deviceName = deviceName;
		info.deviceSecret = deviceSecret;
    	info.productKey = Config.instance.subProductKey();
		DeviceThing.instance.postSubDeviceStatusProperty(info, property,status);
	}
	
	public void subscribeSubDeviceStatus(String deviceName){
		DeviceInfo info = new DeviceInfo();
		info.deviceName = deviceName;
    	info.productKey = Config.instance.subProductKey();
		DeviceThing.instance.subscribeSubDeviceStatus(info);
	}
	
	public void addDeviceNotifyListener(DeviceNotifyListener l){
		listeners.add(l);
	}
	
	public void subDeviceLogin(String deviceName){
		
	}
	
	/**
	 * DeviceNotifyListener is a mark,please use {@link DeviceNotifyStatusAdapter}
	 * @author xiele
	 *
	 */
	public interface DeviceNotifyListener{

		void deviceStatus(String name, int status);
	}
	
	public static abstract class DeviceNotifyStatusAdapter implements DeviceNotifyListener{
	}
}
