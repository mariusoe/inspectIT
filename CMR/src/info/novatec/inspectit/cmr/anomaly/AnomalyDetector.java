package info.novatec.inspectit.cmr.anomaly;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import info.novatec.inspectit.cmr.anomaly.strategy.impl.SimpleStrategy;
import info.novatec.inspectit.cmr.influxdb.InfluxDBService;
import info.novatec.inspectit.cmr.util.Converter;
import info.novatec.inspectit.spring.logger.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	 * The rate of the execution of the anomaly detector.
	 */
	@Value("${anomaly.analyzeRate}")
	private long analyzeRate;

	/**
	 * Instance of the {@link InfluxDBService}.
	 */
	@Autowired
	InfluxDBService influxDb;

	@Override
	public void afterPropertiesSet() throws Exception {
		executorService.scheduleAtFixedRate(this, analyzeRate, analyzeRate, TimeUnit.MILLISECONDS);

		detectionStrategy = new SimpleStrategy(influxDb);

		if (log.isInfoEnabled()) {
			log.info("|-Anomaly Detector active...");
			log.info("||-Anomaly Detector will be executed in a rate of {}ms...", analyzeRate);
		}
	}

	@Override
	public void run() {
		if (log.isInfoEnabled()) {
			log.info("Run anomaly detection. Detection strategy: {}", detectionStrategy.getStrategyName());
		}

		long startTime = System.nanoTime();

		// try to prevent a crash of the executor service
		try {
			detectionStrategy.execute();
		} catch (Exception e) {
			log.error("Exception during anomaly detection!", e);
		}

		long totalDuration = System.nanoTime() - startTime;

		if (log.isInfoEnabled()) {
			log.info("Anomaly detection took " + Converter.nanoToMilliseconds(totalDuration) + " ms");
		}
	}
}
