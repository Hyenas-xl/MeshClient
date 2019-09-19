package org.embest.mesh;

import java.util.Map;
import java.util.logging.Logger;

import org.embest.mesh.ConnectHandle.Connected_Type;
import org.embest.mesh.Provisioner.heartbeat;
import org.embest.mesh.commands.BasicConnection;
import org.embest.mesh.signal.MeshSignalListen;
import org.embest.mesh.signal.MeshSignalListen.MeshHandle;
import org.embest.mesh.signal.ModelSignalListen;
import org.embest.model.mesh.Device;
import org.embest.util.LogUtils;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.Variant;

import com.alibaba.fastjson.JSON;

public class MeshSignalHandle implements DBusSigHandler{
	private static final Logger logger = LogUtils.getLogger("MeshSignalHandle");
	private MeshSignalListen meshSignalListen;
	private ModelSignalListen modelSignalListen = null;
	
	public MeshSignalHandle(MeshSignalListen meshSignalListen){
		this.meshSignalListen = meshSignalListen;
	}
	enum Provision_Type{
		request_key,done;
	}
	enum SignalName{
		target,appkey,bind,pub,sub,composition,node;
	}
	@Override
	public void handle(DBusSignal signal) {
		logger.info("Signal: "+signal.getName());
		if(signal instanceof Provisioner.unprovisioned_device_discovered){
			
			Provisioner.unprovisioned_device_discovered scanSignal = (Provisioner.unprovisioned_device_discovered) signal;
			handleScan(scanSignal.device);
		}else if(signal instanceof Provisioner.provisioning){
			
			Provisioner.provisioning provisionDone = (Provisioner.provisioning) signal;
			handleProvisioning(provisionDone.result);
			
		}else if(signal instanceof Provisioner.connecting){
			
			Provisioner.connecting connecting = (Provisioner.connecting)signal;
			handleConnecting(connecting.result);
		}else if(signal instanceof Provisioner.status){
			
			Provisioner.status statusSignal = (Provisioner.status) signal;
			handleStatus(statusSignal.result);
		}else if(signal instanceof Provisioner.executing){
			Provisioner.executing executing = (Provisioner.executing) signal;
			handleExecuting(executing.result);
		}else if(signal instanceof Model.status){
			Model.status status = (Model.status) signal;
			handleModel(status.result);
		}else if(signal instanceof Provisioner.heartbeat){
			Provisioner.heartbeat  hb = (heartbeat) signal;
			handleHeartBeat(hb.result);
		}
	}
	
	private void handleScan(Map<String,Variant> device){
		if(device != null){
			if(device.containsKey("uuid") && device.containsKey("name")){
				Variant uuidVariant = device.get("uuid");
				Variant nameVariant = device.get("name");
				if(uuidVariant != null){
					String uuid = uuidVariant.getValue().toString();
					String name = nameVariant!=null ? nameVariant.getValue().toString() :"";
					handleMessage(MeshHandle.scan, new Device(uuid, name), null);
				}
			}
		}
	}
	
	private void handleProvisioning(Map<String,Variant> result){
		logger.info("Provisioning: " + JSON.toJSONString(result));
		if(result != null){
			if(result.containsKey("action")){
				Variant actionVariant = result.get("action");
				if(actionVariant != null){
					String action = actionVariant.getValue().toString();
					Provision_Type type = Provision_Type.valueOf(action);
					if(Provision_Type.request_key.equals(type)){
						handleMessage(MeshHandle.requestKey, null, null);
					}else if(Provision_Type.done.equals(type)){
						if(result.containsKey("status") && result.containsKey("unicast")){
							int status = (int) result.get("status").getValue();
							if(status == 0){
								UInt16 unicast = (UInt16) result.get("unicast").getValue();
								handleMessage(MeshHandle.provision_done, unicast, null);
							}else{
								String error = "";
								if(result.containsKey("error")){
									error = result.get("error").getValue().toString();
								}
								handleMessage(MeshHandle.provision_done, null, error);
							}
						}
					}
				}
			}
		}
	}

	private void handleConnecting(Map<String,Variant> result){
		if(result != null){
			logger.info("Connecting : ["+ JSON.toJSONString(result)+"]");
			if(result.containsKey("connected")){
				Variant connectedVariant = result.get("connected"); 
				int status = (int) result.get("connected").getValue();
				
				String error = null;
				if(result.containsKey("error")){
					error = result.get("error").toString();
				}
				Connected_Type type = null;
				UInt16 unicast = null;
				if(result.containsKey("type")){
					String typeStr = result.get("type").getValue().toString();
					try{
						type = Connected_Type.valueOf(typeStr);
					}catch(Exception e){
					}
				}
				if(result.containsKey("unicast")){
					unicast = (UInt16) result.get("unicast").getValue();
				}
				if(ConnectHandle.instance.handleConneted(type, status, unicast)){
					handleMessage(MeshHandle.connect, status==1?true:false, error);
				}
			}
		}
	}
	
	private void handleStatus(Map<String,Variant> map){
		if(map != null){
			Variant nameVariant = map.get("name");
			Variant statusVariant = map.get("status");
			if(nameVariant != null && statusVariant != null){
				String name = nameVariant.getValue().toString();
				Integer status = (Integer) statusVariant.getValue();
				logger.info("Status: ["+ JSON.toJSONString(map)+"]");
				String error = null;
				if(map.containsKey("error")){
					error = map.get("error").getValue().toString();
				}
				SignalName signalName = SignalName.valueOf(name);
				MeshHandle type = null;
				switch (signalName) {
				case target: type = MeshHandle.config_target;
				case appkey: 
					if(type == null){
						type = MeshHandle.config_appkey;
					}
				case bind: 
					if(type == null){
						type = MeshHandle.config_bind;
					}
				case pub: 
					if(type == null){
						type = MeshHandle.config_pub;
					}
				case sub: 
					if(type == null){
						type = MeshHandle.config_sub;
					}
				case node: 
					if(type == null){
						type = MeshHandle.config_node;
					}
					if(status != null && status == 0){
						handleMessage(type, true, null);
					}else{
						handleMessage(type, false, error);
					}
					break;
				case composition: 
					ConnectHandle.instance.notifyConnected();
					if(error != null || status.intValue() != 0){
						handleMessage(MeshHandle.config_composition, null, error);
					}else if(map.containsKey("composition")){
						String deviceInfo = map.get("composition").getValue().toString();
						handleMessage(MeshHandle.config_composition, deviceInfo, null);
					}
					break;
				}
			}
		}
	}
	
	private void handleExecuting(Map<String,Variant> map){
		if(map != null){
			Variant statusVariant = map.get("status");
			Variant methodVariant = map.get("method");
			if(methodVariant != null && statusVariant != null){
				logger.info("handleExecuting : "+JSON.toJSON(map));
				String method = methodVariant.getValue().toString();
				Integer status = (Integer) statusVariant.getValue();
				if("menu".equals(method)){
					handleMessage(MeshHandle.menu,(status!=null && status ==0)?true:false,null);
				}else if("menu_back".equals(method)){
					handleMessage(MeshHandle.menu_back,(status!=null && status ==0)?true:false,null);
				}else if("provision".equals(method)){
					if(map.containsKey("error")){
						String error = map.get("error").toString();
						handleMessage(MeshHandle.provision_done, null, error);
					}
				}else if("appkey_add".equals(method)){
					if(map.containsKey("error")){
						String error = map.get("error").toString();
						handleMessage(MeshHandle.config_appkey, null, error);
					}
				}
			}
		}
	}
	
	private void handleModel(Map<String,Variant> map){
//		logger.info("Model handle: " + JSON.toJSONString(map));
		if(map != null && modelSignalListen != null){
			if(map.containsKey("object")){
				String objPath = map.get("object").getValue().toString();
				if(BasicConnection.DBUS_PATH_ON_OFF.equals(objPath)){
					UInt16 modelId = (UInt16) map.get("mod_id").getValue();
					UInt16 elemenetAddress = (UInt16) map.get("src").getValue();
					UInt16 state = (UInt16) map.get("state").getValue();
					modelSignalListen.onOffState(elemenetAddress, modelId, state);
				}
			}
		}
	}
	
	private void handleMessage(MeshHandle type,Object value,String error){
		if(meshSignalListen != null && type != null){
			meshSignalListen.handle(type, value, error);
		}
	}
	
	private void handleHeartBeat(Map<String,Variant> map){
//		logger.info("HeartBeat : "+JSON.toJSONString(map));
		if(map.containsKey("src")){
			HeartBeatHandler.instance.accept((UInt16) map.get("src").getValue());
		}
	}
	
	public void setModelStateListener(ModelSignalListen listen){
		this.modelSignalListen = listen;
	}
}
