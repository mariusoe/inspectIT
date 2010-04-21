package info.novatec.inspectit.rcp.editor.graph.plot.aggregation;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.CpuInformationData;

import java.util.List;

/**
 * The CpuDataAggregator provides a method for data aggregation.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class CpuDataAggregator implements IDataAggregator {

	/**
	 * {@inheritDoc}
	 */
	public DefaultData aggregateData(List<? extends DefaultData> defaultData, int fromIndex, int toIndex) {
		// do nothing when list is empty
		if (!defaultData.isEmpty()) {
			long platformIdent = defaultData.get(0).getPlatformIdent();
			long sensorTypeIdent = defaultData.get(0).getSensorTypeIdent();

			float minCpuUsage = Float.MAX_VALUE;
			float maxCpuUsage = 0;
			float totalCpuUsage = 0;

			int totalCount = 0;

			for (int i = fromIndex; i <= toIndex; i++) {
				CpuInformationData dataObject = (CpuInformationData) defaultData.get(i);
				// aggregate the values
				totalCount += dataObject.getCount();

				minCpuUsage = Math.min(dataObject.getMinCpuUsage(), minCpuUsage);
				maxCpuUsage = Math.max(dataObject.getMaxCpuUsage(), maxCpuUsage);
				totalCpuUsage += dataObject.getTotalCpuUsage();
			}

			CpuInformationData data = new CpuInformationData(null, platformIdent, sensorTypeIdent);
			data.setMinCpuUsage(minCpuUsage);
			data.setMaxCpuUsage(maxCpuUsage);
			data.setTotalCpuUsage(totalCpuUsage);

			data.setCount(totalCount);

			return data;
		}

		return null;
	}
}
