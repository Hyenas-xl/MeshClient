package org.embest.mesh.commands;

import org.embest.mesh.Provisioner;
import org.embest.mesh.commands.BasicConnection.Connection;

public class ScanCommand extends BasicCommand<String,Provisioner>{

	@Override
	Connection<Provisioner> getConection() {
		
		return BasicConnection.ProvisionerConnection.instance;
	}

	@Override
	public void execute(String t) {
		Provisioner mesh = getDbusInterface();
		if(mesh != null && t != null && t.length() > 0){
			logger.info("Command: DiscoverUnprovisioned "+t);
			mesh.discover_unprovisioned(t);
		}
	}

}
