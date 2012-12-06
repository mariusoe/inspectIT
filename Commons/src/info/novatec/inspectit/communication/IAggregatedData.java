package info.novatec.inspectit.communication;

import java.util.Collection;

/**
 * Interface that marks that the data is aggregated.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of data.
 */
public interface IAggregatedData<E extends DefaultData> {

	/**
	 * Aggregates one sample of data.
	 * 
	 * @param data
	 *            To be aggregated.
	 */
	void aggregate(E data);

	/**
	 * Returns the collection that contains the IDs of the originally aggregated data.
	 * 
	 * @return Returns the collection that contains the IDs of the originally aggregated data.
	 */
	Collection<Long> getAggregatedIds();

	/**
	 * Returns the aggregated data.
	 * 
	 * @return Returns the aggregated data.
	 */
	E getData();
}
