package org.embest.mesh.commands;

import java.util.logging.Logger;

import org.embest.mesh.Model;
//import org.embest.MeshInterface;
import org.embest.mesh.Provisioner;
import org.embest.util.LogUtils;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.exceptions.DBusException;

public class BasicConnection {
	private static final Logger logger = LogUtils.getLogger("BasicConnection");
	private static final String DBUS_NAME = "org.embest";
	private static final String DBUS_PATH_PROVISIONER = "/org/embest/provisioner";
	private static final String DBUS_PATH_CONFIG = "/org/embest/config";
	private static final String DBUS_PATH_SHELL = "/org/embest/shell";
	public static final String DBUS_PATH_ON_OFF = "/org/embest/model/onoff";
	
	protected static abstract class Connection<T extends DBusInterface>{
		private DBusConnection connection = null;
		private T dBusInterface = null;
//		private Model
		
		protected T getDbusInterface(String dbusName,String dbusPath,Class<T> tClass){
			if(dBusInterface == null){
				if(connection == null){
					try {
						connection = DBusConnection.getConnection(DBusConnection.SYSTEM);
						
					} catch (DBusException e) {
						logger.severe("Dbus Connect failed: "+e.getMessage());
						connection = null;
					}
				}
				try {
					dBusInterface = connection.getRemoteObject(dbusName, dbusPath,tClass);
				} catch (DBusException e) {
					logger.severe("Dbus get Remote object["+dbusPath+"] failed.");
					dBusInterface = null;
				}
			}
			return dBusInterface;
		}
		
		abstract T loadDbusInterface();
		
		public synchronized void disconnect(){
			if(connection != null){
				connection.disconnect();
				connection = null;
			}
			dBusInterface = null;
		}
	}
	
	static class ProvisionerConnection extends Connection<Provisioner>{
		protected static final ProvisionerConnection instance = new ProvisionerConnection();
		
		private ProvisionerConnection(){}
		
		public synchronized Provisioner loadDbusInterface(){
			
			return getDbusInterface(DBUS_NAME,DBUS_PATH_PROVISIONER,Provisioner.class);
		}
	}
	
	static class ConfigConnection extends Connection<Provisioner>{
		protected static final ConfigConnection instance = new ConfigConnection();
		
		private ConfigConnection(){}
		@Override
		public synchronized Provisioner loadDbusInterface() {
			
			return getDbusInterface(DBUS_NAME, DBUS_PATH_CONFIG,Provisioner.class);
		}
		
	}
	
	static class ShellConnection extends Connection<Provisioner>{
		protected static final ShellConnection instance = new ShellConnection();
		
		private ShellConnection(){}
		@Override
		public synchronized Provisioner loadDbusInterface() {
			
			return getDbusInterface(DBUS_NAME, DBUS_PATH_SHELL,Provisioner.class);
		}
		
	}
	
	static class OnOffConnection extends Connection<Model>{
		protected static final OnOffConnection instance = new OnOffConnection();
		private OnOffConnection(){}
		
		@Override
		public synchronized Model loadDbusInterface() {
			
			return getDbusInterface(DBUS_NAME, DBUS_PATH_ON_OFF,Model.class);
		}
	}
}
