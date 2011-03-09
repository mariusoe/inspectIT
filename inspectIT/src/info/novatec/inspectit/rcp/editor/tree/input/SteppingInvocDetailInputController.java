package info.novatec.inspectit.rcp.editor.tree.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.ExceptionEventEnum;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.tree.SteppingTreeSubView;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.service.CachedGlobalDataAccessService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.mutable.MutableInt;

/**
 * Extension of the {@link InvocDetailInputController} adapted to serve as an input for a
 * {@link SteppingTreeSubView}.
 * 
 * @author Ivan Senic
 * 
 */
public class SteppingInvocDetailInputController extends InvocDetailInputController implements SteppingTreeInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.tree.steppinginvocdetail";

	/**
	 * List of the objects that are possible to locate in the tree.
	 */
	private List<Object> steppingObjectsList;

	/**
	 * Global data access service.
	 */
	private CachedGlobalDataAccessService globalDataAccessService;

	/**
	 * Is stepping control be initially visible.
	 */
	private boolean initVisible;

	/**
	 * {@link DefaultAbsoluteLocationPath} construct that defines if the stepping control is visible
	 * or not.
	 * 
	 * @param initVisible
	 *            Should stepping control be initially visible.
	 */
	public SteppingInvocDetailInputController(boolean initVisible) {
		this.initVisible = initVisible;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		Object steppingObj = inputDefinition.getAdditionalOption("steppingObjects");
		if (null != steppingObj) {
			steppingObjectsList = (List<Object>) steppingObj;
		} else {
			steppingObjectsList = new ArrayList<Object>();
		}

		globalDataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = super.getPreferenceIds();
		preferences.add(PreferenceId.STEPPABLE_CONTROL);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> getSteppingObjectList() {
		return steppingObjectsList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObjectToSteppingObjectList(Object element) {
		if (!steppingObjectsList.contains(element)) {
			steppingObjectsList.add(element);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean initSteppingControlVisible() {
		return initVisible;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int countOccurrences(Object element) {
		List<Object> input = (List<Object>) getTreeInput();
		if (input != null && !input.isEmpty()) {
			InvocationSequenceData invocation = (InvocationSequenceData) input.get(0);
			if (element instanceof SqlStatementData) {
				return getSqlOccurrenceCount(invocation, (SqlStatementData) element);
			} else if (element instanceof TimerData) {
				return getTimerOccurrenceCount(invocation, (TimerData) element);
			} else if (element instanceof ExceptionSensorData) {
				return getExceptionOccurrenceCount(invocation, (ExceptionSensorData) element);

			}
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isElementOccurrenceReachable(Object element, int occurance) {
		Object object = getElement(element, occurance);
		if (null != object) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getElement(Object template, int occurance) {
		List<Object> input = (List<Object>) getTreeInput();
		if (input != null && !input.isEmpty()) {
			InvocationSequenceData invocation = (InvocationSequenceData) input.get(0);
			MutableInt occurrencesLeft = new MutableInt(occurance);
			if (template instanceof SqlStatementData) {
				return getSqlOccurrence(invocation, (SqlStatementData) template, occurrencesLeft);
			} else if (template instanceof TimerData) {
				return getTimerOccurrence(invocation, (TimerData) template, occurrencesLeft);
			} else if (template instanceof ExceptionSensorData) {
				return getExceptionOccurrence(invocation, (ExceptionSensorData) template, occurrencesLeft);
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getElementTextualRepresentation(Object invAwareData) {
		if (invAwareData instanceof SqlStatementData) {
			SqlStatementData sqlData = (SqlStatementData) invAwareData;
			return "SQL: " + sqlData.getSql();
		} else if (invAwareData instanceof TimerData) {
			TimerData timerData = (TimerData) invAwareData;
			MethodIdent methodIdent = globalDataAccessService.getMethodIdentForId(timerData.getMethodIdent());
			return TextFormatter.getMethodString(methodIdent);
		} else if (invAwareData instanceof ExceptionSensorData) {
			ExceptionSensorData exData = (ExceptionSensorData) invAwareData;
			return "Exception: " + exData.getThrowableType();
		}
		return "";
	}

	/**
	 * Returns the {@link InvocationSequenceData} object that has the wanted timer data object
	 * defined. The wanted occurrence of timer data object is defined via {@link #occurrencesLeft},
	 * before this method is called. This method is recursive, and stops traversing the invocation
	 * sequence tree as soon the wanted element is found.
	 * 
	 * @param invocationData
	 *            Top parent invocation sequence.
	 * @param timerData
	 *            Template timer data.
	 * @param occurrencesLeft 
	 * @return Invocation sequence that has the timer data set and is same as template data.
	 */
	@SuppressWarnings("unchecked")
	private InvocationSequenceData getTimerOccurrence(InvocationSequenceData invocationData, TimerData timerData, MutableInt occurrencesLeft) {
		if (invocationData.getTimerData() != null) {
			if (invocationData.getTimerData().getMethodIdent() == timerData.getMethodIdent()) {
				occurrencesLeft.decrement();
				if (occurrencesLeft.intValue() == 0) {
					return invocationData;
				}
			}
		} else if (invocationData.getSqlStatementData() != null) {
			if (invocationData.getSqlStatementData().getMethodIdent() == timerData.getMethodIdent()) {
				occurrencesLeft.decrement();
				if (occurrencesLeft.intValue() == 0) {
					return invocationData;
				}
			}
		}
		
		if (null != invocationData.getNestedSequences()) {
			for (InvocationSequenceData child : (List<InvocationSequenceData>) invocationData.getNestedSequences()) {
				InvocationSequenceData foundData = getTimerOccurrence(child, timerData, occurrencesLeft);
				if (null != foundData) {
					return foundData;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the {@link InvocationSequenceData} object that has the wanted SQL data object
	 * defined. The wanted occurrence of SQL data object is defined via {@link #occurrencesLeft},
	 * before this method is called. This method is recursive, and stops traversing the invocation
	 * sequence tree as soon the wanted element is found.
	 * 
	 * @param invocationData
	 *            Top parent invocation sequence.
	 * @param sqlData
	 *            Template SQL data.
	 * @param occurrencesLeft 
	 * @return Invocation sequence that has the SQL data set and is same as template data.
	 */
	@SuppressWarnings("unchecked")
	private InvocationSequenceData getSqlOccurrence(InvocationSequenceData invocationData, SqlStatementData sqlData, MutableInt occurrencesLeft) {
		if (invocationData.getSqlStatementData() != null) {
			if (invocationData.getSqlStatementData().getMethodIdent() == sqlData.getMethodIdent() && invocationData.getSqlStatementData().getSql().equals(sqlData.getSql())) {
				occurrencesLeft.decrement();
				if (occurrencesLeft.intValue() == 0) {
					return invocationData;
				}
			}
		}
		if (null != invocationData.getNestedSequences()) {
			for (InvocationSequenceData child : (List<InvocationSequenceData>) invocationData.getNestedSequences()) {
				InvocationSequenceData foundData = getSqlOccurrence(child, sqlData, occurrencesLeft);
				if (null != foundData) {
					return foundData;
				}

			}
		}
		return null;
	}

	/**
	 * Returns the {@link InvocationSequenceData} object that has the wanted Exception data object
	 * defined. The wanted occurrence of Exception data object is defined via
	 * {@link #occurrencesLeft}, before this method is called. The method only returns the
	 * {@link ExceptionSensorData} that has a {@link ExceptionEventEnum#CREATED} value. This method
	 * is recursive, and stops traversing the invocation sequence tree as soon the wanted element is
	 * found.
	 * 
	 * @param invocationData
	 *            Top parent invocation sequence.
	 * @param templateExceptionData
	 *            Template Exception data.
	 * @param occurrencesLeft 
	 * @return Invocation sequence that has the Exception data set in Exceptions list and is same as
	 *         template data.
	 */
	@SuppressWarnings("unchecked")
	private InvocationSequenceData getExceptionOccurrence(InvocationSequenceData invocationData, ExceptionSensorData templateExceptionData, MutableInt occurrencesLeft) {
		if (invocationData.getExceptionSensorDataObjects() != null) {
			for (ExceptionSensorData exData : (List<ExceptionSensorData>) invocationData.getExceptionSensorDataObjects()) {
				if (exData.getExceptionEvent().equals(ExceptionEventEnum.CREATED) && exData.getThrowableType().equals(templateExceptionData.getThrowableType())) {
					occurrencesLeft.decrement();
					if (occurrencesLeft.intValue() == 0) {
						return invocationData;
					}
				}
			}
		}
		if (null != invocationData.getNestedSequences()) {
			for (InvocationSequenceData child : (List<InvocationSequenceData>) invocationData.getNestedSequences()) {
				InvocationSequenceData foundData = getExceptionOccurrence(child, templateExceptionData, occurrencesLeft);
				if (null != foundData) {
					return foundData;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the number of children objects in invocation sequence that have the wanted timer data
	 * object defined. This method is recursive, and traverse the whole invocation tree.
	 * 
	 * @param invocationData
	 *            Top parent invocation sequence.
	 * @param timerData
	 *            Template timer data.
	 * @return Number of children in invocation that have template data set.
	 */
	@SuppressWarnings("unchecked")
	private int getTimerOccurrenceCount(InvocationSequenceData invocationData, TimerData timerData) {
		int occurances = 0;
		if (invocationData.getTimerData() != null) {
			if (invocationData.getTimerData().getMethodIdent() == timerData.getMethodIdent()) {
				occurances++;
			}
		} else if (invocationData.getSqlStatementData() != null) {
			if (invocationData.getSqlStatementData().getMethodIdent() == timerData.getMethodIdent()) {
				occurances++;
			}
		}
		if (null != invocationData.getNestedSequences()) {
			for (InvocationSequenceData child : (List<InvocationSequenceData>) invocationData.getNestedSequences()) {
				occurances += getTimerOccurrenceCount(child, timerData);
			}
		}
		return occurances;
	}

	/**
	 * Returns the number of children objects in invocation sequence that have the wanted SQL data
	 * object defined. This method is recursive, and traverse the whole invocation tree.
	 * 
	 * @param invocationData
	 *            Top parent invocation sequence.
	 * @param sqlData
	 *            Template timer data.
	 * @return Number of children in invocation that have template data set.
	 */
	@SuppressWarnings("unchecked")
	private int getSqlOccurrenceCount(InvocationSequenceData invocationData, SqlStatementData sqlData) {
		int occurances = 0;
		if (invocationData.getSqlStatementData() != null) {
			if (invocationData.getSqlStatementData().getMethodIdent() == sqlData.getMethodIdent() && invocationData.getSqlStatementData().getSql().equals(sqlData.getSql())) {
				occurances++;
			}
		}
		if (null != invocationData.getNestedSequences()) {
			for (InvocationSequenceData child : (List<InvocationSequenceData>) invocationData.getNestedSequences()) {
				occurances += getSqlOccurrenceCount(child, sqlData);
			}
		}
		return occurances;
	}

	/**
	 * Returns the number of children objects in invocation sequence that have the wanted Exception
	 * data object defined. method will only count {@link ExceptionSensorData} that has
	 * {@link ExceptionEventEnum#CREATED} value. This method is recursive, and traverse the whole
	 * invocation tree.
	 * 
	 * @param invocationData
	 *            Top parent invocation sequence.
	 * @param templateExceptionData
	 *            Template exception data.
	 * @return Number of children in invocation that have template data set.
	 */
	@SuppressWarnings("unchecked")
	private int getExceptionOccurrenceCount(InvocationSequenceData invocationData, ExceptionSensorData templateExceptionData) {
		int occurances = 0;
		if (invocationData.getExceptionSensorDataObjects() != null) {
			for (ExceptionSensorData exData : (List<ExceptionSensorData>) invocationData.getExceptionSensorDataObjects()) {
				if (exData.getExceptionEvent().equals(ExceptionEventEnum.CREATED) && exData.getThrowableType().equals(templateExceptionData.getThrowableType())) {
					occurances++;
				}
			}
		}
		if (null != invocationData.getNestedSequences()) {
			for (InvocationSequenceData child : (List<InvocationSequenceData>) invocationData.getNestedSequences()) {
				occurances += getExceptionOccurrenceCount(child, templateExceptionData);
			}
		}
		return occurances;
	}

}
