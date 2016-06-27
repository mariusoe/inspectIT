/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import rocks.inspectit.server.anomaly.stream.SwapCache;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class WeightedStandardDeviationComponent extends AbstractSingleStreamComponent<InvocationSequenceData> implements Runnable {

	/**
	 *
	 */
	private static final int historyLimit = 720;

	private final SwapCache swapCache;

	private final LinkedList<ResidualContainer> residualHistory;

	/**
	 * @param nextComponent
	 */
	public WeightedStandardDeviationComponent(ISingleInputComponent<InvocationSequenceData> nextComponent, ScheduledExecutorService executorService) {
		super(nextComponent);

		swapCache = new SwapCache(10000);
		residualHistory = new LinkedList<ResidualContainer>();

		executorService.scheduleAtFixedRate(this, 5, 5, TimeUnit.SECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> item) {
		swapCache.push(item.getData().getDuration());

		return EFlowControl.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		// try {
		// swapCache.swap();
		//
		// InternalData data = swapCache.getInactive();
		//
		// if (SharedStreamProperties.getConfidenceBand() != null) {
		// double mean = SharedStreamProperties.getConfidenceBand().getMean();
		//
		// if (!Double.isNaN(mean)) {
		// // ResidualContainer container = new ResidualContainer();
		// // residualHistory.add(container);
		// // if (residualHistory.size() > historyLimit) {
		// // residualHistory.removeFirst();
		// // }
		// ResidualContainer container = new ResidualContainer();
		//
		// for (int i = 0; i < data.getIndex().get(); i++) {
		// double pError = Math.abs(1D / mean * data.getData()[i] - 1D);
		// double weight = Math.exp(-pError);
		//
		// container.weightCount++;
		// container.weightSum += weight;
		// container.valueSum += weight * Math.pow(data.getData()[i] - mean, 2);
		// }
		//
		// residualHistory.add(container);
		// if (residualHistory.size() > historyLimit) {
		// residualHistory.removeFirst();
		// }
		//
		// // calculate stddev
		// double vSum = 0D;
		// double wSum = 0D;
		// int wCount = 0;
		// for (ResidualContainer rContainer : residualHistory) {
		// vSum += rContainer.valueSum;
		// wSum += rContainer.weightSum;
		// wCount += rContainer.weightCount;
		// }
		//
		// double variance = vSum / ((wCount - 1D) / wCount * wSum);
		// double stdDeviation = Math.sqrt(variance);
		//
		// SharedStreamProperties.getInfluxService().insert(Point.measurement("status").addField("weightedStddev",
		// stdDeviation).build());
		// }
		// }
		//
		// data.reset();
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	private class ResidualContainer {
		double valueSum = 0D;

		double weightSum = 0D;

		int weightCount = 0;
	}
}
