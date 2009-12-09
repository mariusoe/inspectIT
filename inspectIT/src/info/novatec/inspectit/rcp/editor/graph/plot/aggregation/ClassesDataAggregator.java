package info.novatec.inspectit.rcp.editor.graph.plot.aggregation;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;

import java.util.List;

/**
 * The ClassesDataAggregator provides a method for data aggregation.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ClassesDataAggregator implements IDataAggregator {

	/**
	 * {@inheritDoc}
	 */
	public DefaultData aggregateData(List<? extends DefaultData> defaultData, int fromIndex, int toIndex) {
		// do nothing when list is empty
		if (!defaultData.isEmpty()) {
			long platformIdent = defaultData.get(0).getPlatformIdent();
			long sensorTypeIdent = defaultData.get(0).getSensorTypeIdent();

			int minLoadedClassCountAverage = Integer.MAX_VALUE;
			int maxLoadedClassCountAverage = 0;
			int totalLoadedClassCountAverage = 0;

			long minTotalLoadedClassCountAverage = Long.MAX_VALUE;
			long maxTotalLoadedClassCountAverage = 0;
			long totalTotalLoadedClassCountAverage = 0;

			long minUnloadedClassCountAverage = Long.MAX_VALUE;
			long maxUnloadedClassCountAverage = 0;
			long totalUnloadedClassCountAverage = 0;

			int totalCount = 0;

			for (int i = fromIndex; i <= toIndex; i++) {
				ClassLoadingInformationData dataObject = (ClassLoadingInformationData) defaultData.get(i);
				totalCount += dataObject.getCount();

				minLoadedClassCountAverage = Math.min(dataObject.getMinLoadedClassCount(), minLoadedClassCountAverage);
				maxLoadedClassCountAverage = Math.max(dataObject.getMaxLoadedClassCount(), maxLoadedClassCountAverage);
				totalLoadedClassCountAverage += dataObject.getTotalLoadedClassCount();

				minTotalLoadedClassCountAverage = Math.min(dataObject.getMinTotalLoadedClassCount(), minTotalLoadedClassCountAverage);
				maxTotalLoadedClassCountAverage = Math.max(dataObject.getMaxTotalLoadedClassCount(), maxTotalLoadedClassCountAverage);
				totalTotalLoadedClassCountAverage += dataObject.getTotalTotalLoadedClassCount();

				minUnloadedClassCountAverage = Math.min(dataObject.getMinUnloadedClassCount(), minUnloadedClassCountAverage);
				maxUnloadedClassCountAverage = Math.max(dataObject.getMaxUnloadedClassCount(), maxUnloadedClassCountAverage);
				totalUnloadedClassCountAverage += dataObject.getTotalUnloadedClassCount();
			}

			ClassLoadingInformationData data = new ClassLoadingInformationData(null, platformIdent, sensorTypeIdent);

			data.setMinLoadedClassCount(minLoadedClassCountAverage);
			data.setMaxLoadedClassCount(maxLoadedClassCountAverage);
			data.setTotalLoadedClassCount(totalLoadedClassCountAverage);

			data.setMinTotalLoadedClassCount(minTotalLoadedClassCountAverage);
			data.setMaxTotalLoadedClassCount(maxTotalLoadedClassCountAverage);
			data.setTotalTotalLoadedClassCount(totalTotalLoadedClassCountAverage);

			data.setMinUnloadedClassCount(minUnloadedClassCountAverage);
			data.setMaxUnloadedClassCount(maxUnloadedClassCountAverage);
			data.setTotalUnloadedClassCount(totalUnloadedClassCountAverage);

			data.setCount(totalCount);

			return data;
		}

		return null;
	}
}
