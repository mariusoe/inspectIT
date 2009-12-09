package info.novatec.inspectit.rcp;

import info.novatec.inspectit.rcp.view.server.ServerView;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * The default perspective and layout of the InspectIT UI.
 * 
 * @author Patrice Bouillet
 * 
 */
public class Perspective implements IPerspectiveFactory {

	/**
	 * {@inheritDoc}
	 */
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);

		boolean showTitle = false;
		float ratio = 0.3f;
		layout.addStandaloneView(ServerView.ID, showTitle, IPageLayout.LEFT, ratio, editorArea);
	}

}
