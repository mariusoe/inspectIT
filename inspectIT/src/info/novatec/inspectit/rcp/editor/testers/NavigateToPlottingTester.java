package info.novatec.inspectit.rcp.editor.testers;

import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.TimerData;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Tester for timer data in {@link InvocationSequenceData}.
 * 
 * @author Ivan Senic
 * 
 */
public class NavigateToPlottingTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) receiver;
			Object selectedObject = selection.getFirstElement();
			if (selectedObject instanceof InvocationSequenceData) {
				// only navigate if a real TimerData is provided (not for HttpTimerData or SQL)
				TimerData timerData = ((InvocationSequenceData) selectedObject).getTimerData();
				if (null != timerData && timerData.getClass().equals(TimerData.class)) {
					return true;
				} else {
					return false;
				}
			} else if (selectedObject.getClass().equals(TimerData.class)) {
				return true;
			}
		}
		return false;
	}

}
