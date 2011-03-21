package info.novatec.inspectit.storage.processor.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.processor.AbstractExtractorDataProcessor;

import java.util.List;

/**
 * This is a special type of processor. It extract the children information from a
 * {@link InvocationSequenceData} and passes the data to any chained processor that it has.
 *
 * @author Ivan Senic
 *
 */
public class InvocationExtractorDataProcessor extends AbstractExtractorDataProcessor {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -3793308278679460679L;

	/**
	 * Default constructor.
	 *
	 * @param chainedDataProcessors
	 *            List of the processors that will have the children of invocation passed to.
	 */
	public InvocationExtractorDataProcessor(List<AbstractDataProcessor> chainedDataProcessors) {
		super(chainedDataProcessors);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void processData(DefaultData defaultData) {
		if (defaultData instanceof InvocationSequenceData) {
			InvocationSequenceData invocation = (InvocationSequenceData) defaultData;
			extractDataFromInvocation(invocation);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

	/**
	 * Extract data from the invocation and return it to the storage writer to process it.
	 *
	 * @param invocation
	 *            {@link InvocationSequenceData}
	 */
	@SuppressWarnings("unchecked")
	private void extractDataFromInvocation(InvocationSequenceData invocation) {
		if (null != invocation.getTimerData()) {
			passToChainedProcessors(invocation.getTimerData());
		}
		if (null != invocation.getSqlStatementData()) {
			passToChainedProcessors(invocation.getSqlStatementData());
		}
		if (null != invocation.getExceptionSensorDataObjects()) {
			for (ExceptionSensorData exceptionSensorData : (List<ExceptionSensorData>) invocation.getExceptionSensorDataObjects()) {
				if (exceptionSensorData.getExceptionEvent() == ExceptionEvent.CREATED) {
					passToChainedProcessors(exceptionSensorData);
				}
			}
		}

		for (InvocationSequenceData child : (List<InvocationSequenceData>) invocation.getNestedSequences()) {
			extractDataFromInvocation(child);
		}
	}

}
