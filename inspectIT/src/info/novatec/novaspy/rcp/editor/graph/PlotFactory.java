package info.novatec.novaspy.rcp.editor.graph;

import info.novatec.novaspy.rcp.editor.graph.plot.DefaultClassesPlotController;
import info.novatec.novaspy.rcp.editor.graph.plot.DefaultCombinedMetricsPlotController;
import info.novatec.novaspy.rcp.editor.graph.plot.DefaultCpuPlotController;
import info.novatec.novaspy.rcp.editor.graph.plot.DefaultMemoryPlotController;
import info.novatec.novaspy.rcp.editor.graph.plot.DefaultThreadsPlotController;
import info.novatec.novaspy.rcp.editor.graph.plot.DefaultTimerPlotController;
import info.novatec.novaspy.rcp.editor.graph.plot.PlotController;
import info.novatec.novaspy.rcp.model.SensorTypeEnum;

/**
 * The factory for the plot creation.
 * 
 * @author Patrice Bouillet
 * 
 */
public final class PlotFactory {

	/**
	 * The private constructor.
	 */
	private PlotFactory() {
	}

	/**
	 * Creates and returns an instance of {@link PlotController}.
	 * 
	 * @param sensorTypeEnum
	 *            The {@link SensorTypeEnum}.
	 * @return An instance of {@link PlotController}.
	 */
	public static PlotController createDefaultPlotController(SensorTypeEnum sensorTypeEnum) {
		switch (sensorTypeEnum) {
		case AVERAGE_TIMER:
			return new DefaultTimerPlotController();
		case TIMER:
			return new DefaultTimerPlotController();
		case CLASSLOADING_INFORMATION:
			return new DefaultClassesPlotController();
		case COMPILATION_INFORMATION:
			return null;
		case MEMORY_INFORMATION:
			return new DefaultMemoryPlotController();
		case CPU_INFORMATION:
			return new DefaultCpuPlotController();
		case RUNTIME_INFORMATION:
			return null;
		case SYSTEM_INFORMATION:
			return null;
		case THREAD_INFORMATION:
			return new DefaultThreadsPlotController();
		case MARVIN_WORKFLOW:
			return new DefaultCombinedMetricsPlotController();
		default:
			return null;
		}
	}

	/**
	 * Returns an instance of {@link PlotController}.
	 * 
	 * @param fqn
	 *            The fully-qualified-name.
	 * @return An instance of {@link PlotController}.
	 */
	public static PlotController createDefaultPlotController(String fqn) {
		return createDefaultPlotController(SensorTypeEnum.get(fqn));
	}

}
