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
	 * The many-to-many association to the {@link MethodIdent} objects.
	 */
	private Set<MethodIdent> methodIdents = new HashSet<MethodIdent>(0);

	public MethodSensorTypeIdent() {
	}

	public Set<MethodIdent> getMethodIdents() {
		return methodIdents;
	}

	public void setMethodIdents(Set<MethodIdent> methodIdents) {
		this.methodIdents = methodIdents;
	}

}
