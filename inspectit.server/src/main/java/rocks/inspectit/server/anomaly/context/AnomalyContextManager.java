package rocks.inspectit.server.anomaly.context;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.CMR;
import rocks.inspectit.server.anomaly.configuration.AnomalyDetectionConfigurationProvider;
import rocks.inspectit.server.anomaly.configuration.model.AnomalyDetectionConfiguration;
import rocks.inspectit.server.anomaly.configuration.model.IAnalyzeProcessorConfiguration;
import rocks.inspectit.server.anomaly.configuration.model.IBaselineProcessorConfiguration;
import rocks.inspectit.server.anomaly.configuration.model.IClassifyProcessorConfiguration;
import rocks.inspectit.server.anomaly.context.matcher.IAnomalyContextMatcher;
import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.processor.analyzer.AbstractAnalyzeProcessor;
import rocks.inspectit.server.anomaly.processor.baseline.AbstractBaselineProcessor;
import rocks.inspectit.server.anomaly.processor.classifier.AbstractClassifyProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyContextManager {

	@Autowired
	private AnomalyDetectionConfigurationProvider anomalyConfigurationProvider;

	private final Collection<AnomalyContext> anomalyContexts = new CopyOnWriteArrayList<>();

	public AnomalyContext getAnomalyContext(DefaultData defaultData) {
		for (AnomalyContext context : anomalyContexts) {
			for (IAnomalyContextMatcher matcher : context.getContextMatcher()) {
				if (matcher.matches(defaultData)) {
					return context;
				}
			}
		}

		return createAnomalyContext(defaultData);
	}

	private AnomalyContext createAnomalyContext(DefaultData defaultData) {
		AnomalyDetectionConfiguration configuration = anomalyConfigurationProvider.getConfiguration(defaultData);
		if (configuration == null) {
			return null;
		}

		BeanFactory beanFactory = CMR.getBeanFactory();

		// create analyze processor
		IAnalyzeProcessorConfiguration<? extends AbstractAnalyzeProcessor> analyzeProcessorConfiguration = configuration.getAnalyzeProcessorConfiguration();
		AbstractAnalyzeProcessor analyzeProcessor = beanFactory.getBean(analyzeProcessorConfiguration.getProcessorClass());
		analyzeProcessor.setConfiguration(configuration.getAnalyzeProcessorConfiguration());

		// create baseline processor
		IBaselineProcessorConfiguration<? extends AbstractBaselineProcessor> baselineProcessorConfiguration = configuration.getBaselineProcessorConfiguration();
		AbstractBaselineProcessor baselineProcessor = beanFactory.getBean(baselineProcessorConfiguration.getProcessorClass());
		baselineProcessor.setConfiguration(baselineProcessorConfiguration);

		// create classify processor
		IClassifyProcessorConfiguration<? extends AbstractClassifyProcessor> classifyProcessorConfiguration = configuration.getClassifyProcessorConfiguration();
		AbstractClassifyProcessor classifyProcessor = beanFactory.getBean(classifyProcessorConfiguration.getProcessorClass());
		classifyProcessor.setConfiguration(classifyProcessorConfiguration);

		// create new anomaly context
		AnomalyContext context = new AnomalyContext();
		context.setAnalyzeProcessor(analyzeProcessor);
		context.setBaselineProcessor(baselineProcessor);
		context.setClassifyProcessor(classifyProcessor);

		// copy context matcher
		for (IAnomalyContextMatcher matcher : configuration.getContextMatcher()) {
			context.getContextMatcher().add(matcher.createCopy());
		}

		anomalyContexts.add(context);
		return context;
	}

	/**
	 * Gets {@link #anomalyContexts}.
	 *
	 * @return {@link #anomalyContexts}
	 */
	public Collection<AnomalyContext> getAnomalyContexts() {
		return this.anomalyContexts;
	}
}
