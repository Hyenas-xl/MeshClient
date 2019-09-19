package org.embest.mesh.signal;

public interface MeshSignalListen {
	
	enum MeshHandle{
		scan,requestKey,provision_done,config_target,config_appkey,config_bind,config_pub,config_sub,
		config_composition,config_node,menu,menu_back,connect;
	}
	
	void handle(MeshHandle handle,Object value,String error);
}
