package info.novatec.inspectit.cmr.service;

import java.rmi.RemoteException;
import java.util.List;

/**
 * This is a copy of the {@link IRegistrationService}-Interface. This is needed because Spring 3.0
 * requires to use interaces for bean injection. The {@link IInternRegistrationService} differs only
 * in two ways against the {@link IRegistrationService}. The exception throws declaration on
 * {@link IInternRegistrationService#registerMethodIdent(long, String, String, String, List, String, int)}
 * and {@link IInternRegistrationService#registerPlatformSensorTypeIdent(long, String)} were
 * removed.
 * 
 * @author Dirk Maucher
 * 
 */
public interface IInternRegistrationService {

	/**
	 * A unique platform identifier is generated out of the network interfaces from the target
	 * server and by specifying a self-defined Agent name.
	 * 
	 * Note: the version String of the agent is not used to match existing platform identifications,
	 * that is even if the version String changes the platform identification will still be the
	 * same.
	 * 
	 * @param definedIPs
	 *            The list of all network interfaces.
	 * @param agentName
	 *            The self-defined name of the inspectIT Agent.
	 * @param version
	 *            The version the agent is currently running with.
	 * @return Returns the unique platform identifier.
	 * @throws RemoteException
	 *             Throws a RemoteException if an error occurs in the registering process.
	 * @throws LicenseException
	 *             Throws a {@link LicenseException} to indicate some problems with the licensing
	 *             process.
	 */
	long registerPlatformIdent(List definedIPs, String agentName, String version) throws RemoteException;

	/**
	 * Every instrumented method has to be registered from every Agent. This method returns a unique
	 * value for this method so that measurements acquired from these methods can be linked in the
	 * database.
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
	 */
	long registerMethodIdent(long platformIdent, String packageName, String className, String methodName, List parameterTypes, String returnType, int modifiers);

	/**
	 * Every sensor type which is called by an instrumented method to gather data has to be
	 * registered by calling this method.
	 * 
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param fullyQualifiedClassName
	 *            The fully qualified class name of the sensor type.
	 * @return Returns the unique method sensor type identifier.
	 * @throws RemoteException
	 *             Throws a RemoteException if an error occurs in the registering process.
	 */
	long registerMethodSensorTypeIdent(long platformIdent, String fullyQualifiedClassName) throws RemoteException;

	/**
	 * This method is used to map a registered method sensor type to a registered method.
	 * 
	 * @param methodSensorTypeIdent
	 *            The unique identifier of the sensor type.
	 * @param methodIdent
	 *            The unique identifier of the method.
	 * @throws RemoteException
	 *             Throws a RemoteException if an error occurs in the registering process.
	 */
	void addSensorTypeToMethod(long methodSensorTypeIdent, long methodIdent) throws RemoteException;

	/**
	 * Every sensor type which gathers information about the target platform/system has to be
	 * registered by calling this method.
	 * 
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param fullyQualifiedClassName
	 *            The fully qualified class name of the sensor type.
	 * @return Returns the unique platform sensor type identifier.
	 */
	long registerPlatformSensorTypeIdent(long platformIdent, String fullyQualifiedClassName);

}
