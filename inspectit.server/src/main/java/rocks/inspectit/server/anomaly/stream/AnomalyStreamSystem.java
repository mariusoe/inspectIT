/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lmax.disruptor.dsl.Disruptor;

import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.ItemRateComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.PercentageRateComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.QuadraticScoreFilterComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.RHoltWintersComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.StandardDeviationComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.TSDBWriterComponent;
import rocks.inspectit.server.anomaly.stream.disruptor.InvocationSequenceEventFactory;
import rocks.inspectit.server.anomaly.stream.disruptor.InvocationSequenceEventHandler;
import rocks.inspectit.server.anomaly.stream.disruptor.events.InvocationSequenceEvent;
import rocks.inspectit.server.anomaly.stream.sink.IDataSink;
import rocks.inspectit.server.anomaly.stream.sink.InvocationSequenceSink;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyStreamSystem implements InitializingBean {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * {@link ExecutorService} for sending keep alive messages.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	ScheduledExecutorService executorService;

	@Autowired
	InfluxDBService influx;

	/**
	 *
	 */
	private IDataSink<InvocationSequenceData> invocationSequenceSink;

	private Disruptor<InvocationSequenceEvent> disruptor;

	private final int BUFFER_SIZE = 1024;

	private ISingleInputComponent<InvocationSequenceData> streamEntryComponent;

	private void initDisruptor() {
		InvocationSequenceEventFactory eventFactory = new InvocationSequenceEventFactory();

		disruptor = new Disruptor<>(eventFactory, BUFFER_SIZE, Executors.defaultThreadFactory());

		InvocationSequenceEventHandler eventHandler = new InvocationSequenceEventHandler(streamEntryComponent);
		disruptor.handleEventsWith(eventHandler);

		// Start the Disruptor, starts all threads running
		disruptor.start();

		invocationSequenceSink = new InvocationSequenceSink(disruptor.getRingBuffer());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("|-Initializing AnomalyStreamSystem...");

		SharedStreamProperties.setInfluxService(influx);

		// # # # # # # # # # # # # # # # # # # # # #
		// # init stream
		TSDBWriterComponent problemWriterComponent = new TSDBWriterComponent(null, "problem");
		TSDBWriterComponent normalWriterComponent = new TSDBWriterComponent(null, "normal");

		RHoltWintersComponent wintersComponent = new RHoltWintersComponent(normalWriterComponent, executorService);

		// ConfidenceBandComponent bandComponent = new ConfidenceBandComponent(wintersComponent,
		// 10000, executorService);
		StandardDeviationComponent stddevComponent = new StandardDeviationComponent(wintersComponent, executorService);

		PercentageRateComponent pErrorRateComponent = new PercentageRateComponent(stddevComponent, problemWriterComponent, executorService);
		QuadraticScoreFilterComponent scoreFilterComponent = new QuadraticScoreFilterComponent(pErrorRateComponent);

		ItemRateComponent rateComponent = new ItemRateComponent(scoreFilterComponent, "total throughput");

		streamEntryComponent = rateComponent;
		// #
		// # # # # # # # # # # # # # # # # # # # # #

		log.info("||-Created following AnomalyStream:");
		streamEntryComponent.print("|| ", true);

		initDisruptor();

		if (log.isInfoEnabled()) {
			log.info("|-AnomalyStreamSystem initialized...");

		}
	}

	public void process(InvocationSequenceData data) {
		invocationSequenceSink.process(data);
	}
}
