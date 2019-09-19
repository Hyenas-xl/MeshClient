package org.embest;

import org.embest.ali.AliKitService;
import org.embest.ali.Alikit;
import org.embest.ali.DeviceStatusHandle;
import org.embest.ali.ServiceHandle;
import org.embest.mesh.MeshCommand;
import org.embest.util.TaskUtil;

public class App {

	public static void main(String[] args) {
		Alikit kit = AliKitService.kit();
		kit.init();
		
		AliCommand command = new AliCommand(MeshCommand.instance);
		ServiceHandle serviceHandle = new ServiceHandle();
		serviceHandle.setCommand(command);
		kit.listenService(serviceHandle);
		kit.addDeviceNotifyListener(new DeviceStatusHandle(command));
		serviceHandle.connect();
//		new Thread(()->{
//			AliCommand command = new AliCommand(MeshCommand.instance);
//			TaskUtil.instance.add(()->{
//				try {
//					List<Device> devices = command.scan();
//					if(devices != null && devices.size() > 0){
//						devices.forEach(device ->{
//							System.out.println(device.getUuid()+" "+device.getName());
//						});
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			});
//		}).start();
		
		TaskUtil.instance.run();
	}

}
