/**
 *
 */
package rocks.inspectit.server.anomaly.stream.object;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class InvocationStreamObject extends StreamObject<InvocationSequenceData> {

	private String businessTransaction;

	/**
	 * @param data
	 */
	public InvocationStreamObject(InvocationSequenceData data) {
		super(data);
	}

	/**
	 * Gets {@link #businessTransaction}.
	 *
	 * @return {@link #businessTransaction}
	 */
	public String getBusinessTransaction() {
		return businessTransaction;
	}

	/**
	 * Sets {@link #businessTransaction}.
	 *
	 * @param businessTransaction
	 *            New value for {@link #businessTransaction}
	 */
	public void setBusinessTransaction(String businessTransaction) {
		this.businessTransaction = businessTransaction;
	}

}
