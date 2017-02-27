package rocks.inspectit.server.anomaly.notification;

import rocks.inspectit.server.anomaly.Anomaly;
import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.shared.cs.ci.anomaly.definition.notification.NotificationDefinition;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractNotifier<E extends NotificationDefinition> extends AbstractDefinitionAware<E> {

	public abstract void onStart(Anomaly anomaly);

	public abstract void onUpgrade(Anomaly anomaly);

	public abstract void onDowngrade(Anomaly anomaly);

	public abstract void onEnd(Anomaly anomaly);

}
