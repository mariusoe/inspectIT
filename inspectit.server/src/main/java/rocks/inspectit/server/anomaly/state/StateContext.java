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

	private HealthStatus healthStauts = HealthStatus.UNKNOWN;

	/**
	 * Gets {@link #healthStauts}.
	 * 
	 * @return {@link #healthStauts}
	 */
	public HealthStatus getHealthStauts() {
		return this.healthStauts;
	}

	/**
	 * Sets {@link #healthStauts}.
	 * 
	 * @param healthStauts
	 *            New value for {@link #healthStauts}
	 */
	public void setHealthStauts(HealthStatus healthStauts) {
		this.healthStauts = healthStauts;
	}

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
