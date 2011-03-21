package info.novatec.inspectit.rcp.editor.root;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.preferences.FormPreferencePanel;
import info.novatec.inspectit.rcp.editor.preferences.IPreferencePanel;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * An implementation of a root editor which uses a form to create a nicer view.
 * 
 * @author Patrice Bouillet
 * 
 */
public class FormRootEditor extends AbstractRootEditor {

	/**
	 * The identifier of the {@link FormRootEditor}.
	 */
	public static final String ID = "inspectit.editor.formrooteditor";

	/**
	 * The form toolkit which defines the colors etc.
	 */
	private FormToolkit toolkit;

	/**
	 * The form of the view.
	 */
	private Form form;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createView(Composite parent) {
		// create the toolkit
		this.toolkit = new FormToolkit(parent.getDisplay());

		// create the preference panel with the callback
		IPreferencePanel preferencePanel = new FormPreferencePanel(toolkit);
		// set the preference panel
		setPreferencePanel(preferencePanel);

		// create the form
		form = toolkit.createForm(parent);
		form.setImage(getInputDefinition().getEditorPropertiesData().getImageDescriptor().createImage());
		form.setText(getInputDefinition().getEditorPropertiesData().getHeaderText());
		form.setMessage(getInputDefinition().getEditorPropertiesData().getHeaderDescription());
		form.getBody().setLayout(new GridLayout());

		// add repository source button
		form.getMenuManager().add(getRepositorySourceAction(getInputDefinition().getRepositoryDefinition()));

		// decorate the heading to make it look better
		toolkit.decorateFormHeading(form);

		// create an preference area if the subviews are requesting it
		preferencePanel.createPartControl(form.getBody(), getSubView().getPreferenceIds(), form.getToolBarManager());

		// go further with creating the subview(s)
		getSubView().createPartControl(form.getBody(), toolkit);
		getSubView().getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		super.dispose();
		// manually dispose the image of the form
		form.getImage().dispose();
		// dispose of the toolkit
		toolkit.dispose();
	}

	/**
	 * @return the form
	 */
	public Form getForm() {
		return form;
	}

	/**
	 * Displays the details box with fixed content.
	 * 
	 * @param titleText
	 *            Title text.
	 * @param infoText
	 *            Info text.
	 * @param content
	 *            Content.
	 */
	public void showDetailsBox(final String titleText, final String infoText, final String content) {
		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE;
		boolean takeFocusOnOpen = true;
		boolean persistSize = true;
		boolean persistLocation = true;
		boolean showDialogMenu = false;
		boolean showPersistActions = true;

		PopupDialog dialog = new PopupDialog(getSite().getShell(), shellStyle, takeFocusOnOpen, persistSize, persistLocation, showDialogMenu, showPersistActions, titleText, infoText) {
			private static final int CURSOR_SIZE = 15;

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected Point getInitialLocation(Point initialSize) {
				// show popup relative to cursor
				Display display = getShell().getDisplay();
				Point location = display.getCursorLocation();
				location.x += CURSOR_SIZE;
				location.y += CURSOR_SIZE;
				return location;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected Point getInitialSize() {
				return new Point(400, 200);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected Control createDialogArea(Composite parent) {
				FormToolkit toolkit = new FormToolkit(parent.getDisplay());

				Text text = toolkit.createText(parent, null, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
				GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
				text.setLayoutData(gridData);
				text.setText(content);

				// Use the compact margins employed by PopupDialog.
				GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
				gd.horizontalIndent = PopupDialog.POPUP_HORIZONTALSPACING;
				gd.verticalIndent = PopupDialog.POPUP_VERTICALSPACING;
				text.setLayoutData(gd);

				return text;
			}
		};
		dialog.open();
	}

	/**
	 * Action for displaying the repository information.
	 * 
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition}.
	 * @return Action.
	 */
	private IAction getRepositorySourceAction(final RepositoryDefinition repositoryDefinition) {
		Action action = new Action() {
			@Override
			public void run() {
				showRepositoryDetails(repositoryDefinition);
			}
		};
		action.setText("Source Repository Details");
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			action.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_SERVER_ONLINE_SMALL));
		} else if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			action.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_STOARGE_NEW));
		}
		return action;
	}

	/**
	 * Displays the info about the source repository definition.
	 * 
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition}
	 */
	private void showRepositoryDetails(RepositoryDefinition repositoryDefinition) {
		StringBuilder content = new StringBuilder();
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			content.append("Repository type: Central Management Repository (CMR)\n");
			content.append("Repository name: " + repositoryDefinition.getName() + "\n");
			content.append("Repository address: " + repositoryDefinition.getIp() + ":" + repositoryDefinition.getPort() + "\n");
			showDetailsBox("Source Repository Details", "Central Management Repository (CMR)", content.toString());
		} else if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) repositoryDefinition;
			content.append("Repository type: Storage Repository \n");
			content.append("Repository name: " + repositoryDefinition.getName() + "\n");
			if (storageRepositoryDefinition.getLocalStorageData().isFullyDownloaded()) {
				content.append("Repository availability: fully downloaded\n");
			} else {
				content.append("Repository availability: via CMR repository '" + storageRepositoryDefinition.getCmrRepositoryDefinition().getName() + "' ("
						+ storageRepositoryDefinition.getCmrRepositoryDefinition().getIp() + ":" + storageRepositoryDefinition.getCmrRepositoryDefinition().getPort() + ")\n");
			}
			showDetailsBox("Source Repository Details", "Storage Repository", content.toString());
		}
	}
}
