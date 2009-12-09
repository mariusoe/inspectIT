package info.novatec.inspectit.rcp.editor.preferences.control.samplingrate;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.graph.plot.aggregation.IDataAggregator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The enumeration for sampling rate modes.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public enum SamplingRateMode implements ISamplingRateMode {
	/**
	 * The identifier of the sampling rate modes.
	 */
	TIMEFRAME_DIVIDER {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<? extends DefaultData> adjustSamplingRate(List<? extends DefaultData> defaultDataList, Date from, Date to, int samplingRate) {
			long timeframe = 0;

			if ((samplingRate > 0) && (defaultDataList != null)) {
				timeframe = (to.getTime() - from.getTime()) / samplingRate;
			} else {
				return defaultDataList;
			}

			List<DefaultData> resultList = new ArrayList<DefaultData>();

			// define the start and end position of the first time frame
			long timeframeStartTime = Long.MAX_VALUE;
			long timeframeEndTime = 0;
			int fromIndex = 0;
			int toIndex = -1;

			// find the starting value
			for (int i = 0; i < defaultDataList.size(); i++) {
				Date dataDate = defaultDataList.get(i).getTimeStamp();

				// find first data object which lies in the specified time range
				if ((dataDate.equals(from) || dataDate.after(from)) && dataDate.before(to)) {
					fromIndex = i;
					timeframeStartTime = dataDate.getTime() - (timeframe / 2);
					timeframeEndTime = dataDate.getTime() + (timeframe / 2);

					if (i - 1 >= 0) {
						// we add a data object so that the drawn graph does not
						// start with the first drawn object, but the line will
						// go
						// out of the graph.
						if (i - 1 > 0) {
							// this data object is not the first of the list,
							// thus
							// we add the very first data object to the result
							// list
							// because of the auto range of jfreechart.
							// Otherwise
							// the graph would not scale correctly.
							resultList.add(defaultDataList.get(0));
						}
						resultList.add(defaultDataList.get(i - 1));
					}

					break;
				}
			}

			IDataAggregator dataAggregator = DataAggregatorFactory.getDataAggregator(defaultDataList);
			// iterate over time frames
			while (timeframeStartTime < to.getTime() + timeframe) {
				long averageTime = (timeframeStartTime + timeframeEndTime) / 2;

				for (int i = fromIndex; i < defaultDataList.size(); i++) {
					long dataTime = defaultDataList.get(i).getTimeStamp().getTime();

					if (dataTime > timeframeEndTime) {
						// if the actual data object is not anymore in the
						// actual
						// time frame, then the last data object was
						toIndex = i - 1;
						break;
					} else if (i + 1 == defaultDataList.size()) {
						// if end of list is reached then toIndex is the end of
						// the
						// list
						toIndex = i;
					}
				}

				// aggregate data objects only when toIndex changed
				if ((toIndex >= 0) && (fromIndex <= toIndex)) {
					// aggregate data and set the average time stamp
					DefaultData resultData = dataAggregator.aggregateData(defaultDataList, fromIndex, toIndex);
					resultData.setTimeStamp(new Timestamp(averageTime));
					resultList.add(resultData);

					// set the fromIndex on the actual data object
					fromIndex = toIndex + 1;
				}

				// adjust timeframe
				timeframeStartTime = timeframeEndTime;
				timeframeEndTime += timeframe;
				// reset the toIndex
				toIndex = -1;
			}

			if (0 != fromIndex) {
				// we try to append an object at the right non-visible part of
				// the
				// graph so that a line is drawn.
				if (defaultDataList.size() > fromIndex) {
					resultList.add(defaultDataList.get(fromIndex));
				}
				if (defaultDataList.size() > fromIndex + 1) {
					// there are some objects untouched, thus we need to add the
					// very last data object to the result list for the auto
					// scaling
					// of jfreechart to work.
					resultList.add(defaultDataList.get(defaultDataList.size() - 1));
				}
			}

			return resultList;
		}
	};

}
