package info.novatec.novaspy.cmr.dao;

import info.novatec.novaspy.communication.DefaultData;

import java.util.Date;
import java.util.List;

/**
 * All implementing classes of this interface are storing and retrieving the
 * default data objects, for example in the database.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface DefaultDataDao {

	/**
	 * Persist the {@link DefaultData} object.
	 * 
	 * @param defaultData
	 *            The object to persist.
	 */
	void save(DefaultData defaultData);

	/**
	 * Persists or updates all items in the collection.
	 * 
	 * @param defaultDataCollection
	 *            The collection with {@link DefaultData} objects to persist or
	 *            update.
	 */
	void saveAll(List<DefaultData> defaultDataCollection);

	/**
	 * Returns a list of stored {@link DefaultData} objects in the given
	 * interval, starting minus the passed timeInterval parameter to the current
	 * time.
	 * 
	 * @param template
	 *            The template object to look for.
	 * @param timeInterval
	 *            The time interval to look for the objects. Ranging minus the
	 *            passed time interval parameter up until now.
	 * @return Returns a list of data objects which fulfill the criteria.
	 */
	List<DefaultData> findByExampleWithLastInterval(DefaultData template, long timeInterval);

	/**
	 * Search for data objects which have an ID greater than in the passed
	 * template object.
	 * 
	 * @param template
	 *            The template object to look for with the ID used as the
	 *            marker.
	 * @return Returns a list of data objects which fulfill the criteria.
	 */
	List<DefaultData> findByExampleSinceId(DefaultData template);

	/**
	 * Search for data objects which have an ID greater than in the passed
	 * template object. The Method Ident is always ignored.
	 * 
	 * @param template
	 *            The template object to look for with the ID used as the
	 *            marker.
	 * @return Returns a list of data objects which fulfill the criteria.
	 */
	List<DefaultData> findByExampleSinceIdIgnoreMethodId(DefaultData template);

	/**
	 * Search for data objects which are between the from and to {@link Date}
	 * object.
	 * 
	 * @param template
	 *            The template object to look for.
	 * @param fromDate
	 *            The start date.
	 * @param toDate
	 *            The end date.
	 * @return Returns a list of data objects which fulfill the criteria.
	 */
	List<DefaultData> findByExampleFromToDate(DefaultData template, Date fromDate, Date toDate);

	/**
	 * Searches for the last saved data object.
	 * 
	 * @param template
	 *            The template object to look for.
	 * @return Returns the last saved data object.
	 */
	DefaultData findByExampleLastData(DefaultData template);

}
