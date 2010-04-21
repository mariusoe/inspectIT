package info.novatec.inspectit.communication.data.ci;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The sensor type data object is used to store the information about a sensor
 * type.
 * 
 * @author Matthias Huber
 * 
 */
public class SensorTypeData implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5502036433639956565L;

	/**
	 * This constant defines that no type option is set.
	 */
	public static final int NO_TYPE_OPTION = 0;

	/**
	 * This constant defines that the type option 'aggregate' is set.
	 */
	public static final int AGGREGATE = 1;

	/**
	 * This constant defines that the type option 'optimized' is set.
	 */
	public static final int OPTIMIZED = 2;

	/**
	 * This constant defines that the type option 'raw' is set.
	 */
	public static final int RAW = 3;

	/**
	 * This constant defines that the priority 'invocation' is set.
	 */
	public static final int INVOC = 0;

	/**
	 * This constant defines that the priority 'min' is set.
	 */
	public static final int MIN = 1;
	
	/**
	 * This constant defines that the priority 'low' is set.
	 */
	public static final int LOW = 2;

	/**
	 * This constant defines that the priority 'normal' is set.
	 */
	public static final int NORMAL = 3;

	/**
	 * This constant defines that the priority 'high' is set.
	 */
	public static final int HIGH = 4;
	
	/**
	 * This constant defines that the priority 'max' is set.
	 */
	public static final int MAX = 5;
	
	/**
	 * The id of this instance.
	 */
	private long id;

	/**
	 * The many-to-one association to the {@link EnvironmentData} object.
	 */
	private EnvironmentData environmentData;
	
	/**
	 * The sensor type name. This name will be displayed to the user instead of
	 * the fully qualified name of the sensor type.
	 */
	private String name;

	/**
	 * The fully qualified name of the sensor type class.
	 */
	private String fullyQualifiedName;

	/**
	 * The priority of the sensor type.
	 */
	private int priority;

	/**
	 * The sensor type description.
	 */
	private String description;

	/**
	 * The additional option for this sensor type.
	 * 
	 * 0: no additional option is set
	 * 1: option 'aggregate' is set
	 * 2: option 'optimized' is set
	 * 3: option 'raw' is set
	 */
	private int typeOption = NO_TYPE_OPTION;

	/**
	 * The many-to-many association to the {@link MethodSensorDefinitionData}
	 * objects.
	 */
	private Set methodSensorDefinitions = new HashSet(0);

	/**
	 * Default no-args constructor.
	 */
	public SensorTypeData() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public EnvironmentData getEnvironmentData() {
		return environmentData;
	}

	public void setEnvironmentData(EnvironmentData environmentData) {
		this.environmentData = environmentData;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	public void setFullyQualifiedName(String fullyQualifiedName) {
		this.fullyQualifiedName = fullyQualifiedName;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getTypeOption() {
		return typeOption;
	}

	public void setTypeOption(int typeOptionData) {
		this.typeOption = typeOptionData;
	}

	public Set getMethodSensorDefinitions() {
		return methodSensorDefinitions;
	}

	public void setMethodSensorDefinitions(Set methodSensorDefinitions) {
		this.methodSensorDefinitions = methodSensorDefinitions;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((environmentData == null) ? 0 : environmentData.hashCode());
		result = prime * result + ((fullyQualifiedName == null) ? 0 : fullyQualifiedName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + priority;
		result = prime * result + typeOption;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorTypeData other = (SensorTypeData) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (environmentData == null) {
			if (other.environmentData != null)
				return false;
		} else if (!environmentData.equals(other.environmentData))
			return false;
		if (fullyQualifiedName == null) {
			if (other.fullyQualifiedName != null)
				return false;
		} else if (!fullyQualifiedName.equals(other.fullyQualifiedName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (priority != other.priority)
			return false;
		if (typeOption != other.typeOption)
			return false;
		return true;
	}

}
