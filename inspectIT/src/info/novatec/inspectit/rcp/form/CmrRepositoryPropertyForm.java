package info.novatec.inspectit.rcp.form;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.BufferStatusData;
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
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
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
	private FormToolkit toolkit;
	private Form form;
	private Label address;
	private FormText description;
	private Label recordingIcon;
	private Label recordingStorage;
	private Label status;
	private Label bufferDate;
	private ProgressBar bufferBar;
	private Composite mainComposite;
	private ProgressBar recTimeBar;
	private Label recordingStatusIcon;
	private Label recordingLabel;
	private Label version;
	private Label bufferSize;
	private Label recTime;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}.
	 */
	public CmrRepositoryPropertyForm(Composite parent, FormToolkit toolkit) {
		this(parent, toolkit, null);
	}

	/**
	 * Secondary constructor. Set the displayed {@link CmrRepositoryDefinition}.
	 * 
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}.
	 * @param cmrRepositoryDefinition
	 *            Displayed CMR.
	 */
	public CmrRepositoryPropertyForm(Composite parent, FormToolkit toolkit, CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.toolkit = toolkit;
		this.form = toolkit.createForm(parent);
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		initWidget();
	}

	/**
	 * Instantiate the widgets.
	 */
	private void initWidget() {
		toolkit.decorateFormHeading(form);
		form.getBody().setLayout(new GridLayout(1, false));

		mainComposite = toolkit.createComposite(form.getBody());
		GridLayout gl = new GridLayout(3, false);
		gl.verticalSpacing = 6;
		mainComposite.setLayout(gl);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		toolkit.createLabel(mainComposite, "Address:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		address = toolkit.createLabel(mainComposite, null, SWT.WRAP);
		address.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		toolkit.createLabel(mainComposite, "Version:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		version = toolkit.createLabel(mainComposite, null, SWT.WRAP);
		version.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		toolkit.createLabel(mainComposite, "Description:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		description = toolkit.createFormText(mainComposite, false);
		description.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		description.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				showCmrDescriptionBox();
			}
		});

		toolkit.createLabel(mainComposite, "Status:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		status = toolkit.createLabel(mainComposite, null, SWT.WRAP);
		status.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		toolkit.createLabel(mainComposite, "Buffer occupancy:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		final Composite bufferComposite = toolkit.createComposite(mainComposite);
		gl = new GridLayout(1, true);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		bufferComposite.setLayout(gl);
		bufferComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		bufferBar = new ProgressBar(bufferComposite, SWT.SMOOTH);
		bufferBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		bufferSize = toolkit.createLabel(bufferComposite, null, SWT.CENTER);
		bufferSize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(mainComposite, "Data in buffer since:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		bufferDate = toolkit.createLabel(mainComposite, null, SWT.WRAP);
		bufferDate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		toolkit.createLabel(mainComposite, "Recording:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		recordingIcon = toolkit.createLabel(mainComposite, null, SWT.WRAP);
		recordingIcon.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		recordingLabel = toolkit.createLabel(mainComposite, null, SWT.WRAP);
		recordingLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(mainComposite, "Recording status:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		recordingStatusIcon = toolkit.createLabel(mainComposite, null, SWT.NONE);
		recordingStatusIcon.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		toolkit.createLabel(mainComposite, "Recording storage:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		recordingStorage = toolkit.createLabel(mainComposite, null, SWT.WRAP);
		recordingStorage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		toolkit.createLabel(mainComposite, "Recording time left:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		Composite recordingComposite = toolkit.createComposite(mainComposite);
		gl = new GridLayout(1, true);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		recordingComposite.setLayout(gl);
		recordingComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		recTimeBar = new ProgressBar(recordingComposite, SWT.SMOOTH);
		recTimeBar.setVisible(false);
		recTimeBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		recTime = toolkit.createLabel(recordingComposite, null, SWT.CENTER);
		recTime.setVisible(false);
		recTime.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

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
		form.layout();
		form.getBody().layout();
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
		if (!selection.isEmpty()) {
			if (selection instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				Object firstElement = structuredSelection.getFirstElement();
				while (firstElement != null) {
					if (firstElement instanceof ICmrRepositoryProvider) {
						if (!ObjectUtils.equals(cmrRepositoryDefinition, ((ICmrRepositoryProvider) firstElement).getCmrRepositoryDefinition())) {
							cmrRepositoryDefinition = ((ICmrRepositoryProvider) firstElement).getCmrRepositoryDefinition();
							refreshData();
						}
						return;
					}
					firstElement = ((Component) firstElement).getParent();
				}
			}
		}
		if (null != cmrRepositoryDefinition) {
			cmrRepositoryDefinition = null;
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
					form.setMessage(null);
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
				updateBufferData();

				mainComposite.layout();
				form.layout();

				form.setBusy(false);
			}
		});
	}

	/**
	 * Updates buffer data.
	 */
	private void updateBufferData() {
		boolean dataLoaded = false;
		if (null != cmrRepositoryDefinition && cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
			// buffer information
			BufferStatusData bufferStatusData = cmrRepositoryDefinition.getBufferService().getBufferStatusData();
			if (null != bufferStatusData) {
				dataLoaded = true;
				// Transfer to MB right away
				double bufferMaxOccupancy = (double) bufferStatusData.getMaxBufferSize() / (1024 * 1024);
				double bufferCurrentOccupancy = (double) bufferStatusData.getCurrentBufferSize() / (1024 * 1024);
				bufferBar.setMaximum((int) Math.round(bufferMaxOccupancy));
				bufferBar.setSelection((int) Math.round(bufferCurrentOccupancy));
				int occupancy = (int) (100 * Math.round(bufferCurrentOccupancy) / Math.round(bufferMaxOccupancy));
				String occMb = bufferBar.getSelection() + "MB";
				String maxMb = bufferBar.getMaximum() + "MB";
				String string = occupancy + "% (" + occMb + " / " + maxMb + ")";
				bufferSize.setText(string);

				DefaultData oldestData = bufferStatusData.getBufferOldestElement();
				if (null != oldestData) {
					bufferDate.setText(NumberFormatter.formatTime(oldestData.getTimeStamp().getTime()));
				} else {
					bufferDate.setText("-");
				}
			}
		}

		if (!dataLoaded) {
			bufferDate.setText("");
			bufferBar.setMaximum(Integer.MAX_VALUE);
			bufferBar.setSelection(0);
			bufferSize.setText("");
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
				updateBufferData();
				updateRecordingData();
				this.schedule(UPDATE_CMR_DATA_SLEEP);
			}
			return Status.OK_STATUS;
		}
	}
}
