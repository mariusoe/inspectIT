package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Data object holding http based timer data. All timer related information are inherited from the
 * super class.
 * 
 * <b> Be careful when adding new attributes. Do not forget to add them to the size calculation.
 * </b>
 * 
 * @author Stefan Siegl
 */
public class HttpTimerData extends TimerData {

	/** Generated serial version id. */
	private static final long serialVersionUID = -7868876342858232388L;
	/** String used to represent an unset <code>uri</code> or <code>requestMethod</code>. */
	public static final String UNDEFINED = "n.a.";
	/** String used to represent multiple request methods in an aggregation. */
	public static final String REQUEST_METHOD_MULTIPLE = "MULTIPLE";
	/**
	 * Max URI chars size.
	 */
	private static final int MAX_URI_SIZE = 1000;

	/** The uri. */
	private String uri = UNDEFINED;
	/** Map is String-String[]. */
	private Map<String, String[]> parameters = null;
	/** Map is String-String. */
	private Map<String, String> attributes = null;
	/** Map is String-String. */
	private Map<String, String> headers = null;
	/** Map is String-String. */
	private Map<String, String> sessionAttributes = null;
	/** The request method. */
	private String requestMethod = UNDEFINED;

	/** The default header for tagged requests. */
	public static final String INSPECTIT_TAGGING_HEADER = "inspectit";

	/**
	 * Constructor.
	 * 
	 * @param timeStamp
	 *            the timestamp of this data
	 * @param platformIdent
	 *            the platform identification
	 * @param sensorTypeIdent
	 *            the sensor type
	 * @param methodIdent
	 *            the method this data comes from
	 */
	public HttpTimerData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	/**
	 * Constructor.
	 */
	public HttpTimerData() {
	}

	/**
	 * Checks if this data has the inspectIT tagging header set.
	 * 
	 * @return if this data has the inspectIT tagging header set.
	 */
	public boolean hasInspectItTaggingHeader() {
		if (null == headers) {
			return false;
		}
		return headers.containsKey(INSPECTIT_TAGGING_HEADER);
	}

	/**
	 * Retrieves the value of the inspectit tagging header.
	 * 
	 * @return the value of the inspectit tagging header.
	 */
	public String getInspectItTaggingHeaderValue() {
		if (null == headers) {
			return UNDEFINED;
		}
		return (String) headers.get(INSPECTIT_TAGGING_HEADER);
	}

	/**
	 * Sets the value for the inspectIT header.
	 * 
	 * @param value
	 *            the value for the inspectIT header.
	 */
	public void setInspectItTaggingHeaderValue(String value) {
		if (null == headers) {
			headers = new HashMap<String, String>(1);
		}
		headers.put(INSPECTIT_TAGGING_HEADER, value);
	}

	public String getUri() {
		return uri;
	}

	/**
	 * Sets the uri.
	 * 
	 * @param uri
	 *            the uri.
	 */
	public void setUri(String uri) {
		if (null != uri) {
			if (uri.length() > MAX_URI_SIZE) {
				this.uri = uri.substring(0, MAX_URI_SIZE);
			} else {
				this.uri = uri;
			}
		}
	}

	/**
	 * Returns if the URI is defined for this instance.
	 * 
	 * @return True if {@link #uri} is not null and is different from {@value #UNDEFINED}.
	 */
	public boolean isUriDefined() {
		return uri != null && !UNDEFINED.equals(uri);
	}

	public Map<String, String[]> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String[]> parameters) {
		this.parameters = parameters;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, String> getSessionAttributes() {
		return sessionAttributes;
	}

	public void setSessionAttributes(Map<String, String> sessionAttributes) {
		this.sessionAttributes = sessionAttributes;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, false);
		size += objectSizes.getPrimitiveTypesSize(6, 0, 0, 0, 0, 0);
		size += objectSizes.getSizeOf(uri, requestMethod);

		if (null != parameters) {
			size += objectSizes.getSizeOfHashMap(parameters.size());
			for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
				size += objectSizes.getSizeOf(entry.getKey());
				String[] values = entry.getValue();
				size += objectSizes.getSizeOfArray(values.length);
				for (int i = 0; i < values.length; i++) {
					size += objectSizes.getSizeOf(values[i]);
				}
			}
		}

		if (null != attributes) {
			size += objectSizes.getSizeOfHashMap(attributes.size());
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				size += objectSizes.getSizeOf(entry.getKey());
				size += objectSizes.getSizeOf(entry.getValue());
			}
		}

		if (null != headers) {
			size += objectSizes.getSizeOfHashMap(headers.size());
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				size += objectSizes.getSizeOf(entry.getKey());
				size += objectSizes.getSizeOf(entry.getValue());
			}
		}

		if (null != sessionAttributes) {
			size += objectSizes.getSizeOfHashMap(sessionAttributes.size());
			for (Map.Entry<String, String> entry : sessionAttributes.entrySet()) {
				size += objectSizes.getSizeOf(entry.getKey());
				size += objectSizes.getSizeOf(entry.getValue());
			}
		}

		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		String sup = super.toString();
		return sup + "HttpTimerData [uri=" + uri + ", parameters=" + parameters + ", attributes=" + attributes + ", headers=" + headers + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((requestMethod == null) ? 0 : requestMethod.hashCode());
		result = prime * result + ((sessionAttributes == null) ? 0 : sessionAttributes.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HttpTimerData other = (HttpTimerData) obj;
		if (attributes == null) {
			if (other.attributes != null) {
				return false;
			}
		} else if (!attributes.equals(other.attributes)) {
			return false;
		}
		if (headers == null) {
			if (other.headers != null) {
				return false;
			}
		} else if (!headers.equals(other.headers)) {
			return false;
		}
		if (parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!parameters.equals(other.parameters)) {
			return false;
		}
		if (requestMethod == null) {
			if (other.requestMethod != null) {
				return false;
			}
		} else if (!requestMethod.equals(other.requestMethod)) {
			return false;
		}
		if (sessionAttributes == null) {
			if (other.sessionAttributes != null) {
				return false;
			}
		} else if (!sessionAttributes.equals(other.sessionAttributes)) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		return true;
	}

}
