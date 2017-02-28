package rocks.inspectit.shared.cs.ci.anomaly.definition.notification;

import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinition;

/**
 * @author Marius Oehler
 *
 */
public class NotificationDefinition extends AbstractDefinition {

	private boolean notifyOnlyCritical = false;

	/**
	 * Gets {@link #notifyOnlyCritical}.
	 *
	 * @return {@link #notifyOnlyCritical}
	 */
	public boolean isNotifyOnlyCritical() {
		return this.notifyOnlyCritical;
	}

	/**
	 * Sets {@link #notifyOnlyCritical}.
	 *
	 * @param notifyOnlyCritical
	 *            New value for {@link #notifyOnlyCritical}
	 */
	public void setNotifyOnlyCritical(boolean notifyOnlyCritical) {
		this.notifyOnlyCritical = notifyOnlyCritical;
	}
}
