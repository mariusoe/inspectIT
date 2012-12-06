package info.novatec.inspectit.indexing.aggregation.impl;

import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.AggregatedHttpTimerData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.io.Serializable;

/**
 * Aggregation for {@link HttpTimerData}.
 * 
 * @author Ivan Senic
 * 
 */
public class HttpTimerDataAggregator implements IAggregator<HttpTimerData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 495449254866425040L;

	/**
	 * Is UIR based.
	 */
	private boolean uriBased;

	/**
	 * Should request method be included in aggregation.
	 */
	private boolean includeRequestMethod;

	/**
	 * No-arg constructor.
	 */
	public HttpTimerDataAggregator() {
	}

	/**
	 * Default constructor that defines aggregation parameters.
	 * 
	 * @param uriBased
	 *            Is aggregation URi based.
	 * @param includeRequestMethod
	 *            Should request method be included in aggregation.
	 */
	public HttpTimerDataAggregator(boolean uriBased, boolean includeRequestMethod) {
		this.uriBased = uriBased;
		this.includeRequestMethod = includeRequestMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(IAggregatedData<HttpTimerData> aggregatedObject, HttpTimerData objectToAdd) {
		if (!uriBased) {
			if (!objectToAdd.hasInspectItTaggingHeader()) {
				// use case aggregation for elements that do not have any tagged value does not
				// make sense, thus we ignore these.
				return;
			}
		}

		aggregatedObject.aggregate(objectToAdd);

		if (!includeRequestMethod) {
			// If we have different request methods, we set the request method to "multiple"
			if (!objectToAdd.getRequestMethod().equals(aggregatedObject.getData().getRequestMethod()) && !aggregatedObject.getData().getRequestMethod().equals(HttpTimerData.REQUEST_METHOD_MULTIPLE)) {
				aggregatedObject.getData().setRequestMethod(HttpTimerData.REQUEST_METHOD_MULTIPLE);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IAggregatedData<HttpTimerData> getClone(HttpTimerData httpData) {
		AggregatedHttpTimerData clone = new AggregatedHttpTimerData();
		clone.setPlatformIdent(httpData.getPlatformIdent());
		clone.setSensorTypeIdent(httpData.getSensorTypeIdent());
		clone.setMethodIdent(httpData.getMethodIdent());
		if (uriBased) {
			clone.setUri(httpData.getUri());
		} else {
			// Aggregation based on Usecase. We reset the URI so that we can easily know
			// that use case aggregation is used.
			clone.setUri(HttpTimerData.UNDEFINED);
			clone.setInspectITTaggingHeaderValue(httpData.getInspectItTaggingHeaderValue());
		}
		clone.setRequestMethod(httpData.getRequestMethod());
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAggregationKey(HttpTimerData httpData) {
		final int prime = 31;
		int result = 0;
		if (uriBased) {
			result = prime * result + ((httpData.getUri() == null) ? 0 : httpData.getUri().hashCode());
		} else {
			result = prime * result + ((httpData.getInspectItTaggingHeaderValue() == null) ? 0 : httpData.getInspectItTaggingHeaderValue().hashCode());
		}

		if (includeRequestMethod) {
			result = prime * result + ((httpData.getRequestMethod() == null) ? 0 : httpData.getRequestMethod().hashCode());
		}
		return result;
	}

}
