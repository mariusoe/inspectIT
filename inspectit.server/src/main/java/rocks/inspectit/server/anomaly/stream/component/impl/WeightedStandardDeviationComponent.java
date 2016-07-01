/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.anomaly.stream.ConfidenceBand;
import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.object.StreamContext;
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

	private final Map<String, LinkedList<ResidualContainer>> residualHistoryMap = new HashMap<>();

	private Queue<StreamObject<InvocationSequenceData>> intervalQueue = new ConcurrentLinkedQueue<StreamObject<InvocationSequenceData>>();

	private final double weightFactor = 1;

	@Value("${anomaly.settings.confidenceBandUpdateInterval}")
	private long updateInterval;

	@Autowired
	private SharedStreamProperties streamProperties;

	/**
	 * {@link ExecutorService} for sending keep alive messages.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService executorService;

	public void start() {
		executorService.scheduleAtFixedRate(this, updateInterval, updateInterval, TimeUnit.SECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> item) {
		intervalQueue.add(item);

		return EFlowControl.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		Queue<StreamObject<InvocationSequenceData>> queue = intervalQueue;
		intervalQueue = new ConcurrentLinkedQueue<StreamObject<InvocationSequenceData>>();

		Map<String, ArrayList<StreamObject<InvocationSequenceData>>> invocationStreamMap = new HashMap<>();

		for (StreamObject<InvocationSequenceData> iso : queue) {
			if (!invocationStreamMap.containsKey(iso.getContext().getBusinessTransaction())) {
				invocationStreamMap.put(iso.getContext().getBusinessTransaction(), new ArrayList<StreamObject<InvocationSequenceData>>());
			}

			invocationStreamMap.get(iso.getContext().getBusinessTransaction()).add(iso);
		}

		for (Entry<String, ArrayList<StreamObject<InvocationSequenceData>>> entry : invocationStreamMap.entrySet()) {
			calculateStandardDeviation(entry.getKey(), entry.getValue());
		}
	}

	private void calculateStandardDeviation(String businessTransaction, ArrayList<StreamObject<InvocationSequenceData>> items) {
		StreamContext streamContext = streamProperties.getStreamContext(businessTransaction);

		ConfidenceBand confidenceBand = streamContext.getConfidenceBand();

		if (confidenceBand != null) {
			ResidualContainer container = new ResidualContainer();

			// TODO: 0 check
			double confidenceSize = confidenceBand.getWidth() / 2;

			for (StreamObject<InvocationSequenceData> iso : items) {
				double pError;
				if (confidenceSize <= 0) {
					pError = 0;
				} else {
					pError = confidenceBand.distanceToBand(iso.getData().getDuration()) / confidenceSize;
				}
				double weight = Math.exp(-pError * weightFactor);

				container.weightSum += weight;
				container.valueSum += weight * Math.pow(iso.getData().getDuration() - confidenceBand.getMean(), 2);
			}

			if (!residualHistoryMap.containsKey(businessTransaction)) {
				residualHistoryMap.put(businessTransaction, new LinkedList<ResidualContainer>());
			}
			LinkedList<ResidualContainer> residualContainerList = residualHistoryMap.get(businessTransaction);

			residualContainerList.add(container);
			if (residualContainerList.size() > historyLimit) {
				residualContainerList.removeFirst();
			}

			// calculate stddev
			double valSum = 0D;
			double weightSum = 0D;

			for (ResidualContainer rContainer : residualContainerList) {
				valSum += rContainer.valueSum;
				weightSum += rContainer.weightSum;
			}

			double variance = valSum / weightSum;
			double standardDeviation = Math.sqrt(variance);

			streamContext.setStandardDeviation(standardDeviation);
		}

	}

	private class ResidualContainer {
		double valueSum = 0D;

		double weightSum = 0D;

		int weightCount = 0;
	}
}
