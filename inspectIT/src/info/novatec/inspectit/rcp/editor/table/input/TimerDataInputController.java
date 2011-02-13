package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.service.ITimerDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.TimerData;
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
 * Table input controler for the aggregated Timer data view.
 * 
 * @author Ivan Senic
 * 
 */
public class TimerDataInputController extends AbstractTableInputController {
	
	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.aggregatedtimerdata";

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
		/** Invocation Affiliation. */
		INVOCATION_AFFILLIATION("In Invocations", 120, InspectITConstants.IMG_INVOCATION),
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
	 * Timer data access service.
	 */
	private ITimerDataAccessService timerDataAccessService;

	/**
	 * Global data access service.
	 */
	private CachedGlobalDataAccessService dataAccessService;

	/**
	 * List of Timer data to be displayed.
	 */
	private List<TimerData> timerDataList = new ArrayList<TimerData>();

	/**
	 * Template object used for quering.
	 */
	private TimerData template;

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

		template = new TimerData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		timerDataAccessService = inputDefinition.getRepositoryDefinition().getTimerDataAccessService();
		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
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
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		preferences.add(PreferenceId.CLEAR_BUFFER);
		preferences.add(PreferenceId.LIVEMODE);
		preferences.add(PreferenceId.UPDATE);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canOpenInput(List<? extends DefaultData> data) {
		if (null == data) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTableInput() {
		return timerDataList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void doRefresh(IProgressMonitor monitor) {
		monitor.beginTask("Getting timer data information from the CMR", IProgressMonitor.UNKNOWN);
		List<TimerData> aggregatedTimerData = timerDataAccessService.getAggregatedTimerData(template);

		if (aggregatedTimerData.size() > 0) {
			timerDataList.clear();
			timerDataList.addAll(aggregatedTimerData);
		}

		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContentProvider getContentProvider() {
		return new TimerDataContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return new TimerDataLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TableViewerComparator<? extends DefaultData> getComparator() {
		TimerDataViewerComparator timerDataViewerComparator = new TimerDataViewerComparator();
		for (Column column : Column.values()) {
			timerDataViewerComparator.addColumn(column.column.getColumn(), column);
		}

		return timerDataViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	 * Content provider for the view.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static final class TimerDataContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dispose() {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			return ((List<TimerData>) inputElement).toArray();
		}

	}

	/**
	 * Label provider for the view.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private final class TimerDataLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * {@inheritDoc}
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
	 * Viewer Comparator used by this input controller to display the contents of {@link TimerData}.
	 * 
	 * @author Patrice Bouillet
	 * @author Ivan Senic
	 * 
	 */
	private final class TimerDataViewerComparator extends TableViewerComparator<TimerData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int compareElements(Viewer viewer, TimerData timer1, TimerData timer2) {
			MethodIdent methodIdent1 = dataAccessService.getMethodIdentForId(timer1.getMethodIdent());
			MethodIdent methodIdent2 = dataAccessService.getMethodIdentForId(timer2.getMethodIdent());

			switch ((Column) getEnumSortColumn()) {
			case PACKAGE:
				return methodIdent1.getPackageName().compareTo(methodIdent2.getPackageName());
			case CLASS:
				return methodIdent1.getClassName().compareTo(methodIdent2.getClassName());
			case METHOD:
				String method1 = TextFormatter.getMethodWithParameters(methodIdent1);
				String method2 = TextFormatter.getMethodWithParameters(methodIdent2);
				return method1.compareTo(method2);
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
			return new StyledString(methodIdent.getPackageName());
		case CLASS:
			return new StyledString(methodIdent.getClassName());
		case METHOD:
			return new StyledString(TextFormatter.getMethodWithParameters(methodIdent));
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
			// check if it is a valid data (or if timer data was available)
			if (data.getMin() != Double.MAX_VALUE) {
				return new StyledString(NumberFormatter.formatDouble(data.getAverage()));
			} else {
				return emptyStyledString;
			}
		case MIN:
			if (data.getMin() != Double.MAX_VALUE) {
				return new StyledString(NumberFormatter.formatDouble(data.getMin()));
			} else {
				return emptyStyledString;
			}
		case MAX:
			if (data.getMin() != Double.MAX_VALUE) {
				return new StyledString(NumberFormatter.formatDouble(data.getMax()));
			} else {
				return emptyStyledString;
			}
		case DURATION:
			if (data.getMin() != Double.MAX_VALUE) {
				return new StyledString(NumberFormatter.formatDouble(data.getDuration()));
			} else {
				return emptyStyledString;
			}
		case CPUAVERAGE:
			// check if it is a valid data (or if timer data was available)
			if (data.getCpuMin() != -1 && Double.MAX_VALUE != data.getCpuMin()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuAverage()));
			} else {
				return emptyStyledString;
			}
		case CPUMIN:
			if (data.getCpuMin() != -1 && Double.MAX_VALUE != data.getCpuMin()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuMin()));
			} else {
				return emptyStyledString;
			}
		case CPUMAX:
			if (data.getCpuMin() != -1 && Double.MAX_VALUE != data.getCpuMin()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuMax()));
			} else {
				return emptyStyledString;
			}
		case CPUDURATION:
			if (data.getCpuMin() != -1 && Double.MAX_VALUE != data.getCpuMin()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuDuration()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEAVERAGE:
			if (data.getExclusiveAverage() != -1 && Double.MAX_VALUE != data.getExclusiveMin()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveAverage()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEMAX:
			if (data.getExclusiveMax() != -1 && Double.MAX_VALUE != data.getExclusiveMin()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveMax()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEMIN:
			if (data.getExclusiveMin() != -1 && Double.MAX_VALUE != data.getExclusiveMin()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveMin()));
			} else {
				return emptyStyledString;
			}
		default:
			return new StyledString("error");
		}
	}

}
