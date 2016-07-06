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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.object.StreamContext;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.server.anomaly.utils.StatisticUtils;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class StandardDeviationComponent extends AbstractSingleStreamComponent<InvocationSequenceData> implements Runnable {

	@Value("${anomaly.settings.confidenceBandHistorySize}")
	private int historyLimit;

	private final Map<String, LinkedList<ResidualContainer>> residualHistoryMap = new HashMap<>();

	private Queue<StreamObject<InvocationSequenceData>> intervalQueue = new ConcurrentLinkedQueue<StreamObject<InvocationSequenceData>>();

	@Value("${anomaly.settings.confidenceBandUpdateInterval}")
	private long updateInterval;

	/**
	 * {@link ExecutorService} for sending keep alive messages.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService executorService;

	@Autowired
	private SharedStreamProperties streamProperties;

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

	private void calculateStandardDeviation(String businessTransaction, List<StreamObject<InvocationSequenceData>> invocationList) {
		StreamContext context = streamProperties.getStreamContext(businessTransaction);

		if (!context.isAnomalyActive()) {
			// only active if an anomaly is active
			return;
		}

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

		context.setStandardDeviation(stdDeviation);
	}

	private class ResidualContainer {
		double sum = 0D;

		int count = 0;
	}
}
