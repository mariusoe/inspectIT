package com.ibm.websphere.classloader;

public interface ClassLoaderPlugin {
	public byte[] preDefineApplicationClass(String className, byte[] bytes);
	public byte[] preDefineRuntimeClass(String className, byte[] bytes);
}
