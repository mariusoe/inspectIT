package rocks.inspectit.server.anomaly.notification;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.DefinitionAwareFactory;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitGroupContext;
import rocks.inspectit.server.anomaly.state.StateManager.HealthTransition;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
public class NotificationService {

	@Log
	private Logger log;

	@Autowired
	private DefinitionAwareFactory factory;

	public void handleHealthTransition(HealthTransition transition, ProcessingUnitGroupContext groupContext) {
		AbstractNotifier<?> notifier = factory.createNotifier(groupContext.getGroupConfiguration().getNotificationDefinition());

		switch (transition) {
		case BEGIN:
			notifier.onStart(groupContext.getAnomalyStateContext().getCurrentAnomaly());
			break;
		case DOWNGRADE:
			notifier.onDowngrade(groupContext.getAnomalyStateContext().getCurrentAnomaly());
			break;
		case END:
			notifier.onEnd(groupContext.getAnomalyStateContext().getCurrentAnomaly());
			break;
		case UPGRADE:
			notifier.onUpgrade(groupContext.getAnomalyStateContext().getCurrentAnomaly());
			break;
		case NO_CHANGE:
		default:
			break;
		}
	}
}
