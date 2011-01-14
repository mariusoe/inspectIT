package info.novatec.inspectit.rcp.editor.testers;

import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
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
				// if it is invocation check for the timer data
				return null != ((InvocationSequenceData) selectedObject).getTimerData();
			} else if (selectedObject instanceof TimerData) {
				// if it is timer data, assure that is not SqlStatementData
				return !(selectedObject instanceof SqlStatementData);
			}
		}
		return false;
	}

}
