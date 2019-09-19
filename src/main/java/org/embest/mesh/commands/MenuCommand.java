package org.embest.mesh.commands;

import org.embest.mesh.Provisioner;
import org.embest.mesh.commands.BasicConnection.Connection;
import org.embest.mesh.commands.BasicConnection.ShellConnection;

public class MenuCommand extends BasicCommand<String,Provisioner>{

	@Override
	Connection<Provisioner> getConection() {
		return ShellConnection.instance;
	}

	@Override
	public void execute(String t) {
		Provisioner mesh = getDbusInterface();
		if(mesh != null && t != null){
			logger.info("Command: menu "+t);
			mesh.menu(t);
		}
	}

}
