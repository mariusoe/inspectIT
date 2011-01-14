package info.novatec.inspectit.rcp.editor.testers;

import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.handlers.NavigateToInvocationsHandler;

import java.util.Iterator;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Tester for nadler {@link NavigateToInvocationsHandler}. Test if the given
 * {@link IInvocationAwareData} has any {@link InvocationSequenceData} for navigation.
 * 
 * @author Ivan Senic
 * 
 */
public class NavigateToInvocationsTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
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
		return false;
	}

}
