package info.novatec.inspectit.indexing.aggregation;

/**
 * Interface that defines the operations needed to do a aggregation on the objects. This interface
 * can be used with queries to provide simpler aggregation possibilities.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of data that is aggregated.
 */
public interface IAggregator<E> {

	/**
	 * Performs the aggregation. The aggregation should be done in the aggregatedObject, and
	 * objectToAdd should not be changed.
	 * 
	 * @param aggregatedObject
	 *            Object to hold aggregated values.
	 * @param objectToAdd
	 *            Object which values are added to the other object.
	 */
	void aggregate(E aggregatedObject, E objectToAdd);

	/**
	 * Provides cloned object if the {@link #isCloning()} returns true.
	 * 
	 * @param object
	 *            Object to be cloned.
	 * @return Provides cloned object if the {@link #isCloning()} returns true.
	 */
	E getClone(E object);

	/**
	 * Define if objects should be cloned before aggregation.
	 * 
	 * @return Define if objects should be cloned before aggregation.
	 */
	boolean isCloning();

	/**
	 * Returns aggregation key.
	 * 
	 * @param object
	 *            Object to get key for.
	 * @return Aggregation key.
	 */
	Object getAggregationKey(E object);

}