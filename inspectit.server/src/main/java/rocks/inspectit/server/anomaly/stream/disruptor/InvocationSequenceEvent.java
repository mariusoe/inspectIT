/**
 *
 */
package rocks.inspectit.server.anomaly.stream.disruptor;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class InvocationSequenceEvent {

	private InvocationSequenceData data;

	/**
	 * Gets {@link #data}.
	 *
	 * @return {@link #data}
	 */
	public InvocationSequenceData getData() {
		return data;
	}

	/**
	 * Sets {@link #data}.
	 *
	 * @param data
	 *            New value for {@link #data}
	 */
	public void setData(InvocationSequenceData data) {
		this.data = data;
	}

}
