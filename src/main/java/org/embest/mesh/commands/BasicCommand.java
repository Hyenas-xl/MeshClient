package org.embest.mesh.commands;

import java.util.logging.Logger;

import org.embest.mesh.commands.BasicConnection.Connection;
import org.embest.util.LogUtils;
import org.freedesktop.dbus.DBusInterface;

public abstract class BasicCommand<K,V extends DBusInterface> {
	protected static final Logger logger = LogUtils.getLogger("BasicCommand");
	private Connection<V> connect;
	BasicCommand(){
		connect = getConection();
	}
	void disconnect(){
		if(connect != null){
			connect.disconnect();
		}
	}
	V getDbusInterface(){
		if(connect != null){
			return connect.loadDbusInterface();
		}
		return null;
	}
	abstract Connection<V> getConection();
	
	public abstract void execute(K t);
}
