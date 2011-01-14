package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A extension of the {@link InvocOverviewInputController} that displays the invocations that are
 * statically transfered to the view via invocation aware data.
 * 
 * @author Ivan Senic
 * 
 */
public class NavigationInvocOverviewInputController extends InvocOverviewInputController {
	
	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.navigationinvocoverview";

	/**
	 * List of all invocation sequences that can be displayed.
	 */
	private List<InvocationAwareData> invocationAwareDataList;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		invocationAwareDataList = (List<InvocationAwareData>) inputDefinition.getAdditionalOption("invocationAwareDataList");

		super.setInputDefinition(inputDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		preferences.add(PreferenceId.CLEAR_BUFFER);
		preferences.add(PreferenceId.ITEMCOUNT);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void doRefresh(IProgressMonitor monitor) {
		monitor.beginTask("Updating Invocation Overview", IProgressMonitor.UNKNOWN);
		monitor.subTask("Retrieving the Invocation Overview from the CMR");
		List<InvocationSequenceData> invocData;
		Set<Long> invocationIdsSet = new HashSet<Long>();
		for (InvocationAwareData invocationAwareData : invocationAwareDataList) {
			if (null != invocationAwareData.getInvocationParentsIdSet()) {
				invocationIdsSet.addAll(invocationAwareData.getInvocationParentsIdSet());
			}
		}
		long platformIdent = getInputDefinition().getIdDefinition().getPlatformId();
		invocData = getDataAccessService().getInvocationSequenceOverview(platformIdent, invocationIdsSet, getLimit());
		getInvocationSequenceData().clear();
		if (!invocData.isEmpty()) {
			monitor.subTask("Displaying the Invocation Overview");
			getInvocationSequenceData().addAll(invocData);
		}
		monitor.done();
	}

}
