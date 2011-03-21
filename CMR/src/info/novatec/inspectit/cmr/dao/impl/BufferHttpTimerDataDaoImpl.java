package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.HttpTimerDataDao;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;
import info.novatec.inspectit.indexing.query.factory.impl.HttpTimerDataQueryFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Provides <code>HttpTimerData</code> information from the CMR internal in memory buffer.
 *
 * @author Stefan Siegl
 */
@Repository
public class BufferHttpTimerDataDaoImpl implements HttpTimerDataDao {

	/**
	 * Indexing tree to search for data.
	 */
	@Autowired
	private IBufferTreeComponent<HttpTimerData> indexingTree;

	/**
	 * Index query factory.
	 */
	@Autowired
	private HttpTimerDataQueryFactory<IIndexQuery> httpDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HttpTimerData> getAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		return aggregate(findAllHttpTimers(httpData, null, null), true, includeRequestMethod);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HttpTimerData> getAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		return aggregate(findAllHttpTimers(httpData, fromDate, toDate), true, includeRequestMethod);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HttpTimerData> getTaggedAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		return aggregate(findAllHttpTimers(httpData, null, null), false, includeRequestMethod);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HttpTimerData> getTaggedAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		return aggregate(findAllHttpTimers(httpData, fromDate, toDate), false, includeRequestMethod);
	}

	/**
	 * Return all <code>HttpTimerData</code> objects in the buffer. Currently this is the best
	 * approach as the querying does not feature a better way of specifying elements.
	 *
	 * @param httpData
	 *            <code>HttpTimerData</code> object used to retrieve the platformId
	 * @param fromDate
	 *            the fromDate or <code>null</code> if not applicable
	 * @param toDate
	 *            the toDate or <code>null</code> if not applicable
	 * @return all <code>HttpTimerData</code> objects in the buffer.
	 */
	private List<HttpTimerData> findAllHttpTimers(HttpTimerData httpData, Date fromDate, Date toDate) {
		IIndexQuery query = httpDataQueryFactory.getFindAllHttpTimersQuery(httpData, fromDate, toDate);
		return indexingTree.query(query);
	}

	/**
	 * Performs an aggregation of the given <code>HttpTimerData</code> objects.
	 * <ul>
	 * <li>
	 * For an uri-based aggregation (uriBased = true) the URI stored in the
	 * <code>HttpTimerData</code> objects is used.</li>
	 * <li>
	 * For an usecase-based aggregation (uriBased = false) the value of tagged value header stored
	 * in the <code>HttpTimerData</code> objects is used. If an object does not provide this header,
	 * it is ignored.</li>
	 * <li>
	 * including the request method allows to broaden the categorization criterion from the
	 * uri/usecase to also include the request method (includeRequestMethod = true).</li>
	 * </ul>
	 *
	 * @param input
	 *            the data to aggregate
	 * @param uriBased
	 *            see above
	 * @param includeRequestMethod
	 *            see above
	 * @return an aggregation of the given <code>HttpTimerData</code> objects
	 */
	protected List<HttpTimerData> aggregate(List<HttpTimerData> input, boolean uriBased, boolean includeRequestMethod) {
		Map<AggregationKey, HttpTimerData> aggregateMap = new HashMap<AggregationKey, HttpTimerData>();

		for (HttpTimerData data : input) {
			if (!uriBased) {
				if (!data.hasInspectItTaggingHeader()) {
					// use case aggregation for elements that do not have any tagged value does not
					// make sense, thus we ignore these.
					continue;
				}
			}

			AggregationKey key = createAggregationKey(data, uriBased, includeRequestMethod);

			// Check if we already have an aggregation object in internal aggregation map
			HttpTimerData aggregationObject = aggregateMap.get(key);
			if (null == aggregationObject) {
				// We do not have an aggregation object, so we will use the current http timer
				// data as base for our aggregation object. We need to ensure
				// that we do not aggregate on the same object as this one is still
				// potentially used in invocation sequences.
				aggregationObject = cloneHttpTimerData(data, uriBased);
				aggregationObject.aggregateTimerData(data);
				aggregateMap.put(key, aggregationObject);
			} else {
				aggregationObject.aggregateTimerData(data);
			}

			if (!includeRequestMethod) {
				// If we have different request methods, we set the request method to "multiple"
				if (!data.getRequestMethod().equals(aggregationObject.getRequestMethod()) && !aggregationObject.getRequestMethod().equals(HttpTimerData.REQUEST_METHOD_MULTIPLE)) {
					aggregationObject.setRequestMethod(HttpTimerData.REQUEST_METHOD_MULTIPLE);
				}
			}
		}

		return new ArrayList<HttpTimerData>(aggregateMap.values());
	}

	/**
	 * Builds an aggregation key for the given information.
	 *
	 * @param data
	 *            the timer
	 * @param uriBased
	 *            uriBased or usecase aggregation
	 * @param includeRequestMethod
	 *            if the request method should be used in the categorization
	 * @return an aggregation key for the given information.
	 */
	private AggregationKey createAggregationKey(HttpTimerData data, boolean uriBased, boolean includeRequestMethod) {
		AggregationKey key = new AggregationKey();
		if (uriBased) {
			key.uri = data.getUri();
		} else {
			key.useCase = data.getInspectItTaggingHeaderValue();
		}

		if (includeRequestMethod) {
			key.requestMethod = data.getRequestMethod();
		}

		return key;
	}

	/**
	 * Returns cloned timer data, with copied platform ident, sensor type ident and method ident
	 * from a given timer data. It will <b> not </b> include the timer data!
	 *
	 * @param data
	 *            timer data to copy values to
	 * @param uriBased
	 *            clone for uri based categorization?
	 * @return cloned object
	 */
	private HttpTimerData cloneHttpTimerData(HttpTimerData data, boolean uriBased) {
		HttpTimerData clone = new HttpTimerData();
		clone.setPlatformIdent(data.getPlatformIdent());
		clone.setSensorTypeIdent(data.getSensorTypeIdent());
		clone.setMethodIdent(data.getMethodIdent());
		if (uriBased) {
			clone.setUri(data.getUri());
		} else {
			// Aggregation based on Usecase. We reset the URI so that we can easily know
			// that use case aggregation is used.
			clone.setUri(HttpTimerData.UNDEFINED);
			clone.setInspectITTaggingHeaderValue(data.getInspectItTaggingHeaderValue());
		}
		clone.setRequestMethod(data.getRequestMethod());
		return clone;
	}

	/**
	 * Aggregation Key used for the aggregation of HttpTimerData objects.
	 *
	 * @author Stefan Siegl
	 */
	private static class AggregationKey {
		/** The uri. */
		public String uri = null;
		/** The usecase. */
		public String useCase = null;
		/** The request method. */
		public String requestMethod = null;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((requestMethod == null) ? 0 : requestMethod.hashCode());
			result = prime * result + ((uri == null) ? 0 : uri.hashCode());
			result = prime * result + ((useCase == null) ? 0 : useCase.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AggregationKey other = (AggregationKey) obj;
			if (requestMethod == null) {
				if (other.requestMethod != null)
					return false;
			} else if (!requestMethod.equals(other.requestMethod))
				return false;
			if (uri == null) {
				if (other.uri != null)
					return false;
			} else if (!uri.equals(other.uri))
				return false;
			if (useCase == null) {
				if (other.useCase != null)
					return false;
			} else if (!useCase.equals(other.useCase))
				return false;
			return true;
		}
	}

}
