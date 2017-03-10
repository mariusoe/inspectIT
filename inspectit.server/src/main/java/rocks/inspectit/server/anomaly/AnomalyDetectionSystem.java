package rocks.inspectit.server.anomaly;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.event.CmrStartedEvent;
import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyDetectionSystem implements ApplicationListener<CmrStartedEvent> {

	/**
	 * Interval in seconds.
	 */
	public static final long PROCESSING_INTERVAL_SECONDS = 15L;

	/**
	 * Logger of this class.
	 */
	@Log
	private Logger log;

	/**
	 * {@link ExecutorService} instance.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	ScheduledExecutorService executorService;

	/**
	 * Activation state of this service.
	 */
	@Value("${ads.enabled}")
	boolean enabled;

	@Autowired
	private AnomalyProcessorController anomalyProcessorController;

	private ScheduledFuture<?> controllerFuture;

	@PostConstruct
	public void postConstruct() {
		if (log.isInfoEnabled()) {
			log.info("|-Initialized Anomaly Detection System...");
		}
	}

	@PropertyUpdate(properties = { "ads.enabled" })
	private void start() {
		if (enabled) {
			anomalyProcessorController.initialize();

			this.controllerFuture = executorService.scheduleAtFixedRate(anomalyProcessorController, PROCESSING_INTERVAL_SECONDS, PROCESSING_INTERVAL_SECONDS, TimeUnit.SECONDS);
		} else {
			if ((controllerFuture != null) && !controllerFuture.isDone()) {
				controllerFuture.cancel(false);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(CmrStartedEvent event) {
		if (log.isInfoEnabled()) {
			log.info("Starting the anomaly detection system..");
		}

		start();
	}
}
