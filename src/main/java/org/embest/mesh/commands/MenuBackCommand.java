package org.embest.mesh.commands;

import org.embest.mesh.Provisioner;
import org.embest.mesh.commands.BasicConnection.Connection;
import org.embest.mesh.commands.BasicConnection.ShellConnection;

public class MenuBackCommand extends BasicCommand<Void,Provisioner>{

	@Override
	Connection<Provisioner> getConection() {
		return ShellConnection.instance;
	}

	@Override
	public void execute(Void t) {
		Provisioner mesh = getDbusInterface();
		if(mesh != null && t != null){
			logger.info("Command: menu_back "+t);
			mesh.menu_back();
		}
	}

}
