package info.novatec.novaspy.cmr.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This service is used by the Agents to send their data objects to and it is
 * used by all the graphical interfaces to request these data objects and a part
 * of the domain model.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IAgentStorageService extends Remote {

	/**
	 * Data Objects are used for all transmissions between the Agent(s), the CMR
	 * and the user interface.
	 * 
	 * @param dataObjects
	 *            The list containing all the data objects.
	 * @throws RemoteException
	 *             If a remote exception occurs somewhere.
	 */
	void addDataObjects(List dataObjects) throws RemoteException;

}
