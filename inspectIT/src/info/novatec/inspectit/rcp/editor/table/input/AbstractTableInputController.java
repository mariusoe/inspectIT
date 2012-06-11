package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * The abstract class of the {@link TableInputController} interface to provide some standard
 * methods.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class AbstractTableInputController implements TableInputController {

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
	public Object getTableInput() {
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
	 * Do nothing by default, sub-classes may override.
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
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
	 * Do nothing by default, sub-classes may override.
	 */
	public void doubleClick(DoubleClickEvent event) {
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
	public void setLimit(int limit) {
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
	 * Returns false by default, sub-classes may override.
	 */
	@Override
	public boolean canShowDetails() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getObjectsToSearch(Object tableInput) {
		if (tableInput instanceof Object[]) {
			return (Object[]) tableInput;
		}
		if (tableInput instanceof Collection) {
			return ((Collection<?>) tableInput).toArray();
		}
		return new Object[0];
	}

	/**
	 * {@inheritDoc}
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns true, classes may override.
	 */
	@Override
	public boolean canAlterColumnWidth(TableColumn tableColumn) {
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * By default does nothing, sub-classes may override.
	 */
	@Override
	public void objectChecked(Object object, boolean checked) {
	}


	/**
	 * {@inheritDoc}
	 * <p>
	 * By default false.
	 */
	@Override
	public boolean isCheckStyle() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default false.
	 */
	@Override
	public boolean areItemsInitiallyChecked() {
		return false;
	}

}
