/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.object.InvocationStreamObject;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.cmr.service.IBusinessContextManagementService;

/**
 * @author Marius Oehler
 *
 */
public class BusinessTransactionInjectorComponent extends AbstractSingleStreamComponent<InvocationSequenceData> {

	private final IBusinessContextManagementService businessService;

	/**
	 * @param nextComponent
	 */
	public BusinessTransactionInjectorComponent(ISingleInputComponent<InvocationSequenceData> nextComponent, IBusinessContextManagementService businessService) {
		super(nextComponent);
		this.businessService = businessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> item) {
		int transactionId = item.getData().getBusinessTransactionId();
		int applicationId = item.getData().getApplicationId();
		BusinessTransactionData transactionData = businessService.getBusinessTransactionForId(applicationId, transactionId);

		InvocationStreamObject invocationStreamObject = (InvocationStreamObject) item;
		invocationStreamObject.setBusinessTransaction(transactionData.getName());

		return EFlowControl.CONTINUE;
	}

}
