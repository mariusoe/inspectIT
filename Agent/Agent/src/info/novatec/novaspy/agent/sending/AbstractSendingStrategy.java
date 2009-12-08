package info.novatec.novaspy.agent.sending;

import info.novatec.novaspy.agent.core.ICoreService;

/**
 * Every send strategy has to extend this abstract class. The first method that
 * is called after creating an instance is {@link #start()}. An event listener
 * or starting a thread has to be implemented there. {@link #stop()} immediately
 * stops the strategy. {@link #reset()} is called after a successful
 * {@link #sendNow()} is executed.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class AbstractSendingStrategy implements ISendingStrategy {

	/**
	 * The {@link ICoreService} implementation. Needed to actually trigger the
	 * sending of the data.
	 */
	private ICoreService coreService;

	/**
	 * Send the data to the server.
	 */
	protected final void sendNow() {
		coreService.sendData();
	}

	/**
	 * {@inheritDoc}
	 */
	public final void start(ICoreService coreService) {
		this.coreService = coreService;
		startStrategy();
	}

	/**
	 * This method has to be implemented by every strategy concerning the
	 * sending process. It will start the strategy.
	 */
	protected abstract void startStrategy();

	/**
	 * {@inheritDoc}
	 */
	public abstract void stop();

	/**
	 * Returns the value storage.
	 * 
	 * @return The value storage implementation.
	 */
	protected final ICoreService getCoreService() {
		return coreService;
	}

}
