package rocks.inspectit.server.processor.impl;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.anomaly.AnalyzableDataFactory;
import rocks.inspectit.server.anomaly.context.AnomalyContextManager;
import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.data.HealthStatus;
import rocks.inspectit.server.anomaly.processor.classifier.IClassifyProcessor;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;

public class AnomalyDetectionProcessor extends AbstractCmrDataProcessor {

	@Log
	private Logger log;

	private final AnalyzableDataFactory analyzableDataFactory = new AnalyzableDataFactory();

	@Autowired
	private AnomalyContextManager contextManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		AnomalyContext context = contextManager.getAnomalyContext(defaultData);
		if (context == null) {
			if (log.isDebugEnabled()) {
				log.debug("Data will not be processed because it could not be linked to an anomaly context.");
			}
			return;
		}

		AnalyzableData<?> analyzable = analyzableDataFactory.createAnalyzable(defaultData);
		if (analyzable == null) {
			if (log.isDebugEnabled()) {
				log.debug("Data will not be processed because it could not represented as Analyzable.");
			}
			return;
		}

		context.getIntervalBuffer().add(analyzable);

		IClassifyProcessor classifyProcessor = context.getClassifyProcessor();
		if (classifyProcessor != null) {
			classifyProcessor.classify(context, analyzable);

			if ((analyzable.getHealthStatus() == HealthStatus.CRITICAL) && (context.getAnalyzeProcessor() != null)) {
				context.getAnalyzeProcessor().analyze(analyzable);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return analyzableDataFactory.isAnalyzable(defaultData);
	}

	@PostConstruct
	public void test() {
		new Thread() {
			@Override
			public void run() {
				try {
					sleep(1000);

					InvocationSequenceData data = new InvocationSequenceData();
					data.setDuration(2);
					process(data, null);
					data.setDuration(0);
					process(data, null);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}
}
