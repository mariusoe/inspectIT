package info.novatec.inspectit.rcp.editor.tree.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.ExceptionImageFactory;
import info.novatec.inspectit.rcp.model.ModifiersImageFactory;
import info.novatec.inspectit.rcp.repository.service.cache.CachedDataService;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
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
 * This input controller displays the detail contents of {@link ExceptionSensorData} objects.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionTreeInputController extends AbstractTreeInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.tree.exceptiontree";

	/**
	 * The list of {@link ExceptionSensorData} objects which is displayed.
	 */
	private List<ExceptionSensorData> exceptionSensorData = new ArrayList<ExceptionSensorData>();

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
		/** The event type column. */
		EVENT_TYPE("Event Type", 280, null),
		/** The method column. */
		METHOD_CONSTRUCTOR("Method / Constructor", 500, InspectITConstants.IMG_METHOD_PUBLIC),
		/** The error message column. */
		ERROR_MESSAGE("Error Message", 250, null),
		/** The cause column. */
		CAUSE("Cause", 120, null);

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
			if ((i < 0) || (i >= Column.values().length)) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return Column.values()[i];
		}
	}

	/**
	 * The cached service is needed because of the ID mappings.
	 */
	private CachedDataService cachedDataService;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);
		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getExpandLevel() {
		return TreeViewer.ALL_LEVELS;
	}

	/**
	 * {@inheritDoc}
	 */
	public void createColumns(TreeViewer treeViewer) {
		for (Column column : Column.values()) {
			TreeViewerColumn viewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText(column.name);
			viewerColumn.getColumn().setWidth(column.width);
			if (null != column.imageDescriptor) {
				viewerColumn.getColumn().setImage(column.imageDescriptor.createImage());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getTreeInput() {
		return exceptionSensorData;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new ExceptionTreeContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new ExceptionTreeLabelProvider();
	}

	/**
	 * {@inheritDoc}.
	 * <p>
	 * @see AbstractTreeInputController#showDetails(Shell, Object)
	 */
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
		String infoText = "Exception Tree";

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
				String content;
				if (methodIdent.getPackageName() != null && !methodIdent.getPackageName().equals("")) {
					content = "Package: " + methodIdent.getPackageName() + "\n";
				} else {
					content = "Package: (default)\n";
				}
				content += "Class: " + methodIdent.getClassName() + "\n";
				content += "Method: " + methodIdent.getMethodName() + "\n";
				content += "Parameters: " + methodIdent.getParameters() + "\n\n";
				content += "Throwable Class: " + data.getThrowableType() + "\n";
				content += "Error Message: " + data.getErrorMessage() + "\n";
				content += "Cause: " + data.getCause() + "\n";

				text.setText(content);
			}
		};
		dialog.open();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canOpenInput(List<? extends DefaultData> data) {
		if (null == data) {
			return false;
		}

		if (data.size() == 0) {
			return true;
		}

		if (!(data.get(0) instanceof ExceptionSensorData)) {
			return false;
		}

		return true;
	}

	/**
	 * The exception tree details label provider for this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class ExceptionTreeLabelProvider extends StyledCellIndexLabelProvider {

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
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

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
			case METHOD_CONSTRUCTOR:
				ImageDescriptor imageDescriptor = ModifiersImageFactory.getImageDescriptor(methodIdent.getModifiers());
				Image image = resourceManager.createImage(imageDescriptor);
				image = ExceptionImageFactory.decorateImageWithException(image, data);

				return image;
			case EVENT_TYPE:
				return null;
			case ERROR_MESSAGE:
				return null;
			case CAUSE:
				return null;
			default:
				return null;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Color getBackground(Object element, int index) {
			return null;
		}
	}

	/**
	 * Returns the styled text for a specific column.
	 * 
	 * @param data
	 *            The data object to extract the information from.
	 * @param methodIdent
	 *            The method ident object.
	 * @param enumId
	 *            The enumeration ID.
	 * @return The styled string containing the information from the data object.
	 */
	private static StyledString getStyledTextForColumn(ExceptionSensorData data, MethodIdent methodIdent, Column enumId) {
		StyledString styledString = null;
		switch (enumId) {
		case METHOD_CONSTRUCTOR:
			return new StyledString(TextFormatter.getMethodWithParameters(methodIdent));
		case EVENT_TYPE:
			styledString = new StyledString(data.getExceptionEvent().toString());
			return styledString;
		case ERROR_MESSAGE:
			styledString = new StyledString();
			if (null != data.getErrorMessage()) {
				styledString.append(data.getErrorMessage());
			}
			return styledString;
		case CAUSE:
			styledString = new StyledString();
			if (null != data.getCause()) {
				styledString.append(data.getCause().toString());
			}
			return styledString;
		default:
			return styledString;
		}
	}

	/**
	 * The exception tree details content provider for this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private static final class ExceptionTreeContentProvider implements ITreeContentProvider {

		/**
		 * {@inheritDoc}
		 */
		public Object[] getChildren(Object parent) {
			ExceptionSensorData exceptionSensorData = (ExceptionSensorData) parent;
			List<ExceptionSensorData> exceptionSensorDataList = new ArrayList<ExceptionSensorData>();
			exceptionSensorDataList.add(exceptionSensorData.getChild());

			return exceptionSensorDataList.toArray();
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getParent(Object child) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasChildren(Object parent) {
			if (parent == null) {
				return false;
			}

			if (parent instanceof ExceptionSensorData) {
				ExceptionSensorData exceptionSensorData = (ExceptionSensorData) parent;
				if (null != exceptionSensorData.getChild()) {
					return true;
				}
			}

			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<ExceptionSensorData> exceptionSensorData = (List<ExceptionSensorData>) inputElement;
			return exceptionSensorData.toArray();
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
