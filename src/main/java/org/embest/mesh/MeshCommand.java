package org.embest.mesh;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.embest.mesh.signal.Command;
import org.embest.mesh.signal.CommandCallBack;
import org.embest.mesh.signal.CommandRequestCallBack;
import org.embest.mesh.signal.DeviceStatusCallBack;
import org.embest.mesh.signal.MeshSignalListen;
import org.embest.mesh.signal.ModelSignalListen;
import org.embest.model.mesh.Device;
import org.embest.util.LogUtils;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

public class MeshCommand implements Command,MeshSignalListen{
	private static final Logger logger = LogUtils.getLogger("MeshCommand");
	private CommandCallBack<Device> scanCallBack = null;
	private CommandRequestCallBack<String> requestCallBack = null;
	private CommandCallBack<UInt16> provsionCallBack = null;
//	private CommandCallBack<Boolean> connectCallBack = null;
	private CommandCallBack<Boolean> targetCallBack = null;
	private CommandCallBack<Boolean> appkeyAddCallBack = null;
	private CommandCallBack<Boolean> appkeyDelCallBack = null;
	private CommandCallBack<Boolean> pubCallBack = null;
	private CommandCallBack<Boolean> subCallBack = null;
	private CommandCallBack<Boolean> bindCallBack = null;
	private CommandCallBack<String> compositionCallBack = null;
	private CommandCallBack<Boolean> nodeCallBack = null;
	private DeviceStatusCallBack deviceOnlineCallBack = null;
	private MeshSignalHandle signalHandler = null;
	
	public static final MeshCommand instance = new MeshCommand();
	private MeshCommand(){
		listen();
	}
	
	@Override
	public void deviceOnline(DeviceStatusCallBack callback) {
		this.deviceOnlineCallBack = deviceOnlineCallBack;
	}
	
	@Override
	public void startScan(CommandCallBack<Device> callBack) {
		this.scanCallBack = callBack;
		CommandBuilder.scanCommand().execute("on");
	}
	
	@Override
	public void stopScan() {
		CommandBuilder.scanCommand().execute("off");
	}
	@Override
	public void requestkey(CommandRequestCallBack<String> callback) {
		this.requestCallBack = callback;
	}
	@Override
	public void provision(String uuid, CommandCallBack<UInt16> callback,CommandCallBack<String> compositionCallBack){
		this.provsionCallBack = callback;
		this.compositionCallBack = compositionCallBack;
		CommandBuilder.provisionCommand().execute(uuid);
	}
//	@Override
//	public void connect(UInt16 address) {
////		this.connectCallBack = callBack;
//		CommandBuilder.connectCommand().execute(address);
//	}
//	
//	@Override
//	public void disconnect() {
//		CommandBuilder.disconnectCommand().execute(null);
//	}
	
	@Override
	public void target(UInt16 address, CommandCallBack<Boolean> callBack) {
		this.targetCallBack = callBack;
		CommandBuilder.targetCommand().execute(address);
	}
	
	@Override
	public void appkeyAdd(UInt16 inx, CommandCallBack<Boolean> callBack) {
		this.appkeyAddCallBack = callBack;
		CommandBuilder.appkeyAddCommand().execute(inx);
	}
	
	@Override
	public void appkeyDel(UInt16 inx, CommandCallBack<Boolean> callBack) {
		this.appkeyDelCallBack = callBack;
		CommandBuilder.appkeyDelCommand().execute(inx);
	}

	@Override
	public void pubSet(Map<String, Variant> info, CommandCallBack<Boolean> callBack) {
		this.pubCallBack = callBack;
		CommandBuilder.pubSetCommand().execute(info);
	}

	@Override
	public void subAdd(Map<String, Variant> info, CommandCallBack<Boolean> callBack) {
		this.subCallBack = callBack;
		CommandBuilder.subAddCommand().execute(info);
	}
	
	@Override
	public void bind(Map<String, Variant> info, CommandCallBack<Boolean> callBack) {
		this.bindCallBack = callBack;
		CommandBuilder.bindCommand().execute(info);
	}
	
	@Override
	public void composition(CommandCallBack<String> callBack){
		this.compositionCallBack = callBack;
		CommandBuilder.compositionCommand().execute(null);
	}
	
	@Override
	public void nodeReset(CommandCallBack<Boolean> callBack) {
		this.nodeCallBack = callBack;
		CommandBuilder.nodeResetCommand().execute(null);
	}
	
	@Override
	public void deviceStateListen(ModelSignalListen listen) {
		if(signalHandler != null){
			signalHandler.setModelStateListener(listen);
		}
	}
	
	@Override
	public void deviceOnOffGet(String address) {
		CommandBuilder.deviceOnOffGet().execute(new UInt16(Integer.parseInt(address, 16)));
	}
	
	@Override
	public void deviceOnOffSet(int address, int status) {
		Map<String, Variant> info = new HashMap<>();
		if(address > 0){
			info.put("addr", new Variant<UInt16>(new UInt16(address)));
			info.put("state", new Variant<UInt16>(new UInt16(status)));
			CommandBuilder.deviceOnOffSet().execute(info);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void listen(){
		try{
			DBusConnection conn = DBusConnection.getConnection(DBusConnection.SYSTEM); 
			signalHandler = new MeshSignalHandle(this);
		    conn.addSigHandler(Provisioner.unprovisioned_device_discovered.class,signalHandler);
		    conn.addSigHandler(Provisioner.provisioning.class,signalHandler);
		    conn.addSigHandler(Provisioner.status.class,signalHandler);
		    conn.addSigHandler(Provisioner.connecting.class,signalHandler);
		    conn.addSigHandler(Provisioner.executing.class,signalHandler);
		    conn.addSigHandler(Provisioner.heartbeat.class,signalHandler);
		    conn.addSigHandler(Model.status.class,signalHandler);
		} catch (DBusException e) {
			logger.severe("Listen dbus sigal failed: "+e.getMessage());
		}
	}
	
	@Override
	public void handle(MeshHandle handle, Object value, String error) {
		logger.info("Sigal handle: "+handle+"    "+value);
		switch (handle) {
		case scan:	callback(scanCallBack, value, error); break;
		case requestKey :	
				if(requestCallBack != null){
					String obcode = requestCallBack.execute(null, null);
					if(obcode != null && obcode.length() == 4){
						CommandBuilder.enterKeyCommand().execute(obcode);
					}else{
						provsionCallBack.execute(null, "Obcode is error");
						provsionCallBack = null;
					}
				}
				break;
		case provision_done:	callback(provsionCallBack, value, error); provsionCallBack = null;break;
		case config_target :	callback(targetCallBack, value, error); targetCallBack = null;break;
		case config_appkey :	
				callback(appkeyAddCallBack, value, error); 
				appkeyAddCallBack = null;
				callback(appkeyDelCallBack, value, error); 
				appkeyDelCallBack = null;
				break;
		case config_bind :	callback(bindCallBack, value, error); bindCallBack = null; break;
		case config_pub :	callback(pubCallBack, value, error); pubCallBack = null; break;
		case config_sub :	callback(subCallBack, value, error); subCallBack = null; break;
		case config_composition :	callback(compositionCallBack, value, error); compositionCallBack = null;break;
		case config_node :	callback(nodeCallBack, value, error); nodeCallBack = null;break;
		case connect : 	
//				callback(connectCallBack, value, error); 
				handleConnect((boolean) value, error); 
				break;
		default:
			logger.info("No handle.");
			break;
		}
	}
	
	private void callback(CommandCallBack callBack,Object value,String error){
		if(callBack != null){
			callBack.execute(value, error);
		}
	}
	
	
	private void handleConnect(boolean status,String error){
		logger.info("MeshCommand handle Connect: "+status +"  "+error);
		if(!status){
			if(requestCallBack != null){
				requestCallBack.execute(null, error);
			}
			callback(scanCallBack, null, error);
			callback(provsionCallBack, null, error);
			callback(targetCallBack, null, error);
			callback(appkeyAddCallBack, null, error);
			callback(appkeyDelCallBack, null, error);
			callback(bindCallBack, null, error);
			callback(pubCallBack, null, error);
			callback(subCallBack, null, error);
			callback(compositionCallBack, null, error);
			callback(nodeCallBack, null, error);
		}
	}
}
