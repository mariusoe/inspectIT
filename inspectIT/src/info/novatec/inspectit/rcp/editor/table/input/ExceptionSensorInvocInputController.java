package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.ExceptionImageFactory;
import info.novatec.inspectit.rcp.model.ModifiersImageFactory;
import info.novatec.inspectit.rcp.repository.service.cache.CachedDataService;
import info.novatec.inspectit.rcp.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.ImageDescriptor;
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
	 * This map holds the details that are shown in the popup.
	 */
	private Map<String, List<ExceptionSensorData>> exceptionDetailsMap = new HashMap<String, List<ExceptionSensorData>>();

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
		TIMESTAMP("Timestamp", 150, InspectITConstants.IMG_TIMER),
		/** The fqn column. */
		FQN("Fully-Qualified Name", 400, InspectITConstants.IMG_CLASS),
		/** The constructor column. */
		CONSTRUCTOR("Constructor", 250, InspectITConstants.IMG_METHOD_PUBLIC),
		/** The error message column. */
		ERROR_MESSAGE("Error Message", 250, null);

		/** The real viewer column. */
		private TableViewerColumn column;
		/** The name. */
		private String name;
		/** The width of the column. */
		private int width;
		/** The image descriptor. Can be <code>null</code> */
		private ImageDescriptor imageDescriptor;

		/**
		 * Default constructor which creates a column enumeration object.
		 * 
		 * @param name
		 *            The name of the column.
		 * @param width
		 *            The width of the column.
		 * @param imageName
		 *            The name of the image. Names are defined in {@link InspectITConstants}.
		 */
		private Column(String name, int width, String imageName) {
			this.name = name;
			this.width = width;
			this.imageDescriptor = InspectIT.getDefault().getImageDescriptor(imageName);
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
			viewerColumn.getColumn().setWidth(column.width);
			if (null != column.imageDescriptor) {
				viewerColumn.getColumn().setImage(column.imageDescriptor.createImage());
			}
			column.column = viewerColumn;
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
				content.append("Constructor: " + TextFormatter.getMethodWithParameters(methodIdent));
				content.append("\n");
				content.append("Error Message: " + data.getErrorMessage());
				content.append("\n");

				if (null != exceptionDetailsMap && !exceptionDetailsMap.isEmpty()) {
					content.append("\n");
					content.append("Exception Hierarchy:\n");
					List<ExceptionSensorData> exceptionDetails = exceptionDetailsMap.get(data.getThrowableType());

					for (ExceptionSensorData exceptionSensorData : exceptionDetails) {
						content.append(exceptionSensorData.getExceptionEvent().toString().toLowerCase() + " in "
								+ TextFormatter.getMethodWithParameters(cachedDataService.getMethodIdentForId(exceptionSensorData.getMethodIdent())));
						content.append("\n");
					}
				}
				content.append("\n");
				content.append("Stack Trace: ");
				content.append("\n");
				content.append(data.getStackTrace());

				text.setText(content.toString());
			}
		};
		dialog.open();
	}

	@Override
	public boolean canShowDetails() {
		return true;
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

		if (data.size() != 1) {
			return false;
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
			List<ExceptionSensorData> exceptionSensorDataList = updateErrorMessagesAndExtractOverview(extractExceptionSensorData(invocationSequenceDataList, new ArrayList<ExceptionSensorData>()));
			return exceptionSensorDataList.toArray();
		}

		private List<ExceptionSensorData> extractExceptionSensorData(List<InvocationSequenceData> invocationSequenceDataList, ArrayList<ExceptionSensorData> exceptionSensorDataList) {
			for (InvocationSequenceData invocationSequenceData : invocationSequenceDataList) {
				if (null != invocationSequenceData.getExceptionSensorDataObjects() && !invocationSequenceData.getExceptionSensorDataObjects().isEmpty()) {
					for (Object object : invocationSequenceData.getExceptionSensorDataObjects()) {
						exceptionSensorDataList.add((ExceptionSensorData) object);
					}
				}
				if (null != invocationSequenceData.getNestedSequences() && !invocationSequenceData.getNestedSequences().isEmpty()) {
					extractExceptionSensorData(invocationSequenceData.getNestedSequences(), exceptionSensorDataList);
				}
			}

			return exceptionSensorDataList;
		}

		/**
		 * Need to update the error message on each data object, because the error message is not
		 * saved in the invocation file.
		 * 
		 * @param exceptionSensorDataList
		 *            The list containing the data objects to be updated.
		 * @return A list where the objects' error message is updated.
		 */
		private List<ExceptionSensorData> updateErrorMessagesAndExtractOverview(List<ExceptionSensorData> exceptionSensorDataList) {
			if (null != exceptionSensorDataList && !exceptionSensorDataList.isEmpty()) {
				List<ExceptionSensorData> result = new ArrayList<ExceptionSensorData>();
				result.addAll(exceptionSensorDataList);
				Collections.reverse(result);
				// update the error message on each object
				for (ExceptionSensorData data : result) {
					if (ExceptionEvent.CREATED.equals(data.getExceptionEvent())) {
						updateErrorMessage(data);
					}
				}

				return extractOverviewAndUpdateDetails(result);
			}
			return Collections.emptyList();
		}

		private void updateErrorMessage(ExceptionSensorData data) {
			ExceptionSensorData child = data.getChild();
			if (null != child) {
				// we store in each object the error message from the root data object that has the
				// CREATED event
				if (!ObjectUtils.equals(data.getErrorMessage(), child.getErrorMessage())) {
					child.setErrorMessage(data.getErrorMessage());
				}
				updateErrorMessage(child);
			}
		}

		/**
		 * Extracts the objects for the overview and refreshes the exceptionDetailsMap.
		 * 
		 * @param exceptionSensorDataList
		 *            The list where to retrieve the information from.
		 * @return A list containing all objects for the overview.
		 */
		private List<ExceptionSensorData> extractOverviewAndUpdateDetails(List<ExceptionSensorData> exceptionSensorDataList) {
			List<ExceptionSensorData> overviewObjects = new ArrayList<ExceptionSensorData>();
			for (ExceptionSensorData exceptionSensorData : exceptionSensorDataList) {
				if (exceptionSensorData.getExceptionEvent().equals(ExceptionEvent.CREATED)) {
					overviewObjects.add(exceptionSensorData);
				}

				List<ExceptionSensorData> nestedObjects = null;
				String throwableType = exceptionSensorData.getThrowableType();
				if (!exceptionDetailsMap.containsKey(throwableType)) {
					nestedObjects = new ArrayList<ExceptionSensorData>();
					nestedObjects.add(exceptionSensorData);
					exceptionDetailsMap.put(throwableType, nestedObjects);
				} else {
					nestedObjects = exceptionDetailsMap.get(throwableType);
					nestedObjects.add(exceptionSensorData);
				}
			}
			return overviewObjects;
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
			ExceptionSensorData data = (ExceptionSensorData) element;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			switch (enumId) {
			case CONSTRUCTOR:
				ImageDescriptor imageDescriptor = ModifiersImageFactory.getImageDescriptor(methodIdent.getModifiers());
				Image image = resourceManager.createImage(imageDescriptor);
				image = ExceptionImageFactory.decorateImageWithException(image, data);

				return image;
			case ERROR_MESSAGE:
				return null;
			case TIMESTAMP:
				return null;
			case FQN:
				return null;
			default:
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
				return data1.getTimeStamp().compareTo(data2.getTimeStamp());
			case FQN:
				return data1.getThrowableType().compareTo(data2.getThrowableType());
			case CONSTRUCTOR:
				String method1 = TextFormatter.getMethodWithParameters(methodIdent1);
				String method2 = TextFormatter.getMethodWithParameters(methodIdent2);
				return method1.compareTo(method2);
			case ERROR_MESSAGE:
				return data1.getErrorMessage().compareTo(data1.getErrorMessage());
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
				return new StyledString(NumberFormatter.formatTimeWithMillis(data.getTimeStamp()));
			case FQN:
				return new StyledString(data.getThrowableType());
			case CONSTRUCTOR:
				return new StyledString(TextFormatter.getMethodWithParameters(methodIdent));
			case ERROR_MESSAGE:
				StyledString styledString = new StyledString();
				if (null != data.getErrorMessage()) {
					styledString.append(data.getErrorMessage());
				}
				return styledString;
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
