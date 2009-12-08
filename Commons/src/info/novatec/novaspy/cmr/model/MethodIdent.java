package info.novatec.novaspy.cmr.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Method Ident class is used to store the information of the Agent(s) about
 * an instrumented method into the database.
 * 
 * @author Patrice Bouillet
 * 
 */
public class MethodIdent implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5670026321320934522L;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	private Long id;

	/**
	 * The timestamp which shows when this information was created on the CMR.
	 */
	private Timestamp timeStamp;

	/**
	 * The many-to-many association to the {@link MethodSensorTypeIdent}
	 * objects.
	 */
	private Set methodSensorTypeIdents = new HashSet(0);

	/**
	 * The many-to-one association to the {@link PlatformIdent} object.
	 */
	private PlatformIdent platformIdent;

	/**
	 * The name of the package.
	 */
	private String packageName;

	/**
	 * The name of the class.
	 */
	private String className;

	/**
	 * The name of the method.
	 */
	private String methodName;

	/**
	 * All method parameters stored in a List, converted to a VARCHAR column in
	 * the database via ListStringType.
	 */
	private List parameters = new ArrayList(0);

	/**
	 * The return type.
	 */
	private String returnType;

	/**
	 * The modifiers.
	 */
	private int modifiers;

	public MethodIdent() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Set getMethodSensorTypeIdents() {
		return methodSensorTypeIdents;
	}

	public void setMethodSensorTypeIdents(Set methodSensorTypeIdents) {
		this.methodSensorTypeIdents = methodSensorTypeIdents;
	}

	public PlatformIdent getPlatformIdent() {
		return platformIdent;
	}

	public void setPlatformIdent(PlatformIdent platformIdent) {
		this.platformIdent = platformIdent;
	}

	public List getParameters() {
		return parameters;
	}

	public void setParameters(List parameters) {
		this.parameters = parameters;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnValue) {
		this.returnType = returnValue;
	}

	public void setModifiers(int modifiers) {
		this.modifiers = modifiers;
	}

	public int getModifiers() {
		return modifiers;
	}

	public String toString() {
		return packageName + "." + className + "#" + methodName + parameters + " : " + returnType;
	}

}
