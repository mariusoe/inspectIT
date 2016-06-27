/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.object.InvocationStreamObject;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
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

	private final Map<String, LinkedList<ResidualContainer>> residualHistoryMap;

	private Queue<InvocationStreamObject> intervalQueue;

	/**
	 * @param nextComponent
	 */
	public StandardDeviationComponent(ISingleInputComponent<InvocationSequenceData> nextComponent, ScheduledExecutorService executorService) {
		super(nextComponent);

		residualHistoryMap = new HashMap<>();
		intervalQueue = new ConcurrentLinkedQueue<InvocationStreamObject>();

		executorService.scheduleAtFixedRate(this, 5, 5, TimeUnit.SECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> item) {
		intervalQueue.add((InvocationStreamObject) item);

		return EFlowControl.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		Queue<InvocationStreamObject> queue = intervalQueue;
		intervalQueue = new ConcurrentLinkedQueue<InvocationStreamObject>();

		Map<String, ArrayList<InvocationStreamObject>> invocationStreamMap = new HashMap<>();

		for (InvocationStreamObject iso : queue) {
			if (!invocationStreamMap.containsKey(iso.getBusinessTransaction())) {
				invocationStreamMap.put(iso.getBusinessTransaction(), new ArrayList<InvocationStreamObject>());
			}

			invocationStreamMap.get(iso.getBusinessTransaction()).add(iso);
		}

		for (Entry<String, ArrayList<InvocationStreamObject>> entry : invocationStreamMap.entrySet()) {
			calculateStandardDeviation(entry.getKey(), entry.getValue());
		}
	}

	private void calculateStandardDeviation(String businessTransaction, List<InvocationStreamObject> invocationList) {
		double[] durationArray = new double[invocationList.size()];

		for (int i = 0; i < invocationList.size(); i++) {
			durationArray[i] = invocationList.get(i).getData().getDuration();
		}

		double mean = StatisticUtils.mean(durationArray);

		// calculate residuals of recent data
		ResidualContainer container = new ResidualContainer();
		for (double duration : durationArray) {
			container.sum += Math.pow(duration - mean, 2);
		}
		container.count = durationArray.length;

		if (!residualHistoryMap.containsKey(businessTransaction)) {
			residualHistoryMap.put(businessTransaction, new LinkedList<ResidualContainer>());
		}
		LinkedList<ResidualContainer> residualList = residualHistoryMap.get(businessTransaction);

		residualList.add(container);
		if (residualList.size() > historyLimit) {
			residualList.removeFirst();
		}

		// calculate stddev
		double sum = 0D;
		int count = 0;
		for (ResidualContainer rContainer : residualList) {
			sum += rContainer.sum;
			count += rContainer.count;
		}

		double variance = sum / count;

		double stdDeviation = Math.sqrt(variance);

		SharedStreamProperties.getStreamStatistic(businessTransaction).setStandardDeviation(stdDeviation);
	}

	private class ResidualContainer {
		double sum = 0D;

		int count = 0;
	}
}
