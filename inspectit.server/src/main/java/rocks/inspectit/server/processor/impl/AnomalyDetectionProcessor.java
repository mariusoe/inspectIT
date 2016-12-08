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
import rocks.inspectit.server.anomaly.processor.analyzer.AbstractAnalyzeProcessor;
import rocks.inspectit.server.anomaly.processor.classifier.AbstractClassifyProcessor;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.shared.cs.cmr.service.IBusinessContextRegistryService;

public class AnomalyDetectionProcessor extends AbstractCmrDataProcessor {

	@Log
	private Logger log;

	@Autowired
	private AnomalyContextManager contextManager;

	private final AnalyzableDataFactory analyzableDataFactory = new AnalyzableDataFactory();

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

		AbstractClassifyProcessor<?> classifyProcessor = context.getClassifyProcessor();
		if (classifyProcessor != null) {
			classifyProcessor.classify(context, analyzable);

			if ((analyzable.getHealthStatus() == HealthStatus.CRITICAL) && (context.getAnalyzeProcessor() != null)) {
				AbstractAnalyzeProcessor<?> analyzeProcessor = context.getAnalyzeProcessor();
				analyzeProcessor.analyze(analyzable);
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

	@Autowired
	IBusinessContextRegistryService btxRegistry;

	@PostConstruct
	public void test() {
		ApplicationDefinition app = new ApplicationDefinition("textApp");
		ApplicationData appData = btxRegistry.registerApplication(app);

		BusinessTransactionDefinition definition = new BusinessTransactionDefinition();
		BusinessTransactionData transaction = btxRegistry.registerBusinessTransaction(appData, definition, "testTransaction");

		final int appId = transaction.getApplication().getId();
		final int btxId = transaction.getId();

		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					InvocationSequenceData data = new InvocationSequenceData();
					data.setBusinessTransactionId(btxId);
					data.setApplicationId(appId);
					data.setDuration(Math.random() * 20);
					process(data, null);
				}
			};
		}.start();
	}
}
