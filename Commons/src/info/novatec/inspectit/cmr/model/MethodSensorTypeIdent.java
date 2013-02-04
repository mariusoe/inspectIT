package info.novatec.inspectit.cmr.model;

import java.util.HashSet;
import java.util.Set;

/**
 * The Method Sensor Type Ident class is used to store the sensortypes which are used for methods
 * and basically called when the respective method is called.
 * 
 * @author Patrice Bouillet
 * 
 */
public class MethodSensorTypeIdent extends SensorTypeIdent {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -8933452676894686230L;

	/**
	 * The one-to-many association to the {@link MethodIdentToSensorType} objects.
	 */
	private Set<MethodIdentToSensorType> methodIdentToSensorTypes = new HashSet<MethodIdentToSensorType>(0);

	/**
	 * Gets {@link #methodIdentToSensorTypes}.
	 * 
	 * @return {@link #methodIdentToSensorTypes}
	 */
	public Set<MethodIdentToSensorType> getMethodIdentToSensorTypes() {
		return methodIdentToSensorTypes;
	}

	/**
	 * Sets {@link #methodIdentToSensorTypes}.
	 * 
	 * @param methodIdentToSensorTypes
	 *            New value for {@link #methodIdentToSensorTypes}
	 */
	public void setMethodIdentToSensorTypes(Set<MethodIdentToSensorType> methodIdentToSensorTypes) {
		this.methodIdentToSensorTypes = methodIdentToSensorTypes;
	}

}
