package org.embest;

import java.util.Map;

import org.embest.mesh.signal.Command;
import org.embest.mesh.signal.CommandCallBack;
import org.embest.mesh.signal.CommandRequestCallBack;
import org.embest.mesh.signal.DeviceStatusCallBack;
import org.embest.mesh.signal.ModelSignalListen;
import org.embest.model.mesh.Device;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.Variant;

public class AliCommand implements Command{
	private Command command;
	
	public AliCommand(Command command) {
		this.command = command;
	}
	
	@Override
	public void deviceOnline(DeviceStatusCallBack callback) {
		command.deviceOnline(callback);
	}
	
	@Override
	public void startScan(CommandCallBack<Device> callBack) {
		command.startScan(callBack);
	}

	@Override
	public void requestkey(CommandRequestCallBack<String> callback) {
		command.requestkey(callback);
	}

	@Override
	public void provision(String uuid, CommandCallBack<UInt16> callback,CommandCallBack<String> compositionCallBack) {
		command.provision(uuid, callback,compositionCallBack);
	}

	@Override
	public void target(UInt16 address, CommandCallBack<Boolean> callBack) {
		command.target(address, callBack);
	}

	@Override
	public void appkeyAdd(UInt16 inx, CommandCallBack<Boolean> callBack) {
		command.appkeyAdd(inx, callBack);
	}

	@Override
	public void appkeyDel(UInt16 inx, CommandCallBack<Boolean> callBack) {
		command.appkeyDel(inx, callBack);
	}

	@Override
	public void bind(Map<String, Variant> info, CommandCallBack<Boolean> callBack) {
		command.bind(info, callBack);
	}

	@Override
	public void pubSet(Map<String, Variant> info, CommandCallBack<Boolean> callBack) {
		command.pubSet(info, callBack);
	}

	@Override
	public void subAdd(Map<String, Variant> info, CommandCallBack<Boolean> callBack) {
		command.subAdd(info, callBack);
	}

	@Override
	public void composition(CommandCallBack<String> callBack) {
		command.composition(callBack);
	}

	@Override
	public void nodeReset(CommandCallBack<Boolean> callBack) {
		command.nodeReset(callBack);
	}

	@Override
	public void stopScan() {
		command.stopScan();
	}
	
	@Override
	public void deviceStateListen(ModelSignalListen listen) {
		command.deviceStateListen(listen);
	}
	
	@Override
	public void deviceOnOffGet(String address) {
		command.deviceOnOffGet(address);
	}

	@Override
	public void deviceOnOffSet(int address, int status) {
		command.deviceOnOffSet(address, status);
	}
}
