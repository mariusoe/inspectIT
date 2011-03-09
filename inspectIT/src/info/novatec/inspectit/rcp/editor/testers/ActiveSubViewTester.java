package info.novatec.inspectit.rcp.editor.testers;

import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.rcp.editor.composite.AbstractCompositeSubView;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableSubView;
import info.novatec.inspectit.rcp.editor.tree.SteppingTreeSubView;
import info.novatec.inspectit.rcp.editor.tree.TreeSubView;

import org.eclipse.core.expressions.PropertyTester;

/**
 * @author Patrice Bouillet
 * 
 */
public class ActiveSubViewTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof AbstractRootEditor) {
			AbstractRootEditor rootEditor = (AbstractRootEditor) receiver;
			if ("activeSubView".equals(property)) {
				if ("treeSubView".equals(expectedValue)) {
					return rootEditor.getActiveSubView() instanceof TreeSubView;
				} else if ("tableSubView".equals(expectedValue)) {
					return rootEditor.getActiveSubView() instanceof TableSubView;
				}
			} else if ("hasSubView".equals(property)) {
				if ("steppingTreeSubView".equals(expectedValue)) {
					return isSteppingTreeSubViewOneOfSubViews(rootEditor.getSubView());
				}
			}
		}

		return false;
	}

	/**
	 * Returns if the given sub view is a {@link SteppingTreeSubView} or if the
	 * {@link SteppingTreeSubView} is part of sub-view of {@link AbstractCompositeSubView} in case
	 * this sub-view is provided. This is a recursive method.
	 * 
	 * @param subView
	 *            Sub-view to check.
	 * @return Returns true if the {@link SteppingTreeSubView} is found.
	 */
	private boolean isSteppingTreeSubViewOneOfSubViews(ISubView subView) {
		if (subView instanceof SteppingTreeSubView) {
			return true;
		} else if (subView instanceof AbstractCompositeSubView) {
			AbstractCompositeSubView compositeSubView = (AbstractCompositeSubView) subView;
			for (ISubView viewInCompositeSubView : compositeSubView.getSubViews()) {
				if (isSteppingTreeSubViewOneOfSubViews(viewInCompositeSubView)) {
					return true;
				}
			}
		}
		return false;
	}
}
