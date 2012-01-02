package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.DefaultData;

import java.util.Date;
import java.util.List;

/**
 * Interface used to define all methods which clients (be it graphical/textual) can access the
 * stored information on the CMR.
 * 
 * @author Patrice Bouillet
 * 
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IGlobalDataAccessService {

	/**
	 * Returns all the connected Agents of this CMR. The List contains {@link PlatformIdent}
	 * objects.
	 * 
	 * @return All connected Agents.
	 */
	List<? extends PlatformIdent> getConnectedAgents();

	/**
	 * Returns the last saved data objects (with the given time interval). Returns a list of
	 * {@link DefaultData} objects.
	 * 
	 * @param template
	 *            The template object used to identify which data should be loaded.
	 * @param timeInterval
	 *            The time interval.
	 * @return The last data objects.
	 */
	List<? extends DefaultData> getLastDataObjects(DefaultData template, long timeInterval);

	/**
	 * Returns the last saved data object.
	 * 
	 * @param template
	 *            The template object used to identify which data should be loaded.
	 * @return The last data object.
	 */
	DefaultData getLastDataObject(DefaultData template);

	/**
	 * Returns all last saved data objects since the {@link DefaultData#getId()} stored in the
	 * template object.
	 * 
	 * @param template
	 *            The template data object. The ID needs to be set.
	 * @return List of data objects.
	 */
	List<? extends DefaultData> getDataObjectsSinceId(DefaultData template);

	/**
	 * Returns all last saved data objects since the {@link DefaultData#getId()} stored in the
	 * template object. The only difference to the {@link #getDataObjectsSinceId(DefaultData)} is
	 * the fact that the method id is always ignored. Useful for sensor types like the SQL one where
	 * you don't care about the method being sensored.
	 * 
	 * @param template
	 *            The template data object. The ID needs to be set.
	 * @return List of data objects.
	 */
	List<? extends DefaultData> getDataObjectsSinceIdIgnoreMethodId(DefaultData template);

	/**
	 * All data objects from the passed template in the given time range.
	 * 
	 * @param template
	 *            The template data object.
	 * @param fromDate
	 *            The start date.
	 * @param toDate
	 *            The end date.
	 * @return List of data objects.
	 */
	List<? extends DefaultData> getDataObjectsFromToDate(DefaultData template, Date fromDate, Date toDate);

}
