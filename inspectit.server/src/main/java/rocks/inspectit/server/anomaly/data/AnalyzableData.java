package rocks.inspectit.server.anomaly.data;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 *
 * @author Marius Oehler
 *
 */
public abstract class AnalyzableData<E extends DefaultData> {

	private HealthStatus healthStatus = HealthStatus.UKNOWN;

	protected final E data;

	/**
	 * @param data
	 */
	public AnalyzableData(E data) {
		this.data = data;
	}

	public abstract double getValue();

	public E getData() {
		return data;
	}

	public HealthStatus getHealthStatus() {
		return healthStatus;
	}

	public void setHealthStatus(HealthStatus healthStatus) {
		this.healthStatus = healthStatus;
	}
}
