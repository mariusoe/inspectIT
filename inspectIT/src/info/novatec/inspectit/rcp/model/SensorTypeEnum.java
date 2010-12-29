package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * This enumeration holds all available sensor types with their full qualified name and their image.
 * 
 * @author Patrice Bouillet
 * 
 */
public enum SensorTypeEnum {
	/** The timer sensor type. */
	TIMER("info.novatec.inspectit.agent.sensor.method.timer.TimerSensor", InspectITConstants.IMG_TIMER),
	/** The average timer sensor type. */
	AVERAGE_TIMER("info.novatec.inspectit.agent.sensor.method.averagetimer.AverageTimerSensor", InspectITConstants.IMG_TIMER),
	/** The invocation sequence sensor type. */
	INVOCATION_SEQUENCE("info.novatec.inspectit.agent.sensor.method.invocationsequence.InvocationSequenceSensor", InspectITConstants.IMG_INVOCATION),
	/** The sql sensor type. */
	SQL("info.novatec.inspectit.agent.sensor.method.jdbc.SQLTimerSensor", InspectITConstants.IMG_DATABASE),
	/** The jdbc connection sensor type. */
	JDBC_CONNECTION("info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionSensor", InspectITConstants.IMG_DATABASE, false),
	/** The jdbc statement sensor type. */
	JDBC_STATEMENT("info.novatec.inspectit.agent.sensor.method.jdbc.StatementSensor", InspectITConstants.IMG_DATABASE, false),
	/** The jdbc prepared statement sensor type. */
	JDBC_PREPARED_STATEMENT("info.novatec.inspectit.agent.sensor.method.jdbc.PreparedStatementSensor", InspectITConstants.IMG_DATABASE, false),
	/** The jdbc prepared statement parameter sensor type. */
	JDBC_PREPARED_STATEMENT_PARAMETER("info.novatec.inspectit.agent.sensor.method.jdbc.PreparedStatementParameterSensor", InspectITConstants.IMG_DATABASE, false),
	/** The exception sensor */
	EXCEPTION_SENSOR("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor", InspectITConstants.IMG_EXCEPTION_SENSOR),
	/** The exception sensor overview */
	EXCEPTION_SENSOR_GROUPED("info.novatec.inspectit.agent.sensor.exception.ExceptionSensorOverview", InspectITConstants.IMG_FILTER),
	/** The combined metrics sensor type */
	MARVIN_WORKFLOW("info.novatec.inspectit.agent.sensor.method.marvintimer.MarvinWorkflowSensor", InspectITConstants.IMG_INVOCATION),

	/** The classloading information sensor type. */
	CLASSLOADING_INFORMATION("info.novatec.inspectit.agent.sensor.platform.ClassLoadingInformation", InspectITConstants.IMG_CLASS_OVERVIEW),
	/** The compilation information sensor type. */
	COMPILATION_INFORMATION("info.novatec.inspectit.agent.sensor.platform.CompilationInformation", null),
	/** The memory information sensor type. */
	MEMORY_INFORMATION("info.novatec.inspectit.agent.sensor.platform.MemoryInformation", InspectITConstants.IMG_MEMORY_OVERVIEW),
	/** The cpu information sensor type. */
	CPU_INFORMATION("info.novatec.inspectit.agent.sensor.platform.CpuInformation", InspectITConstants.IMG_CPU_OVERVIEW),
	/** The runtime information sensor type. */
	RUNTIME_INFORMATION("info.novatec.inspectit.agent.sensor.platform.RuntimeInformation", null),
	/** The system information sensor type. */
	SYSTEM_INFORMATION("info.novatec.inspectit.agent.sensor.platform.SystemInformation", InspectITConstants.IMG_SYSTEM_OVERVIEW),
	/** The thread information sensor type. */
	THREAD_INFORMATION("info.novatec.inspectit.agent.sensor.platform.ThreadInformation", InspectITConstants.IMG_THREADS_OVERVIEW);

	/**
	 * The LOOKUP map which is used to get an element of the enumeration when passing the full
	 * qualified name.
	 */
	private static final Map<String, SensorTypeEnum> LOOKUP = new HashMap<String, SensorTypeEnum>();

	static {
		for (SensorTypeEnum s : EnumSet.allOf(SensorTypeEnum.class)) {
			LOOKUP.put(s.getFqn(), s);
		}
	}

	/**
	 * The full qualified name string.
	 */
	private String fqn;

	/**
	 * The image descriptor.
	 */
	private ImageDescriptor imageDescriptor;

	/**
	 * Defines if this sensor type can be opened somehow. By default <b>true</b>.
	 */
	private boolean openable = true;

	/**
	 * Constructs an element of the enumeration with the given full qualified name string.
	 * 
	 * @param fqn
	 *            The full qualified name.
	 * @param imageName
	 *            The name of the image. Names are defined in {@link InspectITConstants}.
	 */
	private SensorTypeEnum(String fqn, String imageName) {
		this.fqn = fqn;
		this.imageDescriptor = InspectIT.getDefault().getImageDescriptor(imageName);
	}

	/**
	 * Same as standard constructor but the openable can be specified in addition.
	 * 
	 * @param fqn
	 *            The full qualified name.
	 * @param imageName
	 *            The name of the image. Names are defined in {@link InspectITConstants}.
	 * @param openable
	 *            Defines if this can be opened somehow in the UI.
	 */
	private SensorTypeEnum(String fqn, String imageName, boolean openable) {
		this.fqn = fqn;
		this.imageDescriptor = InspectIT.getDefault().getImageDescriptor(imageName);
		this.openable = openable;
	}

	/**
	 * The full qualified name of the sensor type.
	 * 
	 * @return The full qualified name.
	 */
	public String getFqn() {
		return fqn;
	}

	/**
	 * Returns an element of the enumeration for the given full qualified name.
	 * 
	 * @param fqn
	 *            The full qualified class name of the sensor type.
	 * @return An element of the enumeration.
	 */
	public static SensorTypeEnum get(String fqn) {
		return LOOKUP.get(fqn);
	}

	/**
	 * Returns all elements of the enumeration for the given list of sensor type ident objects.
	 * 
	 * @param sensorTypes
	 *            The passed sensor type ident objects.
	 * @return A set of SensorTypeEnum objects.
	 */
	public static Set<SensorTypeEnum> getAllOf(Set<? extends SensorTypeIdent> sensorTypes) {
		Set<SensorTypeEnum> sensorTypeSet = EnumSet.noneOf(SensorTypeEnum.class);
		for (SensorTypeIdent sensorType : sensorTypes) {
			sensorTypeSet.add(get(sensorType.getFullyQualifiedClassName()));
		}
		return sensorTypeSet;
	}

	/**
	 * Returns an image descriptor for this sensor type.
	 * 
	 * @return The sensor type image descriptor.
	 */
	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	/**
	 * Returns a displayable name of the sensor type.
	 * 
	 * @return The displayable name.
	 */
	public String getDisplayName() {
		StringBuilder name = new StringBuilder(name().toLowerCase().replaceAll("_", " "));
		Character character = name.charAt(0);
		character = Character.toUpperCase(character);
		name.setCharAt(0, character);

		int i = 0;
		while (i >= 0) {
			i = name.indexOf(" ", i);
			if (i >= 0) {
				i = i + 1;
				character = Character.toUpperCase(name.charAt(i));
				name.setCharAt(i, character);
			}
		}

		return name.toString();
	}

	/**
	 * Returns if this sensor type can be opened somehow.
	 * 
	 * @return if its openable.
	 */
	public boolean isOpenable() {
		return openable;
	}

}
