package info.novatec.novaspy.rcp.editor.graph.plot.aggregation;

import info.novatec.novaspy.communication.DefaultData;
import info.novatec.novaspy.communication.data.ThreadInformationData;

import java.util.List;

/**
 * The ThreadsDataAggregator provides a method for data aggregation.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ThreadsDataAggregator implements IDataAggregator {

	/**
	 * {@inheritDoc}
	 */
	public DefaultData aggregateData(List<? extends DefaultData> defaultData, int fromIndex, int toIndex) {
		// do nothing when list is empty
		if (!defaultData.isEmpty()) {
			long platformIdent = defaultData.get(0).getPlatformIdent();
			long sensorTypeIdent = defaultData.get(0).getSensorTypeIdent();

			int minDaemonThreadCount = Integer.MAX_VALUE;
			int maxDaemonThreadCount = 0;
			int totalDaemonThreadCount = 0;

			int minPeakThreadCount = Integer.MAX_VALUE;
			int maxPeakThreadCount = 0;
			int totalPeakThreadCount = 0;

			int minThreadCount = Integer.MAX_VALUE;
			int maxThreadCount = 0;
			int totalThreadCount = 0;

			long minTotalStartedThreadCount = Long.MAX_VALUE;
			long maxTotalStartedThreadCount = 0;
			long totalTotalStartedThreadCount = 0;

			int totalCount = 0;

			for (int i = fromIndex; i <= toIndex; i++) {
				ThreadInformationData dataObject = (ThreadInformationData) defaultData.get(i);
				// aggregate the values
				totalCount += dataObject.getCount();

				minDaemonThreadCount = Math.min(dataObject.getMinDaemonThreadCount(), minDaemonThreadCount);
				minPeakThreadCount = Math.min(dataObject.getMinPeakThreadCount(), minPeakThreadCount);
				minThreadCount = Math.min(dataObject.getMinThreadCount(), minThreadCount);
				minTotalStartedThreadCount = Math.min(dataObject.getMinTotalStartedThreadCount(), minTotalStartedThreadCount);

				maxDaemonThreadCount = Math.max(dataObject.getMaxDaemonThreadCount(), maxDaemonThreadCount);
				maxPeakThreadCount = Math.max(dataObject.getMaxPeakThreadCount(), maxPeakThreadCount);
				maxThreadCount = Math.max(dataObject.getMaxThreadCount(), maxThreadCount);
				maxTotalStartedThreadCount = Math.max(dataObject.getMaxTotalStartedThreadCount(), maxTotalStartedThreadCount);

				totalDaemonThreadCount += dataObject.getTotalDaemonThreadCount();
				totalPeakThreadCount += dataObject.getTotalPeakThreadCount();
				totalThreadCount += dataObject.getTotalThreadCount();
				totalTotalStartedThreadCount += dataObject.getTotalTotalStartedThreadCount();
			}

			ThreadInformationData data = new ThreadInformationData(null, platformIdent, sensorTypeIdent);

			data.setMinDaemonThreadCount(minDaemonThreadCount);
			data.setMinPeakThreadCount(minPeakThreadCount);
			data.setMinThreadCount(minThreadCount);
			data.setMinTotalStartedThreadCount(minTotalStartedThreadCount);

			data.setMaxDaemonThreadCount(maxDaemonThreadCount);
			data.setMaxPeakThreadCount(maxPeakThreadCount);
			data.setMaxThreadCount(maxThreadCount);
			data.setMaxTotalStartedThreadCount(maxTotalStartedThreadCount);

			data.setTotalDaemonThreadCount(totalDaemonThreadCount);
			data.setTotalPeakThreadCount(totalPeakThreadCount);
			data.setTotalThreadCount(totalThreadCount);
			data.setTotalTotalStartedThreadCount(totalTotalStartedThreadCount);

			data.setCount(totalCount);

			return data;
		}

		return null;
	}
}
