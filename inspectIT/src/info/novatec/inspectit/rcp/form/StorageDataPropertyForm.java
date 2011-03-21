package info.novatec.inspectit.rcp.form;

import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.handlers.AddStorageLabelHandler;
import info.novatec.inspectit.rcp.handlers.RemoveStorageLabelHandler;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.storage.label.edit.LabelTableEditingSupport;
import info.novatec.inspectit.rcp.util.ObjectUtils;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Form for displaying the {@link StorageData} properties.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageDataPropertyForm implements ISelectionChangedListener {

	/**
	 * Number of max characters displayed for storage description.
	 */
	private static final int MAX_DESCRIPTION_LENGTH = 150;

	/**
	 * Leaf that is displayed currently.
	 */
	private IStorageDataProvider storageDataProvider;

	/**
	 * Toolkit used to create widgets.
	 */
	private FormToolkit toolkit;

	/**
	 * Form that will be created.
	 */
	private Form form;

	/**
	 * Label for repository.
	 */
	private Label repository;

	/**
	 * Label for description.
	 */
	private FormText description;

	/**
	 * Label for size on disk.
	 */
	private Label sizeOnDisk;

	/**
	 * Label for storage state.
	 */
	private Label state;

	/**
	 * Table of storage labels.
	 */
	private TableViewer labelsTableViewer;

	/**
	 * Add new label Hyperlink.
	 */
	private Hyperlink addNewLabel;

	/**
	 * remove labels Hyperlink.
	 */
	private Hyperlink removeLabels;

	/**
	 * Main composite where widgets are.
	 */
	private Composite mainComposite;

	/**
	 * {@link TableViewerColumn} for label values. Needed for editing support.
	 */
	private TableViewerColumn valueViewerColumn;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            Parent where form will be created.
	 * @param toolkit
	 *            {@link FormToolkit}.
	 */
	public StorageDataPropertyForm(Composite parent, FormToolkit toolkit) {
		this(parent, toolkit, null);
	}

	/**
	 * Secondary constructor. Set the displayed storage leaf.
	 * 
	 * @param parent
	 *            Parent where form will be created.
	 * @param toolkit
	 *            {@link FormToolkit}.
	 * @param storageDataProvider
	 *            {@link IStorageDataProvider} to display.
	 */
	public StorageDataPropertyForm(Composite parent, FormToolkit toolkit, IStorageDataProvider storageDataProvider) {
		this.toolkit = toolkit;
		this.form = toolkit.createForm(parent);
		this.storageDataProvider = storageDataProvider;
		initWidget();
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
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (!selection.isEmpty()) {
			if (selection instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				Object firstElement = structuredSelection.getFirstElement();
				if (firstElement instanceof IStorageDataProvider) {
					if (!ObjectUtils.equals(storageDataProvider, firstElement)) {
						storageDataProvider = (IStorageDataProvider) firstElement;
						valueViewerColumn.setEditingSupport(new LabelTableEditingSupport(labelsTableViewer, storageDataProvider.getStorageData(), storageDataProvider.getCmrRepositoryDefinition()));
						refreshData();
					}
					return;
				}
			}
		}
		if (null != storageDataProvider) {
			storageDataProvider = null;
			refreshData();
		}
	}

	/**
	 * Refresh the data after selection is changed.
	 */
	private void refreshData() {
		// refresh data asynchronously
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				form.setBusy(true);
				if (null != storageDataProvider) {
					StorageData storageData = storageDataProvider.getStorageData();
					CmrRepositoryDefinition cmrRepositoryDefinition = storageDataProvider.getCmrRepositoryDefinition();
					form.setText(storageData.getName());
					form.setMessage(null);
					repository.setText(cmrRepositoryDefinition.getName() + " (" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + ")");
					String desc = storageData.getDescription();
					if (null != desc) {
						if (desc.length() > MAX_DESCRIPTION_LENGTH) {
							description.setText("<form><p>" + desc.substring(0, MAX_DESCRIPTION_LENGTH) + ".. <a href=\"More\">[More]</a></p></form>", true, false);
						} else {
							description.setText(desc, false, false);
						}
					} else {
						description.setText("", false, false);
					}
					sizeOnDisk.setText(NumberFormatter.formatBytesToMBytes(storageData.getDiskSize()));
					state.setText(TextFormatter.getStorageStateTextualRepresentation(storageData.getState()));
					labelsTableViewer.setInput(storageData.getLabelList());
					labelsTableViewer.refresh();
					addNewLabel.setEnabled(true);
					ImageDescriptor imgDesc = ImageFormatter.getImageDescriptorForStorageLeaf(storageData);
					if (null != imgDesc) {
						form.setImage(imgDesc.createImage());
					}
					mainComposite.setVisible(true);
				} else {
					form.setText(null);
					form.setMessage("Please select a storage to see its properties.", IMessageProvider.INFORMATION);
					mainComposite.setVisible(false);
				}
				mainComposite.layout();
				form.layout();

				form.setBusy(false);
			}
		});
	}

	/**
	 * Instantiate the widgets.
	 */
	private void initWidget() {
		toolkit.decorateFormHeading(form);
		form.getBody().setLayout(new GridLayout(1, false));

		mainComposite = toolkit.createComposite(form.getBody());
		GridLayout gl = new GridLayout(2, false);
		gl.verticalSpacing = 6;
		mainComposite.setLayout(gl);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		toolkit.createLabel(mainComposite, "Repository:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		repository = toolkit.createLabel(mainComposite, null, SWT.WRAP);
		repository.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(mainComposite, "Description:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		description = toolkit.createFormText(mainComposite, false);
		description.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		description.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				showStorageDescriptionBox();
			}
		});
		toolkit.createLabel(mainComposite, "Size on disk:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		sizeOnDisk = toolkit.createLabel(mainComposite, null, SWT.WRAP);
		sizeOnDisk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(mainComposite, "State:").setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		state = toolkit.createLabel(mainComposite, null, SWT.WRAP);
		state.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Table table = toolkit.createTable(mainComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		labelsTableViewer = new TableViewer(table);

		TableViewerColumn viewerColumn = new TableViewerColumn(labelsTableViewer, SWT.NONE);
		viewerColumn.getColumn().setText("Label");
		viewerColumn.getColumn().setMoveable(false);
		viewerColumn.getColumn().setResizable(true);
		viewerColumn.getColumn().setWidth(140);

		valueViewerColumn = new TableViewerColumn(labelsTableViewer, SWT.NONE);
		valueViewerColumn.getColumn().setText("Value");
		valueViewerColumn.getColumn().setMoveable(false);
		valueViewerColumn.getColumn().setResizable(true);
		valueViewerColumn.getColumn().setWidth(100);

		labelsTableViewer.setContentProvider(new ArrayContentProvider());
		labelsTableViewer.setLabelProvider(new StyledCellIndexLabelProvider() {
			@Override
			protected StyledString getStyledText(Object element, int index) {
				if (element instanceof AbstractStorageLabel) {
					AbstractStorageLabel<?> label = (AbstractStorageLabel<?>) element;
					switch (index) {
					case 0:
						return new StyledString(TextFormatter.getLabelName(label));
					case 1:
						return new StyledString(TextFormatter.getLabelValue(label, false));
					default:
					}
				}
				return null;
			}

			@Override
			protected Image getColumnImage(Object element, int index) {
				if (index == 0 && element instanceof AbstractStorageLabel) {
					ImageDescriptor id = ImageFormatter.getImageDescriptorForLabel(((AbstractStorageLabel<?>) element).getStorageLabelType());
					if (null != id) {
						return id.createImage();
					}
				}
				return null;
			}
		});
		labelsTableViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof AbstractStorageLabel && e2 instanceof AbstractStorageLabel) {
					return ((AbstractStorageLabel<?>) e1).compareTo((AbstractStorageLabel<?>) e2);
				}
				return super.compare(viewer, e1, e2);
			}
		});
		labelsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (labelsTableViewer.getSelection().isEmpty()) {
					removeLabels.setEnabled(false);
				} else {
					removeLabels.setEnabled(true);
				}
			}

		});

		addNewLabel = toolkit.createHyperlink(mainComposite, "Add New Label", SWT.RIGHT);
		addNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false, 2, 1));
		addNewLabel.setEnabled(false);
		addNewLabel.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

				Command command = commandService.getCommand(AddStorageLabelHandler.COMMAND);
				ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
				try {
					command.executeWithChecks(executionEvent);
				} catch (Exception exception) {
					throw new RuntimeException(exception);
				}
			}
		});

		removeLabels = toolkit.createHyperlink(mainComposite, "Remove selected", SWT.RIGHT);
		removeLabels.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false, 2, 1));
		removeLabels.setEnabled(false);
		removeLabels.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (!labelsTableViewer.getSelection().isEmpty()) {
					List<AbstractStorageLabel<?>> inputList = new ArrayList<AbstractStorageLabel<?>>();
					for (Object object : ((StructuredSelection) labelsTableViewer.getSelection()).toArray()) {
						if (object instanceof AbstractStorageLabel) {
							inputList.add((AbstractStorageLabel<?>) object);
						}
					}

					IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
					ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

					Command command = commandService.getCommand(RemoveStorageLabelHandler.COMMAND);
					ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
					IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
					context.addVariable(RemoveStorageLabelHandler.INPUT, inputList);
					try {
						command.executeWithChecks(executionEvent);
					} catch (Exception exception) {
						throw new RuntimeException(exception);
					}
				}
			}
		});

		refreshData();
	}

	/**
	 * Shows storage description box.
	 */
	private void showStorageDescriptionBox() {
		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE;
		PopupDialog popupDialog = new PopupDialog(form.getShell(), shellStyle, true, false, false, false, false, "Storage description", "Storage description") {
			private static final int CURSOR_SIZE = 15;

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite composite = (Composite) super.createDialogArea(parent);
				Text text = toolkit.createText(parent, null, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
				GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
				gd.horizontalIndent = 3;
				gd.verticalIndent = 3;
				text.setLayoutData(gd);
				text.setText(storageDataProvider.getStorageData().getDescription());
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
	 * @return If form is disposed.
	 */
	public boolean isDisposed() {
		return form.isDisposed();
	}

	/**
	 * Disposes the form.
	 */
	public void dispose() {
		form.dispose();
	}

}
