package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.service.CachedGlobalDataAccessService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;

/**
 * This input controller displays details of all methods involved in an invocation sequence.
 * 
 * @author Patrice Bouillet
 * 
 */
public class MethodInvocInputController extends AbstractTableInputController {

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static enum Column {
		/** The package column. */
		PACKAGE("Package", 200, InspectITConstants.IMG_PACKAGE),
		/** The class column. */
		CLASS("Class", 200, InspectITConstants.IMG_CLASS),
		/** The method column. */
		METHOD("Method", 300, InspectITConstants.IMG_METHOD_PUBLIC),
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
	 * This data access service is needed because of the ID mappings.
	 */
	private CachedGlobalDataAccessService dataAccessService;

	/**
	 * Empty styled string.
	 */
	private final StyledString emptyStyledString = new StyledString();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
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
	public IContentProvider getContentProvider() {
		return new MethodInvocContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public TableViewerComparator<? extends DefaultData> getComparator() {
		MethodInputViewerComparator methodInputViewerComparator = new MethodInputViewerComparator();
		for (Column column : Column.values()) {
			methodInputViewerComparator.addColumn(column.column.getColumn(), column);
		}

		return methodInputViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new MethodInvocLabelProvider();
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
	 * The content provider for this view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static final class MethodInvocContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<InvocationSequenceData> invocationSequenceDataList = (List<InvocationSequenceData>) inputElement;
			List<TimerData> timerDataList = aggregateTimerData(invocationSequenceDataList, new ArrayList<TimerData>(), new HashMap<Long, TimerData>());
			return timerDataList.toArray();
		}

		/**
		 * Creates a list of aggregated timer data from the invocation data list.
		 * 
		 * @param invocationList
		 *            Invocation list.
		 * @param timerDataList
		 *            List where results will be store (same list is returned). Usually empty list
		 *            should be supplied.
		 * @param cacheMap
		 *            Map for caching. Usually empty map should be supplied.
		 * @return List of aggregated timer data.
		 */
		private List<TimerData> aggregateTimerData(List<InvocationSequenceData> invocationList, List<TimerData> timerDataList, Map<Long, TimerData> cacheMap) {
			for (InvocationSequenceData invocationData : invocationList) {
				TimerData timerData = null;
				if (null != invocationData.getTimerData()) {
					timerData = invocationData.getTimerData();
				} else if (null != invocationData.getSqlStatementData()) {
					timerData = invocationData.getSqlStatementData();
				} else if (null == invocationData.getParentSequence()) {
					timerData = createTimerDataForRootInvocation(invocationData);
				}
				if (null != timerData) {
					TimerData aggregatedTimerData = cacheMap.get(timerData.getMethodIdent());
					if (null != aggregatedTimerData) {
						aggregatedTimerData.aggregateTimerData(timerData);
					} else {
						TimerData clone = new TimerData();
						clone.setPlatformIdent(timerData.getPlatformIdent());
						clone.setMethodIdent(timerData.getMethodIdent());
						clone.setSensorTypeIdent(timerData.getSensorTypeIdent());
						clone.aggregateTimerData(timerData);
						cacheMap.put(timerData.getMethodIdent(), clone);
						timerDataList.add(clone);
					}
				}
				aggregateTimerData(invocationData.getNestedSequences(), timerDataList, cacheMap);
			}

			return timerDataList;
		}

		/**
		 * Creates the timer data from a root invocation object.
		 * 
		 * @param invocationData
		 *            Root invocation object.
		 * @return Timer data with set duration from the invocation and calculated exclusive
		 *         duration.
		 */
		private TimerData createTimerDataForRootInvocation(InvocationSequenceData invocationData) {
			TimerData timerData = new TimerData();
			timerData.setPlatformIdent(invocationData.getPlatformIdent());
			timerData.setMethodIdent(invocationData.getMethodIdent());
			timerData.setDuration(invocationData.getDuration());
			timerData.calculateMax(invocationData.getDuration());
			timerData.calculateMin(invocationData.getDuration());
			timerData.increaseCount();
			double exclusiveTime = invocationData.getDuration() - computeNestedDuration(invocationData);
			timerData.setExclusiveDuration(exclusiveTime);
			timerData.calculateExclusiveMax(exclusiveTime);
			timerData.calculateExclusiveMin(exclusiveTime);
			timerData.increaseExclusiveCount();
			timerData.finalizeData();
			return timerData;
		}

		/**
		 * Computes the duration of the nested invocation elements.
		 * 
		 * @param data
		 *            The data objects which is inspected for its nested elements.
		 * @return The duration of all nested sequences (with their nested sequences as well).
		 */
		private static double computeNestedDuration(InvocationSequenceData data) {
			if (data.getNestedSequences().isEmpty()) {
				return 0;
			}

			double nestedDuration = 0d;
			boolean added = false;
			for (InvocationSequenceData nestedData : (List<InvocationSequenceData>) data.getNestedSequences()) {
				if (null == nestedData.getParentSequence()) {
					nestedDuration = nestedDuration + nestedData.getDuration();
					added = true;
				} else if (null != nestedData.getTimerData()) {
					nestedDuration = nestedDuration + nestedData.getTimerData().getDuration();
					added = true;
				} else if (null != nestedData.getSqlStatementData() && 1 == nestedData.getSqlStatementData().getCount()) {
					nestedDuration = nestedDuration + nestedData.getSqlStatementData().getDuration();
					added = true;
				}
				if (!added && !nestedData.getNestedSequences().isEmpty()) {
					// nothing was added, but there could be child elements with
					// time measurements
					nestedDuration = nestedDuration + computeNestedDuration(nestedData);
				}
				added = false;
			}

			return nestedDuration;
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
	private final class MethodInvocLabelProvider extends StyledCellIndexLabelProvider {

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
			TimerData data = (TimerData) element;
			MethodIdent methodIdent = dataAccessService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, methodIdent, enumId);
		}
	}

	/**
	 * Viewer Comparator used by this input controller to display the contents of
	 * {@link BasicSQLData}.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class MethodInputViewerComparator extends TableViewerComparator<TimerData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int compareElements(Viewer viewer, TimerData timer1, TimerData timer2) {
			MethodIdent methodIdent1 = dataAccessService.getMethodIdentForId(timer1.getMethodIdent());
			MethodIdent methodIdent2 = dataAccessService.getMethodIdentForId(timer2.getMethodIdent());

			switch ((Column) getEnumSortColumn()) {
			case PACKAGE:
				if (methodIdent1.getPackageName() == null || methodIdent1.getPackageName().equals("")) {
					return -1;
				} else if (methodIdent2.getPackageName() == null || methodIdent1.getPackageName().equals("")) {
					return 1;
				} else {
					return methodIdent1.getPackageName().compareTo(methodIdent2.getPackageName());
				}
			case CLASS:
				return methodIdent1.getClassName().compareTo(methodIdent2.getClassName());
			case METHOD:
				String method1 = TextFormatter.getMethodWithParameters(methodIdent1);
				String method2 = TextFormatter.getMethodWithParameters(methodIdent2);
				return method1.compareTo(method2);
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
			case EXCLUSIVESUM:
				return Double.compare(timer1.getExclusiveDuration(), timer2.getExclusiveDuration());
			case EXCLUSIVEAVERAGE:
				return Double.compare(timer1.getExclusiveAverage(), timer2.getExclusiveAverage());
			case EXCLUSIVEMIN:
				return Double.compare(timer1.getExclusiveMin(), timer2.getExclusiveMin());
			case EXCLUSIVEMAX:
				return Double.compare(timer1.getExclusiveMax(), timer2.getExclusiveMax());
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
	 *            The method ident object.
	 * @param enumId
	 *            The enumeration ID.
	 * @return The styled string containing the information from the data object.
	 */
	private StyledString getStyledTextForColumn(TimerData data, MethodIdent methodIdent, Column enumId) {
		switch (enumId) {
		case PACKAGE:
			if (methodIdent.getPackageName() != null && !methodIdent.getPackageName().equals("")) {
				return new StyledString(methodIdent.getPackageName());
			} else {
				return new StyledString("(default)");
			}
		case CLASS:
			return new StyledString(methodIdent.getClassName());
		case METHOD:
			return new StyledString(TextFormatter.getMethodWithParameters(methodIdent));
		case COUNT:
			return new StyledString(String.valueOf(data.getCount()));
		case AVERAGE:
			// check if it is a valid data (or if timer data was available)
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getAverage()));
			} else {
				return emptyStyledString;
			}
		case MIN:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getMin()));
			} else {
				return emptyStyledString;
			}
		case MAX:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getMax()));
			} else {
				return emptyStyledString;
			}
		case DURATION:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getDuration()));
			} else {
				return emptyStyledString;
			}
		case CPUAVERAGE:
			// check if it is a valid data (or if timer data was available)
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuAverage()));
			} else {
				return emptyStyledString;
			}
		case CPUMIN:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuMin()));
			} else {
				return emptyStyledString;
			}
		case CPUMAX:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuMax()));
			} else {
				return emptyStyledString;
			}
		case CPUDURATION:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuDuration()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVESUM:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveDuration()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEAVERAGE:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveAverage()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEMIN:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveMin()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEMAX:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveMax()));
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
	public Object getReadableString(Object object) {
		if (object instanceof TimerData) {
			TimerData data = (TimerData) object;
			StringBuilder sb = new StringBuilder();
			MethodIdent methodIdent = dataAccessService.getMethodIdentForId(data.getMethodIdent());
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
	public SubViewClassification getSubViewClassification() {
		return SubViewClassification.SLAVE;
	}

}
