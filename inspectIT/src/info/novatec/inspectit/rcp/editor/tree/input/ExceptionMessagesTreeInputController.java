package info.novatec.inspectit.rcp.editor.tree.input;

import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionMessagesTreeInputController extends AbstractTreeInputController {
	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.tree.exceptionmessagestree";

	/**
	 * Data access service for getting the stack traces.
	 */
	private IExceptionDataAccessService dataAccessService;

	/**
	 * Map used to associate parent - children objects.
	 */
	private Map<AggregatedExceptionSensorData, List<ExceptionSensorData>> parentChildrenMap;

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private static enum Column {
		/** The error message column. */
		ERROR_MESSAGE("Error Message with Stack Trace", 450, InspectITConstants.IMG_CLASS),
		/** The CREATED column. */
		CREATED("Created", 70, null),
		/** The RETHROWN column. */
		RETHROWN("Rethrown", 70, null),
		/** The HANDLED column. */
		HANDLED("Handled", 70, null);

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
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		dataAccessService = inputDefinition.getRepositoryDefinition().getExceptionDataAccessService();
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
	public int getExpandLevel() {
		return 0;
	}

	/**
	 * {@inheritDoc}.
	 * <P>
	 * 
	 * @see TreeInputController#showDetails(Shell, Object).
	 */
	@Override
	public void showDetails(Shell parent, Object element) {
		final ExceptionSensorData data = (ExceptionSensorData) element;
		String trace = "You selected the error message. Please select a stack trace from the subsequent tree.";

		if (!parentChildrenMap.containsKey(data)) {
			// show stack trace in the tool tip only when we selected a stack
			// trace from the tree and not an error message
			trace = data.getStackTrace();
		}

		final String stackTrace = trace;

		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE;
		boolean takeFocusOnOpen = true;
		boolean persistSize = true;
		boolean persistLocation = true;
		boolean showDialogMenu = false;
		boolean showPersistActions = true;
		String titleText = "StackTrace";
		String infoText = "StackTrace";

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
				return new Point(600, 400);
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
				String content = stackTrace + "\n";
				text.setText(content);
			}
		};
		dialog.open();
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new ExceptionMessagesTreeContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new ExceptionMessagesTreeLabelProvider();
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

		if (!(data.get(0) instanceof AggregatedExceptionSensorData)) {
			return false;
		}

		return true;
	}

	/**
	 * The label provider for this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class ExceptionMessagesTreeLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * {@inheritDoc}
		 */
		protected StyledString getStyledText(Object element, int index) {
			ExceptionSensorData data = (ExceptionSensorData) element;
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, enumId);
		}
	}

	/**
	 * The content provider for this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class ExceptionMessagesTreeContentProvider implements ITreeContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<AggregatedExceptionSensorData> exceptionSensorData = (List<AggregatedExceptionSensorData>) inputElement;
			return exceptionSensorData.toArray();
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			List<AggregatedExceptionSensorData> input = (List<AggregatedExceptionSensorData>) newInput;
			if (input != null && !input.isEmpty()) {
				// we can get any one because all the ones in the list have the same throwable
				// type
				ExceptionSensorData template = input.get(0);
				List<ExceptionSensorData> exceptionStackTraceObjects = dataAccessService.getStackTraceMessagesForThrowableType(template);
				parentChildrenMap = new HashMap<AggregatedExceptionSensorData, List<ExceptionSensorData>>();
				int i = 1;
				for (AggregatedExceptionSensorData aggExceptionSensorData : input) {
					// id has to be set because if the hash value of the object in the map
					// otherwise there is a chance of the same hash values
					aggExceptionSensorData.setId(i++);
					List<ExceptionSensorData> children = new ArrayList<ExceptionSensorData>();
					for (ExceptionSensorData exData : exceptionStackTraceObjects) {
						if (ObjectUtils.equals(exData.getErrorMessage(), aggExceptionSensorData.getErrorMessage())) {
							children.add(exData);
						}
					}
					parentChildrenMap.put(aggExceptionSensorData, children);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public Object[] getChildren(Object parent) {
			if (null != parent && parent instanceof AggregatedExceptionSensorData) {
				if (parentChildrenMap.containsKey(parent)) {
					return parentChildrenMap.get(parent).toArray();
				}
			}

			return null; // NOPMD
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getParent(Object element) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasChildren(Object parent) {
			if (null != parent && parent instanceof AggregatedExceptionSensorData) {
				if (parentChildrenMap.containsKey(parent)) {
					return true;
				}
			}

			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public void dispose() {
		}
	}

	/**
	 * Returns styled string for {@link AggregatedExceptionSensorData}.
	 * 
	 * @param data
	 *            Data.
	 * @param enumId
	 *            Column.
	 * @return String
	 */
	private StyledString getStyledTextForColumn(ExceptionSensorData data, Column enumId) {
		switch (enumId) {
		case ERROR_MESSAGE:
			StyledString styledString;
			if (parentChildrenMap.containsKey(data)) {
				String errorMessage = data.getErrorMessage();
				if (null != errorMessage && !"".equals(errorMessage)) {
					// if error message is provided then it's a first level element
					// of the tree
					styledString = new StyledString(errorMessage);
				} else {
					// is used when there is no error message provided in the first
					// level element
					styledString = new StyledString("No Error Message provided");
				}
			} else {
				String[] stackTraceLines = data.getStackTrace().split("\n");
				if (stackTraceLines.length > 1) {
					styledString = new StyledString(stackTraceLines[1]);
				} else {
					styledString = new StyledString("Stack track not available");
				}
			}
			return styledString;
		case CREATED:
			if (data instanceof AggregatedExceptionSensorData) {
				if (((AggregatedExceptionSensorData) data).getCreated() >= 0) {
					return new StyledString("" + ((AggregatedExceptionSensorData) data).getCreated());
				}
			}
			return new StyledString("");
		case RETHROWN:
			if (data instanceof AggregatedExceptionSensorData) {
				if (((AggregatedExceptionSensorData) data).getPassed() >= 0) {
					return new StyledString("" + ((AggregatedExceptionSensorData) data).getPassed());
				}
			}
			return new StyledString("");
		case HANDLED:
			if (data instanceof AggregatedExceptionSensorData) {
				if (((AggregatedExceptionSensorData) data).getHandled() >= 0) {
					return new StyledString("" + ((AggregatedExceptionSensorData) data).getHandled());
				}
			}
			return new StyledString("");
		default:
			return new StyledString("error");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReadableString(Object object) {
		if (object instanceof AggregatedExceptionSensorData) {
			AggregatedExceptionSensorData data = (AggregatedExceptionSensorData) object;
			StringBuilder sb = new StringBuilder();
			for (Column column : Column.values()) {
				sb.append(getStyledTextForColumn(data, column).toString());
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
		if (object instanceof AggregatedExceptionSensorData) {
			AggregatedExceptionSensorData data = (AggregatedExceptionSensorData) object;
			List<String> values = new ArrayList<String>();
			for (Column column : Column.values()) {
				values.add(getStyledTextForColumn(data, column).toString());
			}
			return values;
		}
		throw new RuntimeException("Could not create the column values!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SubViewClassification getSubViewClassification() {
		return SubViewClassification.SLAVE;
	}
}
