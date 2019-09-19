package org.embest.model.mesh;

public class Device {
	
	private String name;
	private String uuid;
	
	public Device(String uuid,String name){
		this.uuid = uuid;
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public String getUuid() {
		return uuid;
	}
}
