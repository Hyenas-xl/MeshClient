package org.embest.mesh.signal;

public interface CommandRequestCallBack<T> {
	String execute(T value,String error);
}
