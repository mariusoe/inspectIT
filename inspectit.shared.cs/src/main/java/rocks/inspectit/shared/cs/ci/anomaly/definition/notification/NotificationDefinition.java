package rocks.inspectit.shared.cs.ci.anomaly.definition.notification;

import rocks.inspectit.shared.cs.ci.anomaly.definition.AbstractDefinition;

/**
 * @author Marius Oehler
 *
 */
public class NotificationDefinition extends AbstractDefinition {

	private boolean ignoreWarnings = false;

	/**
	 * Gets {@link #ignoreWarnings}.
	 *
	 * @return {@link #ignoreWarnings}
	 */
	public boolean isIgnoreWarnings() {
		return this.ignoreWarnings;
	}

	/**
	 * Sets {@link #ignoreWarnings}.
	 *
	 * @param ignoreWarnings
	 *            New value for {@link #ignoreWarnings}
	 */
	public void setIgnoreWarnings(boolean ignoreWarnings) {
		this.ignoreWarnings = ignoreWarnings;
	}

}
