package info.novatec.inspectit.indexing.aggregation.impl;

import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class encapsulates the aggregation process. When ever aggregation is needed, it this class
 * should be used with combination of available {@link IAggregator}s.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type to be aggregated.
 */
public class AggregationPerformer<E> {

	/**
	 * Map for caching.
	 */
	private Map<Object, E> aggregationMap;

	/**
	 * {@link IAggregator} used.
	 */
	private IAggregator<E> aggregator;

	/**
	 * Default constructor.
	 * 
	 * @param aggregator
	 *            {@link IAggregator} to use. Must not be <code>null</code>.
	 */
	public AggregationPerformer(IAggregator<E> aggregator) {
		if (null == aggregator) {
			throw new IllegalArgumentException("Aggregator can not be null.");
		}
		this.aggregator = aggregator;
		this.aggregationMap = new HashMap<Object, E>();
	}

	/**
	 * Process one element.
	 * 
	 * @param element
	 *            Element to process.
	 */
	public void processElement(E element) {
		Object key = aggregator.getAggregationKey(element);
		E aggregatedObject = aggregationMap.get(key);
		if (null != aggregatedObject) {
			aggregator.aggregate(aggregatedObject, element);
		} else {
			if (aggregator.isCloning()) {
				aggregatedObject = aggregator.getClone(element);
				aggregationMap.put(key, aggregatedObject);
				aggregator.aggregate(aggregatedObject, element);
			} else {
				aggregationMap.put(key, element);
			}
		}
	}

	/**
	 * Process the collection of elements.
	 * 
	 * @param collection
	 *            Collection that should be aggregated.
	 */
	public void processCollection(Collection<E> collection) {
		for (E element : collection) {
			processElement(element);
		}
	}

	/**
	 * Returns aggregation results.
	 * 
	 * @return Returns aggregation results.
	 */
	public List<E> getResultList() {
		return new ArrayList<E>(aggregationMap.values());
	}

}
