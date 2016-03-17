/**
 *
 */
package info.novatec.inspectit.cmr.anomaly;

import info.novatec.inspectit.cmr.tsdb.InfluxDBService;
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
 * This class functions as trigger to regularly start the anomaly detector.
 *
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyScheduler implements InitializingBean, Runnable {

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
	 * Instance of the {@link InfluxDBService}.
	 */
	@Autowired
	InfluxDBService influxDb;

	/**
	 * The rate of the execution of the anomaly detector.
	 */
	@Value("${anomaly.analyzeRate}")
	private long analyzeRate;

	/**
	 * The implemented detection strategies.
	 */
	@Value("${anomaly.detectionStrategies}")
	private String detectionStrategies;

	/**
	 * The anomaly detector.
	 */
	private AnomalyDetector anomalyDetector;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		executorService.scheduleAtFixedRate(this, analyzeRate, analyzeRate, TimeUnit.MILLISECONDS);

		anomalyDetector = new AnomalyDetector(log, influxDb, detectionStrategies);

		if (log.isInfoEnabled()) {
			log.info("|-AnomalyScheduler active...");
			log.info("||-Anomaly Detector will be triggered in a rate of {}ms...", analyzeRate);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		if (log.isInfoEnabled()) {
			log.info("Triggering anomaly detector.");
		}

		// try to prevent a crash of the executor service
		try {
			anomalyDetector.execute(System.currentTimeMillis());
		} catch (Exception e) {
			log.error("Exception during anomaly detection!", e);
		}
	}

}
