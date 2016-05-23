/**
 *
 */
package rocks.inspectit.server.anomaly.utils.processor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rocks.inspectit.server.anomaly.utils.StatisticUtils;
import rocks.inspectit.server.anomaly.utils.processor.DoubleData;
import rocks.inspectit.server.anomaly.utils.processor.IStatisticProcessor;

/**
 * Implementation of a moving average.
 *
 * @author Marius Oehler
 *
 */
public class MovingAverage implements IStatisticProcessor {

	/**
	 * The window size (time span in milliseconds).
	 */
	private final long windowSize;

	/**
	 * The data of the window.
	 */
	private final List<DoubleData> dataList;

	/**
	 * Monitor to prevent parallel modifications of the {@link #dataList}.
	 */
	private final Object modificationMonitor = new Object();

	/**
	 * Constructor.
	 *
	 * @param windowSize
	 *            the window size to use
	 * @param timeUnit
	 *            the time unit of the window size
	 */
	public MovingAverage(long windowSize, TimeUnit timeUnit) {
		this.windowSize = timeUnit.toMillis(windowSize);

		dataList = new ArrayList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void push(long currentTime, double data) {
		dataList.add(new DoubleData(data, currentTime));
		clean(currentTime);
	}

	/**
	 * Removes expired data from the window.
	 *
	 * @param currentTime
	 *            the current time
	 */
	private void clean(long currentTime) {
		long timeBorder = currentTime - windowSize;
		while (!dataList.isEmpty() && dataList.get(0).getTime() < timeBorder) {
			synchronized (modificationMonitor) {
				dataList.remove(0);
			}
		}
	}

	/**
	 * Returns the window data.
	 *
	 * @return List containing all data of the window.
	 */
	public List<DoubleData> getData() {
		List<DoubleData> list = new ArrayList<>();
		synchronized (modificationMonitor) {
			list.addAll(dataList);
		}
		return list;
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public double getValue() {
		return StatisticUtils.mean(getData());
	}
}
