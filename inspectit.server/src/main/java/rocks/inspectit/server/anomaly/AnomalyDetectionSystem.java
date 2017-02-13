package rocks.inspectit.server.anomaly;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyDetectionSystem {

	@Log
	private Logger log;

	/**
	 * {@link ExecutorService} instance.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	ScheduledExecutorService executorService;

	private long detectionInterval = 1000;

	@Autowired
	private AnomalyProcessor anomalyProcessor;

	@PostConstruct
	public void postConstruct() {
		if (log.isInfoEnabled()) {
			log.info("|-Initialized Anomaly Detection System...");
		}
	}

	public void start() {
		executorService.scheduleAtFixedRate(anomalyProcessor, 5000, detectionInterval, TimeUnit.MILLISECONDS);
	}
}
