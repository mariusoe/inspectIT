package info.novatec.inspectit.rcp.repository;

import info.novatec.inspectit.cmr.service.IBufferService;
import info.novatec.inspectit.cmr.service.ICombinedMetricsDataAccessService;
import info.novatec.inspectit.cmr.service.IConfigurationInterfaceDataAccessService;
import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.cmr.service.IHttpTimerDataAccessService;
import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.cmr.service.ILicenseService;
import info.novatec.inspectit.cmr.service.IServerStatusService;
import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.cmr.service.ITimerDataAccessService;
import info.novatec.inspectit.rcp.repository.service.cache.CachedDataService;
import info.novatec.inspectit.rcp.repository.service.storage.StorageGlobalDataAccessService;
import info.novatec.inspectit.rcp.repository.service.storage.StorageInvocationDataAccessService;
import info.novatec.inspectit.rcp.repository.service.storage.StorageNamingConstants;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * ...
 */
public class StorageRepositoryDefinition implements RepositoryDefinition {

	/**
	 * The path of this storage repository definition.
	 */
	private String path;

	/**
	 * The xstream object to load the saved data.
	 */
	private XStream xstream = new XStream(new JettisonMappedXmlDriver());

	/**
	 * {@link IInvocationDataAccessService} service.
	 */
	private IInvocationDataAccessService invocationDataAccessService;

	/**
	 * {@link IGlobalDataAccessService} service.
	 */
	private IGlobalDataAccessService globalDataAccessService;

	/**
	 * Caching component.
	 */
	private CachedDataService cachedDataService;

	/**
	 * The default constructor will use the default storage directory.
	 */
	public StorageRepositoryDefinition() {
		this(StorageNamingConstants.DEFAULT_STORAGE_DIRECTORY);
		invocationDataAccessService = new StorageInvocationDataAccessService(this);
		globalDataAccessService = new StorageGlobalDataAccessService(this);
		cachedDataService = new CachedDataService(globalDataAccessService);
	}

	/**
	 * The storage directory can be specified here.
	 * 
	 * @param path
	 *            the path to the storage area.
	 */
	public StorageRepositoryDefinition(String path) {
		this.path = path;
	}

	@Override
	public CachedDataService getCachedDataService() {
		return cachedDataService;
	}

	@Override
	public IInvocationDataAccessService getInvocationDataAccessService() {
		return invocationDataAccessService;
	}
	
	@Override
	public IGlobalDataAccessService getGlobalDataAccessService() {
		return globalDataAccessService;
	}

	public String getPath() {
		return path;
	}

	public XStream getXstream() {
		return xstream;
	}

	/**
	 * Not supported.
	 */
	@Override
	public String getIp() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public int getPort() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public ILicenseService getLicenseService() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public IServerStatusService getServerStatusService() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public ICombinedMetricsDataAccessService getCombinedMetricsDataAccessService() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public ISqlDataAccessService getSqlDataAccessService() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public IExceptionDataAccessService getExceptionDataAccessService() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public IConfigurationInterfaceDataAccessService getConfigurationInterfaceDataAccessService() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public IBufferService getBufferService() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public ITimerDataAccessService getTimerDataAccessService() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public IHttpTimerDataAccessService getHttpTimerDataAccessService() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public String toString() {
		return "StorageRepositoryDefinition :: " + path;
	}

}
