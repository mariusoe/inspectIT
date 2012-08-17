package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId.TimeResolution;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.text.input.SqlStatementTextInputController;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Input controller for the table view that is displayed below the aggregated SQL table.
 * <p>
 * Shows one statement aggregated on parameter basis.
 * 
 * @author Ivan Senic
 * 
 */
public class SqlParameterAggregationInputControler extends AbstractTableInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.sqlparameteraggregation";

	/**
	 * @author Ivan Senic
	 * 
	 */
	private static enum Column {
		/** The Parameters column. */
		PARAMETERS("Parameters", 600, null),
		/** Invocation Affiliation. */
		INVOCATION_AFFILLIATION("In Invocations", 120, InspectITImages.IMG_INVOCATION),
		/** The count column. */
		COUNT("Count", 80, null),
		/** The average column. */
		AVERAGE("Avg (ms)", 80, null),
		/** The min column. */
		MIN("Min (ms)", 80, null),
		/** The max column. */
		MAX("Max (ms)", 80, null),
		/** The duration column. */
		DURATION("Duration (ms)", 80, null);

		/** The real viewer column. */
		private TableViewerColumn column;
		/** The name. */
		private String name;
		/** The width of the column. */
		private int width;
		/** The image descriptor. Can be <code>null</code> */
		private Image image;

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
			this.image = InspectIT.getDefault().getImage(imageName);
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
	 * Decimal places.
	 */
	private int timeDecimalPlaces = TimeResolution.DECIMAL_PLACES_DEFAULT;

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
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		if (getInputDefinition().getRepositoryDefinition() instanceof CmrRepositoryDefinition) {
			preferences.add(PreferenceId.CLEAR_BUFFER);
		}
		preferences.add(PreferenceId.TIME_RESOLUTION);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		switch (preferenceEvent.getPreferenceId()) {
		case TIME_RESOLUTION:
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeResolution.TIME_DECIMAL_PLACES_ID)) {
				timeDecimalPlaces = (Integer) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeResolution.TIME_DECIMAL_PLACES_ID);
			}
			break;
		default:
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTableInput() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canOpenInput(List<? extends DefaultData> data) {
		if (data != null) {
			for (DefaultData defaultData : data) {
				if (!(defaultData instanceof SqlStatementData)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContentProvider getContentProvider() {
		return new SqlParameterContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return new SqlLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	public void doubleClick(DoubleClickEvent event) {
		final StructuredSelection selection = (StructuredSelection) event.getSelection();
		if (!selection.isEmpty()) {
			Object selected = selection.getFirstElement();
			if (selected instanceof SqlStatementData) {
				passSqlWithParameters((SqlStatementData) selected);
			}
		}
	}

	/**
	 * Returns styled string for the column.
	 * 
	 * @param data
	 *            Data to return string for.
	 * @param enumId
	 *            Enumerated column.
	 * @return {@link StyledString}.
	 */
	private StyledString getStyledTextForColumn(SqlStatementData data, Column enumId) {
		switch (enumId) {
		case PARAMETERS:
			return new StyledString(TextFormatter.getSqlParametersText(data.getParameterValues()));
		case INVOCATION_AFFILLIATION:
			int percentage = (int) (data.getInvocationAffiliationPercentage() * 100);
			int invocations = 0;
			if (null != data.getInvocationParentsIdSet()) {
				invocations = data.getInvocationParentsIdSet().size();
			}
			return TextFormatter.getInvocationAffilliationPercentageString(percentage, invocations);
		case COUNT:
			return new StyledString(Long.toString(data.getCount()));
		case AVERAGE:
			return new StyledString(NumberFormatter.formatDouble(data.getAverage(), timeDecimalPlaces));
		case MIN:
			return new StyledString(NumberFormatter.formatDouble(data.getMin(), timeDecimalPlaces));
		case MAX:
			return new StyledString(NumberFormatter.formatDouble(data.getMax(), timeDecimalPlaces));
		case DURATION:
			return new StyledString(NumberFormatter.formatDouble(data.getDuration(), timeDecimalPlaces));
		default:
			return new StyledString("error");
		}
	}

	/**
	 * Passes the {@link SqlStatementData} to the bottom view that displays the SQL string.
	 * 
	 * @param sqlStatementData
	 *            Data to pass.
	 */
	private void passSqlWithParameters(SqlStatementData sqlStatementData) {
		final List<DefaultData> dataList = new ArrayList<DefaultData>();
		dataList.add(sqlStatementData);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				IRootEditor rootEditor = (IRootEditor) page.getActiveEditor();
				ISubView sqlStringSubView = rootEditor.getSubView().getSubViewWithInputController(SqlStatementTextInputController.class);
				if (null != sqlStringSubView) {
					sqlStringSubView.setDataInput(dataList);
				}
			}
		});
	}

	/**
	 * The sql label provider used by this view.
	 * 
	 * @author Ivan Senic
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
	 * Viewer Comparator used by this input controller.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static final class SqlViewerComparator extends TableViewerComparator<SqlStatementData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int compareElements(Viewer viewer, SqlStatementData sql1, SqlStatementData sql2) {
			IBaseLabelProvider baseLabelProvider = ((ContentViewer) viewer).getLabelProvider();
			SqlLabelProvider sqlLabelProvider = (SqlLabelProvider) baseLabelProvider;
			switch ((Column) getEnumSortColumn()) {
			case PARAMETERS:
				String params1 = sqlLabelProvider.getStyledText(sql1, Column.PARAMETERS.ordinal()).getString();
				String params2 = sqlLabelProvider.getStyledText(sql2, Column.PARAMETERS.ordinal()).getString();
				return params1.compareTo(params2);
			case INVOCATION_AFFILLIATION:
				return Double.compare(sql1.getInvocationAffiliationPercentage(), sql2.getInvocationAffiliationPercentage());
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
			default:
				return 0;
			}
		}

	}

	/**
	 * @author Ivan Senic
	 * 
	 */
	private final class SqlParameterContentProvider extends ArrayContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(final Viewer viewer, Object oldInput, Object newInput) {
			if (null == newInput || Objects.equals(newInput, Collections.emptyList())) {
				viewer.getControl().setEnabled(false);
			} else {
				final List<SqlStatementData> list = (List<SqlStatementData>) newInput;
				viewer.getControl().setEnabled(true);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						SqlParameterAggregationInputControler.this.passSqlWithParameters(list.get(0));
						StructuredSelection structuredSelection = new StructuredSelection(list.get(0));
						viewer.setSelection(structuredSelection, true);
					}
				});
			}

		}
	}

}
