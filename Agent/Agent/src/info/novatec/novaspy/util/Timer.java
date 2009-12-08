package info.novatec.novaspy.util;

import com.vladium.utils.timing.ITimer;
import com.vladium.utils.timing.TimerFactory;

/**
 * Timer utility class which is basically a wrapper around the Timer returned by
 * the {@link TimerFactory}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class Timer {

	/**
	 * The native timer.
	 */
	private final ITimer nativeTimer;

	/**
	 * Initializes this class and retrieves a new timer from the
	 * {@link TimerFactory}.
	 */
	public Timer() {
		nativeTimer = TimerFactory.newTimer();
	}

	/**
	 * Returns the current time.
	 * 
	 * @see ITimer#getCurrentTime().
	 * @return The time as a double value.
	 */
	public double getCurrentTime() {
		return nativeTimer.getCurrentTime();
	}

}
