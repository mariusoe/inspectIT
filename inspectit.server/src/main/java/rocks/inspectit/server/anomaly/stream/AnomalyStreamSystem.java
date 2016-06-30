/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Resource;

import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lmax.disruptor.dsl.Disruptor;

import rocks.inspectit.server.alearting.AlertingAdapterFactory;
import rocks.inspectit.server.alearting.adapter.IAlertAdapter;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.BusinessTransactionAlertingComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.BusinessTransactionInjectorComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.ItemRateComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.PercentageRateComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.QuadraticScoreFilterComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.RHoltWintersComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.StandardDeviationComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.TSDBWriterComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.WarmUpFilterComponent;
import rocks.inspectit.server.anomaly.stream.disruptor.InvocationSequenceEventFactory;
import rocks.inspectit.server.anomaly.stream.disruptor.InvocationSequenceEventHandler;
import rocks.inspectit.server.anomaly.stream.disruptor.events.InvocationSequenceEvent;
import rocks.inspectit.server.anomaly.stream.sink.IDataSink;
import rocks.inspectit.server.anomaly.stream.sink.InvocationSequenceSink;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.IBusinessContextManagementService;

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

	@Autowired
	IBusinessContextManagementService businessService;

	@Value("${anomaly.isActive}")
	boolean anomalyDectectionIsActive;

	@Autowired
	AlertingAdapterFactory alertingAdapterFactory;

	@Autowired
	StreamComponentFactory streamComponentFactory;

	/**
	 *
	 */
	private IDataSink<InvocationSequenceData> invocationSequenceSink;

	private Disruptor<InvocationSequenceEvent> disruptor;

	private final int BUFFER_SIZE = 1024;

	private ISingleInputComponent<InvocationSequenceData> streamEntryComponent;

	@SuppressWarnings("unchecked")
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
		if (anomalyDectectionIsActive) {
			log.info("|-Initializing AnomalyStreamSystem...");

			IAlertAdapter gitterAdapter = alertingAdapterFactory.getGitterAdapter();
			IAlertAdapter emailAdapter = alertingAdapterFactory.getEmailAdapter();

			gitterAdapter.sendMessage("===================================");
			gitterAdapter.sendMessage("AnomalyDetection has been started");
			gitterAdapter.sendMessage("===================================");

			SharedStreamProperties.setInfluxService(influx);
			SharedStreamProperties.setAlertingComponent(gitterAdapter);

			// init stream
			streamEntryComponent = initStream();

			log.info("||-Created following AnomalyStream:");
			streamEntryComponent.print("|| ", true);

			initDisruptor();

			if (log.isInfoEnabled()) {
				log.info("|-AnomalyStreamSystem initialized...");
			}
		} else {
			log.info("|-AnomalyDetection is not enabled");
		}
	}

	private ISingleInputComponent<InvocationSequenceData> initStream() {
		TSDBWriterComponent problemWriterComponent = new TSDBWriterComponent(null, "problem");
		TSDBWriterComponent normalWriterComponent = new TSDBWriterComponent(null, "normal");

		RHoltWintersComponent wintersComponent;
		try {
			wintersComponent = new RHoltWintersComponent(normalWriterComponent, executorService);
		} catch (RserveException e) {
			return null;
		}

		// ConfidenceBandComponent bandComponent = new ConfidenceBandComponent(wintersComponent,
		// 10000, executorService);
		StandardDeviationComponent stddevComponent = new StandardDeviationComponent(wintersComponent, executorService);

		BusinessTransactionAlertingComponent alertingComponent = new BusinessTransactionAlertingComponent(null);

		PercentageRateComponent pErrorRateComponent = new PercentageRateComponent(stddevComponent, problemWriterComponent, alertingComponent, executorService);

		QuadraticScoreFilterComponent scoreFilterComponent = new QuadraticScoreFilterComponent(pErrorRateComponent);

		ItemRateComponent rateComponent = new ItemRateComponent(scoreFilterComponent, "total throughput");

		BusinessTransactionInjectorComponent businessTransactionInjector = new BusinessTransactionInjectorComponent(rateComponent, businessService);

		WarmUpFilterComponent warmUpFilter = new WarmUpFilterComponent(businessTransactionInjector);

		return warmUpFilter;
	}

	public void process(InvocationSequenceData data) {
		invocationSequenceSink.process(data);
	}
}
