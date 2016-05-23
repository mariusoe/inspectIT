/**
 *
 */
package rocks.inspectit.server.anomaly;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.strategy.AbstractAnomalyDetectionStrategy;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.spring.logger.Log;

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
		if (log.isInfoEnabled()) {
			log.info("|-Initializing AnomalyTrigger:");
		}

		executorService.scheduleAtFixedRate(this, analyzeRate, analyzeRate, TimeUnit.MILLISECONDS);

		List<AbstractAnomalyDetectionStrategy> strategyList = new ArrayList<>();

		// instanziate detection strategies
		for (String strategyClass : detectionStrategies) {
			if (StringUtils.isEmpty(strategyClass)) {
				continue;
			}

			try {
				AbstractAnomalyDetectionStrategy strategy = (AbstractAnomalyDetectionStrategy) Class.forName(strategyClass).newInstance();
				strategy.initialization(influxDb);
				strategyList.add(strategy);
			} catch (ClassNotFoundException e) {
				if (log.isWarnEnabled()) {
					log.error("||-The detection strategy class '{}' was not found!", strategyClass);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				if (log.isWarnEnabled()) {
					log.error("||-The detection strategy '{}' could not be instantiated!", strategyClass);
				}
			}
		}

		// anomaly detector
		anomalyDetector = new AnomalyDetector(strategyList);

		if (log.isInfoEnabled()) {
			log.info("||-AnomalyTrigger active...");
			log.info("|||-Anomaly Detector will be triggered in a rate of {}ms...", analyzeRate);
		}
	}

	private boolean test = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		if (test) {
			log.info("<<TEST>> Triggering anomaly detector.");

			if (influxDb.isBatching()) {
				influxDb.disableBatching();
			}

			long time = System.currentTimeMillis() - 3600 * 1000 * 2;
			time -= time % 5000;

			while (time < System.currentTimeMillis() - 5000) {
				anomalyDetector.execute(time);
				time += 5000;
			}

			test = false;
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Triggering anomaly detector.");
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
