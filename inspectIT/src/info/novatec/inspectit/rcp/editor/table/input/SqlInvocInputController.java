package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.indexing.aggregation.impl.AggregationPerformer;
import info.novatec.inspectit.indexing.aggregation.impl.SqlStatementDataAggregator;
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
import info.novatec.inspectit.rcp.repository.service.cache.CachedDataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.PopupDialog;
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
import org.hibernate.jdbc.util.FormatStyle;
import org.hibernate.jdbc.util.Formatter;

/**
 * This input controller displays the contents of {@link SqlStatementData} objects in an invocation
 * sequence.
 * 
 * @author Patrice Bouillet
 * 
 */
public class SqlInvocInputController extends AbstractTableInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.sqlinvoc";

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static enum Column {
		/** The timestamp column. */
		TIMESTAMP("Timestamp", 130, InspectITImages.IMG_TIMER, false, true),
		/** The statement column. */
		STATEMENT("Statement", 600, InspectITImages.IMG_DATABASE, true, true),
		/** The count column. */
		COUNT("Count", 80, null, true, false),
		/** The average column. */
		AVERAGE("Avg (ms)", 80, null, true, false),
		/** The min column. */
		MIN("Min (ms)", 80, null, true, false),
		/** The max column. */
		MAX("Max (ms)", 80, null, true, false),
		/** The duration column. */
		DURATION("Duration (ms)", 80, null, true, true),
		/** The prepared column. */
		PREPARED("Prepared?", 60, null, false, true);

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
	 * The cached service is needed because of the ID mappings.
	 */
	private CachedDataService cachedDataService;

	/**
	 * List that is displayed after processing the invocation.
	 */
	private List<SqlStatementData> sqlStatementDataList;

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
	public void doubleClick(final DoubleClickEvent event) {
		if (canShowDetails()) {
			// double click on an exception item will open a details window
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					// double click on an sql item will open a details window
					TableViewer tableViewer = (TableViewer) event.getSource();
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					Shell parent = tableViewer.getTable().getShell();
					showDetails(parent, selection.getFirstElement());
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void showDetails(Shell parent, Object element) {
		final SqlStatementData data = (SqlStatementData) element;
		final MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());

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
				if (methodIdent.getPackageName() != null && !methodIdent.getPackageName().equals("")) {
					content = "Package: " + methodIdent.getPackageName() + "\n";
				} else {
					content = "Package: (default)\n";
				}
				content += "Class: " + methodIdent.getClassName() + "\n";
				content += "Method: " + methodIdent.getMethodName() + "\n";
				content += "Parameters: " + methodIdent.getParameters() + "\n";

				content += "\n";
				content += "Avg (ms): " + data.getAverage() + "\n";
				content += "Min (ms): " + data.getMin() + "\n";
				content += "Max (ms): " + data.getMax() + "\n";
				content += "Max (ms): " + data.getMax() + "\n";
				content += "Total duration (ms): " + data.getDuration() + "\n";

				if (rawMode) {
					content += "\n";
					content += "Is Prepared Statement: " + data.isPreparedStatement() + "\n";
				}

				Formatter sqlFormatter = FormatStyle.BASIC.getFormatter();
				content += "\n";
				if (rawMode) {
					content += "SQL: " + sqlFormatter.format(data.getSqlWithParameterValues()) + "\n";
				} else {
					content += "SQL: " + sqlFormatter.format(data.getSql()) + "\n";
				}

				text.setText(content);
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
		return new SqlInvocContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public TableViewerComparator<? extends DefaultData> getComparator() {
		SqlInputViewerComparator sqlInputViewerComparator = new SqlInputViewerComparator();
		for (Column column : Column.values()) {
			sqlInputViewerComparator.addColumn(column.column.getColumn(), column);
		}

		return sqlInputViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new SqlInvocLabelProvider();
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
	 * @author Patrice Bouillet
	 * 
	 */
	private final class SqlInvocContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<InvocationSequenceData> invocationSequenceDataList = (List<InvocationSequenceData>) inputElement;
			sqlStatementDataList = getRawInputList(invocationSequenceDataList, new ArrayList<SqlStatementData>());
			if (!rawMode) {
				AggregationPerformer<SqlStatementData> aggregationPerformer = new AggregationPerformer<SqlStatementData>(new SqlStatementDataAggregator(true));
				aggregationPerformer.processCollection(sqlStatementDataList);
				sqlStatementDataList = aggregationPerformer.getResultList();
			} else {
				Collections.sort(sqlStatementDataList, new Comparator<SqlStatementData>() {
					@Override
					public int compare(SqlStatementData o1, SqlStatementData o2) {
						return o1.getTimeStamp().compareTo(o2.getTimeStamp());
					}
				});
			}

			return sqlStatementDataList.toArray();
		}

		/**
		 * Returns the raw list, with no aggregation.
		 * 
		 * @param invocationSequenceDataList
		 *            Input as list of invocations
		 * @param sqlStatementDataList
		 *            List where results will be stored. Needed because of reflection. Note that
		 *            this list will be returned as the result.
		 * @return List of raw order SQL data.
		 */
		private List<SqlStatementData> getRawInputList(List<InvocationSequenceData> invocationSequenceDataList, List<SqlStatementData> sqlStatementDataList) {
			for (InvocationSequenceData invocationSequenceData : invocationSequenceDataList) {
				if (null != invocationSequenceData.getSqlStatementData()) {
					sqlStatementDataList.add(invocationSequenceData.getSqlStatementData());
				}
				if (null != invocationSequenceData.getNestedSequences() && !invocationSequenceData.getNestedSequences().isEmpty()) {
					getRawInputList(invocationSequenceData.getNestedSequences(), sqlStatementDataList);
				}
			}

			return sqlStatementDataList;
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
	 * The sql label provider used by this view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class SqlInvocLabelProvider extends StyledCellIndexLabelProvider {

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
	 * Viewer Comparator used by this input controller to display the contents of
	 * {@link BasicSQLData}.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class SqlInputViewerComparator extends TableViewerComparator<SqlStatementData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int compareElements(Viewer viewer, SqlStatementData sql1, SqlStatementData sql2) {
			switch ((Column) getEnumSortColumn()) {
			case TIMESTAMP:
				if (rawMode) {
					return sql1.getTimeStamp().compareTo(sql2.getTimeStamp());
				} else {
					return 0;
				}
			case STATEMENT:
				if (rawMode) {
					return sql1.getSqlWithParameterValues().compareTo(sql2.getSqlWithParameterValues());
				} else {
					return sql1.getSql().compareTo(sql2.getSql());
				}
			case COUNT:
				return Long.valueOf(sql1.getCount()).compareTo(Long.valueOf(sql2.getCount()));
			case AVERAGE:
				return Double.compare(sql1.getAverage(), sql2.getAverage());
			case MIN:
				return Double.compare(sql1.getMin(), sql2.getMin());
			case MAX:
				return Double.compare(sql1.getMax(), sql2.getMax());
			case DURATION:
				return Double.compare(sql1.getDuration(), sql2.getDuration());
			case PREPARED:
				if (rawMode) {
					return Boolean.compare(sql1.isPreparedStatement(), sql2.isPreparedStatement());
				} else {
					return 0;
				}
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
	 * @param enumId
	 *            The enumeration ID.
	 * @return The styled string containing the information from the data object.
	 */
	private StyledString getStyledTextForColumn(SqlStatementData data, Column enumId) {
		switch (enumId) {
		case TIMESTAMP:
			if (rawMode) {
				return new StyledString(NumberFormatter.formatTimeWithMillis(data.getTimeStamp()));
			} else {
				return emptyStyledString;
			}
		case STATEMENT:
			if (rawMode) {
				String sql = TextFormatter.clearLineBreaks(data.getSqlWithParameterValues());
				return new StyledString(sql);
			} else {
				String sql = TextFormatter.clearLineBreaks(data.getSql());
				return new StyledString(sql);
			}
		case COUNT:
			return new StyledString(Long.toString(data.getCount()));
		case AVERAGE:
			return new StyledString(NumberFormatter.formatDouble(data.getAverage()));
		case MIN:
			return new StyledString(NumberFormatter.formatDouble(data.getMin()));
		case MAX:
			return new StyledString(NumberFormatter.formatDouble(data.getMax()));
		case DURATION:
			return new StyledString(NumberFormatter.formatDouble(data.getDuration()));
		case PREPARED:
			if (rawMode) {
				return new StyledString(Boolean.toString(data.isPreparedStatement()));
			} else {
				return emptyStyledString;
			}
		default:
			return new StyledString("error");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReadableString(Object object) {
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
	public List<String> getColumnValues(Object object) {
		if (object instanceof SqlStatementData) {
			SqlStatementData data = (SqlStatementData) object;
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
	public Object[] getObjectsToSearch(Object tableInput) {
		return sqlStatementDataList.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SubViewClassification getSubViewClassification() {
		return SubViewClassification.SLAVE;
	}

}
