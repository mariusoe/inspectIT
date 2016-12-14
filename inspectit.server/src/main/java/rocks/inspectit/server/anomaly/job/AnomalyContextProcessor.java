package rocks.inspectit.server.anomaly.job;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.context.AnomalyContextManager;
import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.data.HealthStatus;
import rocks.inspectit.server.anomaly.processor.baseline.AbstractBaselineProcessor;
import rocks.inspectit.server.anomaly.state.AnomalyStateManager;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyContextProcessor implements Runnable {

	@Log
	private Logger log;

	/**
	 * {@link ExecutorService} instance.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService executorService;

	@Autowired
	private AnomalyContextManager contextManager;

	@Autowired
	private AnomalyStateManager stateManager;

	@Value("${anomaly.active}")
	boolean anomalyDetectionActive;

	@Autowired
	private InfluxDBDao influxDao;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		Collection<AnomalyContext> contexts = contextManager.getAnomalyContexts();

		for (AnomalyContext context : contexts) {
			Collection<AnalyzableData<?>> data = context.swapBuffer();

			AbstractBaselineProcessor<?> baselineProcessor = context.getBaselineProcessor();
			if (baselineProcessor != null) {
				if (log.isDebugEnabled()) {
					log.debug("Process baseline of {}", context);
				}
				baselineProcessor.process(context, data);
			}

			double criticalRate = calculateCriticalRate(data);
			stateManager.handle(context, criticalRate);

			Builder builder = Point.measurement("ad_confidenceband");
			builder.tag("context", context.getId());
			builder.addField("lower_bound", context.getConfidenceBand().getLowerBound());
			builder.addField("upper_bound", context.getConfidenceBand().getUpperBound());
			builder.addField("baseline", context.getBaseline());
			builder.addField("critical_rate", criticalRate);
			influxDao.insert(builder.build());
		}
	}

	private double calculateCriticalRate(Collection<AnalyzableData<?>> data) {
		if (CollectionUtils.isEmpty(data)) {
			return 0D;
		}

		int criticalCounter = 0;

		for (AnalyzableData<?> analyzableData : data) {
			if (analyzableData.getHealthStatus() == HealthStatus.CRITICAL) {
				criticalCounter++;
			}
		}

		return (1D / data.size()) * criticalCounter;
	}

	@PostConstruct
	private void postConstruct() {
		if (anomalyDetectionActive) {
			executorService.scheduleWithFixedDelay(this, 5L, 5L, TimeUnit.SECONDS);
		}
	}
}
