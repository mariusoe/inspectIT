package rocks.inspectit.server.anomaly.notification.impl;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.Anomaly;
import rocks.inspectit.server.anomaly.notification.AbstractNotifier;
import rocks.inspectit.shared.all.spring.logger.Log;
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
	public void onStart(Anomaly anomaly) {
		log.info("Anomaly started for {} [{}] ", anomaly.getGroupConfiguration().getName(), anomaly.getGroupConfiguration().getGroupId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onUpgrade(Anomaly anomaly) {
		log.info("Anomaly upgraded for {} [{}] ", anomaly.getGroupConfiguration().getName(), anomaly.getGroupConfiguration().getGroupId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDowngrade(Anomaly anomaly) {
		log.info("Anomaly downgraded for {} [{}] ", anomaly.getGroupConfiguration().getName(), anomaly.getGroupConfiguration().getGroupId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnd(Anomaly anomaly) {
		log.info("Anomaly ended: {}", anomaly.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
	}
}
