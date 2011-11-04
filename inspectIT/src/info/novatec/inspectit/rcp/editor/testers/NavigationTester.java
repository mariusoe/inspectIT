package info.novatec.inspectit.rcp.editor.testers;

import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Tester for all navigations.
 * 
 * @author Ivan Senic
 * 
 */
public class NavigationTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("canNavigateToPlotting".equals(property)) {
			if (receiver instanceof StructuredSelection) {
				StructuredSelection selection = (StructuredSelection) receiver;
				Object selectedObject = selection.getFirstElement();
				if (selectedObject instanceof InvocationSequenceData) {
					// only navigate if a real TimerData is provided (not for HttpTimerData or SQL)
					TimerData timerData = ((InvocationSequenceData) selectedObject).getTimerData();
					return null != timerData && timerData.getClass().equals(TimerData.class);
				} else if (selectedObject.getClass().equals(TimerData.class)) {
					return true;
				}
			}
		} else if ("canNavigateToInvocations".equals(property)) {
			if (receiver instanceof StructuredSelection) {
				StructuredSelection selection = (StructuredSelection) receiver;
				for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
					Object selectedObject = iterator.next();
					if (selectedObject instanceof InvocationAwareData) {
						InvocationAwareData invocationAwareData = (InvocationAwareData) selectedObject;
						if (!invocationAwareData.isOnlyFoundOutsideInvocations()) {
							return true;
						}
					}
				}
			}
		} else if ("canNavigateToExceptionType".equals(property)) {
			StructuredSelection selection = (StructuredSelection) receiver;
			Object selectedObject = selection.getFirstElement();
			if (selectedObject instanceof InvocationSequenceData) {
				List<ExceptionSensorData> exceptions = ((InvocationSequenceData) selectedObject).getExceptionSensorDataObjects();
				if (null != exceptions && !exceptions.isEmpty()) {
					for (ExceptionSensorData exceptionSensorData : exceptions) {
						if (null != exceptionSensorData.getThrowableType()) {
							return true;
						}
					}
				}
			} else if (selectedObject instanceof ExceptionSensorData) {
				return ((ExceptionSensorData) selectedObject).getThrowableType() != null;
			}
		}

		return false;
	}

}
