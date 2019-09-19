package org.embest.mesh;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import org.embest.ali.AliKitService;
import org.embest.ali.Config;
import org.embest.model.SubDeviceInfo;
import org.embest.util.LogUtils;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.Variant;

public class HeartBeatHandler {
	private static final Logger logger = LogUtils.getLogger("HeartBeatHandler");
	private int checkPeriod = 20000;
	private long timeoutLine = 40000;
	private int period = 6;
	private int netInx = 0;
	private int pubAddr = 49152;// c000
	public static final HeartBeatHandler instance = new HeartBeatHandler();
	private Map<UInt16,NodeStatus> map = new HashMap<>();
	private Lock lock = new ReentrantLock();
	
	private HeartBeatHandler(){
		new Thread(new NodeCheck()).start();
	}
	
	public void listenDevice(UInt16 unicast){
		Map<String, Variant> info = new HashMap<>();
		info.put("pub_addr", new Variant(new UInt16(pubAddr)));
		info.put("net_idx", new Variant(new UInt16(netInx)));
		info.put("period", new Variant(new UInt16(period)));
		lock.lock();
		NodeStatus nodeStatus = new NodeStatus();
		nodeStatus.unicast = unicast;
		map.put(unicast, nodeStatus);
		lock.unlock();
		CommandBuilder.heartBeatPubSetCommand().execute(info);
	}
	
	public void accept(UInt16 unicast){
		lock.lock();
		NodeStatus nodeStatus = map.get(unicast);
		if(nodeStatus == null){
			nodeStatus = new NodeStatus();
			nodeStatus.unicast = unicast;
			map.put(unicast, nodeStatus);
		}
		nodeStatus.setOnLine(true);
		nodeStatus.time = System.currentTimeMillis();
		lock.unlock();
	}
	
	class NodeCheck implements Runnable{

		@Override
		public void run() {
			while(true){
				lock.lock();
					for(UInt16 key : map.keySet()){
						NodeStatus nodeStatus = map.get(key);
						if(nodeStatus != null){
							long time = System.currentTimeMillis() - nodeStatus.time;
							if(time > timeoutLine){
								nodeStatus.setOnLine(false);
							}
						}
					}
				lock.unlock();
				synchronized (NodeCheck.this) {
					try {
						wait(checkPeriod);
					} catch (InterruptedException e) {
					}
				}
			}
		}
		
	}
	
	class NodeStatus{
		UInt16 unicast;
		long time = 0L;
		boolean onLine = false;
		
		void setOnLine(boolean onLine) {
//			logger.info("Post Node["+unicast+"] status["+onLine+"]");
			this.onLine = onLine;
			List<SubDeviceInfo> subDeviceInfos = Config.instance.getSubDeviceByUnicast(unicast.intValue());
			if(subDeviceInfos.size() > 0){
				subDeviceInfos.forEach(subDeviceInfo->{
					if(onLine){
						AliKitService.kit().loginSubDevice(subDeviceInfo.getDeviceName());
					}else{
						AliKitService.kit().logoutSubDevice(subDeviceInfo.getDeviceName());
					}
				});
			}
		}
	}
}
