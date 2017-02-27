package rocks.inspectit.server.anomaly.notification.impl;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.notification.AbstractNotifier;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionConfigurationGroup;
import rocks.inspectit.shared.cs.ci.anomaly.definition.notification.LogNotificationDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
public class LogNotifier extends AbstractNotifier<LogNotificationDefinition> {

	@Log
	private Logger log;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart(AnomalyDetectionConfigurationGroup configurationGroup) {
		log.info("Anomaly started for " + configurationGroup.getGroupId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onUpgrade(AnomalyDetectionConfigurationGroup configurationGroup) {
		log.info("Anomaly is upgraded for " + configurationGroup.getGroupId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDowngrade(AnomalyDetectionConfigurationGroup configurationGroup) {
		log.info("Anomaly is downgraded for " + configurationGroup.getGroupId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnd(AnomalyDetectionConfigurationGroup configurationGroup) {
		log.info("Anomaly ended for " + configurationGroup.getGroupId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
	}
}
