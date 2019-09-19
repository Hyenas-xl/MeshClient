package org.embest.mesh.commands;

import java.util.Map;

import org.embest.mesh.Provisioner;
import org.embest.mesh.commands.BasicConnection.ConfigConnection;
import org.embest.mesh.commands.BasicConnection.Connection;
import org.freedesktop.dbus.Variant;

import com.alibaba.fastjson.JSON;

public class BindCommand extends BasicCommand<Map<String,Variant>,Provisioner>{

	@Override
	Connection<Provisioner> getConection() {
		return ConfigConnection.instance;
	}

	@Override
	public void execute(Map<String, Variant> t) {
		Provisioner mesh = getDbusInterface();
		if(mesh != null && t != null){
			logger.info("Command: bind "+JSON.toJSONString(t));
			mesh.bind(t);
		}
	}

}
