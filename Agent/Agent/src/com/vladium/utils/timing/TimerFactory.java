package com.vladium.utils.timing;

import java.util.logging.Logger;

// ----------------------------------------------------------------------------
/**
 * This non-instantiable non-extendible class acts as a Factory for
 * {@link ITimer} implementations.
 * 
 * @author (C) <a href="mailto:vroubtsov@illinoisalumni.org">Vlad Roubtsov</a>,
 *         2002
 */
public abstract class TimerFactory {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(TimerFactory.class.getName());

	// public: ................................................................

	/**
	 * Creates a new instance of {@link ITimer} which is returned in 'ready'
	 * state. If the JNI-based/high-resolution implementation is not available
	 * this will return an instance of <code>JavaSystemTimer</code>, so this
	 * method is guaranteed not to fail.
	 * 
	 * @return ITimer a new timer instance in 'ready' state [never null]
	 */
	public static ITimer newTimer() {
		Class clazz = System.class;
		try {
			clazz.getDeclaredMethod("nanoTime", new Class[0]);
			LOGGER.info("Using Java 1.5+ nano time resolution");
			return new JavaNanoTimer();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			LOGGER.info("Standard Java 1.5+ nano time resolution not available!");
		}

		try {
			ITimer timer = new HRTimer();
			LOGGER.info("Using native high resolution timer");
			return timer;
		} catch (Throwable t) {
			LOGGER.info("Native high resolution timer not available!");
			LOGGER.info("Falling back to old Java system timer resolution");
			return new JavaSystemTimer();
		}
	}

	// protected: .............................................................

	// package: ...............................................................

	// private: ...............................................................

	private TimerFactory() {
	} // prevent subclassing

} // end of class
// ----------------------------------------------------------------------------
