package rocks.inspectit.server.anomaly.notification;

import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.state.StateManager.HealthTransition;

/**
 * @author Marius Oehler
 *
 */
@Component
public class NotificationService {

	public void handleHealthTransition(HealthTransition transition) {

	}

}
