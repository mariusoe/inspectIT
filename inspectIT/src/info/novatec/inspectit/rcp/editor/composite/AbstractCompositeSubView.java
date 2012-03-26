package info.novatec.inspectit.rcp.editor.composite;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.AbstractSubView;
import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * Some general methods for composite views are implemented in here.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class AbstractCompositeSubView extends AbstractSubView {

	/**
	 * The list containing all the sub-views which are painted in this composite sub-view.
	 */
	private List<ISubView> subViews = new ArrayList<ISubView>();

	/**
	 * Maximizes the given {@link ISubView}. The {@link ISubView} has to contained in this composite
	 * sub-view.
	 * 
	 * @param subView
	 *            Sub-view to maximize.
	 */
	public abstract void maximizeSubView(ISubView subView);

	/**
	 * Minimizes the given {@link ISubView}. The {@link ISubView} has to contained in this composite
	 * sub-view.
	 */
	public abstract void restoreMaximization();

	/**
	 * Adds a new sub-view to this composite view.
	 * 
	 * @param subView
	 *            The {@link ISubView} which will be added.
	 */
	public void addSubView(ISubView subView) {
		subViews.add(subView);
	}

	/**
	 * @return the subViews
	 */
	public List<ISubView> getSubViews() {
		// makes the list unmodifiable so that it can not be edited.
		return Collections.unmodifiableList(subViews);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferenceIds = EnumSet.noneOf(PreferenceId.class);

		for (ISubView subView : subViews) {
			preferenceIds.addAll(subView.getPreferenceIds());
		}

		return preferenceIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		// just delegate to all sub-views.
		for (ISubView subView : subViews) {
			subView.doRefresh();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		// just delegate to all sub-views.
		for (ISubView subView : subViews) {
			subView.preferenceEventFired(preferenceEvent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDataInput(List<? extends DefaultData> data) {
		// just delegate to all sub-views.
		for (ISubView subView : subViews) {
			subView.setDataInput(data);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRootEditor(AbstractRootEditor rootEditor) {
		super.setRootEditor(rootEditor);

		for (ISubView subView : subViews) {
			subView.setRootEditor(rootEditor);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		// just delegate to all sub-views.
		for (ISubView subView : subViews) {
			subView.dispose();
		}
	}

}