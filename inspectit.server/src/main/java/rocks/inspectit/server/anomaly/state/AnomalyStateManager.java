package rocks.inspectit.server.anomaly.state;

import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.context.model.AnomalyContext;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyStateManager {

	public void handle(AnomalyContext context, double criticalRate) {
		// TODO
		System.out.println("state -> " + criticalRate);
	}

}
