package info.novatec.inspectit.rcp.editor.exception.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.exception.DeferredStackTraces;
import info.novatec.inspectit.rcp.editor.exception.input.ExceptionOverviewInputController.ExtendedExceptionSensorData;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.tree.TreeViewerComparator;
import info.novatec.inspectit.rcp.editor.tree.input.TreeInputController;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.DeferredTreeContentManager;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionMessagesTreeInputController implements TreeInputController {
	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.exceptionmessagestree";

	/**
	 * The list of invocation sequence data objects which is displayed.
	 */
	private List<ExtendedExceptionSensorData> exceptionSensorDataList = new ArrayList<ExtendedExceptionSensorData>();

	/**
	 * The inputDefinition of this view that is passed to the
	 * {@link ExceptionMessagesTreeContentProvider}.
	 */
	private InputDefinition inputDefinition;

	/**
	 * The private inner enumeration used to define the used IDs which are
	 * mapped into the columns. The order in this enumeration represents the
	 * order of the columns. If it is reordered, nothing else has to be changed.
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
		 *            The name of the image. Names are defined in
		 *            {@link InspectITConstants}.
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
	public void setInputDefinition(InputDefinition inputDefinition) {
		this.inputDefinition = inputDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getTreeInput() {
		return exceptionSensorDataList;
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
	public void dispose() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void doubleClick(DoubleClickEvent event) {
		TreeViewer treeViewer = (TreeViewer) event.getSource();
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		final ExceptionSensorData data = (ExceptionSensorData) selection.getFirstElement();
		String trace = "You selected the error message. Please select a stack trace from the subsequent tree.";

		if (null == data.getThrowableType()) {
			// show stack trace in the tool tip only when we selected a stack
			// trace from the tree and not an error message
			trace = data.getStackTrace();
		}

		final String stackTrace = trace;

		Shell parent = treeViewer.getTree().getShell();
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
	public ViewerFilter[] getFilters() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {

	}

	/**
	 * {@inheritDoc}
	 */
	public TreeViewerComparator<? extends DefaultData> getComparator() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new ExceptionMessagesTreeContentProvider(inputDefinition);
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
	public Set<PreferenceId> getPreferenceIds() {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh(IProgressMonitor monitor) {
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canOpenInput(List<? extends DefaultData> data) {
		if (null == data) {
			return false;
		}

		if (data.size() == 0) {
			return false;
		}

		if (!(data.get(0) instanceof ExtendedExceptionSensorData)) {
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
			ExtendedExceptionSensorData data = (ExtendedExceptionSensorData) element;
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
	private static final class ExceptionMessagesTreeContentProvider implements ITreeContentProvider {
		/**
		 * The deferred manager is used here to update the tree in a concurrent
		 * thread so the UI responds much better if many items are displayed.
		 */
		private DeferredTreeContentManager manager;

		/**
		 * The inputDefinition that is needed in {@link DeferredStackTraces}.
		 * 
		 */
		private InputDefinition inputDefinition;

		public ExceptionMessagesTreeContentProvider(InputDefinition inputDefinition) {
			super();
			this.inputDefinition = inputDefinition;
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<ExtendedExceptionSensorData> exceptionSensorData = (List<ExtendedExceptionSensorData>) inputElement;

			// set the inputDefinition so that it later can be used within the
			// DeferredStackTraces class
			for (ExtendedExceptionSensorData data : exceptionSensorData) {
				data.setInputDefinition(this.inputDefinition);
			}
			return exceptionSensorData.toArray();
		}

		/**
		 * {@inheritDoc}
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			manager = new DeferredTreeContentManager((AbstractTreeViewer) viewer);
		}

		/**
		 * {@inheritDoc}
		 */
		public void dispose() {
		}

		/**
		 * {@inheritDoc}
		 */
		public Object[] getChildren(Object parent) {
			if (manager.isDeferredAdapter(parent)) {
				if (parent instanceof ExtendedExceptionSensorData) {
					Object[] children = manager.getChildren(parent);

					if (null == children) {
						children = new Object[0];
					}
					return children;
				}
			}

			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getParent(Object element) {
			return element;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasChildren(Object parent) {
			if (null != parent && parent instanceof ExtendedExceptionSensorData) {
				return true;
			}

			return false;
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
	 * @return The styled string containing the information from the data
	 *         object.
	 */
	private StyledString getStyledTextForColumn(ExtendedExceptionSensorData data, Column enumId) {
		switch (enumId) {
		case ERROR_MESSAGE:
			StyledString styledString;
			String errorMessage = data.getErrorMessage();
			if (null != errorMessage && !"".equals(errorMessage)) {
				// if error message is provided then it's a first level element
				// of the tree
				styledString = new StyledString(errorMessage);
			} else if (null == data.getExceptionEventString() && null != data.getStackTrace()) {
				// otherwise we use an excerpt of the stack trace for the second
				// level element
				String stackTrace = crop(data.getStackTrace(), 80);
				styledString = new StyledString(stackTrace);
			} else {
				// is used when there is no error message provided in the first
				// level element
				styledString = new StyledString("No Error Message provided");
			}
			return styledString;
		case CREATED:
			if (data.getCreatedCounter() >= 0) {
				return new StyledString("" + data.getCreatedCounter());
			}
			return new StyledString("");
		case RETHROWN:
			if (data.getRethrownCounter() >= 0) {
				return new StyledString("" + data.getRethrownCounter());
			}
			return new StyledString("");
		case HANDLED:
			if (data.getHandledCounter() >= 0) {
				return new StyledString("" + data.getHandledCounter());
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
		if (object instanceof ExtendedExceptionSensorData) {
			ExtendedExceptionSensorData data = (ExtendedExceptionSensorData) object;
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
	 * Crops a string if it is longer than the specified maxLength.
	 * 
	 * @param value
	 *            The value to crop.
	 * @param maxLength
	 *            The maximum length of the string. All characters above
	 *            maxLength will be cropped.
	 * @return A cropped string which length is smaller than the maxLength.
	 */
	private String crop(String value, int maxLength) {
		if (null != value && value.length() > maxLength) {
			return value.substring(0, maxLength);
		}
		return value;
	}

}
