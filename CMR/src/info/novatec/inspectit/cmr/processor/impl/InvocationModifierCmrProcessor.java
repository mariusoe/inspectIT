package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.processor.AbstractChainedCmrDataProcessor;
import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.InvocationSequenceDataHelper;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.StatelessSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * d performing necessary calculation and fixes. This is special type of chained processor that does
 * not pass the incoming object to the chained processors, but might do so with some other objects.
 * 
 * @author Ivan Senic
 * 
 */
public class InvocationModifierCmrProcessor extends AbstractChainedCmrDataProcessor {

	/**
	 * Message processor for exception that we need to call directly. It's because we need to do
	 * that for all exceptions, but we will send only one to the chained processors, cause in the
	 * chain there will be indexed and stuff and we don’t want that for all exceptions, but only
	 * that survive constructor delegation.
	 */
	@Autowired
	ExceptionMessageCmrProcessor exceptionMessageCmrProcessor;

	/**
	 * Default constructor.
	 * 
	 * @param dataProcessors
	 *            Chained processors.
	 */
	public InvocationModifierCmrProcessor(List<AbstractCmrDataProcessor> dataProcessors) {
		super(dataProcessors);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void processData(DefaultData defaultData, StatelessSession session) {
		InvocationSequenceData invocation = (InvocationSequenceData) defaultData;
		extractDataFromInvocation(session, invocation, invocation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean shouldBePassedToChainedProcessors(DefaultData defaultData) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

	/**
	 * Extract data from the invocation in the way that timer data is saved to the Db, while SQL
	 * statements and Exceptions are indexed into the root branch.
	 * 
	 * @param session
	 *            Session needed for DB persistence.
	 * @param invData
	 *            Invocation data to be extracted.
	 * @param topInvocationParent
	 *            Top invocation object.
	 * 
	 */
	private void extractDataFromInvocation(StatelessSession session, InvocationSequenceData invData, InvocationSequenceData topInvocationParent) {
		double exclusiveDurationDelta = 0d;
		Collection<InvocationSequenceData> childRemoveCollection = null;
		Set<Long> identityHashCodeSet = null;

		for (InvocationSequenceData child : (List<InvocationSequenceData>) invData.getNestedSequences()) {
			// pass child to chained processors
			passToChainedProcessors(child, session);

			// include times from timer, sql or invocation itself
			if (null != child.getTimerData()) {
				exclusiveDurationDelta += child.getTimerData().getDuration();
			} else if (null != child.getSqlStatementData()) {
				// I don't know if the situation that both timer and sql are set in one
				// invocation, but just to be sure I only include the time of the sql, if i did
				// not already included the time of the timer before
				exclusiveDurationDelta += child.getSqlStatementData().getDuration();
			} else {
				exclusiveDurationDelta += InvocationSequenceDataHelper.computeNestedDuration(child);
			}

			// deal with exceptions
			if (null == identityHashCodeSet && CollectionUtils.isNotEmpty(child.getExceptionSensorDataObjects())) {
				identityHashCodeSet = new HashSet<>();
			}
			childRemoveCollection = processExceptionSensorData(session, child, topInvocationParent, identityHashCodeSet, childRemoveCollection);

			// go to the recursion
			extractDataFromInvocation(session, child, topInvocationParent);
		}

		// process the SQL Statement and Timer
		processSqlStatementData(session, invData, topInvocationParent);
		processTimerData(session, invData, topInvocationParent, exclusiveDurationDelta);

		// alter size if some were removed
		if (CollectionUtils.isNotEmpty(childRemoveCollection)) {
			int oldChildrenListSize = invData.getNestedSequences().size();
			invData.getNestedSequences().removeAll(childRemoveCollection);
			int newChildrenListSize = invData.getNestedSequences().size();

			InvocationSequenceData alterChildCountInvocation = invData;
			int alterSize = oldChildrenListSize - newChildrenListSize;
			while (null != alterChildCountInvocation) {
				alterChildCountInvocation.setChildCount(alterChildCountInvocation.getChildCount() - alterSize);
				alterChildCountInvocation = alterChildCountInvocation.getParentSequence();
			}
		}
	}

	/**
	 * Process SQL statement if one exists in the invData object and passes it to the chained
	 * processors.
	 * 
	 * @param session
	 *            Session needed for DB persistence.
	 * @param invData
	 *            Invocation data to be processed.
	 * @param topInvocationParent
	 *            Top invocation object.
	 */
	private void processSqlStatementData(StatelessSession session, InvocationSequenceData invData, InvocationSequenceData topInvocationParent) {
		SqlStatementData sqlStatementData = invData.getSqlStatementData();
		if (null != sqlStatementData) {
			topInvocationParent.setNestedSqlStatements(Boolean.TRUE);
			sqlStatementData.addInvocationParentId(topInvocationParent.getId());
			passToChainedProcessors(sqlStatementData, session);
		}
	}

	/**
	 * Process timer data if one exists in the invData object and passes it to the chained
	 * processors.
	 * 
	 * @param session
	 *            Session needed for DB persistence.
	 * @param invData
	 *            Invocation data to be processed.
	 * @param topInvocationParent
	 *            Top invocation object.
	 * @param exclusiveDurationDelta
	 *            Duration to subtract from timer duration to get the exclusive duration.
	 */
	private void processTimerData(StatelessSession session, InvocationSequenceData invData, InvocationSequenceData topInvocationParent, double exclusiveDurationDelta) {
		TimerData timerData = invData.getTimerData();
		if (null != timerData) {
			double exclusiveTime = invData.getTimerData().getDuration() - exclusiveDurationDelta;
			timerData.setExclusiveCount(1L);
			timerData.setExclusiveDuration(exclusiveTime);
			timerData.calculateExclusiveMax(exclusiveTime);
			timerData.calculateExclusiveMin(exclusiveTime);
			timerData.addInvocationParentId(topInvocationParent.getId());
			passToChainedProcessors(invData.getTimerData(), session);
		}
	}

	/**
	 * Process all the exceptions in the invData, deal with constructor delegation and passes
	 * exceptions that survive to the chained processors.<br>
	 * <br>
	 * Only the exceptions which identity hash code was not yet processed will be processed. Due to
	 * the
	 * {@link #manageExceptionConstructorDelegation(InvocationSequenceData, ExceptionSensorData, Collection)
	 * constructor delegation problem} it can happen than exception in the invocation has already
	 * been processed when it's super exception has been processed. For example when we have
	 * constructor delegation problem as following: <br>
	 * <br>
	 * Parent invocation:
	 * <ul>
	 * <li>Child 1 -> Exception: new Exception() PROCESSED</li>
	 * <li>Child 2 -> Exception: new BusinessException() PROCESSED IN CDM</li>
	 * <li>Child 3 -> Exception: new ConcreteBusinessException() PROCESSED IN CDM</li>
	 * </ul>
	 * the second and third exceptions are skipped by this method cause they are already processed
	 * in
	 * {@link #manageExceptionConstructorDelegation(InvocationSequenceData, ExceptionSensorData, Collection)
	 * constructor delegation management}.<br>
	 * <br>
	 * Note also that only exception data with CREATED event are processed, since the PASSED and
	 * HANDLED should be connected as children to the CREATED one.
	 * 
	 * @param session
	 *            Session needed for DB persistence.
	 * @param invData
	 *            Invocation data to be processed.
	 * @param topInvocationParent
	 *            Top invocation object.
	 * @param identityHashCodeSet
	 *            Set of all throwable identity hash code that have been already been processed.
	 *            This set might be updated in the method. Can be <code>null</code>.
	 * @param childRemoveCollection
	 *            List to add all children that should be removed from invocation. Can be
	 *            <code>null</code>.
	 * @return Returns back the child remove collection. It might happen that it's initialized in
	 *         the method and thus must be correctly returned to the caller.
	 */
	private Collection<InvocationSequenceData> processExceptionSensorData(StatelessSession session, InvocationSequenceData invData, InvocationSequenceData topInvocationParent,
			Set<Long> identityHashCodeSet, Collection<InvocationSequenceData> childRemoveCollection) {
		if (CollectionUtils.isNotEmpty(invData.getExceptionSensorDataObjects())) {
			for (ExceptionSensorData exceptionData : invData.getExceptionSensorDataObjects()) {
				if (exceptionData.getExceptionEvent() == ExceptionEvent.CREATED) {
					// only if created exception is in invocation set to the parent
					topInvocationParent.setNestedExceptions(Boolean.TRUE);

					// we need to directly call Exception message processor, cause it can not be
					// chained
					exceptionMessageCmrProcessor.process(exceptionData, session);

					// manage constructor delegation
					if (identityHashCodeSet.add(exceptionData.getThrowableIdentityHashCode())) {
						Pair<ExceptionSensorData, Collection<InvocationSequenceData>> result = manageExceptionConstructorDelegation(invData.getParentSequence(), exceptionData, childRemoveCollection);
						childRemoveCollection = result.getSecond();
						ExceptionSensorData dataToIndex = result.getFirst();
						if (null != dataToIndex) {
							dataToIndex.addInvocationParentId(topInvocationParent.getId());
							passToChainedProcessors(dataToIndex, session);
						}
					}
				}
			}
		}
		return childRemoveCollection;
	}

	/**
	 * This method checks if the one invocation has as children the exceptions that are wrongly
	 * created as effect of constructor delegation. For example:<br>
	 * <br>
	 * Parent invocation:
	 * <ul>
	 * <li>Child 1 -> Exception: new Exception()</li>
	 * <li>Child 2 -> Exception: new BusinessException()</li>
	 * <li>Child 3 -> Exception: new ConcreteBusinessException()</li>
	 * </ul>
	 * Cause all there exception classes are instrumented, here we wrongly get three exception
	 * objects, instead of just one (ConcreteBusinessException) which was in fact created. Thus we
	 * need to remove the first two from the invocation. <br>
	 * <br>
	 * The removed ones will be added to the child remove collection.
	 * 
	 * @param parent
	 *            Invocation parent where the exception data is found.
	 * @param firstExceptionData
	 *            Exception that needs to be handled.
	 * @param childRemoveCollection
	 *            Collection where invocation children for removal will be added.
	 * @return Returns exception data to be indexed (ConcreteBusinessException in example) and child
	 *         remove collection as a {@link Pair}. It might happen that collection might be
	 *         initialized in the method and thus must be correctly returned to the caller.
	 */
	private Pair<ExceptionSensorData, Collection<InvocationSequenceData>> manageExceptionConstructorDelegation(InvocationSequenceData parent, ExceptionSensorData firstExceptionData,
			Collection<InvocationSequenceData> childRemoveCollection) {
		InvocationSequenceData lastInvocationChild = null;
		ExceptionSensorData lastExceptionData = null;
		long identityHashCode = firstExceptionData.getThrowableIdentityHashCode();
		long sensorTypeIdent = -1;
		int lastExceptionDataChildIndex = -1;

		for (int i = 0, j = parent.getNestedSequences().size(); i < j; i++) {
			InvocationSequenceData invData = (InvocationSequenceData) parent.getNestedSequences().get(i);
			if (null != invData.getExceptionSensorDataObjects()) {
				for (ExceptionSensorData exData : (List<ExceptionSensorData>) invData.getExceptionSensorDataObjects()) {
					if (exData.getThrowableIdentityHashCode() == identityHashCode) {
						if (null != lastInvocationChild) {
							lastInvocationChild.setExceptionSensorDataObjects(null);
						}
						lastInvocationChild = invData;
						lastExceptionData = exData;
						lastExceptionDataChildIndex = i;
						sensorTypeIdent = invData.getSensorTypeIdent();
					}
				}
			}
		}

		lastExceptionDataChildIndex--;
		while (lastExceptionDataChildIndex >= 0) {
			InvocationSequenceData invData = (InvocationSequenceData) parent.getNestedSequences().get(lastExceptionDataChildIndex);
			if (invData.getSensorTypeIdent() != sensorTypeIdent || null != invData.getTimerData() || null != invData.getSqlStatementData() || null != invData.getExceptionSensorDataObjects()) {
				break;
			}
			// init the child collection if it's not been yet initialized
			if (null == childRemoveCollection) {
				childRemoveCollection = new ArrayList<>();
			}
			childRemoveCollection.add(invData);
			lastExceptionDataChildIndex--;
		}

		if (null != lastExceptionData) {
			return new Pair<ExceptionSensorData, Collection<InvocationSequenceData>>(lastExceptionData, childRemoveCollection);
		} else {
			return new Pair<ExceptionSensorData, Collection<InvocationSequenceData>>(firstExceptionData, childRemoveCollection);
		}
	}

	/**
	 * Simple utility class to help returning two objects from method.
	 * 
	 * @author Ivan Senic
	 * 
	 * @param <P>
	 *            First object
	 * @param <R>
	 *            Second object
	 */
	private static final class Pair<P, R> {
		private P first; // NOCHK
		private R second; // NOCHK

		public Pair(P first, R second) { // NOCHK
			this.first = first;
			this.second = second;
		}

		public P getFirst() {
			return first;
		}

		public R getSecond() {
			return second;
		}

	}

}
