package org.embest.mesh;

import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

public interface Model extends DBusInterface{

	void get(UInt16 address);
	
	void set(Map<String,Variant> info);
	
	public static class status extends DBusSignal{
		 public final Map<String,Variant>  result;
		 public status(String path,Map<String,Variant>  result) throws DBusException {
			 super(path);
			 this.result = result;
		 }
	 }
}
