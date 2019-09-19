package org.embest.mesh.signal;

public interface CommandCallBack<T> {
	void execute(T value,String error);
}
