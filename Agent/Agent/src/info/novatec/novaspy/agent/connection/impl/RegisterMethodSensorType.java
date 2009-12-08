package info.novatec.novaspy.agent.connection.impl;

import info.novatec.novaspy.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.novaspy.agent.connection.AbstractRemoteMethodCall;
import info.novatec.novaspy.agent.connection.ServerUnavailableException;
import info.novatec.novaspy.cmr.service.IRegistrationService;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Class which encapsulates the request to the {@link Remote} object
 * {@link IRegistrationService}. The method to call is
 * {@link IRegistrationService#registerMethodSensorTypeIdent(long, String)}
 * 
 * @author Patrice Bouillet
 * 
 */
public class RegisterMethodSensorType extends AbstractRemoteMethodCall {

	/**
	 * The registration object which is used for the actual registering.
	 */
	private final Remote registrationService;

	/**
	 * The method sensor type configuration which is registered at the server.
	 */
	private final MethodSensorTypeConfig methodSensorTypeConfig;

	/**
	 * The ID of the current platform used for the registering process.
	 */
	private final long platformId;

	/**
	 * The only constructor for this class accepts two attributes.
	 * 
	 * @param registrationService
	 *            The {@link Remote} object.
	 * @param methodSensorTypeConfig
	 *            The {@link MethodSensorTypeConfig} which is registered at the
	 *            server.
	 * @param platformId
	 *            The ID of the platform.
	 */
	public RegisterMethodSensorType(IRegistrationService registrationService, MethodSensorTypeConfig methodSensorTypeConfig, long platformId) {
		this.registrationService = registrationService;
		this.methodSensorTypeConfig = methodSensorTypeConfig;
		this.platformId = platformId;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Remote getRemoteObject() throws ServerUnavailableException {
		return registrationService;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Object performRemoteCall(Remote remoteObject) throws RemoteException {
		IRegistrationService reg = (IRegistrationService) remoteObject;

		return new Long(reg.registerMethodSensorTypeIdent(platformId, methodSensorTypeConfig.getClassName()));
	}
}