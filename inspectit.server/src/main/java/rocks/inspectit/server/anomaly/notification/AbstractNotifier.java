package rocks.inspectit.server.anomaly.notification;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionConfigurationGroup;
import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.shared.cs.ci.anomaly.definition.notification.NotificationDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractNotifier<E extends NotificationDefinition> extends AbstractDefinitionAware<E> {

	public abstract void onStart(AnomalyDetectionConfigurationGroup configurationGroup);

	public abstract void onUpgrade(AnomalyDetectionConfigurationGroup configurationGroup);

	public abstract void onDowngrade(AnomalyDetectionConfigurationGroup configurationGroup);

	public abstract void onEnd(AnomalyDetectionConfigurationGroup configurationGroup);

}
