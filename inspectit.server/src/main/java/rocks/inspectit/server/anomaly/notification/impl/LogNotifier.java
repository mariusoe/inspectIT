package rocks.inspectit.server.anomaly.notification.impl;

import java.util.Date;

import org.apache.commons.lang.time.DurationFormatUtils;
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
		if (getDefinition().isPrintBegin() && log.isInfoEnabled()) {
			log.info("[Anomaly] The detection group '{}' (btx: {}) detected an anomaly.", anomaly.getGroupConfiguration().getName(), anomaly.getGroupConfiguration().getBusinessTransaction());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onUpgrade(Anomaly anomaly) {
		if (getDefinition().isPrintTransitions() && log.isInfoEnabled()) {
			log.info("Anomaly upgraded for {} [{}] ", anomaly.getGroupConfiguration().getName(), anomaly.getGroupConfiguration().getId());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDowngrade(Anomaly anomaly) {
		if (getDefinition().isPrintTransitions() && log.isInfoEnabled()) {
			log.info("Anomaly downgraded for {} [{}] ", anomaly.getGroupConfiguration().getName(), anomaly.getGroupConfiguration().getId());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnd(Anomaly anomaly) {
		if (getDefinition().isPrintEnd() && log.isInfoEnabled()) {
			log.info("[Anomaly] The anomaly detected by detection group '{}' (btx: {}) has ended.", anomaly.getGroupConfiguration().getName(),
					anomaly.getGroupConfiguration().getBusinessTransaction());

			log.info("          |- started: {}", new Date(anomaly.getStartTime()));
			log.info("          |- ended: {}", new Date(anomaly.getEndTime()));
			log.info("          |- duration: {}", DurationFormatUtils.formatDurationHMS(anomaly.getEndTime() - anomaly.getStartTime()));
			log.info("          |- was critical: {}", anomaly.isCritical());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
	}
}
