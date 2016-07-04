/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

import java.util.concurrent.Executors;

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
import rocks.inspectit.server.anomaly.stream.component.impl.BusinessTransactionContextInjectorComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.ConfidenceBandComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.ForecastComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.PercentageRateComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.QuadraticScoreFilterComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.StandardDeviationComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.TSDBWriterComponent;
import rocks.inspectit.server.anomaly.stream.disruptor.InvocationSequenceEventFactory;
import rocks.inspectit.server.anomaly.stream.disruptor.InvocationSequenceEventHandler;
import rocks.inspectit.server.anomaly.stream.disruptor.events.InvocationSequenceEvent;
import rocks.inspectit.server.anomaly.stream.sink.IDataSink;
import rocks.inspectit.server.anomaly.stream.sink.InvocationSequenceSink;
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
		// tsdb writer for normal requests
		TSDBWriterComponent normalWriter = streamComponentFactory.createTSDBWriter();
		normalWriter.setDataTypeTag("normal");

		// tsdbwriter for slow request
		TSDBWriterComponent problemWriter = streamComponentFactory.createTSDBWriter();
		problemWriter.setDataTypeTag("problem");

		// calculating the confidence band
		ConfidenceBandComponent confidenceBand = streamComponentFactory.createConfidenceBand();
		confidenceBand.setNextComponent(normalWriter);
		confidenceBand.start();

		// cb calculation using R and HoltWinters method
		// RHoltWintersComponent holtWinters = streamComponentFactory.createRHoltWinters();
		// holtWinters.setNextComponent(confidenceBand);
		// holtWinters.start();

		ForecastComponent forecastComponent = streamComponentFactory.createForecastComponent();
		forecastComponent.setNextComponent(confidenceBand);
		forecastComponent.start();

		// weighted standard deviation calculation
		// WeightedStandardDeviationComponent standardDeviation =
		// streamComponentFactory.createWeightedStandardDeviation();
		// standardDeviation.setNextComponent(holtWinters);
		// standardDeviation.start();

		// standard deviation calculation
		StandardDeviationComponent standardDeviation = streamComponentFactory.createStandardDeviation();
		standardDeviation.setNextComponent(forecastComponent);
		standardDeviation.start();

		// alerting
		BusinessTransactionAlertingComponent businessTransactionAlerting = streamComponentFactory.createBusinessTransactionAlerting();

		// calculating slow request rate
		PercentageRateComponent percentageRate = streamComponentFactory.createPercentageRate();
		percentageRate.setNextComponentOne(standardDeviation);
		percentageRate.setNextComponentTwo(problemWriter);
		percentageRate.setTriggerComponent(businessTransactionAlerting);
		percentageRate.start();

		// determine whether data is normal or slow
		QuadraticScoreFilterComponent quadraticScoreFilter = streamComponentFactory.createQuadraticScoreFilter();
		quadraticScoreFilter.setNextComponent(percentageRate);

		// business transaction injection
		BusinessTransactionContextInjectorComponent businessTransactionInjector = streamComponentFactory.createBusinessTransactionInjector();
		businessTransactionInjector.setNextComponent(quadraticScoreFilter);

		// warmup filter
		// WarmUpFilterComponent warmUpFilter = streamComponentFactory.createWarmUpFilter();
		// warmUpFilter.setNextComponent(businessTransactionInjector);

		// set entry point
		return businessTransactionInjector;
	}

	public void process(InvocationSequenceData data) {
		invocationSequenceSink.process(data);
	}
}
