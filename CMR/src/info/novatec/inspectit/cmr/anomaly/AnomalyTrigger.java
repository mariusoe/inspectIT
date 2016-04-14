/**
 *
 */
package info.novatec.inspectit.cmr.anomaly;

import info.novatec.inspectit.cmr.anomaly.strategy.AbstractAnomalyDetectionStrategy;
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
public class AnomalyTrigger implements InitializingBean, Runnable {

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
	 * The implemented detection strategy classes.
	 */
	@Value("${anomaly.detectionStrategies}")
	private String[] detectionStrategies;

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

		AbstractAnomalyDetectionStrategy[] strategies = new AbstractAnomalyDetectionStrategy[detectionStrategies.length];

		// instanziate detection strategies
		for (int i = 0; i < detectionStrategies.length; i++) {
			try {
				AbstractAnomalyDetectionStrategy strategy = (AbstractAnomalyDetectionStrategy) Class.forName(detectionStrategies[i]).newInstance();
				strategy.initialization(influxDb);
				strategies[i] = strategy;
			} catch (ClassNotFoundException e) {
				if (log.isWarnEnabled()) {
					log.error(String.format("The detection strategy class '{}' was not found!", detectionStrategies[i]));
				}
			} catch (InstantiationException | IllegalAccessException e) {
				if (log.isWarnEnabled()) {
					log.error(String.format("The detection strategy '{}' could not be instantiated!", detectionStrategies[i]));
				}
			}
		}

		// anomaly detector
		anomalyDetector = new AnomalyDetector(strategies);

		if (log.isInfoEnabled()) {
			log.info("|-AnomalyScheduler active...");
			log.info("||-Anomaly Detector will be triggered in a rate of {}ms...", analyzeRate);
		}
	}

	private boolean test = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		if (false) {
			log.info("<<TEST>> Triggering anomaly detector.");

			if (influxDb.isBatching()) {
				influxDb.disableBatching();
			}

			long time = System.currentTimeMillis() - 3600 * 1000;
			time -= time % 5000;

			while (time < System.currentTimeMillis() - 5000) {
				anomalyDetector.execute(time);
				time += 5000;
			}

			test = false;
		} else {
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

}
