package info.novatec.novaspy.rcp.editor.testers;

import info.novatec.novaspy.rcp.editor.root.AbstractRootEditor;
import info.novatec.novaspy.rcp.editor.table.TableSubView;
import info.novatec.novaspy.rcp.editor.tree.TreeSubView;

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
