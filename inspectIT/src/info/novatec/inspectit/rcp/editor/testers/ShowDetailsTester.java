package info.novatec.inspectit.rcp.editor.testers;

import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableSubView;
import info.novatec.inspectit.rcp.editor.tree.TreeSubView;

import org.eclipse.core.expressions.PropertyTester;

/**
 * @author Patrice Bouillet
 * 
 */
public class ShowDetailsTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof AbstractRootEditor) {
			AbstractRootEditor rootEditor = (AbstractRootEditor) receiver;
			if ("tableShowDetails".equals(property) && rootEditor.getActiveSubView() instanceof TableSubView) {
				TableSubView tableSubView = (TableSubView) rootEditor.getActiveSubView();
				return tableSubView.getTableInputController().canShowDetails();
			}
			if ("treeShowDetails".equals(property) && rootEditor.getActiveSubView() instanceof TreeSubView) {
				TreeSubView treeSubView = (TreeSubView) rootEditor.getActiveSubView();
				return treeSubView.getTreeInputController().canShowDetails();
			}
		}

		return false;
	}
}
