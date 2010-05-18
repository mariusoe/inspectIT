package info.novatec.inspectit.rcp.editor.testers;

import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableSubView;

import org.eclipse.core.expressions.PropertyTester;

/**
 * @author Patrice Bouillet
 * 
 */
public class TableShowDetailsTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof AbstractRootEditor) {
			AbstractRootEditor rootEditor = (AbstractRootEditor) receiver;
			if (rootEditor.getActiveSubView() instanceof TableSubView) {
				TableSubView tableSubView = (TableSubView) rootEditor.getActiveSubView();
				return tableSubView.getTableInputController().canShowDetails();
			}
		}

		return false;
	}
}
