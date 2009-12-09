package info.novatec.inspectit.rcp.editor.testers;

import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableSubView;
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
			}
		}

		return false;
	}
}
