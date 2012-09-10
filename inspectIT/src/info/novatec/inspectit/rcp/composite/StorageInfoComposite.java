package info.novatec.inspectit.rcp.composite;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Composite that show the storage info.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageInfoComposite extends Composite {

	/**
	 * Not available string.
	 */
	private static final String NOT_AVAILABLE = "N/A";

	/**
	 * Widgets.
	 */
	private Label name;
	private Label description;
	private Label size;
	private Label downloaded;

	/**
	 * If there should be information if storage is downloaded or not.
	 */
	private boolean showDataDownloaded;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            a widget which will be the parent of the new instance (cannot be null)
	 * @param style
	 *            the style of widget to construct
	 * @param showDataDownloaded
	 *            If there should be information if storage is downloaded or not.
	 * @see Composite#Composite(Composite, int)
	 */
	public StorageInfoComposite(Composite parent, int style, boolean showDataDownloaded) {
		super(parent, style);
		this.showDataDownloaded = showDataDownloaded;
		init();
	}

	/**
	 * Secondary constructor. Displays the information from the storage data.
	 * 
	 * @param parent
	 *            a widget which will be the parent of the new instance (cannot be null)
	 * @param style
	 *            the style of widget to construct
	 * @param showDataDownloaded
	 *            If there should be information if storage is downloaded or not.
	 * @param storageData
	 *            Data to display information for.
	 */
	public StorageInfoComposite(Composite parent, int style, boolean showDataDownloaded, IStorageData storageData) {
		this(parent, style, showDataDownloaded);
		displayStorageData(storageData);
	}

	/**
	 * Initializes the widget.
	 */
	private void init() {
		// define layout
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		Group group = new Group(this, SWT.NONE);
		group.setText("Storage Info");
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 10;
		gl.marginWidth = 10;
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setLayout(gl);

		new Label(group, SWT.NONE).setText("Name:");
		name = new Label(group, SWT.NONE);

		new Label(group, SWT.NONE).setText("Description:");
		description = new Label(group, SWT.NONE);

		new Label(group, SWT.NONE).setText("Size on disk:");
		size = new Label(group, SWT.NONE);

		if (showDataDownloaded) {
			new Label(group, SWT.NONE).setText("Data downloaded:");
			downloaded = new Label(group, SWT.NONE);
		}
	}

	/**
	 * Displays the storage data.
	 * 
	 * @param storageData
	 *            Data to display information for.
	 */
	public final void displayStorageData(IStorageData storageData) {
		if (null != storageData) {
			name.setText(storageData.getName());
			String desc = "";
			if (null != storageData.getDescription()) {
				desc = storageData.getDescription();
			}
			description.setText(desc);
			size.setText(NumberFormatter.humanReadableByteCount(storageData.getDiskSize()));
			if (showDataDownloaded) {
				LocalStorageData localStorageData = null;
				if (storageData instanceof LocalStorageData) {
					localStorageData = (LocalStorageData) storageData;
				} else if (storageData instanceof StorageData) {
					localStorageData = InspectIT.getDefault().getInspectITStorageManager().getLocalDataForStorage((StorageData) storageData);
				}
				boolean notDownloaded = (null == localStorageData || !localStorageData.isFullyDownloaded());

				if (notDownloaded) {
					downloaded.setText("No");
				} else {
					downloaded.setText("Yes");
				}
			}
		} else {
			showDataUnavailable();
		}
		this.layout(true, true);
	}

	/**
	 * Updates the composite to display the not available info.
	 */
	public final void showDataUnavailable() {
		name.setText(NOT_AVAILABLE);
		description.setText(NOT_AVAILABLE);
		size.setText(NOT_AVAILABLE);
		if (showDataDownloaded) {
			downloaded.setText(NOT_AVAILABLE);
		}
	}

}
