/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.anomaly.stream.component.AbstractDoubleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.object.HealthTag;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.util.Pair;

/**
 * @author Marius Oehler
 *
 */
public class PercentageRateComponent extends AbstractDoubleStreamComponent<InvocationSequenceData> implements Runnable {

	private final Map<String, CounterPair> counterMap = new HashMap<>();

	@Value("${anomaly.settings.errorRateWindowSize}")
	private long interval;

	private ISingleInputComponent<Pair<String, Double>> triggerComponent;

	@Autowired
	private InfluxDBService influxService;

	/**
	 * {@link ExecutorService} for sending keep alive messages.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService executorService;

	public void start() {
		executorService.scheduleAtFixedRate(this, interval, interval, TimeUnit.SECONDS);
	}

	/**
	 * Sets {@link #triggerComponent}.
	 *
	 * @param triggerComponent
	 *            New value for {@link #triggerComponent}
	 */
	public void setTriggerComponent(ISingleInputComponent<Pair<String, Double>> triggerComponent) {
		this.triggerComponent = triggerComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processOneImpl(StreamObject<InvocationSequenceData> streamObject) {
		if (!counterMap.containsKey(streamObject.getContext().getBusinessTransaction())) {
			counterMap.put(streamObject.getContext().getBusinessTransaction(), new CounterPair());
		}

		counterMap.get(streamObject.getContext().getBusinessTransaction()).totalCounter.incrementAndGet();

		streamObject.setHealthTag(HealthTag.OK);

		return EFlowControl.CONTINUE_ONE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processOneTwo(StreamObject<InvocationSequenceData> streamObject) {
		if (!counterMap.containsKey(streamObject.getContext().getBusinessTransaction())) {
			counterMap.put(streamObject.getContext().getBusinessTransaction(), new CounterPair());
		}

		counterMap.get(streamObject.getContext().getBusinessTransaction()).totalCounter.incrementAndGet();
		counterMap.get(streamObject.getContext().getBusinessTransaction()).errorCounter.incrementAndGet();

		if (streamObject.getData().getDuration() < streamObject.getContext().getCurrentMean()) {
			streamObject.setHealthTag(HealthTag.FAST);
		} else {
			streamObject.setHealthTag(HealthTag.SLOW);
		}

		return EFlowControl.CONTINUE_TWO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		for (String businessTransaction : counterMap.keySet()) {

			CounterPair counterPair = counterMap.get(businessTransaction);
			counterMap.put(businessTransaction, new CounterPair());

			double rate = 0;
			if (counterPair.totalCounter.get() > 0) {
				rate = 1D / counterPair.totalCounter.get() * counterPair.errorCounter.get();
			}

			Builder builder = Point.measurement("statistics");
			builder.addField("errorRate", rate);
			builder.tag("buisnessTransaction", businessTransaction);

			influxService.insert(builder.build());

			StreamObject<Pair<String, Double>> object = new StreamObject<Pair<String, Double>>(new Pair<String, Double>(businessTransaction, rate));
			triggerComponent.process(object);
		}
	}

	class CounterPair {
		AtomicLong totalCounter = new AtomicLong(0L);
		AtomicLong errorCounter = new AtomicLong(0L);
	}
}
