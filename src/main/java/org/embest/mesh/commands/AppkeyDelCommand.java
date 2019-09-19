package org.embest.mesh.commands;

import org.embest.mesh.Provisioner;
import org.embest.mesh.commands.BasicConnection.ConfigConnection;
import org.embest.mesh.commands.BasicConnection.Connection;
import org.freedesktop.dbus.UInt16;

public class AppkeyDelCommand extends BasicCommand<UInt16,Provisioner>{

	@Override
	Connection<Provisioner> getConection() {
		return ConfigConnection.instance;
	}

	@Override
	public void execute(UInt16 t) {
		Provisioner mesh = getDbusInterface();
		if(mesh != null && t != null){
			logger.info("Command: appkey_del "+t);
			mesh.appkey_del(t);
		}
	}
}