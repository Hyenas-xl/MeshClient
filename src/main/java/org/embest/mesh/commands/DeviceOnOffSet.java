package org.embest.mesh.commands;

import java.util.Map;

import org.embest.mesh.Model;
import org.embest.mesh.commands.BasicConnection.Connection;
import org.embest.mesh.commands.BasicConnection.OnOffConnection;
import org.freedesktop.dbus.Variant;

import com.alibaba.fastjson.JSON;

public class DeviceOnOffSet extends BasicCommand<Map<String,Variant>,Model>{

	@Override
	Connection<Model> getConection() {
		return OnOffConnection.instance;
	}

	@Override
	public void execute(Map<String,Variant> t) {
		Model mesh = getDbusInterface();
		if(mesh != null && t != null){
			logger.info("Command: Device onoff set: "+JSON.toJSONString(t));
			mesh.set(t);
		}
	}

}
