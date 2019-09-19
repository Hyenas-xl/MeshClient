package org.embest.mesh.commands;

import org.embest.mesh.Provisioner;
import org.embest.mesh.commands.BasicConnection.ConfigConnection;
import org.embest.mesh.commands.BasicConnection.Connection;

public class NodeResetCommand extends BasicCommand<Void,Provisioner>{

	@Override
	Connection<Provisioner> getConection() {
		return ConfigConnection.instance;
	}

	@Override
	public void execute(Void t) {
		Provisioner mesh = getDbusInterface();
		if(mesh != null){
			logger.info("Command: node_reset ");
			mesh.node_reset();
		}
	}

}
