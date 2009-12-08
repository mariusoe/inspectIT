package info.novatec.novaspy.rcp.model;

import info.novatec.novaspy.rcp.NovaSpy;
import info.novatec.novaspy.rcp.NovaSpyConstants;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * This enumeration holds all available sensor types with their full qualified
 * name and their image.
 * 
 * @author Patrice Bouillet
 * 
 */
public enum SensorTypeEnum {
	/** The timer sensor type. */
	TIMER("info.novatec.novaspy.agent.sensor.method.timer.TimerSensor", NovaSpyConstants.IMG_TIMER),
	/** The average timer sensor type. */
	AVERAGE_TIMER("info.novatec.novaspy.agent.sensor.method.averagetimer.AverageTimerSensor", NovaSpyConstants.IMG_TIMER),
	/** The invocation sequence sensor type. */
	INVOCATION_SEQUENCE("info.novatec.novaspy.agent.sensor.method.invocationsequence.InvocationSequenceSensor", NovaSpyConstants.IMG_INVOCATION),
	/** The sql sensor type. */
	SQL("info.novatec.novaspy.agent.sensor.method.jdbc.SQLTimerSensor", NovaSpyConstants.IMG_DATABASE),
	/** The jdbc connection sensor type. */
	JDBC_CONNECTION("info.novatec.novaspy.agent.sensor.method.jdbc.ConnectionSensor", NovaSpyConstants.IMG_DATABASE),
	/** The jdbc statement sensor type. */
	JDBC_STATEMENT("info.novatec.novaspy.agent.sensor.method.jdbc.StatementSensor", NovaSpyConstants.IMG_DATABASE),
	/** The jdbc prepared statement sensor type. */
	JDBC_PREPARED_STATEMENT("info.novatec.novaspy.agent.sensor.method.jdbc.PreparedStatementSensor", NovaSpyConstants.IMG_DATABASE),
	/** The jdbc prepared statement parameter sensor type. */
	JDBC_PREPARED_STATEMENT_PARAMETER("info.novatec.novaspy.agent.sensor.method.jdbc.PreparedStatementParameterSensor", NovaSpyConstants.IMG_DATABASE),
	/** The exception tracer */
	EXCEPTION_TRACER("info.novatec.novaspy.agent.sensor.exception.ExceptionTracingSensor", NovaSpyConstants.IMG_INVOCATION),
	/** The exception tracer overview */
	EXCEPTION_TRACER_OVERVIEW("info.novatec.novaspy.agent.sensor.exception.ExceptionTracingSensorOverview", NovaSpyConstants.IMG_CLASS_OVERVIEW),
	/** The combined metrics sensor type */
	MARVIN_WORKFLOW("info.novatec.novaspy.agent.sensor.method.marvintimer.MarvinWorkflowSensor", NovaSpyConstants.IMG_INVOCATION),

	/** The classloading information sensor type. */
	CLASSLOADING_INFORMATION("info.novatec.novaspy.agent.sensor.platform.ClassLoadingInformation", NovaSpyConstants.IMG_CLASS_OVERVIEW),
	/** The compilation information sensor type. */
	COMPILATION_INFORMATION("info.novatec.novaspy.agent.sensor.platform.CompilationInformation", null),
	/** The memory information sensor type. */
	MEMORY_INFORMATION("info.novatec.novaspy.agent.sensor.platform.MemoryInformation", NovaSpyConstants.IMG_MEMORY_OVERVIEW),
	/** The cpu information sensor type. */
	CPU_INFORMATION("info.novatec.novaspy.agent.sensor.platform.CpuInformation", NovaSpyConstants.IMG_CPU_OVERVIEW),
	/** The runtime information sensor type. */
	RUNTIME_INFORMATION("info.novatec.novaspy.agent.sensor.platform.RuntimeInformation", null),
	/** The system information sensor type. */
	SYSTEM_INFORMATION("info.novatec.novaspy.agent.sensor.platform.SystemInformation", NovaSpyConstants.IMG_SYSTEM_OVERVIEW),
	/** The thread information sensor type. */
	THREAD_INFORMATION("info.novatec.novaspy.agent.sensor.platform.ThreadInformation", NovaSpyConstants.IMG_THREADS_OVERVIEW);

	/**
	 * The LOOKUP map which is used to get an element of the enumeration when
	 * passing the full qualified name.
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
	 * Constructs an element of the enumeration with the given full qualified
	 * name string.
	 * 
	 * @param fqn
	 *            The full qualified name.
	 * @param imageName
	 *            The name of the image. Names are defined in
	 *            {@link NovaSpyConstants}.
	 */
	private SensorTypeEnum(String fqn, String imageName) {
		this.fqn = fqn;
		this.imageDescriptor = NovaSpy.getDefault().getImageDescriptor(imageName);
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

}
