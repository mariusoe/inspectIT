package info.novatec.inspectit.rcp.editor.tree.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.tree.TreeViewerComparator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;

/**
 * The abstract class of the {@link TreeInputController} interface to provide
 * some standard methods.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class AbstractTreeInputController implements TreeInputController {

	/**
	 * The input definition.
	 */
	private InputDefinition inputDefinition;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		Assert.isNotNull(inputDefinition);

		this.inputDefinition = inputDefinition;
	}

	/**
	 * Returns the input definition.
	 * 
	 * @return The input definition.
	 */
	protected InputDefinition getInputDefinition() {
		Assert.isNotNull(inputDefinition);

		return inputDefinition;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public boolean canOpenInput(List<? extends DefaultData> data) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Do nothing by default, sub-classes may override.
	 */
	public void createColumns(TreeViewer treeViewer) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Do nothing by default, sub-classes may override.
	 */
	public void doRefresh(IProgressMonitor monitor) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public TreeViewerComparator<? extends DefaultData> getComparator() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public IContentProvider getContentProvider() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public ViewerFilter[] getFilters() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public IBaseLabelProvider getLabelProvider() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return an empty set by default, sub-classes may override.
	 */
	public Set<PreferenceId> getPreferenceIds() {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public String getReadableString(Object object) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <code>null</code> by default, sub-classes may override.
	 */
	public Object getTreeInput() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Do nothing by default, sub-classes may override.
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Do nothing by default, sub-classes may override.
	 */
	public void showDetails(Shell parent, Object element) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Return <b>2</b> by default, sub-classes may override.
	 */
	public int getExpandLevel() {
		return 2;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Do nothing by default, sub-classes may override.
	 */
	public void dispose() {
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * By default controller sets the sub view to be master.
	 */
	@Override
	public SubViewClassification getSubViewClassification() {
		return SubViewClassification.MASTER;
	}
}
