package org.embest.mesh.commands;

import org.embest.mesh.Model;
import org.embest.mesh.commands.BasicConnection.Connection;
import org.embest.mesh.commands.BasicConnection.OnOffConnection;
import org.freedesktop.dbus.UInt16;

public class DeviceOnOffGet extends BasicCommand<UInt16,Model>{

	@Override
	Connection<Model> getConection() {
		return OnOffConnection.instance;
	}

	@Override
	public void execute(UInt16 t) {
		Model mesh = getDbusInterface();
		if(mesh != null && t != null){
			logger.info("Command: Device onoff get: ");
			mesh.get(t);
		}
	}

}
