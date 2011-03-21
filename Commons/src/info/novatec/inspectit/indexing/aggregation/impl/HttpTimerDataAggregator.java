package info.novatec.inspectit.indexing.aggregation.impl;

import java.io.Serializable;

import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

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
	 * Is cloning active.
	 */
	private boolean cloning;

	/**
	 * Is UIR based.
	 */
	private boolean uriBased;

	/**
	 * Should request method be included in aggregation.
	 */
	private boolean includeRequestMethod;

	/**
	 * Default constructor that defines aggregation parameters.
	 *
	 * @param cloning
	 *            Should cloning be active.
	 * @param uriBased
	 *            Is aggregation URi based.
	 * @param includeRequestMethod
	 *            Should request method be included in aggregation.
	 */
	public HttpTimerDataAggregator(boolean cloning, boolean uriBased, boolean includeRequestMethod) {
		this.cloning = cloning;
		this.uriBased = uriBased;
		this.includeRequestMethod = includeRequestMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(HttpTimerData aggregatedObject, HttpTimerData objectToAdd) {
		if (!uriBased) {
			if (!objectToAdd.hasInspectItTaggingHeader()) {
				// use case aggregation for elements that do not have any tagged value does not
				// make sense, thus we ignore these.
				return;
			}
		}

		aggregatedObject.aggregateTimerData(objectToAdd);

		if (!includeRequestMethod) {
			// If we have different request methods, we set the request method to "multiple"
			if (!objectToAdd.getRequestMethod().equals(aggregatedObject.getRequestMethod()) && !aggregatedObject.getRequestMethod().equals(HttpTimerData.REQUEST_METHOD_MULTIPLE)) {
				aggregatedObject.setRequestMethod(HttpTimerData.REQUEST_METHOD_MULTIPLE);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpTimerData getClone(HttpTimerData httpData) {
		HttpTimerData clone = new HttpTimerData();
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
	public boolean isCloning() {
		return cloning;
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
