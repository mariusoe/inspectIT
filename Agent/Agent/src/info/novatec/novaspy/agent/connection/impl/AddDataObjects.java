package info.novatec.novaspy.agent.connection.impl;

import info.novatec.novaspy.agent.connection.AbstractRemoteMethodCall;
import info.novatec.novaspy.agent.connection.ServerUnavailableException;
import info.novatec.novaspy.cmr.service.IAgentStorageService;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Class which encapsulates the request to the {@link Remote} object
 * {@link IRepository}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class AddDataObjects extends AbstractRemoteMethodCall {

	/**
	 * The reference to the repository which accepts our data.
	 */
	private final Remote repository;

	/**
	 * A list containing our measurements we want to send.
	 */
	private final List dataObjects;

	/**
	 * The only constructor for this class accepts 2 attributes. The first one
	 * is the {@link Remote} object, which will be used to send the data. The
	 * second one, a {@link List} of measurements, is the actual data.
	 * 
	 * @param repository
	 *            The {@link Remote} object.
	 * @param dataObjects
	 *            The {@link List} of data objects to send.
	 */
	public AddDataObjects(IAgentStorageService repository, List dataObjects) {
		this.repository = repository;
		this.dataObjects = dataObjects;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Remote getRemoteObject() throws ServerUnavailableException {
		return repository;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Object performRemoteCall(Remote remoteObject) throws RemoteException {
		IAgentStorageService repo = (IAgentStorageService) remoteObject;
		repo.addDataObjects(dataObjects);
		return null;
	}

}
