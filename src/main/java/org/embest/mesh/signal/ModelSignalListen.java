package org.embest.mesh.signal;

import org.freedesktop.dbus.UInt16;

public interface ModelSignalListen {
	
	void onOffState(UInt16 elementAddr,UInt16 modelId,UInt16 state);

}
