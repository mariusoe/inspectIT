package info.novatec.inspectit.cmr.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * The registration service is used and called by all inspectIT Agents. First,
 * they have to call the {@link #registerPlatformIdent(List, String)} method
 * which returns a unique id for their JVM. Afterwards, all sensor types (no
 * matter if method- or platform-) are registered. Then they are going to
 * register all methods which are instrumented by that Agent. The last step is
 * to map the method sensor type to the instrumented method by calling the
 * {@link #addSensorTypeToMethod(long, long)} method.
 * <p>
 * All of this information will be persisted in the database. The returned
 * values are basically representing the index of the data in the db.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IRegistrationService extends Remote {

	/**
	 * A unique platform identifier is generated out of the network interfaces
	 * from the target server and by specifying a self-defined Agent name.
	 * 
	 * @param definedIPs
	 *            The list of all network interfaces.
	 * @param agentName
	 *            The self-defined name of the inspectIT Agent.
	 * @return Returns the unique platform identifier.
	 * @throws RemoteException
	 *             Throws a RemoteException if an error occurs in the
	 *             registering process.
	 * @throws LicenseException
	 *             Throws a {@link LicenseException} to indicate some problems
	 *             with the licensing process.
	 */
	long registerPlatformIdent(List definedIPs, String agentName) throws RemoteException;

	/**
	 * Every instrumented method has to be registered from every Agent. This
	 * method returns a unique value for this method so that measurements
	 * acquired from these methods can be linked in the database.
	 * 
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param packageName
	 *            The name of the package.
	 * @param className
	 *            The name of the class.
	 * @param methodName
	 *            The name of the method.
	 * @param parameterTypes
	 *            The parameter types of the method.
	 * @param returnType
	 *            The return type of the method.
	 * @param modifiers
	 *            The modifiers.
	 * @return Returns the unique method identifier.
	 * @throws RemoteException
	 *             Throws a RemoteException if an error occurs in the
	 *             registering process.
	 */
	long registerMethodIdent(long platformIdent, String packageName, String className, String methodName, List parameterTypes, String returnType, int modifiers)
			throws RemoteException;

	/**
	 * Every sensor type which is called by an instrumented method to gather
	 * data has to be registered by calling this method.
	 * 
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param fullyQualifiedClassName
	 *            The fully qualified class name of the sensor type.
	 * @return Returns the unique method sensor type identifier.
	 * @throws RemoteException
	 *             Throws a RemoteException if an error occurs in the
	 *             registering process.
	 */
	long registerMethodSensorTypeIdent(long platformIdent, String fullyQualifiedClassName) throws RemoteException;

	/**
	 * This method is used to map a registered method sensor type to a
	 * registered method.
	 * 
	 * @param methodSensorTypeIdent
	 *            The unique identifier of the sensor type.
	 * @param methodIdent
	 *            The unique identifier of the method.
	 * @throws RemoteException
	 *             Throws a RemoteException if an error occurs in the
	 *             registering process.
	 */
	void addSensorTypeToMethod(long methodSensorTypeIdent, long methodIdent) throws RemoteException;

	/**
	 * Every sensor type which gathers information about the target
	 * platform/system has to be registered by calling this method.
	 * 
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param fullyQualifiedClassName
	 *            The fully qualified class name of the sensor type.
	 * @return Returns the unique platform sensor type identifier.
	 * @throws RemoteException
	 *             Throws a RemoteException if an error occurs in the
	 *             registering process.
	 */
	long registerPlatformSensorTypeIdent(long platformIdent, String fullyQualifiedClassName) throws RemoteException;

}
