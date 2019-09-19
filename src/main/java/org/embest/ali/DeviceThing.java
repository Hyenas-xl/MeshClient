package org.embest.ali;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.embest.model.SubDeviceInfo;
import org.embest.util.BASE64;
import org.embest.util.LogUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.alink.dm.api.DeviceInfo;
import com.aliyun.alink.dm.api.IDMCallback;
import com.aliyun.alink.dm.api.IThing;
import com.aliyun.alink.dm.api.InitResult;
import com.aliyun.alink.dm.api.SignUtils;
import com.aliyun.alink.dm.model.ResponseModel;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.channel.gateway.api.subdevice.ISubDeviceActionListener;
import com.aliyun.alink.linksdk.channel.gateway.api.subdevice.ISubDeviceChannel;
import com.aliyun.alink.linksdk.channel.gateway.api.subdevice.ISubDeviceConnectListener;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper;
import com.aliyun.alink.linksdk.tmp.listener.IPublishResourceListener;
import com.aliyun.alink.linksdk.tools.AError;

public class DeviceThing {
	private static final Logger logger = LogUtils.getLogger(DeviceThing.class.getName());
//	private IThing mThing = null;
	public static final DeviceThing instance = new DeviceThing();
	private Map<String, Boolean> subDeviceState = new HashMap<>();
	private Map<String,IThing> thingMap = new HashMap<>();
	
	private DeviceThing(){}
	
	public void subdevice(){
		LinkKit.getInstance().getGateway().gatewayGetSubDevices(new IConnectSendListener() {
		    @Override
		    public void onResponse(ARequest aRequest, AResponse aResponse) {
		        try {
		        	System.out.println("subdevice: "+aResponse.data.toString());
		            ResponseModel<List<DeviceInfo>> response = JSONObject.parseObject(aResponse.data.toString(), new TypeReference<ResponseModel<List<DeviceInfo>>>() {}.getType());
		            List<DeviceInfo> data = response.data;
		            if(data != null){
		            	data.forEach(deviceInfo -> addSubDevice(deviceInfo));
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		    }

			@Override
			public void onFailure(ARequest paramARequest, AError paramAError) {
				
			}
		});
	}
	
	public void addSubDevice(DeviceInfo info){
		addSubDevice(info,null);
	}
	
	public void addSubDevice(DeviceInfo info,Consumer<Void> consumer){
		LinkKit.getInstance().getGateway().gatewayAddSubDevice(info, new ISubDeviceConnectListener() {
			
			@Override
			public void onDataPush(String arg0, AMessage arg1) {
			}
			
			@Override
			public void onConnectResult(boolean isSuccess, ISubDeviceChannel iSubDeviceChannel, AError aError) {
				if (isSuccess) {
//					loginSubDevice(info);
					SubDeviceInfo subDevice = new SubDeviceInfo();
					subDevice.setDeviceName(info.deviceName);
					subDevice.setDeviceSecret(BASE64.encryptBASE64(info.deviceSecret));
					Config.instance.addSubDevices(subDevice);
					System.out.println("Add subdevice["+info.deviceName+"] ok");
					if(consumer != null){
						consumer.accept(null);
					}
				}else{
					logger.severe(info.deviceName+" add called with: aError = [" + aError + "]");
				}
			}
			
			@Override
			public String getSignValue() {
		        Map<String, String> signMap = new HashMap<>();
		        signMap.put("productKey", info.productKey);
		        signMap.put("deviceName", info.deviceName);
		        signMap.put("clientId", getClientId());
		        return SignUtils.hmacSign(signMap, info.deviceSecret);
			}
			
			@Override
			public String getSignMethod() {
				return "hmacsha1";
			}
			
			@Override
			public Map<String, Object> getSignExtraData() {
				return null;
			}
			
			@Override
			public String getClientId() {
				return info.deviceName;
			}
		});
	}
	
	public void loginSubDevice(String name){
		SubDeviceInfo subDevice = Config.instance.getSubDevice(name);
		DeviceInfo info = new DeviceInfo();
		info.deviceSecret = BASE64.decryptBASE64(subDevice.getDeviceSecret());
		info.deviceName = subDevice.getDeviceName();
    	info.productKey = Config.instance.subProductKey();
    	loginSubDevice(info);
	}
	
	public void loginSubDevice(DeviceInfo deviceInfo){
		if(subDeviceState.containsKey(deviceInfo.deviceName) && subDeviceState.get(deviceInfo.deviceName)){
			return;
		}
		logger.info(deviceInfo.deviceName +" login.");
		addSubDevice(deviceInfo, v-> {
			LinkKit.getInstance().getGateway().gatewaySubDeviceLogin(deviceInfo, new ISubDeviceActionListener() {
				
				@Override
				public void onSuccess() {
					subDeviceState.put(deviceInfo.deviceName, true);
					proxySubDeviceThing(deviceInfo);
				}
				
				@Override
				public void onFailed(AError aError) {
					logger.severe(deviceInfo.deviceName+" login called with: aError = [" + aError + "]");
					subDeviceState.put(deviceInfo.deviceName, false);
				}
			});
		});
	}
	
	public void logoutSubDevice(String name){
		SubDeviceInfo subDevice = Config.instance.getSubDevice(name);
		DeviceInfo info = new DeviceInfo();
		info.deviceName = subDevice.getDeviceName();
		info.deviceSecret = BASE64.decryptBASE64(subDevice.getDeviceSecret());
    	info.productKey = Config.instance.subProductKey();
    	logoutSubDevice(info);
	}
	
	public void logoutSubDevice(DeviceInfo deviceInfo){
		if(!subDeviceState.containsKey(deviceInfo.deviceName) || !subDeviceState.get(deviceInfo.deviceName)){
			return;
		}
		logger.info(deviceInfo.deviceName +" logout.");
		LinkKit.getInstance().getGateway().gatewaySubDeviceLogout(deviceInfo,new ISubDeviceActionListener() {
			
			@Override
			public void onSuccess() {
				subDeviceState.put(deviceInfo.deviceName, false);
				thingMap.remove(deviceInfo.deviceName);
			}
			
			@Override
			public void onFailed(AError aError) {
				logger.severe(deviceInfo.deviceName+" logout called with: aError = [" + aError + "]");
			}
		});
	}

	public void proxySubDeviceThing(DeviceInfo deviceInfo){
		Map<String, ValueWrapper> subDevInitState = new HashMap<String, ValueWrapper>();
		LinkKit.getInstance().getGateway().initSubDeviceThing(null, deviceInfo, subDevInitState, new IDMCallback<InitResult>() {
            @Override
            public void onSuccess(InitResult initResult) {
            	IThing thing = LinkKit.getInstance().getGateway().getSubDeviceThing(deviceInfo).first;
            	thingMap.put(deviceInfo.deviceName, thing);
            }

            @Override
            public void onFailure(AError aError) {
            	logger.severe("initSubDeviceThing onFailure() called with " + aError);
            }
        });
	}
	
	@SuppressWarnings("unchecked")
	public void postSubDeviceStatusProperty(DeviceInfo deviceInfo,String property,int value){
		IThing thing = thingMap.get(deviceInfo.deviceName);
		if(thing == null){
			logger.severe("Not find gateway subdevice thing.");
			return;
		}
		logger.info("Post ["+deviceInfo.deviceName+","+deviceInfo.deviceSecret+"] proterty:"+value);
		Map<String, ValueWrapper> paramMap = new HashMap<String, ValueWrapper>();
		ValueWrapper valueWrapper = new ValueWrapper<Integer>();
		valueWrapper.setValue(value);
		paramMap.put(property, valueWrapper);
		
		thing.thingPropertyPost(paramMap, new IPublishResourceListener() {
			
			@Override
			public void onSuccess(String resId, Object o) {
				System.out.println("post property Success called with: s = [" + resId + "], o = [" + o + "]");
			}
			
			@Override
			public void onError(String resId, AError aError) {
				logger.severe("post property called with " + aError);
			}
		});
	}

	public void subscribeSubDeviceStatus(DeviceInfo deviceInfo){
		String topic = String.format("/sys/%s/%s/thing/service/property/set", deviceInfo.productKey,deviceInfo.deviceName);
		LinkKit.getInstance().getGateway().gatewaySubDeviceSubscribe(topic, deviceInfo, new ISubDeviceActionListener() {
			
			@Override
			public void onSuccess() {
				System.out.println("Subdevice subscribe ["+deviceInfo.deviceName+"] ok.");
			}
			
			@Override
			public void onFailed(AError arg0) {
				logger.severe("Subdevice subscribe ["+deviceInfo.deviceName+"] status: " + arg0);
			}
		});
	}
}
