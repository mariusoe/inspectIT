package info.novatec.inspectit.rcp.repository.service;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.InspectIT;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

/**
 * @author Patrice Bouillet
 * 
 */
public class GlobalDataAccessService implements IGlobalDataAccessService {

	/**
	 * The Name of the global data access service.
	 */
	private static final String GLOBAL_DATA_ACCESS_SERVICE = "GlobalDataAccessService";

	/**
	 * The proxy factory bean by Spring which initializes the data access
	 * service.
	 */
	private final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean;

	/**
	 * The global data access service exposed by the CMR and initialized by
	 * Spring.
	 */
	private final IGlobalDataAccessService globalDataAccessService;

	/**
	 * This map is needed to store the mapping between the ID's and the
	 * {@link PlatformIdent} objects. Some views / editors need this information
	 * because they can only access the ID.
	 */
	private Map<Long, PlatformIdent> platformMap = new ConcurrentHashMap<Long, PlatformIdent>();

	/**
	 * This map is needed to store the mapping between the ID's and the
	 * {@link SensorTypeIdent} objects. Some views / editors need this
	 * information because they can only access the ID.
	 */
	private Map<Long, SensorTypeIdent> sensorTypeMap = new ConcurrentHashMap<Long, SensorTypeIdent>();

	/**
	 * This map is needed to store the mapping between the ID's and the
	 * {@link MethodIdent} objects. Some views / editors need this information
	 * because they can only access the ID.
	 */
	private Map<Long, MethodIdent> methodMap = new ConcurrentHashMap<Long, MethodIdent>();

	/**
	 * Pool size.
	 */
	private static final int POOL_SIZE = 2;

	/**
	 * Max Pool size.
	 */
	private static final int MAX_POOL_SIZE = 2;

	/**
	 * Keep alive time.
	 */
	private static final long KEEP_ALIVE_TIME = 10;

	/**
	 * The thread pool.
	 */
	private static ThreadPoolExecutor threadPool = null;

	/**
	 * The queue.
	 */
	private static final ArrayBlockingQueue<Runnable> QUEUE = new ArrayBlockingQueue<Runnable>(5);

	/**
	 * Default constructor needs the ip and the port of the service.
	 * 
	 * @param ip
	 *            The ip.
	 * @param port
	 *            The port.
	 */
	public GlobalDataAccessService(String ip, int port) {
		httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(IGlobalDataAccessService.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://" + ip + ":" + port + "/remoting/" + GLOBAL_DATA_ACCESS_SERVICE);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		globalDataAccessService = (IGlobalDataAccessService) httpInvokerProxyFactoryBean.getObject();

		threadPool = new ThreadPoolExecutor(POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, QUEUE);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<PlatformIdent> getConnectedAgents() {
		try {
			final List<PlatformIdent> platformIdents = globalDataAccessService.getConnectedAgents();

			// TODO use the thread pool for the next call
			platformMap.clear();
			methodMap.clear();
			sensorTypeMap.clear();

			for (PlatformIdent platformIdent : platformIdents) {
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

			return platformIdents;
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the agent details from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<? extends DefaultData> getLastDataObjects(DefaultData template, long timeInterval) {
		try {
			return globalDataAccessService.getLastDataObjects(template, timeInterval);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the last data objects (time) from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<? extends DefaultData> getDataObjectsFromToDate(DefaultData template, Date fromDate, Date toDate) {
		try {
			return globalDataAccessService.getDataObjectsFromToDate(template, fromDate, toDate);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving data objects from->to from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public DefaultData getLastDataObject(DefaultData template) {
		try {
			return globalDataAccessService.getLastDataObject(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the last data object from the CMR!", e, -1);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<? extends DefaultData> getDataObjectsSinceId(DefaultData template) {
		try {
			return globalDataAccessService.getDataObjectsSinceId(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the data objects since id from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<? extends DefaultData> getDataObjectsSinceIdIgnoreMethodId(DefaultData template) {
		try {
			return globalDataAccessService.getDataObjectsSinceIdIgnoreMethodId(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the data objects since id (ignore method id) from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * Returns the mapped {@link PlatformIdent} object for the passed platform
	 * id.
	 * 
	 * @param platformId
	 *            The long value.
	 * @return The {@link PlatformIdent} object.
	 */
	public PlatformIdent getPlatformIdentForId(long platformId) {
		Long id = Long.valueOf(platformId);
		if (!platformMap.containsKey(id)) {
			refreshIdents();
		}

		return platformMap.get(id);
	}

	/**
	 * Returns the mapped {@link SensorTypeIdent} object for the passed sensor
	 * type id.
	 * 
	 * @param sensorTypeId
	 *            The long value.
	 * @return The {@link SensorTypeIdent} object.
	 */
	public SensorTypeIdent getSensorTypeIdentForId(long sensorTypeId) {
		Long id = Long.valueOf(sensorTypeId);
		if (!sensorTypeMap.containsKey(id)) {
			refreshIdents();
		}

		return sensorTypeMap.get(Long.valueOf(id));
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
		if (!methodMap.containsKey(id)) {
			refreshIdents();
		}

		return methodMap.get(Long.valueOf(methodId));
	}

	/**
	 * Internal refresh of the idents. Currently everything is loaded again.
	 */
	private void refreshIdents() {
		getConnectedAgents();
	}
}
