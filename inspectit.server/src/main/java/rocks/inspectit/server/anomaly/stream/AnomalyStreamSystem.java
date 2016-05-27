/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.stream.comp.AbstractResultProcessor;
import rocks.inspectit.server.anomaly.stream.comp.AbstractStreamProcessor;
import rocks.inspectit.server.anomaly.stream.comp.impl.BaselineStreamProcessor;
import rocks.inspectit.server.anomaly.stream.comp.impl.ErrorRateCalculator;
import rocks.inspectit.server.anomaly.stream.comp.impl.TSDBWriter;
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
	private AbstractStreamProcessor<InvocationSequenceData> streamProcessor;

	/**
	 *
	 */
	private AbstractResultProcessor<InvocationSequenceData> resultProcessor;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		resultProcessor = new TSDBWriter(influx);
		resultProcessor.setNextProcessor(new ErrorRateCalculator(influx));

		streamProcessor = new BaselineStreamProcessor(influx, 10000, resultProcessor, executorService);

		if (log.isInfoEnabled()) {
			log.info("|-AnomalyStreamSystem initialized...");
		}
	}

	public AbstractStreamProcessor<InvocationSequenceData> getStream() {
		return streamProcessor;
	}
}
