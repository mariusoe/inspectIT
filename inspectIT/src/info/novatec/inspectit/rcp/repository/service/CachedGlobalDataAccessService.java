package info.novatec.inspectit.rcp.repository.service;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;

/**
 * Retrieves cached information about the different ident objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface CachedGlobalDataAccessService extends IGlobalDataAccessService {

	/**
	 * Returns a cached method ident object.
	 * 
	 * @param methodId
	 *            The method ID
	 * @return a cached method ident object.
	 */
	MethodIdent getMethodIdentForId(long methodId);

	/**
	 * Returns a cached sensor type ident object.
	 * 
	 * @param sensorTypeId
	 *            The sensor type ID
	 * @return a cached sensor type ident object.
	 */
	SensorTypeIdent getSensorTypeIdentForId(long sensorTypeId);

	/**
	 * Returns a cached platform ident object.
	 * 
	 * @param platformId
	 *            The platform ID
	 * @return a cached platform ident object.
	 */
	PlatformIdent getPlatformIdentForId(long platformId);

}
