/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.SwapCache;
import rocks.inspectit.server.anomaly.stream.SwapCache.InternalData;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.utils.StatisticUtils;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class StandardDeviationComponent extends AbstractSingleStreamComponent<InvocationSequenceData> implements Runnable {

	/**
	 *
	 */
	private static final int historyLimit = 720;

	private final SwapCache swapCache;

	private final LinkedList<ResidualContainer> residualHistory;

	/**
	 * @param nextComponent
	 */
	public StandardDeviationComponent(ISingleInputComponent<InvocationSequenceData> nextComponent, ScheduledExecutorService executorService) {
		super(nextComponent);

		swapCache = new SwapCache(10000);
		residualHistory = new LinkedList<ResidualContainer>();

		executorService.scheduleAtFixedRate(this, 5, 5, TimeUnit.SECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(InvocationSequenceData item) {
		swapCache.push(item.getDuration());

		return EFlowControl.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		swapCache.swap();

		InternalData data = swapCache.getInactive();
		double mean = StatisticUtils.mean(data.getData(), data.getIndex().get());

		// calculate residuals of recent data
		ResidualContainer container = new ResidualContainer();
		for (int i = 0; i < data.getIndex().get(); i++) {
			container.sum += Math.pow(data.getData()[i] - mean, 2);
		}
		container.count = data.getIndex().get();
		data.reset();

		residualHistory.add(container);
		if (residualHistory.size() > historyLimit) {
			residualHistory.removeFirst();
		}

		// calculate stddev
		double sum = 0D;
		int count = 0;
		for (ResidualContainer rContainer : residualHistory) {
			sum += rContainer.sum;
			count += rContainer.count;
		}

		double variance = sum / count;

		double stdDeviation = Math.sqrt(variance);

		SharedStreamProperties.setStandardDeviation(stdDeviation);
	}

	private class ResidualContainer {
		double sum = 0D;

		int count = 0;
	}
}
