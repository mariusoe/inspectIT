package info.novatec.novaspy.rcp.editor.tree.input;

import info.novatec.novaspy.cmr.model.MethodIdent;
import info.novatec.novaspy.communication.DefaultData;
import info.novatec.novaspy.communication.data.InvocationSequenceData;
import info.novatec.novaspy.communication.data.ParameterContentData;
import info.novatec.novaspy.communication.data.SqlStatementData;
import info.novatec.novaspy.communication.data.TimerData;
import info.novatec.novaspy.rcp.NovaSpy;
import info.novatec.novaspy.rcp.NovaSpyConstants;
import info.novatec.novaspy.rcp.editor.InputDefinition;
import info.novatec.novaspy.rcp.editor.preferences.PreferenceId;
import info.novatec.novaspy.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.novaspy.rcp.editor.tree.TreeViewerComparator;
import info.novatec.novaspy.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.novaspy.rcp.formatter.NumberFormatter;
import info.novatec.novaspy.rcp.formatter.TextFormatter;
import info.novatec.novaspy.rcp.model.ModifiersImageFactory;
import info.novatec.novaspy.rcp.repository.service.GlobalDataAccessService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.hibernate.pretty.Formatter;

/**
 * This input controller displays the detail contents of
 * {@link InvocationSequenceData} objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InvocDetailInputController implements TreeInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "novaspy.subview.tree.invocdetail";

	/**
	 * The total duration of the displayed invocation.
	 */
	private double invocationDuration = 0.0d;

	/**
	 * The cache holding the color objects which are disposed at the end.
	 */
	private Map<Integer, Color> colorCache = new HashMap<Integer, Color>();

	/**
	 * The private inner enumeration used to define the used IDs which are
	 * mapped into the columns. The order in this enumeration represents the
	 * order of the columns. If it is reordered, nothing else has to be changed.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static enum Column {
		/** The method column. */
		METHOD("Method", 700, NovaSpyConstants.IMG_CALL_HIERARCHY),
		/** The duration column. */
		DURATION("Duration (ms)", 100, NovaSpyConstants.IMG_LAST_HOUR),
		/** The exclusive duration column. */
		EXCLUSIVE("Exclusive duration (ms)", 100, null),
		/** The cpu duration column. */
		CPUDURATION("Cpu Duration (ms)", 100, null),
		/** The count column. */
		SQL("SQL", 300, NovaSpyConstants.IMG_DATABASE),
		/** The parameter/field contents. */
		PARAMETER("Parameter Content", 200, null);

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
		 *            {@link NovaSpyConstants}.
		 */
		private Column(String name, int width, String imageName) {
			this.name = name;
			this.width = width;
			this.imageDescriptor = NovaSpy.getDefault().getImageDescriptor(imageName);
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
	private GlobalDataAccessService globalDataAccessService;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		globalDataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
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
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new InvocDetailContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new InvocDetailLabelProvider();
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
	public Set<PreferenceId> getPreferenceIds() {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh(IProgressMonitor monitor) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void doubleClick(final DoubleClickEvent event) {
		TreeViewer treeViewer = (TreeViewer) event.getSource();
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		final InvocationSequenceData data = (InvocationSequenceData) selection.getFirstElement();
		final MethodIdent methodIdent = globalDataAccessService.getMethodIdentForId(data.getMethodIdent());

		Shell parent = treeViewer.getTree().getShell();
		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE;
		boolean takeFocusOnOpen = true;
		boolean persistSize = true;
		boolean persistLocation = true;
		boolean showDialogMenu = false;
		boolean showPersistActions = true;
		String titleText = TextFormatter.getMethodString(methodIdent);
		String infoText = "Invocation Sequence Details";

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

			@SuppressWarnings("unchecked")
			private void addText(Text text) {
				String content;
				content = "Package: " + methodIdent.getPackageName() + "\n";
				content += "Class: " + methodIdent.getClassName() + "\n";
				content += "Method: " + methodIdent.getMethodName() + "\n";
				content += "Parameters: " + methodIdent.getParameters() + "\n";

				if (null != data.getTimerData()) {
					TimerData timer = data.getTimerData();
					content += "\n";
					content += "Method duration: " + timer.getDuration() + "\n";
				}

				if (null != data.getSqlStatementData()) {
					SqlStatementData sql = data.getSqlStatementData();
					Formatter sqlFormatter = new Formatter(sql.getSql());
					content += "\n";
					content += "SQL: " + sqlFormatter.format() + "\n";
				}

				if (null != data.getParameterContentData() && !data.getParameterContentData().isEmpty()) {
					content += "\n";
					content += "Parameter Contents:\n";
					Set<ParameterContentData> parameterContents = data.getParameterContentData();
					for (ParameterContentData parameterContentData : parameterContents) {
						if (parameterContentData.isMethodParameter()) {
							content += "Method Parameter #" + parameterContentData.getSignaturePosition() + " '" + parameterContentData.getName() + "': " + parameterContentData.getContent() + "\n";
						} else {
							content += "Parameter '" + parameterContentData.getName() + "': " + parameterContentData.getContent() + "\n";
						}
					}
				}

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

		if (data.size() != 1) {
			return false;
		}

		if (!(data.get(0) instanceof InvocationSequenceData)) {
			return false;
		}

		// we are saving the complete duration of this invocation sequence
		invocationDuration = ((InvocationSequenceData) data.get(0)).getDuration();

		return true;
	}

	/**
	 * The invoc detail label provider for this view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class InvocDetailLabelProvider extends StyledCellIndexLabelProvider {

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
			InvocationSequenceData data = (InvocationSequenceData) element;
			MethodIdent methodIdent = globalDataAccessService.getMethodIdentForId(data.getMethodIdent());
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
			InvocationSequenceData data = (InvocationSequenceData) element;
			MethodIdent methodIdent = globalDataAccessService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			switch (enumId) {
			case METHOD:
				return ModifiersImageFactory.getImageDescriptor(methodIdent.getModifiers()).createImage();
			case DURATION:
				return null;
			case CPUDURATION:
				return null;
			case EXCLUSIVE:
				return null;
			case SQL:
				return null;
			case PARAMETER:
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
			InvocationSequenceData data = (InvocationSequenceData) element;
			double duration = -1.0d;

			if (null == data.getParentSequence()) {
				duration = data.getDuration();
			} else if (null != data.getTimerData()) {
				duration = data.getTimerData().getDuration();
			} else if (null != data.getSqlStatementData() && 1 == data.getSqlStatementData().getCount()) {
				duration = data.getSqlStatementData().getDuration();
			}

			if (-1.0d != duration) {
				double exclusiveTime = duration - (computeNestedDuration(data));

				// compute the correct color
				int colorValue = 255 - (int) ((exclusiveTime / invocationDuration) * 100);

				if (colorValue > 255 || colorValue < 0) {
					NovaSpy.getDefault().createErrorDialog("The computation of the color value for the detail view returned an invalid value: " + colorValue, null, -1);
					return null;
				}

				// check if the color is in our cache
				if (colorCache.containsKey(colorValue)) {
					return colorCache.get(colorValue);
				}

				Color color = new Color(Display.getDefault(), colorValue, colorValue, colorValue);
				colorCache.put(colorValue, color);
				return color;
			}

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
	 * @return The styled string containing the information from the data
	 *         object.
	 */
	private static StyledString getStyledTextForColumn(InvocationSequenceData data, MethodIdent methodIdent, Column enumId) {
		StyledString styledString = null;
		switch (enumId) {
		case METHOD:
			return TextFormatter.getStyledMethodString(methodIdent);
		case DURATION:
			styledString = new StyledString();
			if (null == data.getParentSequence()) {
				styledString.append(NumberFormatter.formatDouble(data.getDuration()));
			} else if (null != data.getTimerData()) {
				styledString.append(NumberFormatter.formatDouble(data.getTimerData().getDuration()));
			} else if (null != data.getSqlStatementData() && 1 == data.getSqlStatementData().getCount()) {
				styledString.append(NumberFormatter.formatDouble(data.getSqlStatementData().getDuration()));
			}
			return styledString;
		case CPUDURATION:
			styledString = new StyledString();
			if (null != data.getTimerData()) {
				styledString.append(NumberFormatter.formatDouble(data.getTimerData().getCpuDuration()));
			}
			return styledString;
		case EXCLUSIVE:
			styledString = new StyledString();
			double duration = -1.0d;

			if (null == data.getParentSequence()) {
				duration = data.getDuration();
			} else if (null != data.getTimerData()) {
				duration = data.getTimerData().getDuration();
			} else if (null != data.getSqlStatementData() && 1 == data.getSqlStatementData().getCount()) {
				duration = data.getSqlStatementData().getDuration();
			}

			if (-1.0d != duration) {
				double exclusiveTime = duration - (computeNestedDuration(data));
				styledString.append(NumberFormatter.formatDouble(exclusiveTime));
			}

			return styledString;
		case SQL:
			styledString = new StyledString();
			if (null != data.getSqlStatementData()) {
				styledString.append(data.getSqlStatementData().getSql());
			}
			return styledString;
		case PARAMETER:
			styledString = new StyledString();
			if (null != data.getParameterContentData()) {
				@SuppressWarnings("unchecked")
				Set<ParameterContentData> parameters = data.getParameterContentData();
				for (ParameterContentData parameterContentData : parameters) {
					if (parameterContentData.isMethodParameter()) {
						styledString.append("Method Parameter #");
						styledString.append(String.valueOf(parameterContentData.getSignaturePosition()));
					} else {
						styledString.append("Parameter");

					}
					styledString.append(" '");
					styledString.append(parameterContentData.getName());
					styledString.append("': ");
					styledString.append(parameterContentData.getContent().substring(1));
					styledString.append(", ");
				}
			}
		default:
			return styledString;
		}
	}

	/**
	 * Computes the duration of the nested invocation elements.
	 * 
	 * @param data
	 *            The data objects which is inspected for its nested elements.
	 * @return The duration of all nested sequences (with their nested sequences
	 *         as well).
	 */
	@SuppressWarnings("unchecked")
	private static double computeNestedDuration(InvocationSequenceData data) {
		if (data.getNestedSequences().isEmpty()) {
			return 0;
		}

		double nestedDuration = 0d;
		for (InvocationSequenceData nestedData : (List<InvocationSequenceData>) data.getNestedSequences()) {
			if (null == nestedData.getParentSequence()) {
				nestedDuration = nestedDuration + nestedData.getDuration();
			} else if (null != nestedData.getTimerData()) {
				nestedDuration = nestedDuration + nestedData.getTimerData().getDuration();
			} else if (null != nestedData.getSqlStatementData() && 1 == nestedData.getSqlStatementData().getCount()) {
				nestedDuration = nestedDuration + nestedData.getSqlStatementData().getDuration();
			}
		}

		return nestedDuration;
	}

	/**
	 * The invoc detail content provider for this view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static final class InvocDetailContentProvider implements ITreeContentProvider {

		/**
		 * The deferred manager is used here to update the tree in a concurrent
		 * thread so the UI responds much better if many items are displayed.
		 */
		private DeferredTreeContentManager manager;

		/**
		 * {@inheritDoc}
		 */
		public Object[] getChildren(Object parent) {
			if (manager.isDeferredAdapter(parent)) {
				Object[] children = manager.getChildren(parent);

				return children;
			}

			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getParent(Object child) {
			if (child instanceof InvocationSequenceData) {
				InvocationSequenceData invocationSequenceData = (InvocationSequenceData) child;
				return invocationSequenceData.getParentSequence();
			}

			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasChildren(Object parent) {
			if (parent == null) {
				return false;
			}

			if (parent instanceof InvocationSequenceData) {
				InvocationSequenceData invocationSequenceData = (InvocationSequenceData) parent;
				if (!invocationSequenceData.getNestedSequences().isEmpty()) {
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
			List<InvocationSequenceData> invocationSequenceData = (List<InvocationSequenceData>) inputElement;
			return invocationSequenceData.toArray();
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

	}

	/**
	 * {@inheritDoc}
	 */
	public String getReadableString(Object object) {
		if (object instanceof InvocationSequenceData) {
			InvocationSequenceData data = (InvocationSequenceData) object;
			StringBuilder sb = new StringBuilder();
			MethodIdent methodIdent = globalDataAccessService.getMethodIdentForId(data.getMethodIdent());
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
	public void dispose() {
		for (Entry<Integer, Color> entry : colorCache.entrySet()) {
			entry.getValue().dispose();
		}
		colorCache.clear();
	}

}
