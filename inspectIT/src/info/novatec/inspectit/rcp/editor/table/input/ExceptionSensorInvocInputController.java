package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.indexing.aggregation.impl.AggregationPerformer;
import info.novatec.inspectit.indexing.aggregation.impl.ExceptionDataAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.ExceptionDataAggregator.ExceptionAggregationType;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.handlers.ShowHideColumnsHandler;
import info.novatec.inspectit.rcp.model.ExceptionImageFactory;
import info.novatec.inspectit.rcp.model.ModifiersImageFactory;
import info.novatec.inspectit.rcp.repository.service.cache.CachedDataService;
import info.novatec.inspectit.rcp.util.ObjectUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This input controller displays the contents of {@link ExceptionSensorData} objects in an
 * invocation sequence.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionSensorInvocInputController extends AbstractTableInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.exceptionsensorinvoc";

	/**
	 * The resource manager is used for the images etc.
	 */
	private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private static enum Column {
		/** The timestamp column. */
		TIMESTAMP("Timestamp", 150, InspectITImages.IMG_TIMER, false, true),
		/** The fqn column. */
		FQN("Fully-Qualified Name", 400, InspectITImages.IMG_CLASS, true, true),
		/** The count column. */
		CREATED("Created", 60, null, true, false),
		/** The RETHROWN column. */
		RETHROWN("Rethrown", 60, null, true, false),
		/** The HANDLED column. */
		HANDLED("Handled", 60, null, true, false),
		/** The constructor column. */
		CONSTRUCTOR("Constructor", 250, InspectITImages.IMG_METHOD_PUBLIC, false, true),
		/** The error message column. */
		ERROR_MESSAGE("Error Message", 250, null, false, true);

		/** The real viewer column. */
		private TableViewerColumn column;
		/** The name. */
		private String name;
		/** The width of the column. */
		private int width;
		/** The image descriptor. Can be <code>null</code> */
		private Image image;
		/** If the column should be shown in aggregated mode. */
		private boolean showInAggregatedMode;
		/** If the column should be shown in raw mode. */
		private boolean showInRawMode;

		/**
		 * Default constructor which creates a column enumeration object.
		 * 
		 * @param name
		 *            The name of the column.
		 * @param width
		 *            The width of the column.
		 * @param imageName
		 *            The name of the image. Names are defined in {@link InspectITImages}.
		 * @param showInAggregatedMode
		 *            If the column should be shown in aggregated mode.
		 * @param showInRawMode
		 *            If the column should be shown in raw mode.
		 * 
		 */
		private Column(String name, int width, String imageName, boolean showInAggregatedMode, boolean showInRawMode) {
			this.name = name;
			this.width = width;
			this.image = InspectIT.getDefault().getImage(imageName);
			this.showInAggregatedMode = showInAggregatedMode;
			this.showInRawMode = showInRawMode;
		}

		/**
		 * Converts an ordinal into a column.
		 * 
		 * @param i
		 *            The ordinal.
		 * @return The appropriate column.
		 */
		public static Column fromOrd(int i) {
			if (i < 0 || i >= Column.values().length) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return Column.values()[i];
		}
	}

	/**
	 * This data access service is needed because of the ID mappings.
	 */
	private CachedDataService cachedDataService;

	/**
	 * List that is displayed after processing the invocation.
	 */
	private List<ExceptionSensorData> exceptionSensorDataList;

	/**
	 * Empty styled string.
	 */
	private final StyledString emptyStyledString = new StyledString();

	/**
	 * Should view display raw mode or not.
	 */
	private boolean rawMode = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createColumns(TableViewer tableViewer) {
		for (Column column : Column.values()) {
			TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText(column.name);
			if (column.showInAggregatedMode) {
				viewerColumn.getColumn().setWidth(column.width);
			} else {
				viewerColumn.getColumn().setWidth(0);
			}
			if (null != column.image) {
				viewerColumn.getColumn().setImage(column.image);
			}
			column.column = viewerColumn;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canAlterColumnWidth(TableColumn tableColumn) {
		for (Column column : Column.values()) {
			if (Objects.equals(column.column.getColumn(), tableColumn)) {
				return (column.showInRawMode && rawMode) || (column.showInAggregatedMode && !rawMode);
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		preferences.add(PreferenceId.INVOCATION_SUBVIEW_MODE);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		if (PreferenceId.INVOCATION_SUBVIEW_MODE.equals(preferenceEvent.getPreferenceId())) {
			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();
			if (null != preferenceMap && preferenceMap.containsKey(PreferenceId.InvocationSubviewMode.RAW)) {
				Boolean isRawMode = (Boolean) preferenceMap.get(PreferenceId.InvocationSubviewMode.RAW);
				rawMode = isRawMode.booleanValue();
				handleRawAggregatedColumnVisibility(rawMode);
			}
		}
	}

	/**
	 * Handles the raw and aggregated columns hiding/showing.
	 * 
	 * @param rawMode
	 *            Is raw mode active.
	 */
	private void handleRawAggregatedColumnVisibility(boolean rawMode) {
		for (Column column : Column.values()) {
			if (rawMode) {
				if (column.showInRawMode && !column.showInAggregatedMode && !ShowHideColumnsHandler.isColumnHidden(this.getClass(), column.name)) {
					Integer width = ShowHideColumnsHandler.getRememberedColumnWidth(this.getClass(), column.name);
					column.column.getColumn().setWidth((null != width) ? width.intValue() : column.width);
				} else if (!column.showInRawMode && column.showInAggregatedMode) {
					column.column.getColumn().setWidth(0);
				}
			} else {
				if (!column.showInRawMode && column.showInAggregatedMode && !ShowHideColumnsHandler.isColumnHidden(this.getClass(), column.name)) {
					Integer width = ShowHideColumnsHandler.getRememberedColumnWidth(this.getClass(), column.name);
					column.column.getColumn().setWidth((null != width) ? width.intValue() : column.width);
				} else if (column.showInRawMode && !column.showInAggregatedMode) {
					column.column.getColumn().setWidth(0);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		// double click on an sql item will open a details window
		TableViewer tableViewer = (TableViewer) event.getSource();
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Shell parent = tableViewer.getTable().getShell();
		showDetails(parent, selection.getFirstElement());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void showDetails(Shell parent, Object element) {
		final ExceptionSensorData data = (ExceptionSensorData) element;
		final MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());

		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE;
		boolean takeFocusOnOpen = true;
		boolean persistSize = true;
		boolean persistLocation = true;
		boolean showDialogMenu = false;
		boolean showPersistActions = true;
		String titleText = TextFormatter.getMethodString(methodIdent);
		String infoText = "Exception Details";

		PopupDialog dialog = new PopupDialog(parent, shellStyle, takeFocusOnOpen, persistSize, persistLocation, showDialogMenu, showPersistActions, titleText, infoText) {
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
				this.addText(text);

				// Use the compact margins employed by PopupDialog.
				GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
				gd.horizontalIndent = PopupDialog.POPUP_HORIZONTALSPACING;
				gd.verticalIndent = PopupDialog.POPUP_VERTICALSPACING;
				text.setLayoutData(gd);

				return text;
			}

			private void addText(Text text) {
				StringBuffer content = new StringBuffer("Fully-Qualified Name: " + data.getThrowableType() + "\n");
				if (rawMode) {
					content.append("Constructor: " + TextFormatter.getMethodWithParameters(methodIdent));
					content.append("\n");
				}

				content.append("Error Message: " + data.getErrorMessage());
				content.append("\n");

				if (rawMode) {
					content.append("\n");
					content.append("Exception Hierarchy:\n");
					ExceptionSensorData child = data;

					while (null != child) {
						content.append(child.getExceptionEvent().toString().toLowerCase() + " in "
								+ TextFormatter.getMethodWithParameters(cachedDataService.getMethodIdentForId(child.getMethodIdent())));
						content.append("\n");
						child = child.getChild();
					}

					content.append("\n");
					content.append("Stack Trace: ");
					content.append("\n");
					content.append(data.getStackTrace());

				}
				text.setText(content.toString());
			}
		};
		dialog.open();
	}

	@Override
	public boolean canShowDetails() {
		return rawMode;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new ExceptionSensorInvocContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public TableViewerComparator<? extends DefaultData> getComparator() {
		ExceptionSensorInputViewerComparator exceptionSensorInputViewerComparator = new ExceptionSensorInputViewerComparator();
		for (Column column : Column.values()) {
			exceptionSensorInputViewerComparator.addColumn(column.column.getColumn(), column);
		}

		return exceptionSensorInputViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new ExceptionSensorInvocLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canOpenInput(List<? extends DefaultData> data) {
		if (null == data) {
			return false;
		}

		if (data.isEmpty()) {
			return true;
		}

		if (!(data.get(0) instanceof InvocationSequenceData)) {
			return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLimit(int limit) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(IProgressMonitor monitor) {
	}

	/**
	 * The content provider for this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class ExceptionSensorInvocContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<InvocationSequenceData> invocationSequenceDataList = (List<InvocationSequenceData>) inputElement;
			exceptionSensorDataList = getRawExceptionSensorDataList(invocationSequenceDataList, new ArrayList<ExceptionSensorData>());
			if (!rawMode) {
				AggregationPerformer<ExceptionSensorData> aggregationPerformer = new AggregationPerformer<ExceptionSensorData>(new ExceptionDataAggregator(
						ExceptionAggregationType.GROUP_EXCEPTION_OVERVIEW));
				aggregationPerformer.processCollection(exceptionSensorDataList);
				exceptionSensorDataList = aggregationPerformer.getResultList();
			}
			return exceptionSensorDataList.toArray();
		}

		/**
		 * Returns raw list of exceptions.
		 * 
		 * @param invocationSequenceDataList
		 *            Invocations.
		 * @param exceptionSensorDataList
		 *            Result list.
		 * @return List of exceptions for raw display.
		 */
		private List<ExceptionSensorData> getRawExceptionSensorDataList(List<InvocationSequenceData> invocationSequenceDataList, List<ExceptionSensorData> exceptionSensorDataList) {
			for (InvocationSequenceData invocationSequenceData : invocationSequenceDataList) {
				if (null != invocationSequenceData.getExceptionSensorDataObjects() && !invocationSequenceData.getExceptionSensorDataObjects().isEmpty()) {
					for (ExceptionSensorData object : invocationSequenceData.getExceptionSensorDataObjects()) {
						if (ObjectUtils.equals(object.getExceptionEvent(), ExceptionEvent.CREATED)) {
							exceptionSensorDataList.add((ExceptionSensorData) object);
						}
					}
				}
				if (null != invocationSequenceData.getNestedSequences() && !invocationSequenceData.getNestedSequences().isEmpty()) {
					getRawExceptionSensorDataList(invocationSequenceData.getNestedSequences(), exceptionSensorDataList);
				}
			}

			return exceptionSensorDataList;
		}

		/**
		 * {@inheritDoc}
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/**
		 * {@inheritDoc}
		 */
		public void dispose() {
		}

	}

	/**
	 * The exception sensor label provider used by this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class ExceptionSensorInvocLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * Creates the styled text.
		 * 
		 * @param element
		 *            The element to create the styled text for.
		 * @param index
		 *            The index in the column.
		 * @return The created styled string.
		 */
		@Override
		public StyledString getStyledText(Object element, int index) {
			ExceptionSensorData data = (ExceptionSensorData) element;
			Column enumId = Column.fromOrd(index);
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());

			return getStyledTextForColumn(data, methodIdent, enumId);
		}

		/**
		 * Returns the column image for the given element at the given index.
		 * 
		 * @param element
		 *            The element.
		 * @param index
		 *            The index.
		 * @return Returns the Image.
		 */
		@Override
		public Image getColumnImage(Object element, int index) {
			if (rawMode) {
				Column enumId = Column.fromOrd(index);
				switch (enumId) {
				case CONSTRUCTOR:
					ExceptionSensorData data = (ExceptionSensorData) element;
					MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
					Image image = ModifiersImageFactory.getImage(methodIdent.getModifiers());
					image = ExceptionImageFactory.decorateImageWithException(image, data, resourceManager);
					return image;
				default:
					return null;
				}
			} else {
				return null;
			}
		}
	}

	/**
	 * Viewer Comparator used by this input controller to display the contents of
	 * {@link ExceptionSensorData}.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class ExceptionSensorInputViewerComparator extends TableViewerComparator<ExceptionSensorData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int compareElements(Viewer viewer, ExceptionSensorData data1, ExceptionSensorData data2) {
			MethodIdent methodIdent1 = cachedDataService.getMethodIdentForId(data1.getMethodIdent());
			MethodIdent methodIdent2 = cachedDataService.getMethodIdentForId(data2.getMethodIdent());

			switch ((Column) getEnumSortColumn()) {
			case TIMESTAMP:
				if (rawMode) {
					return data1.getTimeStamp().compareTo(data2.getTimeStamp());
				}
				return 0;
			case FQN:
				return data1.getThrowableType().compareTo(data2.getThrowableType());
			case CREATED:
				if (!rawMode && data1 instanceof AggregatedExceptionSensorData && data2 instanceof AggregatedExceptionSensorData) {
					return Long.compare(((AggregatedExceptionSensorData) data1).getCreated(), ((AggregatedExceptionSensorData) data2).getCreated());
				}
				return 0;
			case RETHROWN:
				if (!rawMode && data1 instanceof AggregatedExceptionSensorData && data2 instanceof AggregatedExceptionSensorData) {
					return Long.compare(((AggregatedExceptionSensorData) data1).getPassed(), ((AggregatedExceptionSensorData) data2).getPassed());
				}
				return 0;
			case HANDLED:
				if (!rawMode && data1 instanceof AggregatedExceptionSensorData && data2 instanceof AggregatedExceptionSensorData) {
					return Long.compare(((AggregatedExceptionSensorData) data1).getHandled(), ((AggregatedExceptionSensorData) data2).getHandled());
				}
				return 0;
			case CONSTRUCTOR:
				if (rawMode) {
					String method1 = TextFormatter.getMethodWithParameters(methodIdent1);
					String method2 = TextFormatter.getMethodWithParameters(methodIdent2);
					return method1.compareTo(method2);
				}
				return 0;
			case ERROR_MESSAGE:
				if (rawMode) {
					return data1.getErrorMessage().compareTo(data1.getErrorMessage());
				}
				return 0;
			default:
				return 0;
			}
		}

	}

	/**
	 * Returns the styled text for a specific column.
	 * 
	 * @param data
	 *            The data object to extract the information from.
	 * @param methodIdent
	 *            The method ident object where to retrieve information from.
	 * @param enumId
	 *            The enumeration ID.
	 * @return The styled string containing the information from the data object.
	 */
	private StyledString getStyledTextForColumn(ExceptionSensorData data, MethodIdent methodIdent, Column enumId) {
		if (null != data) {
			switch (enumId) {
			case TIMESTAMP:
				if (rawMode) {
					return new StyledString(NumberFormatter.formatTimeWithMillis(data.getTimeStamp()));
				} else {
					return emptyStyledString;
				}
			case FQN:
				return new StyledString(data.getThrowableType());
			case CREATED:
				if (!rawMode && data instanceof AggregatedExceptionSensorData) {
					return new StyledString(NumberFormatter.formatLong(((AggregatedExceptionSensorData) data).getCreated()));
				} else if (ExceptionEvent.CREATED.equals(data.getExceptionEvent())) {
					return new StyledString("Yes");
				} else {
					return emptyStyledString;
				}
			case RETHROWN:
				if (!rawMode && data instanceof AggregatedExceptionSensorData) {
					return new StyledString(NumberFormatter.formatLong(((AggregatedExceptionSensorData) data).getPassed()));
				} else if (ExceptionEvent.PASSED.equals(data.getExceptionEvent())) {
					return new StyledString("Yes");
				} else {
					return emptyStyledString;
				}
			case HANDLED:
				if (!rawMode && data instanceof AggregatedExceptionSensorData) {
					return new StyledString(NumberFormatter.formatLong(((AggregatedExceptionSensorData) data).getHandled()));
				} else if (ExceptionEvent.HANDLED.equals(data.getExceptionEvent())) {
					return new StyledString("Yes");
				} else {
					return emptyStyledString;
				}
			case CONSTRUCTOR:
				if (rawMode) {
					return new StyledString(TextFormatter.getMethodWithParameters(methodIdent));
				} else {
					return emptyStyledString;
				}
			case ERROR_MESSAGE:
				if (rawMode) {
					StyledString styledString = new StyledString();
					if (null != data.getErrorMessage()) {
						styledString.append(data.getErrorMessage());
					}
					return styledString;
				} else {
					return emptyStyledString;
				}
			default:
				return new StyledString("error");
			}
		}
		return new StyledString("error");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReadableString(Object object) {
		if (object instanceof ExceptionSensorData) {
			ExceptionSensorData data = (ExceptionSensorData) object;
			StringBuilder sb = new StringBuilder();
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			for (Column column : Column.values()) {
				sb.append(getStyledTextForColumn(data, methodIdent, column).toString());
				sb.append("\t");
			}
			return sb.toString();
		}
		throw new RuntimeException("Could not create the human readable string!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getColumnValues(Object object) {
		if (object instanceof ExceptionSensorData) {
			ExceptionSensorData data = (ExceptionSensorData) object;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			List<String> values = new ArrayList<String>();
			for (Column column : Column.values()) {
				values.add(getStyledTextForColumn(data, methodIdent, column).toString());
			}
			return values;
		}
		throw new RuntimeException("Could not create the column values!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getObjectsToSearch(Object tableInput) {
		return exceptionSensorDataList.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		resourceManager.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SubViewClassification getSubViewClassification() {
		return SubViewClassification.SLAVE;
	}
}
