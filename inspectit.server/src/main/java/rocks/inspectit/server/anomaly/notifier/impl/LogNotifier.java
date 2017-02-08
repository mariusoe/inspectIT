package rocks.inspectit.server.anomaly.notifier.impl;

import java.util.Date;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.Anomaly;
import rocks.inspectit.server.anomaly.notifier.AbstractNotifier;
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
		if (getDefinition().isIgnoreWarnings() && !anomaly.isCritical()) {
			return;
		}
		if (getDefinition().isPrintBegin() && log.isInfoEnabled()) {
			String btxString;
			if (anomaly.isBusinessTransactionRelated()) {
				btxString = " (btx: " + anomaly.getBusinessTransactionName() + ")";
			} else {
				btxString = "";
			}
			log.info("[Anomaly] The detection group '{}'{} detected an anomaly.", anomaly.getGroupConfiguration().getName(), btxString);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onUpgrade(Anomaly anomaly) {
		if (getDefinition().isIgnoreWarnings() && !anomaly.isCritical()) {
			return;
		}
		if (getDefinition().isPrintTransitions() && log.isInfoEnabled()) {
			log.info("Anomaly upgraded for {} [{}] ", anomaly.getGroupConfiguration().getName(), anomaly.getGroupConfiguration().getGroupId());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDowngrade(Anomaly anomaly) {
		if (getDefinition().isIgnoreWarnings() && !anomaly.isCritical()) {
			return;
		}
		if (getDefinition().isPrintTransitions() && log.isInfoEnabled()) {
			log.info("Anomaly downgraded for {} [{}] ", anomaly.getGroupConfiguration().getName(), anomaly.getGroupConfiguration().getGroupId());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnd(Anomaly anomaly) {
		if (getDefinition().isIgnoreWarnings() && !anomaly.isCritical()) {
			return;
		}

		if (getDefinition().isPrintEnd() && log.isInfoEnabled()) {
			String btxString;
			if (anomaly.isBusinessTransactionRelated()) {
				btxString = " (btx: " + anomaly.getBusinessTransactionName() + ")";
			} else {
				btxString = "";
			}

			log.info("[Anomaly] The anomaly detected by detection group '{}'{} has ended.", anomaly.getGroupConfiguration().getName(), btxString);
			log.info("          |- started: {}", new Date(anomaly.getStartTime()));
			log.info("          |- ended: {}", new Date(anomaly.getEndTime()));
			log.info("          |- duration: {}", DurationFormatUtils.formatDurationHMS(anomaly.getEndTime() - anomaly.getStartTime()));
			log.info("          |- was critical: {}", anomaly.isCritical());
			log.info("          |- parallel critical: {}/{}", anomaly.getParallelCriticalProcessingUnits(), anomaly.getGroupConfiguration().getConfigurations().size());
			log.info("          |- max value: {}", anomaly.getMaxValue());
			log.info("          |- max violation: {} [{} {}]", anomaly.getMaxViolationValue(), anomaly.getMaxViolationDelta(), anomaly.getMaxViolationThresholdType());
			log.info("          |- max violation date: {}", new Date(anomaly.getMaxViolationTime()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDefinitionUpdate() {
	}
}
