package info.novatec.inspectit.cmr.service;

/**
 * This interface is used to retrieve the status of the CMR.
 * 
 * @author Patrice Bouillet
 * 
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IServerStatusService {

	/**
	 * String returned for version not available.
	 */
	String VERSION_NOT_AVAILABLE = "n/a";

	int SERVER_OFFLINE = 0;

	int SERVER_ONLINE = 1;

	int SERVER_STARTING = 2;

	int SERVER_STOPPING = 4;

	/**
	 * Returns the current server status.
	 * 
	 * @return The server status.
	 */
	int getServerStatus();

	/**
	 * Returns the current version of the server.
	 * 
	 * @return the current version of the server.
	 */
	String getVersion();

}
