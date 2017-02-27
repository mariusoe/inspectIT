package rocks.inspectit.server.anomaly.notification;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.DefinitionAwareFactory;
import rocks.inspectit.server.anomaly.state.StateManager.HealthTransition;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionConfigurationGroup;

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

	public void handleHealthTransition(HealthTransition transition, AnomalyDetectionConfigurationGroup configurationGroup) {
		AbstractNotifier<?> notifier = factory.createNotifier(configurationGroup.getNotificationDefinition());

		switch (transition) {
		case BEGIN:
			notifier.onStart(configurationGroup);
			break;
		case DOWNGRADE:
			notifier.onDowngrade(configurationGroup);
			break;
		case END:
			notifier.onEnd(configurationGroup);
			break;
		case UPGRADE:
			notifier.onUpgrade(configurationGroup);
			break;
		case NO_CHANGE:
		default:
			break;
		}
	}
}
