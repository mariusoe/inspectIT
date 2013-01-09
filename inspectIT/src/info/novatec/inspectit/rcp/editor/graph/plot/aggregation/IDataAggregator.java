package info.novatec.inspectit.rcp.editor.graph.plot.aggregation;

import info.novatec.inspectit.communication.DefaultData;

import java.util.List;

/**
 * The interface for all data aggregator classes.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface IDataAggregator {

	/**
	 * Aggregates the data from fromIndex to toIndex and returns a new {@link DefaultData} with
	 * aggregated values.
	 * 
	 * @param defaultData
	 *            The {@link List} of {@link DefaultData} to aggregate.
	 * @param fromIndex
	 *            Aggregation begins at this position.
	 * @param toIndex
	 *            Aggregation ends at this position.
	 * @return A new aggregated {@link DefaultData}.
	 */
	DefaultData aggregateData(List<? extends DefaultData> defaultData, int fromIndex, int toIndex);
}
