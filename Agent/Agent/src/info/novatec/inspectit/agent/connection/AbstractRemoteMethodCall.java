package info.novatec.inspectit.agent.connection;

import info.novatec.inspectit.agent.connection.impl.AdditiveWaitRetryStrategy;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * See http://www.onjava.com/pub/a/onjava/2001/10/17/rmi.html?page=3 for more details.
 * 
 * @author William Grosso
 * 
 */
public abstract class AbstractRemoteMethodCall {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AbstractRemoteMethodCall.class.getName());

	/**
	 * Performs the actual call to the server.
	 * 
	 * @return The object returned from the server (if there is one).
	 * @throws ServerUnavailableException
	 *             Throws a ServerUnavailable exception if the server isn't available anymore due to
	 *             network problems or something else.
	 */
	public final Object makeCall() throws ServerUnavailableException {
		RetryStrategy strategy = getRetryStrategy();
		while (strategy.shouldRetry()) {
			Remote remoteObject = getRemoteObject();
			if (null == remoteObject) {
				throw new ServerUnavailableException();
			}
			try {
				return performRemoteCall(remoteObject);
			} catch (RemoteException remoteException) {
				try {
					strategy.remoteExceptionOccured();
				} catch (RetryException retryException) {
					handleRetryException(remoteObject);
				}
			}
		}
		return null;
	}

	/*
	 * The next 4 methods define the core behavior. Of these, two must be implemented by the
	 * subclass (and so are left abstract). The remaining three can be altered to provide customized
	 * retry handling.
	 */

	/**
	 * getRemoteObject is a template method which should, in most cases, return the stub.
	 * 
	 * @return The Remote Stub
	 * @throws ServerUnavailableException
	 *             Throws a ServerUnavailable exception if the server isn't available anymore due to
	 *             network problems or something else.
	 */
	protected abstract Remote getRemoteObject() throws ServerUnavailableException;

	/**
	 * performRemoteCall is a template method which actually makes the remote method invocation.
	 * 
	 * @param remoteObject
	 *            The actual remote object.
	 * @return The {@link Object} received from the server.
	 * @throws RemoteException
	 *             If an exception was thrown during the call on the server.
	 */
	protected abstract Object performRemoteCall(Remote remoteObject) throws RemoteException;

	/**
	 * Returns the selected retry strategy.
	 * 
	 * @return The retry strategy.
	 */
	protected final RetryStrategy getRetryStrategy() {
		return new AdditiveWaitRetryStrategy();
	}

	/**
	 * This method is executed if some calls to the server weren't successful.
	 * 
	 * @param remoteObject
	 *            The remote object.
	 * @throws ServerUnavailableException
	 *             The exception {@link ServerUnavailableException} is always thrown when this
	 *             method is entered.
	 */
	protected final void handleRetryException(final Remote remoteObject) throws ServerUnavailableException {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Repeated attempts to communicate with " + remoteObject + " failed.");
		}
		throw new ServerUnavailableException();
	}

}
