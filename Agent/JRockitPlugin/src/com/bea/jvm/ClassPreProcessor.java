package com.bea.jvm;

public interface ClassPreProcessor {

	byte[] preProcess(ClassLoader classLoader, String className,
			byte[] classBytes);

}
