package org.embest.model;

import java.io.Serializable;

public class SubDeviceInfo implements Serializable{
	
	private String deviceName;
	private String deviceSecret;
	private int address;
	private int unicast;
	private int element;
	private int modelId;
	private int server;
	
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getDeviceSecret() {
		return deviceSecret;
	}
	public void setDeviceSecret(String deviceSecret) {
		this.deviceSecret = deviceSecret;
	}
	public int getAddress() {
		return address;
	}
	public void setAddress(int address) {
		this.address = address;
	}
	public int getUnicast() {
		return unicast;
	}
	public void setUnicast(int unicast) {
		this.unicast = unicast;
	}
	public int getElement() {
		return element;
	}
	public void setElement(int element) {
		this.element = element;
	}
	public int getModelId() {
		return modelId;
	}
	public void setModelId(int modelId) {
		this.modelId = modelId;
	}
	public int getServer() {
		return server;
	}
	public void setServer(int server) {
		this.server = server;
	}
	@Override
	public int hashCode() {
		return deviceName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return hashCode() == obj.hashCode();
	}
}
