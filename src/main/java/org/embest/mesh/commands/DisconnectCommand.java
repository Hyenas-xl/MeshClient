package org.embest.mesh.commands;

import org.embest.mesh.Provisioner;
import org.embest.mesh.commands.BasicConnection.Connection;
import org.embest.mesh.commands.BasicConnection.ProvisionerConnection;

public class DisconnectCommand extends BasicCommand<Void,Provisioner>{

	@Override
	Connection<Provisioner> getConection() {
		return ProvisionerConnection.instance;
	}

	@Override
	public void execute(Void t) {
		Provisioner mesh = getDbusInterface();
		if(mesh != null){
			logger.info("Command: Disconnect");
			mesh.disconnect();
		}
	}

}
