package info.novatec.inspectit.rcp.editor.graph.plot.aggregation;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.List;

/**
 * The TimerDataAggregator provides a method for data aggregation.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class TimerDataAggregator implements IDataAggregator {

	/**
	 * {@inheritDoc}
	 */
	public DefaultData aggregateData(List<? extends DefaultData> defaultData, int fromIndex, int toIndex) {
		// do nothing when list is empty
		if (!defaultData.isEmpty()) {
			long platformIdent = defaultData.get(0).getPlatformIdent();
			long sensorTypeIdent = defaultData.get(0).getSensorTypeIdent();
			double duration = 0;
			double max = 0;
			double min = Double.MAX_VALUE;
			long totalCount = 0;

			for (int i = fromIndex; i <= toIndex; i++) {
				TimerData dataObject = (TimerData) defaultData.get(i);
				// aggregate the values
				totalCount += dataObject.getCount();
				duration += dataObject.getDuration();
				max = Math.max(dataObject.getMax(), max);
				min = Math.min(dataObject.getMin(), min);
			}

			TimerData data = new TimerData();
			data.setSensorTypeIdent(sensorTypeIdent);
			data.setPlatformIdent(platformIdent);
			data.setMin(min);
			data.setMax(max);
			data.setDuration(duration);
			data.setCount(totalCount);
			data.setAverage(duration / totalCount);

			return data;
		}

		return null;
	}

}
