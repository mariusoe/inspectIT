package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.service.CachedGlobalDataAccessService;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ContentViewer;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.hibernate.jdbc.util.FormatStyle;
import org.hibernate.jdbc.util.Formatter;

/**
 * This input controller displays the contents of {@link SqlStatementData}
 * objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public class SqlInputController extends AbstractTableInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.sql";

	/**
	 * The private inner enumeration used to define the used IDs which are
	 * mapped into the columns. The order in this enumeration represents the
	 * order of the columns. If it is reordered, nothing else has to be changed.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static enum Column {
		/** The statement column. */
		STATEMENT("Statement", 600, InspectITConstants.IMG_DATABASE),
		/** The count column. */
		COUNT("Count", 80, null),
		/** The average column. */
		AVERAGE("Avg (ms)", 80, null),
		/** The min column. */
		MIN("Min (ms)", 80, null),
		/** The max column. */
		MAX("Max (ms)", 80, null),
		/** The duration column. */
		DURATION("Duration (ms)", 80, null),
		/** The method column. */
		METHOD("Method", 500, InspectITConstants.IMG_METHOD_PUBLIC);

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
			if (i < 0 || i >= Column.values().length) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return Column.values()[i];
		}
	}

	/**
	 * The limit of the result set.
	 */
	private int limit = 10;

	/**
	 * The template which is send to the Repository to retrieve the actual data.
	 */
	private SqlStatementData template;

	/**
	 * The loaded data from the server which will be displayed.
	 */
	private List<SqlStatementData> sqlData = new ArrayList<SqlStatementData>();

	/**
	 * The data access service to access the data on the CMR.
	 */
	private ISqlDataAccessService dataAccessService;

	/**
	 * This data access service is needed because of the ID mappings.
	 */
	private CachedGlobalDataAccessService globalDataAccessService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new SqlStatementData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setId(-1);

		dataAccessService = inputDefinition.getRepositoryDefinition().getSqlDataAccessService();
		globalDataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
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
	public Object getTableInput() {
		return sqlData;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new SqlContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new SqlLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public TableViewerComparator<? extends DefaultData> getComparator() {
		SqlViewerComparator sqlViewerComparator = new SqlViewerComparator();
		for (Column column : Column.values()) {
			sqlViewerComparator.addColumn(column.column.getColumn(), column);
		}

		return sqlViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		preferences.add(PreferenceId.LIVEMODE);
		preferences.add(PreferenceId.UPDATE);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void doRefresh(IProgressMonitor monitor) {
		monitor.beginTask("Getting SQL information from the CMR", IProgressMonitor.UNKNOWN);
		List<SqlStatementData> invocData = dataAccessService.getAggregatedSqlStatements(template);

		if (invocData.size() > 0) {
			sqlData.clear();
			sqlData.addAll(invocData);
		}

		monitor.done();
	}

	/**
	 * The sql label provider used by this view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class SqlLabelProvider extends StyledCellIndexLabelProvider {

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
			SqlStatementData data = (SqlStatementData) element;
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, enumId);
		}

	}

	/**
	 * The sql content provider used by this view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class SqlContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<SqlStatementData> sqlData = (List<SqlStatementData>) inputElement;
			return sqlData.toArray();
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
	 * Viewer Comparator used by this input controller to display the contents
	 * of {@link BasicSQLData}.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static final class SqlViewerComparator extends TableViewerComparator<SqlStatementData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int compareElements(Viewer viewer, SqlStatementData sql1, SqlStatementData sql2) {
			switch ((Column) getEnumSortColumn()) {
			case STATEMENT:
				return sql1.getSql().compareTo(sql2.getSql());
			case COUNT:
				return Long.valueOf(sql1.getCount()).compareTo(Long.valueOf(sql2.getCount()));
			case AVERAGE:
				return Double.compare(sql1.getAverage(), sql2.getAverage());
			case MIN:
				return Double.compare(sql1.getMin(), sql2.getMin());
			case MAX:
				return Double.compare(sql1.getMax(), sql2.getMax());
			case METHOD:
				IBaseLabelProvider baseLabelProvider = ((ContentViewer) viewer).getLabelProvider();
				SqlLabelProvider sqlLabelProvider = (SqlLabelProvider) baseLabelProvider;
				String text1 = sqlLabelProvider.getStyledText(sql1, Column.METHOD.ordinal()).getString();
				String text2 = sqlLabelProvider.getStyledText(sql2, Column.METHOD.ordinal()).getString();
				return text1.compareTo(text2);
			case DURATION:
				return Double.compare(sql1.getDuration(), sql2.getDuration());
			default:
				return 0;
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		TableViewer tableViewer = (TableViewer) event.getSource();
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		final SqlStatementData data = (SqlStatementData) selection.getFirstElement();
		final MethodIdent methodIdent = globalDataAccessService.getMethodIdentForId(data.getMethodIdent());

		Shell parent = tableViewer.getTable().getShell();
		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE;
		boolean takeFocusOnOpen = true;
		boolean persistSize = true;
		boolean persistLocation = true;
		boolean showDialogMenu = false;
		boolean showPersistActions = true;
		String titleText = TextFormatter.getMethodString(methodIdent);
		String infoText = "SQL Details";

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
				content = "Package: " + methodIdent.getPackageName() + "\n";
				content += "Class: " + methodIdent.getClassName() + "\n";
				content += "Method: " + methodIdent.getMethodName() + "\n";
				content += "Parameters: " + methodIdent.getParameters() + "\n";

				content += "\n";
				content += "Avg (ms): " + data.getAverage() + "\n";
				content += "Min (ms): " + data.getMin() + "\n";
				content += "Max (ms): " + data.getMax() + "\n";
				content += "Total duration (ms): " + data.getDuration() + "\n";

				content += "\n";
				content += "Is Prepared Statement: " + data.isPreparedStatement() + "\n";

				Formatter sqlFormatter = FormatStyle.BASIC.getFormatter();
				content += "\n";
				content += "SQL: " + sqlFormatter.format(data.getSql()) + "\n";

				text.setText(content);
			}
		};
		dialog.open();
	}

	/**
	 * Returns the styled text for a specific column.
	 * 
	 * @param data
	 *            The data object to extract the information from.
	 * @param enumId
	 *            The enumeration ID.
	 * @return The styled string containing the information from the data
	 *         object.
	 */
	private StyledString getStyledTextForColumn(SqlStatementData data, Column enumId) {
		switch (enumId) {
		case STATEMENT:
			return new StyledString(data.getSql());
		case COUNT:
			return new StyledString(Long.toString(data.getCount()));
		case AVERAGE:
			return new StyledString(NumberFormatter.formatDouble(data.getAverage()));
		case MIN:
			return new StyledString(NumberFormatter.formatDouble(data.getMin()));
		case MAX:
			return new StyledString(NumberFormatter.formatDouble(data.getMax()));
		case METHOD:
			MethodIdent methodIdent = globalDataAccessService.getMethodIdentForId(data.getMethodIdent());
			return TextFormatter.getStyledMethodString(methodIdent);
		case DURATION:
			return new StyledString(NumberFormatter.formatDouble(data.getDuration()));
		default:
			return new StyledString("error");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getReadableString(Object object) {
		if (object instanceof SqlStatementData) {
			SqlStatementData data = (SqlStatementData) object;
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
	public void dispose() {
		sqlData.clear();
	}

}
