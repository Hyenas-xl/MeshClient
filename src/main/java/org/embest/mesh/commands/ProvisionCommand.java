package org.embest.mesh.commands;

import org.embest.mesh.Provisioner;
import org.embest.mesh.commands.BasicConnection.Connection;
import org.embest.mesh.commands.BasicConnection.ProvisionerConnection;

public class ProvisionCommand extends BasicCommand<String,Provisioner>{

	@Override
	public void execute(String t) {
		Provisioner mesh = getDbusInterface();
		if(mesh != null && t != null && t.length() > 0){
			logger.info("Command: Provision "+t);
			mesh.provision(t);
		}
	}

	@Override
	Connection<Provisioner> getConection() {
		return ProvisionerConnection.instance;
	}

}
