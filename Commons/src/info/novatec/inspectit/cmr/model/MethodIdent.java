package info.novatec.inspectit.cmr.model;

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

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + modifiers;
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
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
		MethodIdent other = (MethodIdent) obj;
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (methodName == null) {
			if (other.methodName != null) {
				return false;
			}
		} else if (!methodName.equals(other.methodName)) {
			return false;
		}
		if (modifiers != other.modifiers) {
			return false;
		}
		if (packageName == null) {
			if (other.packageName != null) {
				return false;
			}
		} else if (!packageName.equals(other.packageName)) {
			return false;
		}
		if (parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!parameters.equals(other.parameters)) {
			return false;
		}
		if (returnType == null) {
			if (other.returnType != null) {
				return false;
			}
		} else if (!returnType.equals(other.returnType)) {
			return false;
		}
		if (timeStamp == null) {
			if (other.timeStamp != null) {
				return false;
			}
		} else if (!timeStamp.equals(other.timeStamp)) {
			return false;
		}
		return true;
	}

}
