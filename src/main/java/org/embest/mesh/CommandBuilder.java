package org.embest.mesh;

//import java.util.Map;
//import java.util.logging.Logger;

//import org.embest.MeshInterface;
import org.embest.mesh.commands.*;
//import org.embest.util.LogUtils;
//import org.freedesktop.dbus.DBusConnection;
//import org.freedesktop.dbus.UInt16;
//import org.freedesktop.dbus.Variant;
//import org.freedesktop.dbus.exceptions.DBusException;


public class CommandBuilder {
//	private static final Logger logger = LogUtils.getLogger("CommandBuilder");
//	private static final String DBUS_NAME = "org.embest";
//	private static final String DBUS_PATH_PROVISIONER = "/org/embest/Provisioner";
//	private static final String DBUS_PATH_CONFIG = "/org/embest/Config";
	
	public static ScanCommand scanCommand(){
		return new ScanCommand();
	}
	
	public static ProvisionCommand provisionCommand(){
		return new ProvisionCommand();
	}
	
	public static EnterKeyCommand enterKeyCommand(){
		return new EnterKeyCommand();
	}
	
	public static ConnectCommand connectCommand(){
		return new ConnectCommand();
	}
	
	public static DisconnectCommand disconnectCommand(){
		return new DisconnectCommand();
	}
	
	public static TargetCommand targetCommand(){
		return new TargetCommand();
	}
	
	public static AppkeyAddCommand appkeyAddCommand(){
		return new AppkeyAddCommand();
	}
	
	public static AppkeyDelCommand appkeyDelCommand(){
		return new AppkeyDelCommand();
	}
	
	public static BindCommand bindCommand(){
		return new BindCommand();
	}
	
	public static PubSetCommand pubSetCommand(){
		return new PubSetCommand();
	}
	
	public static SubAddCommand subAddCommand(){
		return new SubAddCommand();
	}
	
	public static CompositionCommand compositionCommand(){
		return new CompositionCommand();
	}
	
	public static NodeResetCommand nodeResetCommand(){
		return new NodeResetCommand();
	}
	
	public static MenuCommand menuCommand(){
		return new MenuCommand();
	}
	
	public static MenuBackCommand menuBackCommand(){
		return new MenuBackCommand();
	}
	
	public static DeviceOnOffGet deviceOnOffGet(){
		return new DeviceOnOffGet();
	}
	
	public static DeviceOnOffSet deviceOnOffSet(){
		return new DeviceOnOffSet();
	}
	
	public static HeartBeatPubSetCommand heartBeatPubSetCommand(){
		return new HeartBeatPubSetCommand();
	}
	
//	private static abstract class Connection{
//		private DBusConnection connection = null;
//		private MeshInterface meshInterface = null;
//		
//		protected MeshInterface loadMeshInterface(String dbusName,String dbusPath){
//			if(meshInterface == null){
//				if(connection == null){
//					try {
//						connection = DBusConnection.getConnection(DBusConnection.SYSTEM);
//						
//					} catch (DBusException e) {
//						logger.severe("Dbus Connect failed: "+e.getMessage());
//						connection = null;
//					}
//				}
//				try {
//					meshInterface = connection.getRemoteObject(dbusName, dbusPath,MeshInterface.class);
//				} catch (DBusException e) {
//					logger.severe("Dbus get Remote object["+dbusPath+"] failed.");
//					meshInterface = null;
//				}
//			}
//			return meshInterface;
//		}
//		
//		abstract MeshInterface getMeshInterface();
//		
//		public synchronized void disconnect(){
//			if(connection != null){
//				connection.disconnect();
//				connection = null;
//			}
//			meshInterface = null;
//		}
//	}
//	
//	private static class ProvisionerConnection extends Connection{
//		static final ProvisionerConnection instance = new ProvisionerConnection();
//		
//		private ProvisionerConnection(){}
//		
//		public synchronized MeshInterface getMeshInterface(){
//			
//			return loadMeshInterface(DBUS_NAME,DBUS_PATH_PROVISIONER);
//		}
//	}
//	
//	private static class ConfigConnection extends Connection{
//		static final ConfigConnection instance = new ConfigConnection();
//		
//		private ConfigConnection(){}
//		@Override
//		MeshInterface getMeshInterface() {
//			
//			return loadMeshInterface(DBUS_NAME, DBUS_PATH_CONFIG);
//		}
//		
//	}
//	
//	public static abstract class Command<T>{
//		private Connection connect;
//		Command(){
//			connect = getConection();
//		}
//		void disconnect(){
//			if(connect != null){
//				connect.disconnect();
//			}
//		}
//		MeshInterface getMeshInterface(){
//			if(connect != null){
//				return connect.getMeshInterface();
//			}
//			return null;
//		}
//		abstract Connection getConection();
//		
//		abstract void execute(T t);
//	}
//	
//	public static class ScanCommand extends Command<String>{
//
//		@Override
//		void execute(String t) {
//			MeshInterface mesh = getMeshInterface();
//			if(mesh != null && t != null && t.length() > 0){
//				logger.info("Command: DiscoverUnprovisioned "+t);
//				mesh.DiscoverUnprovisioned(t);
//			}
//		}
//
//		@Override
//		Connection getConection() {
//			return ProvisionerConnection.instance;
//		}
//	}
//	public static class ProvisionCommand extends Command<String>{
//
//		@Override
//		void execute(String t) {
//			MeshInterface mesh = getMeshInterface();
//			if(mesh != null && t != null && t.length() > 0){
//				logger.info("Command: Provision "+t);
//				mesh.Provision(t);
//			}
//		}
//
//		@Override
//		Connection getConection() {
//			return ProvisionerConnection.instance;
//		}
//	}
//	
//	public static class EnterKeyCommand extends Command<String> {
//
//		@Override
//		void execute(String t) {
//			MeshInterface mesh = getMeshInterface();
//			if(mesh != null && t != null && t.length() > 0){
//				logger.info("Command: EnterKey "+t);
//				mesh.EnterKey(t);
//			}
//		}
//
//		@Override
//		Connection getConection() {
//			return ProvisionerConnection.instance;
//		}
//	}
//	
//	public static class ConnectCommand extends Command<UInt16> {
//
//		@Override
//		void execute(UInt16 t) {
//			MeshInterface mesh = getMeshInterface();
//			if(mesh != null && t != null){
//				logger.info("Command: Connect "+t);
//				mesh.Connect(t);
//			}
//		}
//
//		@Override
//		Connection getConection() {
//			return ProvisionerConnection.instance;
//		}
//	}
//	
//	public static class DisconnectCommand extends Command<Void> {
//
//		@Override
//		void execute(Void t) {
//			MeshInterface mesh = getMeshInterface();
//			if(mesh != null){
//				logger.info("Command: Disconnect");
//				mesh.Disconnect();
//			}
//		}
//
//		@Override
//		Connection getConection() {
//			return ProvisionerConnection.instance;
//		}
//	}
//
//	public static class targetCommand extends Command<UInt16>{
//
//		@Override
//		Connection getConection() {
//			return ConfigConnection.instance;
//		}
//
//		@Override
//		void execute(UInt16 t) {
//			MeshInterface mesh = getMeshInterface();
//			if(mesh != null && t != null){
//				logger.info("Command: target "+t);
//				mesh.target(t);
//			}
//		}
//	}
//
//	public static class AppkeyAddCommand extends Command<UInt16>{
//
//		@Override
//		Connection getConection() {
//			return ConfigConnection.instance;
//		}
//
//		@Override
//		void execute(UInt16 t) {
//			MeshInterface mesh = getMeshInterface();
//			if(mesh != null && t != null){
//				logger.info("Command: appkey_add "+t);
//				mesh.appkey_add(t);
//			}
//		}
//	}
//	
//	public static class AppkeyDelCommand extends Command<UInt16>{
//
//		@Override
//		Connection getConection() {
//			return ConfigConnection.instance;
//		}
//
//		@Override
//		void execute(UInt16 t) {
//			MeshInterface mesh = getMeshInterface();
//			if(mesh != null && t != null){
//				logger.info("Command: appkey_del "+t);
//				mesh.appkey_del(t);
//			}
//		}
//	}
//
//	public static class BindCommand extends Command<Map<String,Variant>>{
//
//		@Override
//		Connection getConection() {
//			return ConfigConnection.instance;
//		}
//
//		@Override
//		void execute(Map<String, Variant> t) {
//			MeshInterface mesh = getMeshInterface();
//			if(mesh != null && t != null){
//				logger.info("Command: bind "+JSON.toJSONString(t));
//				mesh.bind(t);
//			}
//		}
//	}
//	
//	public static class PubSetCommand extends Command<Map<String,Variant>>{
//
//		@Override
//		Connection getConection() {
//			return ConfigConnection.instance;
//		}
//
//		@Override
//		void execute(Map<String, Variant> t) {
//			MeshInterface mesh = getMeshInterface();
//			if(mesh != null && t != null){
//				logger.info("Command: pub_set "+JSON.toJSONString(t));
//				mesh.pub_set(t);
//			}
//		}
//	}
//	public static class SubAddCommand extends Command<Map<String,Variant>>{
//
//		@Override
//		Connection getConection() {
//			return ConfigConnection.instance;
//		}
//
//		@Override
//		void execute(Map<String, Variant> t) {
//			MeshInterface mesh = getMeshInterface();
//			if(mesh != null && t != null){
//				logger.info("Command: sub_add "+JSON.toJSONString(t));
//				mesh.sub_add(t);
//			}
//		}
//	}
}
