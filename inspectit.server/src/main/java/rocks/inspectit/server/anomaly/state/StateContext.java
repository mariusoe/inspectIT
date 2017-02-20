package rocks.inspectit.server.anomaly.state;

import java.util.LinkedList;

import rocks.inspectit.server.anomaly.HealthStatus;

/**
 * @author Marius Oehler
 *
 */
public class StateContext {

	public enum AnomalyState {
		ACTIVE, INACTIVE;
	}

	private LinkedList<HealthStatus> healthList = new LinkedList<>();

	private int maxHealthListSize = 1;

	private HealthStatus continuousHealthStauts = HealthStatus.UNKNOWN;

	public HealthStatus getLowestHealthStatus(int elements) {
		HealthStatus returnStatus = HealthStatus.UNKNOWN;

		for (int i = healthList.size() - 1; i >= Math.max(0, healthList.size() - elements); i--) {
			HealthStatus healthStatus = healthList.get(i);
			if (healthStatus == HealthStatus.UNKNOWN) {
				continue;
			}
			if ((returnStatus == HealthStatus.UNKNOWN) || (returnStatus.ordinal() > healthStatus.ordinal())) {
				returnStatus = healthStatus;
			}
		}

		return returnStatus;
	}

	public HealthStatus getHighestHealthStatus(int elements) {
		HealthStatus returnStatus = HealthStatus.UNKNOWN;

		for (int i = healthList.size() - 1; i >= Math.max(0, healthList.size() - elements); i--) {
			HealthStatus healthStatus = healthList.get(i);
			if (healthStatus == HealthStatus.UNKNOWN) {
				continue;
			}
			if ((returnStatus == HealthStatus.UNKNOWN) || (returnStatus.ordinal() < healthStatus.ordinal())) {
				returnStatus = healthStatus;
			}
		}

		return returnStatus;
	}

	/**
	 * Gets {@link #continuousHealthStauts}.
	 *
	 * @return {@link #continuousHealthStauts}
	 */
	public HealthStatus getContinuousHealthStauts() {
		return this.continuousHealthStauts;
	}

	/**
	 * Sets {@link #continuousHealthStauts}.
	 *
	 * @param continuousHealthStauts
	 *            New value for {@link #continuousHealthStauts}
	 */
	public void setContinuousHealthStauts(HealthStatus continuousHealthStauts) {
		this.continuousHealthStauts = continuousHealthStauts;
	}

	public void addHealthStatus(HealthStatus status) {
		healthList.add(status);

		if (healthList.size() > maxHealthListSize) {
			healthList.removeFirst();
		}
	}

	public void setMaxHealthListSize(int size) {
		maxHealthListSize = size;
	}
}
