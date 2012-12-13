package info.novatec.inspectit.communication.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class to easier query {@link InvocationSequenceData} objects.
 * 
 * @author Stefan Siegl
 */
public class InvocationSequenceDataHelper {

	/**
	 * Checks if the invocation sequence data object itself contains parameters.
	 * 
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return if this data object contains captured parameters.
	 */
	public static boolean hasCapturedParametersInInvocationSequence(InvocationSequenceData data) {
		return null != data.getParameterContentData() && !data.getParameterContentData().isEmpty();
	}

	/**
	 * Checks whether the invocation sequence data object itself or some nested data element
	 * (timerdata) provides captured parameters.
	 * 
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether the invocation sequence data object itself or some nested data element
	 *         (timerdata) provides captured parameters.
	 */
	public static boolean hasCapturedParameters(InvocationSequenceData data) {
		if (hasCapturedParametersInInvocationSequence(data)) {
			return true;
		}
		return (hasTimerData(data) && null != data.getTimerData().getParameterContentData() && !data.getTimerData().getParameterContentData().isEmpty());
	}

	/**
	 * Returns the captured data of the invocation sequence.
	 * 
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return the captured data of the invocation sequence.
	 */
	public static Set<ParameterContentData> getCapturedParameters(InvocationSequenceData data) {
		Set<ParameterContentData> parameterContents = new HashSet<ParameterContentData>(data.getParameterContentData());
		if (hasTimerData(data)) {
			parameterContents.addAll(data.getTimerData().getParameterContentData());
		}
		return parameterContents;
	}

	/**
	 * Checks whether this data object contains a timer data object of some sort.
	 * 
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object contains a timer data object.
	 */
	public static boolean hasTimerData(InvocationSequenceData data) {
		return null != data.getTimerData();
	}

	/**
	 * Checks whether this data object contains a http timer data object.
	 * 
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object contains a http timer data object.
	 */
	public static boolean hasHttpTimerData(InvocationSequenceData data) {
		return hasTimerData(data) && data.getTimerData().getClass().equals(HttpTimerData.class);
	}

	/**
	 * Checks whether this data object contains exception data.
	 * 
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object contains exception data.
	 */
	public static boolean hasExceptionData(InvocationSequenceData data) {
		return null != data.getExceptionSensorDataObjects() && !data.getExceptionSensorDataObjects().isEmpty();
	}

	/**
	 * Checks whether this data object contains SQL data.
	 * 
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object contains SQL data.
	 */
	public static boolean hasSQLData(InvocationSequenceData data) {
		return null != data.getSqlStatementData() && 1 == data.getSqlStatementData().getCount();
	}

	/**
	 * Checks whether this data object has a parent element.
	 * 
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object has a parent element.
	 */
	public static boolean hasParentElementInSequence(InvocationSequenceData data) {
		return null != data.getParentSequence();
	}

	/**
	 * Checks whether this data object is the root element of the invocation.
	 * 
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object is the root element of the invocation.
	 */
	public static boolean isRootElementInSequence(InvocationSequenceData data) {
		return hasParentElementInSequence(data) == false;
	}

	/**
	 * Checks whether this data object has nested SQL statements.
	 * 
	 * @param data
	 *            {@link InvocationSequenceData}
	 * @return True if it has nested SQLs, salse otherwise.
	 */
	public static boolean hasNestedSqlStatements(InvocationSequenceData data) {
		return null != data.isNestedSqlStatements() && data.isNestedSqlStatements().booleanValue();
	}

	/**
	 * Checks whether this data object has nested SQL statements.
	 * 
	 * @param data
	 *            {@link InvocationSequenceData}
	 * @return True if it has nested SQLs, salse otherwise.
	 */
	public static boolean hasNestedExceptions(InvocationSequenceData data) {
		return null != data.isNestedExceptions() && data.isNestedExceptions().booleanValue();
	}

	/**
	 * Calculates the duration starting from this invocation sequence data element.
	 * 
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return the duration starting from this invocation sequence data element.
	 */
	public static double calculateDuration(InvocationSequenceData data) {
		double duration = -1.0d;
		if (InvocationSequenceDataHelper.hasTimerData(data)) {
			duration = data.getTimerData().getDuration();
		} else if (InvocationSequenceDataHelper.hasSQLData(data)) {
			duration = data.getSqlStatementData().getDuration();
		} else if (InvocationSequenceDataHelper.isRootElementInSequence(data)) {
			duration = data.getDuration();
		}
		return duration;
	}

	/**
	 * Computes the duration of the nested invocation elements.
	 * 
	 * @param data
	 *            The data objects which is inspected for its nested elements.
	 * @return The duration of all nested sequences (with their nested sequences as well).
	 */
	public static double computeNestedDuration(InvocationSequenceData data) {
		if (data.getNestedSequences().isEmpty()) {
			return 0;
		}

		double nestedDuration = 0d;
		boolean added = false;
		for (InvocationSequenceData nestedData : (List<InvocationSequenceData>) data.getNestedSequences()) {
			if (hasTimerData(nestedData)) {
				nestedDuration = nestedDuration + nestedData.getTimerData().getDuration();
				added = true;
			} else if (hasSQLData(nestedData)) {
				nestedDuration = nestedDuration + nestedData.getSqlStatementData().getDuration();
				added = true;
			} else if (isRootElementInSequence(nestedData)) {
				nestedDuration = nestedDuration + nestedData.getDuration();
				added = true;
			}
			if (!added && !nestedData.getNestedSequences().isEmpty()) {
				// nothing was added, but there could be child elements with
				// time measurements
				nestedDuration = nestedDuration + computeNestedDuration(nestedData);
			}
			added = false;
		}

		return nestedDuration;
	}
}
