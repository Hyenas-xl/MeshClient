package org.embest.mesh;

import java.util.logging.Logger;

import org.embest.ali.AliKitService;
import org.embest.ali.Config;
import org.embest.mesh.signal.ModelSignalListen;
import org.embest.model.SubDeviceInfo;
import org.embest.util.BASE64;
import org.embest.util.LogUtils;
import org.freedesktop.dbus.UInt16;

public class DeviceStatusListener implements ModelSignalListen{

	private static final Logger logger = LogUtils.getLogger("DeviceStatusListener");
	
	@Override
	public void onOffState(UInt16 elementAddr, UInt16 modelId, UInt16 state) {
//		logger.info("Post "+ elementAddr +" "+modelId+" "+state);
		SubDeviceInfo info = Config.instance.getSubDevice(elementAddr.intValue(), modelId.intValue());
		if(info != null){
			AliKitService.kit().postSubDeviceStatusProperty(info.getDeviceName(),BASE64.decryptBASE64(info.getDeviceSecret()),"status", state.intValue());
		}else{
			logger.severe("Not find device[model_id:"+modelId.intValue()+",ele_addr:"+elementAddr.intValue()+"]");
		}
	}

}
