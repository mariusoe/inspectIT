package info.novatec.novaspy.rcp;

import info.novatec.novaspy.rcp.view.server.ServerView;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * The default perspective and layout of the NovaSpy UI.
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
