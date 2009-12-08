package info.novatec.novaspy.cmr.model;

import java.util.HashSet;
import java.util.Set;

/**
 * The Method Sensor Type Ident class is used to store the sensortypes which are
 * used for methods and basically called when the respective method is called.
 * 
 * @author Patrice Bouillet
 * 
 */
public class MethodSensorTypeIdent extends SensorTypeIdent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8933452676894686230L;
	/**
	 * The many-to-many association to the {@link MethodIdent} objects.
	 */
	private Set methodIdents = new HashSet(0);

	public MethodSensorTypeIdent() {
	}

	public Set getMethodIdents() {
		return methodIdents;
	}

	public void setMethodIdents(Set methodIdents) {
		this.methodIdents = methodIdents;
	}

}
