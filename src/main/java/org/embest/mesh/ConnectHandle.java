package org.embest.mesh;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.embest.util.LogUtils;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.Variant;

public class ConnectHandle {
	private static final Logger logger = LogUtils.getLogger("ConnectHandle");
	private Consumer<Boolean> netConsumer = null;
	private Consumer<Boolean> identityConsumer = null;
	private Connected_Type currentType = null;
	private UInt16 currentNode = null;
	private boolean reconnected = true;
//	private NodeConnectedListener nodeConnectedListener = null;
	public static ConnectHandle instance = new ConnectHandle();
	private Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	
	private ConnectHandle(){};
	enum Connected_Type{
		net,identity;
	}
	
	public void connectedNetStatus(Consumer<Boolean> consumer){
		this.netConsumer = consumer;
	}
	
	public void cleanNetConsumer(){
		this.netConsumer = null;
	}
	public void cleanIdentityConsumer(){
		this.identityConsumer = null;
	}
	
	public void setReconnected(boolean connected){
		this.reconnected = connected;
	}
	
	public void connectNet(){
		disconnect();
		reconnected = true;
		Map<String,Variant> info = new HashMap<String, Variant>();
		info.put("net_idx", new Variant(new UInt16(0)));
		info.put("node_addr", new Variant(new UInt16(0)));
		CommandBuilder.connectCommand().execute(info);
	}
	
	public void connectNode(UInt16 unicast,Consumer<Boolean> consumer){
		if(unicast == null){
			return;
		}
		if(currentType != null){
			if(Connected_Type.identity.equals(currentType) && unicast.equals(currentNode)){
				consumer.accept(true);
				return;
			}
		}
		
		disconnect();
		reconnected = false;
		this.identityConsumer = consumer;
		logger.info("Connect node: " + unicast);
		Map<String,Variant> info = new HashMap<String, Variant>();
		info.put("net_idx", new Variant(new UInt16(0)));
		info.put("node_addr", new Variant(unicast));
		CommandBuilder.connectCommand().execute(info);
	}
	
	public void disconnect(){
		reconnected = false;
		currentType = null;
		currentNode = null;
		CommandBuilder.disconnectCommand().execute(null);
		lock.lock();
		try {
			condition.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.severe("Disconnect timeout");;
		} finally {
			lock.unlock();
		}
	}
	
	public boolean handleConneted(Connected_Type type,int status,UInt16 unicast){
		logger.info("Connected: "+type +" "+status+" "+unicast);
		currentType = type;
		if(status == 0){
			currentType = null;
			currentNode = null;
			notifyConnected();
			if(Connected_Type.identity.equals(type)){
				currentNode = unicast;
				if(identityConsumer != null){
					identityConsumer.accept(false);
				}
				reconnected = true;
			}
			if(reconnected){
				connectNet();
				return true;
			}
		}else{
			if(Connected_Type.identity.equals(type)){
				lock.lock();
				try{
					System.out.println("wait");
					condition.await(20,TimeUnit.SECONDS);
					System.out.println("go on");
					currentNode = unicast;
					if(identityConsumer != null){
						identityConsumer.accept(true);
					}
				} catch (InterruptedException e) {
					logger.severe("Connected failed: " + e.getMessage());
				}finally{
					lock.unlock();
				}
				reconnected = true;
			}else if(Connected_Type.net.equals(type)){
				if(netConsumer != null){
					netConsumer.accept(status == 1);
				}
			}
		}
//		if(status == 0){
//			if(Connected_Type.identity.equals(currentType) && currentNode != null){
////				if(nodeConnectedListener != null){
////					nodeConnectedListener.connected(currentNode, false);
////				}
//				currentType = null;
//				currentNode = null;
//			}else if(reconnected){
//				currentType = null;
//				currentNode = null;
//				connectNet();
//				return true;
//			}
//			notifyConnected();
//		}else{
//			currentType = type;
//			if(Connected_Type.identity.equals(type)){
//				lock.lock();
//				try{
//					System.out.println("wait");
//					condition.await(20,TimeUnit.SECONDS);
//					System.out.println("go on");
//					currentNode = unicast;
//					reconnected = false; 
//					if(identityConsumer != null){
//						identityConsumer.accept(status == 1);
//					}
//	//				if(nodeConnectedListener != null){
//	//					nodeConnectedListener.connected(unicast, status==1);
//	//				}
//				} catch (InterruptedException e) {
//					logger.severe("Connected failed: " + e.getMessage());
//				}finally{
//					lock.unlock();
//				}
//			}else if(Connected_Type.net.equals(type)){
//				if(netConsumer != null){
//					netConsumer.accept(status == 1);
//				}
//			}
//			
//		}
		return false;
	}
	
	public void notifyConnected(){
		lock.lock();
		condition.signal();
		lock.unlock();
	}
//	
//	interface NodeConnectedListener{
//		void connected(UInt16 unicast,boolean status);
//	}
}
