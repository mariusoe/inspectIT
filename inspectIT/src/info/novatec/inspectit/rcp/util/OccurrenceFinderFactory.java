package info.novatec.inspectit.rcp.util;

import info.novatec.inspectit.communication.ExceptionEventEnum;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;

import java.util.List;

import org.apache.commons.lang.mutable.MutableInt;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Factory for finding the occurrence of elements in the {@link InvocationSequenceData} based on the
 * element type.
 * 
 * @author Ivan Senic
 * 
 */
public final class OccurrenceFinderFactory {

	/**
	 * Private constructor because of the factory.
	 */
	private OccurrenceFinderFactory() {
	}

	/**
	 * Occurrence finder for {@link TimerData}.
	 */
	private static TimerOccurrenceFinder timerOccurrenceFinder = new TimerOccurrenceFinder();

	/**
	 * Occurrence finder for {@link SqlStatementData}.
	 */
	private static SqlOccurrenceFinder sqlOccurrenceFinder = new SqlOccurrenceFinder();

	/**
	 * Occurrence finder for {@link ExceptionSensorData}.
	 */
	private static ExceptionOccurrenceFinder exceptionOccurrenceFinder = new ExceptionOccurrenceFinder();

	/**
	 * Counts number of occurrences of the element in the given invocation.
	 * 
	 * @param invocation
	 *            Invocation to search in.
	 * @param element
	 *            Wanted element.
	 * @param filters
	 *            Array of filters that each found occurrence has to pass.
	 * @return Number of occurrences found and filtered.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ElementOccurrenceCount getOccurrenceCount(InvocationSequenceData invocation, Object element, ViewerFilter[] filters) {
		OccurrenceFinder finder = getOccurrenceFinder(element);
		return finder.getOccurrenceCount(invocation, element, filters, null);
	}

	/**
	 * Returns the {@link InvocationSequenceData} that holds the proper occurrence of the wanted
	 * element if it exists.
	 * 
	 * @param invocation
	 *            Invocation to serach in.
	 * @param element
	 *            Wanted element.
	 * @param occurrence
	 *            Wanted occurrence.
	 * @param filters
	 *            Array of filters that each found occurrence has to pass.
	 * @return Returns the {@link InvocationSequenceData} that holds the proper occurrence of the
	 *         wanted element if it exists.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static InvocationSequenceData getOccurrence(InvocationSequenceData invocation, Object element, int occurrence, ViewerFilter[] filters) {
		OccurrenceFinder finder = getOccurrenceFinder(element);
		return finder.getOccurrence(invocation, element, new MutableInt(occurrence), filters);
	}

	/**
	 * Returns the proper {@link OccurrenceFinder} for the given element, based on elements class.
	 * 
	 * @param element
	 *            Element.
	 * @return {@link OccurrenceFinder} or null if the finder does not exists for the given object
	 *         type.
	 */
	@SuppressWarnings("rawtypes")
	private static OccurrenceFinder getOccurrenceFinder(Object element) {
		if (element.getClass().equals(SqlStatementData.class)) {
			return sqlOccurrenceFinder;
		} else if (element.getClass().equals(TimerData.class)) {
			return timerOccurrenceFinder;
		} else if (ExceptionSensorData.class.isAssignableFrom(element.getClass())) {
			return exceptionOccurrenceFinder;
		}
		RuntimeException exception = new  RuntimeException("Occurrence finder factory was not able to supply the correct occurrence finder for the object of class " + element.getClass().getName() + ".");
		InspectIT.getDefault().createErrorDialog("Exception thrown during locating of stepping object.", exception, -1);
		throw exception;
	}

	/**
	 * Abstract class that holds the shared functionality of all occurrence finders.
	 * 
	 * @author Ivan Senic
	 * 
	 * @param <E>
	 *            Type of the element finder can locate.
	 */
	private abstract static class OccurrenceFinder<E> {

		/**
		 * Returns the number of children objects in invocation sequence that have the wanted
		 * template object defined. This method is recursive, and traverse the whole invocation
		 * tree.
		 * 
		 * @param invocationData
		 *            Top parent invocation sequence.
		 * @param template
		 *            Template data to search for.
		 * @param filters
		 *            Active filters of the tree viewer.
		 * @param elementOccurrence
		 *            Element occurrence count.
		 * @return Number of children in invocation that have template data set.
		 */
		@SuppressWarnings("unchecked")
		public ElementOccurrenceCount getOccurrenceCount(InvocationSequenceData invocationData, E template, ViewerFilter[] filters, ElementOccurrenceCount elementOccurrence) {
			if (!getConcreteClass().isAssignableFrom(template.getClass())) {
				return null;
			}
			ElementOccurrenceCount occurrenceCount;
			if (null == elementOccurrence) {
				occurrenceCount = new ElementOccurrenceCount();
			} else {
				occurrenceCount = elementOccurrence;
			}

			boolean found = occurrenceFound(invocationData, template);
			if (found && filtersPassed(invocationData, filters)) {
				occurrenceCount.increaseVisibleOccurrences();
			} else if (found) {
				occurrenceCount.increaseFilteredOccurrences();
			}

			if (null != invocationData.getNestedSequences()) {
				for (InvocationSequenceData child : (List<InvocationSequenceData>) invocationData.getNestedSequences()) {
					getOccurrenceCount(child, template, filters, occurrenceCount);
				}
			}
			return occurrenceCount;
		}

		/**
		 * Returns the {@link InvocationSequenceData} object that has the wanted template data
		 * object defined. The wanted occurrence of E object is defined via {@link #occurrencesLeft}
		 * , before this method is called. This method is recursive, and stops traversing the
		 * invocation sequence tree as soon the wanted element is found.
		 * 
		 * @param invocationData
		 *            Top parent invocation sequence.
		 * @param template
		 *            Template data.
		 * @param occurrencesLeft
		 *            Occurrence to search for.
		 * @param filters
		 *            Active filters of the tree viewer.
		 * @return Invocation sequence that has the Exception data set in Exceptions list and is
		 *         same as template data.
		 */
		@SuppressWarnings("unchecked")
		public InvocationSequenceData getOccurrence(InvocationSequenceData invocationData, E template, MutableInt occurrencesLeft, ViewerFilter[] filters) {
			if (!getConcreteClass().isAssignableFrom(template.getClass())) {
				return null;
			}
			if (occurrenceFound(invocationData, template) && filtersPassed(invocationData, filters)) {
				occurrencesLeft.decrement();
				if (occurrencesLeft.intValue() == 0) {
					return invocationData;
				}
			}
			if (null != invocationData.getNestedSequences()) {
				for (InvocationSequenceData child : (List<InvocationSequenceData>) invocationData.getNestedSequences()) {
					InvocationSequenceData foundData = getOccurrence(child, template, occurrencesLeft, filters);
					if (null != foundData) {
						return foundData;
					}
				}
			}
			return null;
		}

		/**
		 * Returns if the template objects is found in the invocation data.
		 * 
		 * @param invocationData
		 *            Invocation data to look in.
		 * @param template
		 *            Template object.
		 * @return Return depends on the implementing classes.
		 */
		public abstract boolean occurrenceFound(InvocationSequenceData invocationData, E template);

		/**
		 * Returns the concrete class that finder is working with.
		 * 
		 * @return Returns the concrete class that finder is working with.
		 */
		public abstract Class<E> getConcreteClass();

		/**
		 * Returns if the invocation data object is passing all given filters.
		 * 
		 * @param invocationData
		 *            Invocation data.
		 * @param filters
		 *            Array of filters.
		 * @return True if all filters are passed, or filters array is null or empty.
		 */
		private boolean filtersPassed(InvocationSequenceData invocationData, ViewerFilter[] filters) {
			boolean passed = true;
			if (null != filters) {
				for (ViewerFilter filter : filters) {
					if (!filter.select(null, invocationData.getParentSequence(), invocationData)) {
						passed = false;
						break;
					}
				}
			}
			return passed;
		}

	}

	/**
	 * Occurrence finder for {@link ExceptionSensorData}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class ExceptionOccurrenceFinder extends OccurrenceFinder<ExceptionSensorData> {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public boolean occurrenceFound(InvocationSequenceData invocationData, ExceptionSensorData exceptionSensorData) {
			if (invocationData.getExceptionSensorDataObjects() != null) {
				for (ExceptionSensorData exData : (List<ExceptionSensorData>) invocationData.getExceptionSensorDataObjects()) {
					if (exData.getExceptionEvent().equals(ExceptionEventEnum.CREATED) && exData.getThrowableType().equals(exceptionSensorData.getThrowableType())) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public Class<ExceptionSensorData> getConcreteClass() {
			return ExceptionSensorData.class;
		}

	}

	/**
	 * Occurrence finder for {@link SqlStatementData}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class SqlOccurrenceFinder extends OccurrenceFinder<SqlStatementData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean occurrenceFound(InvocationSequenceData invocationData, SqlStatementData sqlStatementData) {
			if (invocationData.getSqlStatementData() != null) {
				if (invocationData.getSqlStatementData().getMethodIdent() == sqlStatementData.getMethodIdent() && invocationData.getSqlStatementData().getSql().equals(sqlStatementData.getSql())) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<SqlStatementData> getConcreteClass() {
			return SqlStatementData.class;
		}

	}

	/**
	 * Occurrence finder for {@link TimerData}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class TimerOccurrenceFinder extends OccurrenceFinder<TimerData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean occurrenceFound(InvocationSequenceData invocationData, TimerData timerData) {
			if (invocationData.getTimerData() != null) {
				if (invocationData.getTimerData().getMethodIdent() == timerData.getMethodIdent()) {
					return true;
				}
			} else {
				if (invocationData.getMethodIdent() == timerData.getMethodIdent()) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<TimerData> getConcreteClass() {
			return TimerData.class;
		}

	}
}
