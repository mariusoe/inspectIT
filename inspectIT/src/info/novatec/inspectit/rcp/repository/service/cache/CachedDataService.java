package info.novatec.inspectit.rcp.repository.service.cache;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.cmr.service.exception.ServiceException;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.rcp.InspectIT;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.Assert;

/**
 * The default implementation of the cached ident objects. Provides a protected-visible method to
 * analyze and put a list of platform ident objects into the cache.
 * 
 * @author Patrice Bouillet
 * 
 */
public class CachedDataService {

	/**
	 * Delegated service.
	 */
	private IGlobalDataAccessService globalDataAccessService;

	/**
	 * @param globalDataAccessService
	 *            Delegated service.
	 */
	public CachedDataService(IGlobalDataAccessService globalDataAccessService) {
		super();
		Assert.isNotNull(globalDataAccessService);
		this.globalDataAccessService = globalDataAccessService;
	}

	/**
	 * This map is needed to store the mapping between the ID's and the {@link PlatformIdent}
	 * objects. Some views / editors need this information because they can only access the ID.
	 */
	private Map<Long, PlatformIdent> platformMap = new ConcurrentHashMap<Long, PlatformIdent>();

	/**
	 * This map is needed to store the mapping between the ID's and the {@link SensorTypeIdent}
	 * objects. Some views / editors need this information because they can only access the ID.
	 */
	private Map<Long, SensorTypeIdent> sensorTypeMap = new ConcurrentHashMap<Long, SensorTypeIdent>();

	/**
	 * This map is needed to store the mapping between the ID's and the {@link MethodIdent} objects.
	 * Some views / editors need this information because they can only access the ID.
	 */
	private Map<Long, MethodIdent> methodMap = new ConcurrentHashMap<Long, MethodIdent>();

	/**
	 * Updates the data in the cache for the one agent. This method should be called with care,
	 * since it removes and inserts all the sensor data.
	 * 
	 * @param platformIdent
	 *            Agento to refresh.
	 */
	public void refreshData(PlatformIdent platformIdent) {
		platformMap.remove(platformIdent.getId());
		platformMap.put(platformIdent.getId(), platformIdent);

		for (MethodIdent methodIdent : (Set<MethodIdent>) platformIdent.getMethodIdents()) {
			methodMap.remove(methodIdent.getId());
			methodMap.put(methodIdent.getId(), methodIdent);
		}

		for (SensorTypeIdent sensorTypeIdent : (Set<SensorTypeIdent>) platformIdent.getSensorTypeIdents()) {
			sensorTypeMap.remove(sensorTypeIdent.getId());
			sensorTypeMap.put(sensorTypeIdent.getId(), sensorTypeIdent);
		}
	}

	/**
	 * Returns the mapped {@link PlatformIdent} object for the passed platform id.
	 * 
	 * @param platformId
	 *            The long value.
	 * @return The {@link PlatformIdent} object.
	 */
	public PlatformIdent getPlatformIdentForId(long platformId) {
		Long id = Long.valueOf(platformId);
		// load only if the id is not 0
		if (0 != id.longValue() && !platformMap.containsKey(id)) {
			refreshIdents();
		}

		return platformMap.get(id);
	}

	/**
	 * Returns the mapped {@link SensorTypeIdent} object for the passed sensor type id.
	 * 
	 * @param sensorTypeId
	 *            The long value.
	 * @return The {@link SensorTypeIdent} object.
	 */
	public SensorTypeIdent getSensorTypeIdentForId(long sensorTypeId) {
		Long id = Long.valueOf(sensorTypeId);
		// load only if the id is not 0
		if (0 != id.longValue() && !sensorTypeMap.containsKey(id)) {
			refreshIdents();
		}

		return sensorTypeMap.get(id);
	}

	/**
	 * Returns the mapped {@link MethodIdent} object for the passed method id.
	 * 
	 * @param methodId
	 *            The long value.
	 * @return The {@link MethodIdent} object.
	 */
	public MethodIdent getMethodIdentForId(long methodId) {
		Long id = Long.valueOf(methodId);
		// load only if the id is not 0
		if (0 != id.longValue() && !methodMap.containsKey(id)) {
			refreshIdents();
		}

		return methodMap.get(id);
	}

	/**
	 * Internal refresh of the idents. Currently everything is loaded again.
	 */
	private void refreshIdents() {
		Map<PlatformIdent, AgentStatusData> agentMap = globalDataAccessService.getAgentsOverview();
		platformMap.clear();
		methodMap.clear();
		sensorTypeMap.clear();

		for (PlatformIdent overview : agentMap.keySet()) {
			PlatformIdent platformIdent;
			try {
				platformIdent = globalDataAccessService.getCompleteAgent(overview.getId());
			} catch (ServiceException e) {
				InspectIT.getDefault().createErrorDialog("Exception occured trying to refresh sensor information for the agent " + overview.getAgentName() + ".", e, -1);
				continue;
			}
			platformMap.put(platformIdent.getId(), platformIdent);

			for (MethodIdent methodIdent : (Set<MethodIdent>) platformIdent.getMethodIdents()) {
				if (!methodMap.containsKey(methodIdent.getId())) {
					methodMap.put(methodIdent.getId(), methodIdent);
				}
			}

			for (SensorTypeIdent sensorTypeIdent : (Set<SensorTypeIdent>) platformIdent.getSensorTypeIdents()) {
				if (!sensorTypeMap.containsKey(sensorTypeIdent.getId())) {
					sensorTypeMap.put(sensorTypeIdent.getId(), sensorTypeIdent);
				}
			}
		}
	}

}
