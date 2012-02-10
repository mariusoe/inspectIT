package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;

/**
 * InputController for <code>HttpTimerData</code> view.
 * 
 * @author Stefan Siegl
 */
public class HttpTimerDataInputController extends AbstractHttpInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.aggregatedhttptimerdata";

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Stefan Siegl
	 * 
	 */
	private static enum Column {
		/** The package column. */
		URI("URI", 300, InspectITConstants.IMG_HTTP_AGGREGATE),
		/** The http method. */
		HTTP_METHOD("Method", 80, null),
		/** Invocation Affiliation. */
		INVOCATION_AFFILLIATION("In Invocations", 130, InspectITConstants.IMG_INVOCATION),
		/** The count column. */
		COUNT("Count", 60, null),
		/** The average column. */
		AVERAGE("Avg (ms)", 60, null),
		/** The minimum column. */
		MIN("Min (ms)", 60, null),
		/** The maximum column. */
		MAX("Max (ms)", 60, null),
		/** The duration column. */
		DURATION("Duration (ms)", 70, null),
		/** The average exclusive duration column. */
		EXCLUSIVEAVERAGE("Exc. Avg (ms)", 80, null),
		/** The min exclusive duration column. */
		EXCLUSIVEMIN("Exc. Min (ms)", 80, null),
		/** The max exclusive duration column. */
		EXCLUSIVEMAX("Exc. Max (ms)", 80, null),
		/** The total exclusive duration column. */
		EXCLUSIVESUM("Exc. duration (ms)", 80, null),
		/** The cpu average column. */
		CPUAVERAGE("Cpu Avg (ms)", 60, null),
		/** The cpu minimum column. */
		CPUMIN("Cpu Min (ms)", 60, null),
		/** The cpu maximum column. */
		CPUMAX("Cpu Max (ms)", 60, null),
		/** The cpu duration column. */
		CPUDURATION("Cpu Duration (ms)", 70, null);

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
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void doRefresh(IProgressMonitor monitor) {
		monitor.beginTask("Getting HTTP timer data information", IProgressMonitor.UNKNOWN);
		List<HttpTimerData> aggregatedTimerData;

		if (autoUpdate) {
			aggregatedTimerData = httptimerDataAccessService.getAggregatedTimerData(template, httpCatorizationOnRequestMethodActive);
		} else {
			aggregatedTimerData = httptimerDataAccessService.getAggregatedTimerData(template, httpCatorizationOnRequestMethodActive, fromDate, toDate);
		}

		timerDataList.clear();
		if (aggregatedTimerData.size() > 0) {
			timerDataList.addAll(aggregatedTimerData);
		}

		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	public IBaseLabelProvider getLabelProvider() {
		return new StyledCellIndexLabelProvider() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public StyledString getStyledText(Object element, int index) {
				HttpTimerData data = (HttpTimerData) element;
				MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
				Column enumId = Column.fromOrd(index);

				StyledString styledString = getStyledTextForColumn(data, methodIdent, enumId);
				if (addWarnSign(data, enumId)) {
					styledString.append(TextFormatter.getWarningSign());
				}
				return styledString;
			}

			/**
			 * Decides if the warn sign should be added for the specific column.
			 *
			 * @param data
			 *            TimerData
			 * @param column
			 *            Column to check.
			 * @return True if warn sign should be added.
			 */
			private boolean addWarnSign(TimerData data, Column column) {
				switch (column) {
				case EXCLUSIVEAVERAGE:
				case EXCLUSIVEMAX:
				case EXCLUSIVEMIN:
				case EXCLUSIVESUM:
					int affPercentage = (int) (data.getInvocationAffiliationPercentage() * 100);
					return data.isExclusiveTimeDataAvailable() && affPercentage < 100;
				default:
					return false;
				}
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TableViewerComparator<? extends DefaultData> getComparator() {
		HttpDataTableViewerComparator httpTimerDataViewerComparator = new HttpDataTableViewerComparator();
		for (Column column : Column.values()) {
			httpTimerDataViewerComparator.addColumn(column.column.getColumn(), column);
		}

		return httpTimerDataViewerComparator;
	}

	/**
	 * Comparator for <code>HttpTimerData</code> in the http view.
	 * 
	 * @author Stefan Siegl
	 */
	private final class HttpDataTableViewerComparator extends TableViewerComparator<HttpTimerData> {

		@Override
		protected int compareElements(Viewer viewer, HttpTimerData timer1, HttpTimerData timer2) {
			switch ((Column) getEnumSortColumn()) {
			case URI:
				return timer1.getUri().compareTo(timer2.getUri());
			case HTTP_METHOD:
				return timer1.getRequestMethod().compareTo(timer2.getRequestMethod());
			case INVOCATION_AFFILLIATION:
				return Double.compare(timer1.getInvocationAffiliationPercentage(), timer2.getInvocationAffiliationPercentage());
			case COUNT:
				return Long.valueOf(timer1.getCount()).compareTo(Long.valueOf(timer2.getCount()));
			case AVERAGE:
				return Double.compare(timer1.getAverage(), timer2.getAverage());
			case MIN:
				return Double.compare(timer1.getMin(), timer2.getMin());
			case MAX:
				return Double.compare(timer1.getMax(), timer2.getMax());
			case DURATION:
				return Double.compare(timer1.getDuration(), timer2.getDuration());
			case CPUAVERAGE:
				return Double.compare(timer1.getCpuAverage(), timer2.getCpuAverage());
			case CPUMIN:
				return Double.compare(timer1.getCpuMin(), timer2.getCpuMin());
			case CPUMAX:
				return Double.compare(timer1.getCpuMax(), timer2.getCpuMax());
			case CPUDURATION:
				return Double.compare(timer1.getCpuDuration(), timer2.getCpuDuration());
			case EXCLUSIVEAVERAGE:
				return Double.compare(timer1.getExclusiveAverage(), timer2.getExclusiveAverage());
			case EXCLUSIVEMAX:
				return Double.compare(timer1.getExclusiveMax(), timer2.getExclusiveMax());
			case EXCLUSIVEMIN:
				return Double.compare(timer1.getExclusiveMin(), timer2.getExclusiveMin());
			case EXCLUSIVESUM:
				return Double.compare(timer1.getExclusiveDuration(), timer2.getExclusiveDuration());
			default:
				return 0;
			}

		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReadableString(Object object) {
		if (object instanceof HttpTimerData) {
			HttpTimerData data = (HttpTimerData) object;
			StringBuilder sb = new StringBuilder();
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			for (Column column : Column.values()) {
				sb.append(getStyledTextForColumn(data, methodIdent, column).toString());
				sb.append("\t");
			}
			return sb.toString();
		} else {
			throw new RuntimeException("Could not create the human readable string! Class is: " + object.getClass().getCanonicalName());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getColumnValues(Object object) {
		if (object instanceof HttpTimerData) {
			HttpTimerData data = (HttpTimerData) object;
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
	private StyledString getStyledTextForColumn(HttpTimerData data, MethodIdent methodIdent, Column enumId) {
		switch (enumId) {
		case URI:
			return new StyledString(data.getUri());
		case HTTP_METHOD:
			return new StyledString(data.getRequestMethod());
		case INVOCATION_AFFILLIATION:
			int percentage = (int) (data.getInvocationAffiliationPercentage() * 100);
			int invocations = 0;
			if (null != data.getInvocationParentsIdSet()) {
				invocations = data.getInvocationParentsIdSet().size();
			}
			return TextFormatter.getInvocationAffilliationPercentageString(percentage, invocations);
		case COUNT:
			return new StyledString(String.valueOf(data.getCount()));
		case AVERAGE:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getAverage(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case MIN:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getMin(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case MAX:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getMax(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case DURATION:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getDuration(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case CPUAVERAGE:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuAverage(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case CPUMIN:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuMin(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case CPUMAX:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuMax(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case CPUDURATION:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuDuration(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEAVERAGE:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveAverage(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEMAX:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveMax(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEMIN:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveMin(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVESUM:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveDuration(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		default:
			return new StyledString("error");
		}
	}

}
