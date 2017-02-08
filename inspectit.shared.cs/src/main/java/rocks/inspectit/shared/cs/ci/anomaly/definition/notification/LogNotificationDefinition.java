package rocks.inspectit.shared.cs.ci.anomaly.definition.notification;

/**
 * @author Marius Oehler
 *
 */
public class LogNotificationDefinition extends NotificationDefinition {

	private boolean printBegin = true;

	private boolean printEnd = true;

	private boolean printTransitions = true;

	/**
	 * Gets {@link #printBegin}.
	 *
	 * @return {@link #printBegin}
	 */
	public boolean isPrintBegin() {
		return this.printBegin;
	}

	/**
	 * Sets {@link #printBegin}.
	 *
	 * @param printBegin
	 *            New value for {@link #printBegin}
	 */
	public void setPrintBegin(boolean printBegin) {
		this.printBegin = printBegin;
	}

	/**
	 * Gets {@link #printEnd}.
	 *
	 * @return {@link #printEnd}
	 */
	public boolean isPrintEnd() {
		return this.printEnd;
	}

	/**
	 * Sets {@link #printEnd}.
	 *
	 * @param printEnd
	 *            New value for {@link #printEnd}
	 */
	public void setPrintEnd(boolean printEnd) {
		this.printEnd = printEnd;
	}

	/**
	 * Gets {@link #printTransitions}.
	 *
	 * @return {@link #printTransitions}
	 */
	public boolean isPrintTransitions() {
		return this.printTransitions;
	}

	/**
	 * Sets {@link #printTransitions}.
	 *
	 * @param printTransitions
	 *            New value for {@link #printTransitions}
	 */
	public void setPrintTransitions(boolean printTransitions) {
		this.printTransitions = printTransitions;
	}
}
