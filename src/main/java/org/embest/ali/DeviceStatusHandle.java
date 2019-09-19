package org.embest.ali;

import java.util.logging.Logger;

import org.embest.ali.Monitor.DeviceNotifyStatusAdapter;
import org.embest.mesh.signal.Command;
import org.embest.util.LogUtils;

public class DeviceStatusHandle extends DeviceNotifyStatusAdapter{
	private static final Logger logger = LogUtils.getLogger("DeviceStatusHandle");
	private Command command = null;
	public DeviceStatusHandle(Command command){
		this.command = command;
	}
	@Override
	public void deviceStatus(String name, int status) {
		logger.info("Device status set: [name:"+name+",status:"+status+"]");
		if(command != null){
			int address = Config.instance.getDeviceAddress(name);
			if(address > 0){
				command.deviceOnOffSet(address, status);
			}else{
				logger.severe("Device status set: [name:"+name+"] cannot find address.");
			}
		}
	}

}
