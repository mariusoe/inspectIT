package info.novatec.novaspy.agent.sending.impl;

import info.novatec.novaspy.agent.sending.AbstractSendingStrategy;

import java.util.Map;


/**
 * Implements a strategy to wait a specific (user-defined) time and then
 * executes the sending of the data.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TimeStrategy extends AbstractSendingStrategy {

	/**
	 * The default wait time.
	 */
	public static final long DEFAULT_WAIT_TIME = 5000L;

	/**
	 * The wait time.
	 */
	private long time = DEFAULT_WAIT_TIME;

	/**
	 * The thread which waits the specified time and starts the sending process.
	 */
	private volatile Trigger trigger;

	/**
	 * If we are allowed to send something right now.
	 */
	private boolean allowSending = true;

	/**
	 * {@inheritDoc}
	 */
	public void startStrategy() {
		trigger = new Trigger();
		trigger.start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		// Interrupt the thread to stop it
		Thread temp = trigger;
		trigger = null;
		synchronized (temp) {
			temp.interrupt();
		}
	}

	/**
	 * The Trigger class is basically a {@link Thread} which starts the sending
	 * process once the specified time is passed by.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private class Trigger extends Thread {

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			Thread thisThread = Thread.currentThread();
			while (trigger == thisThread) {
				try {
					synchronized (this) {
						wait(time);
					}

					if (allowSending) {
						sendNow();
					}
				} catch (InterruptedException e) {
					// nothing to do
				}
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map settings) {
		this.time = Long.parseLong((String) settings.get("time"));
	}

}
