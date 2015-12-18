package info.novatec.inspectit.cmr.anomaly;

import info.novatec.inspectit.cmr.anomaly.strategy.DummyStrategy;
import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.influxdb.InfluxDbService;
import info.novatec.inspectit.cmr.util.Converter;
import info.novatec.inspectit.spring.logger.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyDetector implements InitializingBean, Runnable {

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

	/**
	 * The used {@link AbstractAnomalyDetectionStrategy}.
	 */
	private AbstractAnomalyDetectionStrategy detectionStrategy;

	/**
	 * Instance of the {@link InfluxDbService}.
	 */
	@Autowired
	InfluxDbService influxDb;

	@Override
	public void afterPropertiesSet() throws Exception {
		executorService.scheduleAtFixedRate(this, 5000, 5000, TimeUnit.MILLISECONDS);

		detectionStrategy = new DummyStrategy(influxDb);

		if (log.isInfoEnabled()) {
			log.info("|-Anomaly Detector active...");
		}
	}

	@Override
	public void run() {
		if (log.isInfoEnabled()) {
			log.info("Run anomaly detection...");
		}
		long startTime = System.nanoTime();

		try {
			detectionStrategy.detect();
		} catch (Exception e) {
			log.error("Exception during anomaly detection!", e);
		}

		long totalDuration = System.nanoTime() - startTime;
		if (log.isInfoEnabled()) {
			log.info("Anomaly detection took " + Converter.nanoToMilliseconds(totalDuration) + " ms");
		}
	}
}
