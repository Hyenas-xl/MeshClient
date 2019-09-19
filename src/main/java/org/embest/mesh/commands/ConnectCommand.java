package org.embest.mesh.commands;

import java.util.Map;

import org.embest.mesh.Provisioner;
import org.embest.mesh.commands.BasicConnection.Connection;
import org.embest.mesh.commands.BasicConnection.ProvisionerConnection;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.Variant;

public class ConnectCommand extends BasicCommand<Map<String,Variant>,Provisioner>{

	@Override
	Connection<Provisioner> getConection() {
		return ProvisionerConnection.instance;
	}

	@Override
	/**
	 * 目前只支持一个 ，默认0
	 */
	public void execute(Map<String,Variant> t) {
		Provisioner mesh = getDbusInterface();
		if(mesh != null && t != null){
			logger.info("Command: Connect "+t);
			mesh.connect(t);
		}
	}

}
