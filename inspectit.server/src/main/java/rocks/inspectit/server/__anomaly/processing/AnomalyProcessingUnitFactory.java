package rocks.inspectit.server.__anomaly.processing;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.cs.anomaly.configuration.AnomalyDetectionConfiguration;
import rocks.inspectit.shared.cs.anomaly.extractor.IValueExtractor;
import rocks.inspectit.shared.cs.anomaly.selector.AbstractDataSelector;
import rocks.inspectit.shared.cs.anomaly.selector.IDataSelectorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyProcessingUnitFactory {

	@Autowired
	private BeanFactory beanFactory;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AnomalyProcessingUnit createProcessingUnit(AnomalyDetectionConfiguration configuration) {
		AnomalyProcessingUnit processingUnit = beanFactory.getBean(AnomalyProcessingUnit.class);

		processingUnit.setConfiguration(configuration);

		for (IDataSelectorConfiguration selectorConfiguration : configuration.getDataSelectors()) {
			AbstractDataSelector dataSelector = beanFactory.getBean(selectorConfiguration.getDataSelectorClass());
			dataSelector.setConfiguration(selectorConfiguration);
			processingUnit.getDataSelectors().add(dataSelector);
		}

		IValueExtractor<DefaultData> valueExtractor = (IValueExtractor<DefaultData>) beanFactory.getBean(configuration.getValueExtractorClass());
		processingUnit.setValueExtractor(valueExtractor);

		return processingUnit;
	}

}
