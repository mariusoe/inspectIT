package info.novatec.novaspy.cmr.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The Sensor Type Ident class is the abstract base class for the
 * {@link MethodSensorTypeIdent} and {@link PlatformSensorTypeIdent} classes.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class SensorTypeIdent implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -8196924255255396992L;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	private Long id;

	/**
	 * The many-to-many association to the {@link PlatformIdent} objects.
	 */
	private Set platformIdents = new HashSet(0);

	/**
	 * The fully qualified class name of the sensor type.
	 */
	private String fullyQualifiedClassName;

	public SensorTypeIdent() {
	}

	public Set getPlatformIdents() {
		return platformIdents;
	}

	public void setPlatformIdents(Set platformIdents) {
		this.platformIdents = platformIdents;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullyQualifiedClassName() {
		return fullyQualifiedClassName;
	}

	public void setFullyQualifiedClassName(String fullyQualifiedClassName) {
		this.fullyQualifiedClassName = fullyQualifiedClassName;
	}

}
