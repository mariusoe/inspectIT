package com.vladium.utils.timing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Uses the Java 1.5 timer method to get the current time. This is the preferred
 * method.
 * 
 * @author Patrice Bouillet
 * 
 */
public class JavaNanoTimer implements ITimer, ITimerConstants {

	/**
	 * The method which can only be used Java 1.5+.
	 */
	private static Method nanoMethod;

	static {
		Class clazz = System.class;
		try {
			nanoMethod = clazz.getDeclaredMethod("nanoTime", new Class[0]);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public double getCurrentTime() {
		try {
			Object currentTime = nanoMethod.invoke(nanoMethod, new Object[0]);
			return ((Long) currentTime).doubleValue() / 1000000.0d;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	public double getDuration() {
		return 0;
	}

}
