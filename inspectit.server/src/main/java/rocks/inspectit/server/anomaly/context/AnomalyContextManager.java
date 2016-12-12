package rocks.inspectit.server.anomaly.context;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.CMR;
import rocks.inspectit.server.anomaly.configuration.AnomalyDetectionConfigurationProvider;
import rocks.inspectit.server.anomaly.context.matcher.AbstractAnomalyContextMatcher;
import rocks.inspectit.server.anomaly.context.model.AnomalyContext;
import rocks.inspectit.server.anomaly.processor.analyzer.AbstractAnalyzeProcessor;
import rocks.inspectit.server.anomaly.processor.baseline.AbstractBaselineProcessor;
import rocks.inspectit.server.anomaly.processor.classifier.AbstractClassifyProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.matcher.AbstractContextMatcherConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractAnalyzeProcessorConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractBaselineProcessorConfiguration;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.AbstractClassifyProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyContextManager {

	@Log
	Logger log;

	@Autowired
	private AnomalyDetectionConfigurationProvider anomalyConfigurationProvider;

	private final Collection<AnomalyContext> anomalyContexts = new CopyOnWriteArrayList<>();

	public AnomalyContext getAnomalyContext(DefaultData defaultData) {
		for (AnomalyContext context : anomalyContexts) {
			for (AbstractAnomalyContextMatcher<?> matcher : context.getContextMatcher()) {
				if (matcher.matches(defaultData)) {
					return context;
				}
			}
		}

		return createAnomalyContext(defaultData);
	}

	private AnomalyContext createAnomalyContext(DefaultData defaultData) {
		Collection<AnomalyDetectionConfiguration> configurations = anomalyConfigurationProvider.getConfigurations();
		if (CollectionUtils.isEmpty(configurations)) {
			return null;
		}

		for (AnomalyDetectionConfiguration configuration : configurations) {
			try {
				return createcreateAnomalyContext(defaultData, configuration);
			} catch (ClassNotFoundException e) {
				if (log.isErrorEnabled()) {
					log.error("Class of anomaly processor could not be found. Configuration will be removed.", e);
				}
				anomalyContexts.remove(configuration);
			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private AnomalyContext createcreateAnomalyContext(DefaultData defaultData, AnomalyDetectionConfiguration configuration) throws BeansException, ClassNotFoundException {
		BeanFactory beanFactory = CMR.getBeanFactory();
		if (beanFactory == null) {
			return null;
		}

		AnomalyContext context = new AnomalyContext();

		// create matcher and check if the context matches the data
		boolean matches = false;
		for (AbstractContextMatcherConfiguration matcherConfiguration : configuration.getContextMatcher()) {
			AbstractAnomalyContextMatcher contextMatcher = (AbstractAnomalyContextMatcher) beanFactory.getBean(Class.forName(matcherConfiguration.getContextMatcher().getFqn()));
			contextMatcher.setConfiguration(matcherConfiguration);

			matches |= contextMatcher.matches(defaultData);

			context.getContextMatcher().add(contextMatcher);
		}

		if (!matches) {
			return null;
		}

		// create analyze processor
		AbstractAnalyzeProcessorConfiguration analyzeProcessorConfiguration = configuration.getAnalyzeProcessorConfiguration();
		String analyzeProcessorFqn = analyzeProcessorConfiguration.getAnomalyProcessor().getFqn();
		AbstractAnalyzeProcessor analyzeProcessor = (AbstractAnalyzeProcessor) beanFactory.getBean(Class.forName(analyzeProcessorFqn));
		analyzeProcessor.setConfiguration(analyzeProcessorConfiguration);

		// create baseline processor
		AbstractBaselineProcessorConfiguration baselineProcessorConfiguration = configuration.getBaselineProcessorConfiguration();
		String baselineProcessorFqn = baselineProcessorConfiguration.getAnomalyProcessor().getFqn();
		AbstractBaselineProcessor baselineProcessor = (AbstractBaselineProcessor) beanFactory.getBean(Class.forName(baselineProcessorFqn));
		baselineProcessor.setConfiguration(baselineProcessorConfiguration);

		// create classify processor
		AbstractClassifyProcessorConfiguration classifyProcessorConfiguration = configuration.getClassifyProcessorConfiguration();
		String classifyProcessorFqn = classifyProcessorConfiguration.getAnomalyProcessor().getFqn();
		AbstractClassifyProcessor classifyProcessor = (AbstractClassifyProcessor) beanFactory.getBean(Class.forName(classifyProcessorFqn));
		classifyProcessor.setConfiguration(classifyProcessorConfiguration);

		// create new anomaly context
		context.setAnalyzeProcessor(analyzeProcessor);
		context.setBaselineProcessor(baselineProcessor);
		context.setClassifyProcessor(classifyProcessor);

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
