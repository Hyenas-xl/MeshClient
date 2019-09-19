//package org.embest;
//
//import java.util.Map;
//
//import org.freedesktop.dbus.DBusInterface;
//import org.freedesktop.dbus.DBusSignal;
//import org.freedesktop.dbus.UInt16;
//import org.freedesktop.dbus.Variant;
//import org.freedesktop.dbus.exceptions.DBusException;
//
//public interface MeshInterface extends DBusInterface{
//	void discover_unprovisioned(String onoff);
//	
//	void provision(String uuid);
//	
//	void enter_key(String key);
//	
//	void connect(UInt16 address);
//	
//	void disconnect();
//	
//	void target(UInt16 unicast);
//	
//	void appkey_add(UInt16 inx);
//	
//	void appkey_del(UInt16 inx);
//	
//	void bind(Map<String,Variant> info);
//	
//	void pub_set(Map<String,Variant> info);
//	
//	void sub_add(Map<String,Variant> info);
//	
//	void composition_get();
//	
//	void node_reset();
//	
//	void menu(String name);
//	
//	void menu_back();
//	
//	void get(UInt16 address);
//	
//	void set(Map<String,Variant> info);
//	
//	 public static class unprovisioned_device_discovered extends DBusSignal {
//		 public final Map<String,Variant> device; 
//		 public unprovisioned_device_discovered(String path,Map<String,Variant> device) throws DBusException {
//			super(path,device);
//			this.device = device;
//		}
//	 }
//	 
//	 public static class provisioning extends DBusSignal {
//		 public final Map<String,Variant> result; 
//		 public provisioning(String path,Map<String,Variant>  result) throws DBusException {
//			super(path,result);
//			this.result = result;
//		}
//	 }
//	 
//	 public static class connecting extends DBusSignal {
//		 public final Map<String,Variant> result; 
//		 public connecting(String path,Map<String,Variant>  result) throws DBusException {
//			super(path,result);
//			this.result = result;
//		}
//	 }
//	 
//	 public static class status extends DBusSignal{
//		 public final Map<String,Variant>  result;
//		 public status(String path,Map<String,Variant>  result) throws DBusException {
//			 super(path);
//			 this.result = result;
//		 }
//	 }
//	 
//	 public static class executing extends DBusSignal{
//		 public final Map<String,Variant>  result;
//		 public executing(String path,Map<String,Variant> result) throws DBusException {
//			 super(path);
//			 this.result = result;
//		}
//	 }
//}
