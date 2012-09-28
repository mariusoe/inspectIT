package info.novatec.inspectit.rcp.form;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.Component;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.recording.RecordingData;
import info.novatec.inspectit.storage.recording.RecordingProperties;

import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.progress.UIJob;

/**
 * Class having a form for displaying the properties of a {@link CmrRepositoryDefinition}.
 * 
 * @author Ivan Senic
 * 
 */
public class CmrRepositoryPropertyForm implements ISelectionChangedListener {

	/**
	 * Number of max characters displayed for CMR description.
	 */
	private static final int MAX_DESCRIPTION_LENGTH = 150;

	/**
	 * {@link CmrRepositoryDefinition} to be displayed.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Current recording data.
	 */
	private RecordingData recordingData;

	/**
	 * Job for recording end count-down.
	 */
	private RecordCountdownJob recordCountdownJob = new RecordCountdownJob();

	/**
	 * Job for updating the CMR data.
	 */
	private UpdateCmrDataJob updateCmrDataJob = new UpdateCmrDataJob();

	/**
	 * Widgets.
	 */
	private Composite mainComposite;
	private FormToolkit toolkit;
	private ManagedForm managedForm;
	private ScrolledForm form;
	private Label address;
	private FormText description;
	private Label recordingIcon;
	private Label recordingStorage;
	private Label status;
	private Label bufferDate;
	private ProgressBar bufferBar;
	private ProgressBar recTimeBar;
	private Label recordingStatusIcon;
	private Label recordingLabel;
	private Label version;
	private Label bufferSize;
	private Label recTime;
	private Label spaceLeftLabel;
	private ProgressBar spaceLeftBar;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            Parent composite.
	 */
	public CmrRepositoryPropertyForm(Composite parent) {
		this(parent, null);
	}

	/**
	 * Secondary constructor. Set the displayed {@link CmrRepositoryDefinition}.
	 * 
	 * @param parent
	 *            Parent composite.
	 * @param cmrRepositoryDefinition
	 *            Displayed CMR.
	 */
	public CmrRepositoryPropertyForm(Composite parent, CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.managedForm = new ManagedForm(parent);
		this.toolkit = managedForm.getToolkit();
		this.form = managedForm.getForm();
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		initWidget();
	}

	/**
	 * Instantiate the widgets.
	 */
	private void initWidget() {
		Composite body = form.getBody();
		body.setLayout(new TableWrapLayout());
		managedForm.getToolkit().decorateFormHeading(form.getForm());
		mainComposite = toolkit.createComposite(body, SWT.NONE);
		mainComposite.setLayout(new TableWrapLayout());
		mainComposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// START - General section
		Section generalSection = toolkit.createSection(mainComposite, Section.TITLE_BAR);
		generalSection.setText("General information");

		Composite generalComposite = toolkit.createComposite(generalSection, SWT.NONE);
		TableWrapLayout tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		generalComposite.setLayout(tableWrapLayout);
		generalComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		toolkit.createLabel(generalComposite, "Address:");
		address = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		toolkit.createLabel(generalComposite, "Version:");
		version = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		toolkit.createLabel(generalComposite, "Description:");
		description = toolkit.createFormText(generalComposite, true);
		description.setLayoutData(new TableWrapData(TableWrapData.FILL));
		description.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				showCmrDescriptionBox();
			}
		});

		toolkit.createLabel(generalComposite, "Status:");
		status = toolkit.createLabel(generalComposite, null, SWT.WRAP);

		generalSection.setClient(generalComposite);
		generalSection.setLayout(new TableWrapLayout());
		generalSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// END - General section

		// START - Buffer section
		Section bufferSection = toolkit.createSection(mainComposite, Section.TITLE_BAR);
		bufferSection.setText("Buffer status");

		Composite bufferSectionComposite = toolkit.createComposite(bufferSection, SWT.NONE);
		tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		bufferSectionComposite.setLayout(tableWrapLayout);
		bufferSectionComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		toolkit.createLabel(bufferSectionComposite, "Data in buffer since:");
		bufferDate = toolkit.createLabel(bufferSectionComposite, null, SWT.WRAP);

		toolkit.createLabel(bufferSectionComposite, "Buffer occupancy:");
		bufferBar = new ProgressBar(bufferSectionComposite, SWT.SMOOTH);
		bufferBar.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// help label
		toolkit.createLabel(bufferSectionComposite, null);

		bufferSize = toolkit.createLabel(bufferSectionComposite, null, SWT.CENTER);
		bufferSize.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		bufferSection.setClient(bufferSectionComposite);
		bufferSection.setLayout(new TableWrapLayout());
		bufferSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// END - Buffer section

		// START - Storage section
		Section storageSection = toolkit.createSection(mainComposite, Section.TITLE_BAR);
		storageSection.setText("Storage status");

		Composite storageSectionComposite = toolkit.createComposite(storageSection, SWT.NONE);
		tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		storageSectionComposite.setLayout(tableWrapLayout);
		storageSectionComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		toolkit.createLabel(storageSectionComposite, "Storage space left:");

		spaceLeftBar = new ProgressBar(storageSectionComposite, SWT.SMOOTH);
		spaceLeftBar.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// help label
		toolkit.createLabel(storageSectionComposite, null);

		spaceLeftLabel = toolkit.createLabel(storageSectionComposite, null, SWT.CENTER);
		spaceLeftLabel.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		toolkit.createLabel(storageSectionComposite, "Recording:");
		Composite recordingHelpComposite = toolkit.createComposite(storageSectionComposite);
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		recordingHelpComposite.setLayout(gl);
		recordingIcon = toolkit.createLabel(recordingHelpComposite, null, SWT.WRAP);
		recordingLabel = toolkit.createLabel(recordingHelpComposite, null, SWT.WRAP);

		toolkit.createLabel(storageSectionComposite, "Recording status:");
		recordingStatusIcon = toolkit.createLabel(storageSectionComposite, null, SWT.NONE);

		toolkit.createLabel(storageSectionComposite, "Recording storage:");
		recordingStorage = toolkit.createLabel(storageSectionComposite, null, SWT.WRAP);

		toolkit.createLabel(storageSectionComposite, "Recording time left:");

		recTimeBar = new ProgressBar(storageSectionComposite, SWT.SMOOTH);
		recTimeBar.setVisible(false);
		recTimeBar.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// help label
		toolkit.createLabel(storageSectionComposite, null);

		//
		recTime = toolkit.createLabel(storageSectionComposite, null, SWT.CENTER);
		recTime.setVisible(false);
		recTime.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		storageSection.setClient(storageSectionComposite);
		tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		storageSection.setLayout(tableWrapLayout);
		storageSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// END - Storage section

		refreshData();
	}

	/**
	 * Sets layout data for the form.
	 * 
	 * @param layoutData
	 *            LayoutData.
	 */
	public void setLayoutData(Object layoutData) {
		form.setLayoutData(layoutData);
	}

	/**
	 * Refreshes the property form.
	 */
	public void refresh() {
		refreshData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (!selection.isEmpty() && selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (!(firstElement instanceof Component)) {
				// it is possible that the PendingAdapterUpdate is in the selection because it
				// is still loading the agents
				return;
			}

			while (firstElement != null) {
				if (firstElement instanceof ICmrRepositoryProvider) { // NOPMD
					if (!ObjectUtils.equals(cmrRepositoryDefinition, ((ICmrRepositoryProvider) firstElement).getCmrRepositoryDefinition())) {
						cmrRepositoryDefinition = ((ICmrRepositoryProvider) firstElement).getCmrRepositoryDefinition();
						refreshData();
					}
					return;
				}
				firstElement = ((Component) firstElement).getParent();
			}
		}
		if (null != cmrRepositoryDefinition) {
			cmrRepositoryDefinition = null; // NOPMD
			refreshData();
		}

	}

	/**
	 * Shows the description box.
	 */
	private void showCmrDescriptionBox() {
		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE;
		PopupDialog popupDialog = new PopupDialog(form.getShell(), shellStyle, true, false, false, false, false, "CMR description", "CMR description") {
			private static final int CURSOR_SIZE = 15;

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite composite = (Composite) super.createDialogArea(parent);
				Text text = toolkit.createText(parent, null, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
				GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
				gd.horizontalIndent = 3;
				gd.verticalIndent = 3;
				text.setLayoutData(gd);
				text.setText(cmrRepositoryDefinition.getDescription());
				return composite;
			}

			@Override
			protected Point getInitialLocation(Point initialSize) {
				// show popup relative to cursor
				Display display = getShell().getDisplay();
				Point location = display.getCursorLocation();
				location.x += CURSOR_SIZE;
				location.y += CURSOR_SIZE;
				return location;
			}

			@Override
			protected Point getInitialSize() {
				return new Point(400, 200);
			}
		};
		popupDialog.open();

	}

	/**
	 * Refreshes the data on the view.
	 */
	private void refreshData() {
		// refresh data asynchronously
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				form.setBusy(true);

				if (null != cmrRepositoryDefinition) {
					form.setText(cmrRepositoryDefinition.getName());
					form.setMessage(null, IMessageProvider.NONE);
					address.setText(cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort());
					String desc = cmrRepositoryDefinition.getDescription();
					if (null != desc) {
						if (desc.length() > MAX_DESCRIPTION_LENGTH) {
							description.setText("<form><p>" + desc.substring(0, MAX_DESCRIPTION_LENGTH) + ".. <a href=\"More\">[More]</a></p></form>", true, false);
						} else {
							description.setText(desc, false, false);
						}
					} else {
						description.setText("", false, false);
					}
					status.setText(cmrRepositoryDefinition.getOnlineStatus().toString());
					if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
						form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_ONLINE_SMALL));
						String versionString = cmrRepositoryDefinition.getServerStatusService().getVersion();
						version.setText(versionString);
					} else if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.CHECKING) {
						form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_REFRESH_SMALL));
						version.setText("n/a");
					} else {
						form.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_OFFLINE_SMALL));
						version.setText("n/a");
					}
					updateCmrDataJob.schedule();
					mainComposite.setVisible(true);
				} else {
					form.setText(null);
					form.setMessage("Please select a CMR to see its properties.", IMessageProvider.INFORMATION);
					mainComposite.setVisible(false);
					updateCmrDataJob.cancel();
				}
				updateRecordingData();
				updateCmrManagementData();

				form.getBody().layout(true, true);
				form.setBusy(false);
			}
		});
	}

	/**
	 * Updates buffer data.
	 */
	private void updateCmrManagementData() {
		boolean dataLoaded = false;
		if (null != cmrRepositoryDefinition && cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
			// buffer information
			CmrStatusData cmrStatusData = cmrRepositoryDefinition.getCmrManagementService().getCmrStatusData();
			if (null != cmrStatusData) {
				dataLoaded = true;
				// Transfer to MB right away
				double bufferMaxOccupancy = (double) cmrStatusData.getMaxBufferSize() / (1024 * 1024);
				double bufferCurrentOccupancy = (double) cmrStatusData.getCurrentBufferSize() / (1024 * 1024);
				bufferBar.setMaximum((int) Math.round(bufferMaxOccupancy));
				bufferBar.setSelection((int) Math.round(bufferCurrentOccupancy));
				int occupancy = (int) (100 * Math.round(bufferCurrentOccupancy) / Math.round(bufferMaxOccupancy));

				String occMb = NumberFormatter.humanReadableByteCount(cmrStatusData.getCurrentBufferSize());
				String maxMb = NumberFormatter.humanReadableByteCount(cmrStatusData.getMaxBufferSize());
				String string = occupancy + "% (" + occMb + " / " + maxMb + ")";
				bufferSize.setText(string);

				DefaultData oldestData = cmrStatusData.getBufferOldestElement();
				if (null != oldestData) {
					bufferDate.setText(NumberFormatter.formatTime(oldestData.getTimeStamp().getTime()));
				} else {
					bufferDate.setText("-");
				}

				// hard drive space data
				int spaceOccupancy = (int) (100 * (double) cmrStatusData.getStorageDataSpaceLeft() / cmrStatusData.getStorageMaxDataSpace());
				StringBuilder spaceLeftStringBuilder = new StringBuilder(String.valueOf(spaceOccupancy));
				spaceLeftStringBuilder.append("% (");
				spaceLeftStringBuilder.append(NumberFormatter.humanReadableByteCount(cmrStatusData.getStorageDataSpaceLeft()));
				spaceLeftStringBuilder.append(" / ");
				spaceLeftStringBuilder.append(NumberFormatter.humanReadableByteCount(cmrStatusData.getStorageMaxDataSpace()));
				spaceLeftStringBuilder.append(')');
				spaceLeftLabel.setText(spaceLeftStringBuilder.toString());
				spaceLeftBar.setMaximum((int) (cmrStatusData.getStorageMaxDataSpace() / 1024 / 1024));
				spaceLeftBar.setSelection((int) (cmrStatusData.getStorageDataSpaceLeft() / 1024 / 1024));
				if (!cmrStatusData.isCanWriteMore()) {
					spaceLeftBar.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					spaceLeftBar.setToolTipText("Space left is critically low and no write is possible anymore");
				} else if (cmrStatusData.isWarnSpaceLeftActive()) {
					spaceLeftBar.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW));
					spaceLeftBar.setToolTipText("Space left is reaching critical level");
				} else {
					spaceLeftBar.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
					spaceLeftBar.setToolTipText("Enough space left");
				}
			}
		}

		if (!dataLoaded) {
			bufferDate.setText("");
			bufferBar.setMaximum(Integer.MAX_VALUE);
			bufferBar.setSelection(0);
			bufferSize.setText("");
			spaceLeftBar.setMaximum(Integer.MAX_VALUE);
			spaceLeftBar.setSelection(0);
			spaceLeftBar.setToolTipText("");
			spaceLeftLabel.setText("");
		}
	}

	/**
	 * Updates recording data.
	 */
	private void updateRecordingData() {
		boolean countdownJobActive = false;
		boolean dataLoaded = false;
		recordingIcon.setImage(null);
		if (null != cmrRepositoryDefinition && cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
			// recording information
			recordingData = cmrRepositoryDefinition.getStorageService().getRecordingData();
			if (null != recordingData) {
				recordingIcon.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_RECORD));
				recordingLabel.setText("Active");
				// get the storage name
				StorageData storage = recordingData.getRecordingStorage();
				if (null != storage) {
					recordingStorage.setText(storage.getName());
				} else {
					recordingStorage.setText("");
				}

				// check if the recording time is limited
				if (null != recordingData.getRecordingProperties().getRecordEndDate()) {
					countdownJobActive = true;
				} else {
					recTimeBar.setVisible(false);
					recTime.setVisible(false);
				}

				// recording status stuff
				recordingStatusIcon.setImage(ImageFormatter.getWritingStatusImage(recordingData.getRecordingWritingStatus()));
				recordingStatusIcon.setToolTipText(TextFormatter.getWritingStatusText(recordingData.getRecordingWritingStatus()));

				dataLoaded = true;

			} else {
				recordingIcon.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_RECORD_GRAY));
			}
		}

		if (!dataLoaded) {
			recordingStorage.setText("");
			recTimeBar.setVisible(false);
			recordingStatusIcon.setImage(null);
			recordingStatusIcon.setToolTipText("");
			recordingLabel.setText("Not Active");
		}

		if (countdownJobActive) {
			recordCountdownJob.schedule();
		} else {
			recordCountdownJob.cancel();
		}
	}

	/**
	 * 
	 * @return Returns if the form is disposed.
	 */
	public boolean isDisposed() {
		return form.isDisposed();
	}

	/**
	 * Disposes the for.
	 */
	public void dispose() {
		form.dispose();
		recordCountdownJob.cancel();
	}

	/**
	 * Class for updating the recording count down.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class RecordCountdownJob extends UIJob {

		/**
		 * Default constructor.
		 */
		public RecordCountdownJob() {
			super("Update Recording Countdown");
			setUser(false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (null != recordingData && !form.isDisposed()) {
				RecordingProperties recordingProperties = recordingData.getRecordingProperties();
				Date endDate = recordingProperties.getRecordEndDate();
				Date startDate = recordingProperties.getRecordStartDate();
				if (null != endDate && null != startDate) {
					Date now = new Date();
					long millisMore = endDate.getTime() - now.getTime();
					if (millisMore > 0) {
						if (!recTimeBar.isVisible()) {
							recTimeBar.setVisible(true);
						}
						recTimeBar.setMaximum((int) (endDate.getTime() - startDate.getTime()));
						recTimeBar.setSelection((int) (recTimeBar.getMaximum() - (now.getTime() - startDate.getTime())));

						if (!recTime.isVisible()) {
							recTime.setVisible(true);
						}
						String string;
						if (millisMore > 0) {
							long sec = (millisMore / 1000) % 60;
							long minutes = millisMore / (1000 * 60) % 60;
							long hours = millisMore / (1000 * 60 * 60) % 24;
							long days = millisMore / (1000 * 60 * 60 * 24);
							string = days + "d, " + hours + "h, " + minutes + "m, " + sec + "s";
						} else {
							string = "";
						}
						recTime.setText(string);

					} else {
						if (recTimeBar.isVisible()) {
							recTimeBar.setVisible(false);
						}
						if (recTime.isVisible()) {
							recTime.setVisible(false);
						}
						this.cancel();
						refreshData();
					}
				}
			}
			this.schedule(1000);
			return Status.OK_STATUS;
		}
	}

	/**
	 * Job that updates the CMR data every 30s.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class UpdateCmrDataJob extends UIJob {

		/**
		 * Sleep time in milliseconds.
		 */
		private static final long UPDATE_CMR_DATA_SLEEP = 30000;

		/**
		 * Default constructor.
		 */
		public UpdateCmrDataJob() {
			super("Update CMR Data");
			setUser(false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (null != cmrRepositoryDefinition && !form.isDisposed()) {
				updateCmrManagementData();
				updateRecordingData();
				this.schedule(UPDATE_CMR_DATA_SLEEP);
			}
			return Status.OK_STATUS;
		}
	}
}
