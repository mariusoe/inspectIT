package info.novatec.inspectit.agent.config.impl;

import info.novatec.inspectit.agent.sensor.ISensor;

import java.util.Hashtable;
import java.util.Map;


/**
 * Abstract sensor type configuration class which is used by the
 * {@link MethodSensorTypeConfig} and the {@link PlatformSensorTypeConfig}.
 * 
 * @author Patrice Bouillet
 * @see MethodSensorTypeConfig
 * @see PlatformSensorTypeConfig
 * 
 */
public abstract class AbstractSensorTypeConfig {

	/**
	 * The hash value of this sensor type.
	 */
	private long id = -1;

	/**
	 * The sensor type for this kind of sensor type configuration.
	 */
	private ISensor sensorType;

	/**
	 * The name of the class.
	 */
	private String className;

	/**
	 * Some additional parameters.
	 */
	private Map parameters = new Hashtable();

	/**
	 * Returns the id.
	 * 
	 * @return The id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Set the id of this sensor type.
	 * 
	 * @param id
	 *            The id to set.
	 */
	public void setId(final long id) {
		this.id = id;
	}

	/**
	 * Returns the sensor type of this configuration.
	 * 
	 * @return Returns the sensor type.
	 */
	public ISensor getSensorType() {
		return sensorType;
	}

	/**
	 * Set the sensor type of this configuration.
	 * 
	 * @param sensorType
	 *            The sensor type.
	 */
	public void setSensorType(final ISensor sensorType) {
		this.sensorType = sensorType;
	}

	/**
	 * Returns the class name of the sensor type as fully qualified.
	 * 
	 * @return The class name.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * The class name has to be stored as fully qualified, example:
	 * <code>java.lang.String</code>.
	 * 
	 * @param className
	 *            The class name.
	 */
	public void setClassName(final String className) {
		this.className = className;
	}

	/**
	 * Returns a {@link Map} of optional parameters. Is never null, but the size
	 * of the map could be 0.
	 * 
	 * @return A map of parameters.
	 */
	public Map getParameters() {
		return parameters;
	}

	/**
	 * The {@link Map} of parameters stores additional information about the
	 * sensor type. Key and value should be both Strings.
	 * 
	 * @param parameters
	 *            The parameters.
	 */
	public void setParameters(final Map parameters) {
		this.parameters = parameters;
	}

}
