package info.novatec.inspectit.communication.data;

import java.io.Serializable;

/**
 * Stores the content and meta-data of a method parameter or of a field of a
 * class.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ParameterContentData implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -8005782295084781051L;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	private long id;

	/**
	 * The id of the method sensor.
	 */
	private long methodSensorId;

	/**
	 * The name of the parameter. This can only be set if this class stores the
	 * content of a class field as method parameters don't have a name which can
	 * be accessed via reflection.
	 */
	private String name;

	/**
	 * The content of the field / parameter.
	 */
	private String content;

	/**
	 * Defines if this object stores the content of a method parameter.
	 */
	private boolean isMethodParameter = false;

	/**
	 * If the content of a method parameter is stored the position of the
	 * parameter in the signature has to be saved, too.
	 */
	private int signaturePosition = -1;

	/**
	 * Default no-args constructor.
	 */
	public ParameterContentData() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setMethodSensorId(long methodSensorId) {
		this.methodSensorId = methodSensorId;
	}

	public long getMethodSensorId() {
		return methodSensorId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isMethodParameter() {
		return isMethodParameter;
	}

	public void setMethodParameter(boolean isMethodParameter) {
		this.isMethodParameter = isMethodParameter;
	}

	public int getSignaturePosition() {
		return signaturePosition;
	}

	public void setSignaturePosition(int signaturePosition) {
		this.signaturePosition = signaturePosition;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return content;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + (isMethodParameter ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + signaturePosition;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ParameterContentData other = (ParameterContentData) obj;
		if (content == null) {
			if (other.content != null) {
				return false;
			}
		} else if (!content.equals(other.content)) {
			return false;
		}
		if (isMethodParameter != other.isMethodParameter) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (signaturePosition != other.signaturePosition) {
			return false;
		}
		return true;
	}

}
