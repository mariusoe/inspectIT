/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.object.StreamContext;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.cmr.service.IBusinessContextManagementService;

/**
 * @author Marius Oehler
 *
 */
public class BusinessTransactionContextInjectorComponent extends AbstractSingleStreamComponent<InvocationSequenceData> {

	@Autowired
	private IBusinessContextManagementService businessService;

	@Autowired
	private SharedStreamProperties streamProperties;

	/**
	 *
	 */
	private BusinessTransactionContextInjectorComponent() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> item) {
		int transactionId = item.getData().getBusinessTransactionId();
		int applicationId = item.getData().getApplicationId();
		BusinessTransactionData transactionData = businessService.getBusinessTransactionForId(applicationId, transactionId);

		String businessTransaction = transactionData.getName();

		if (!streamProperties.getStreamContextMap().containsKey(businessTransaction)) {
			StreamContext context = new StreamContext();
			context.setStartTime(System.currentTimeMillis());
			context.setBusinessTransaction(businessTransaction);

			streamProperties.getStreamContextMap().put(businessTransaction, context);
		}

		StreamContext streamContext = streamProperties.getStreamContextMap().get(businessTransaction);
		item.setContext(streamContext);

		streamContext.incrementRequestCount();

		return EFlowControl.CONTINUE;
	}

}
