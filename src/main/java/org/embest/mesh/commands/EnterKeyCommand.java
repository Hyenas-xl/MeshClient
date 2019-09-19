package org.embest.mesh.commands;

import org.embest.mesh.Provisioner;
import org.embest.mesh.commands.BasicConnection.Connection;
import org.embest.mesh.commands.BasicConnection.ProvisionerConnection;

public class EnterKeyCommand extends BasicCommand<String,Provisioner>{

	@Override
	Connection<Provisioner> getConection() {
		return ProvisionerConnection.instance;
	}

	@Override
	public void execute(String t) {
		Provisioner mesh = getDbusInterface();
		if(mesh != null && t != null && t.length() > 0){
			logger.info("Command: EnterKey "+t);
			mesh.enter_key(t);
		}
	}

}
