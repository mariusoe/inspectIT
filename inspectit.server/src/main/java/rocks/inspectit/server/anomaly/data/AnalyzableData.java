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

	public abstract String getValueDescription();

	public E getData() {
		return data;
	}

	public HealthStatus getHealthStatus() {
		return healthStatus;
	}

	public void setHealthStatus(HealthStatus healthStatus) {
		this.healthStatus = healthStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("AnalyzableData[dataType=").append(data.getClass().getSimpleName()).append("; description=").append(getValueDescription()).append("; value=").append(getValue())
				.append("; health=").append(healthStatus).append(']');
		return buffer.toString();
	}
}
