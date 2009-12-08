package info.novatec.novaspy.rcp.editor.preferences.control.samplingrate;

import info.novatec.novaspy.communication.DefaultData;
import info.novatec.novaspy.communication.data.ClassLoadingInformationData;
import info.novatec.novaspy.communication.data.CpuInformationData;
import info.novatec.novaspy.communication.data.MemoryInformationData;
import info.novatec.novaspy.communication.data.ThreadInformationData;
import info.novatec.novaspy.communication.data.TimerData;
import info.novatec.novaspy.rcp.editor.graph.plot.aggregation.ClassesDataAggregator;
import info.novatec.novaspy.rcp.editor.graph.plot.aggregation.CpuDataAggregator;
import info.novatec.novaspy.rcp.editor.graph.plot.aggregation.IDataAggregator;
import info.novatec.novaspy.rcp.editor.graph.plot.aggregation.MemoryDataAggregator;
import info.novatec.novaspy.rcp.editor.graph.plot.aggregation.ThreadsDataAggregator;
import info.novatec.novaspy.rcp.editor.graph.plot.aggregation.TimerDataAggregator;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * The data aggregator factory.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public final class DataAggregatorFactory {

	/**
	 * The {@link ClassesDataAggregator}.
	 */
	private static final IDataAggregator CLASSLOADING_DATA_AGGREGATOR = new ClassesDataAggregator();

	/**
	 * The {@link CpuDataAggregator}.
	 */
	private static final IDataAggregator CPU_DATA_AGGREGATOR = new CpuDataAggregator();

	/**
	 * The {@link MemoryDataAggregator}.
	 */
	private static final IDataAggregator MEMORY_DATA_AGGREGATOR = new MemoryDataAggregator();

	/**
	 * The {@link TimerDataAggregator}.
	 */
	private static final IDataAggregator TIMER_DATA_AGGREGATOR = new TimerDataAggregator();

	/**
	 * The {@link ThreadsDataAggregator}.
	 */
	private static final IDataAggregator THREAD_DATA_AGGREGATOR = new ThreadsDataAggregator();

	/**
	 * The private constructor.
	 */
	private DataAggregatorFactory() {
	}

	/**
	 * Returns an instance of the {@link IDataAggregator}.
	 * 
	 * @param defaultDataList
	 *            The {@link List} with {@link DefaultData} objects.
	 * @return An instance of the {@link IDataAggregator}.
	 */
	public static IDataAggregator getDataAggregator(List<? extends DefaultData> defaultDataList) {
		if ((null == defaultDataList) || (0 == defaultDataList.size())) {
			throw new InvalidParameterException("Invalid parameter for the data aggregator factory!");
		}

		DefaultData defaultData = defaultDataList.get(0);
		if (defaultData instanceof TimerData) {
			return TIMER_DATA_AGGREGATOR;
		} else if (defaultData instanceof ClassLoadingInformationData) {
			return CLASSLOADING_DATA_AGGREGATOR;
		} else if (defaultData instanceof CpuInformationData) {
			return CPU_DATA_AGGREGATOR;
		} else if (defaultData instanceof MemoryInformationData) {
			return MEMORY_DATA_AGGREGATOR;
		} else if (defaultData instanceof ThreadInformationData) {
			return THREAD_DATA_AGGREGATOR;
		}

		throw new RuntimeException("No data aggregator could be found!");
	}

}
