package info.novatec.novaspy.rcp.editor.table.input;

import info.novatec.novaspy.cmr.model.MethodIdent;
import info.novatec.novaspy.communication.DefaultData;
import info.novatec.novaspy.communication.MethodSensorData;
import info.novatec.novaspy.communication.data.InvocationSequenceData;
import info.novatec.novaspy.communication.data.TimerData;
import info.novatec.novaspy.rcp.NovaSpy;
import info.novatec.novaspy.rcp.NovaSpyConstants;
import info.novatec.novaspy.rcp.editor.InputDefinition;
import info.novatec.novaspy.rcp.editor.table.TableViewerComparator;
import info.novatec.novaspy.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.novaspy.rcp.formatter.NumberFormatter;
import info.novatec.novaspy.rcp.formatter.TextFormatter;
import info.novatec.novaspy.rcp.repository.service.GlobalDataAccessService;

import java.util.ArrayList;
import java.util.List;

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
 * This input controller displays details of all methods involved in an
 * invocation sequence.
 * 
 * @author Patrice Bouillet
 * 
 */
public class MethodInvocInputController extends AbstractTableInputController {

	/**
	 * The private inner enumeration used to define the used IDs which are
	 * mapped into the columns. The order in this enumeration represents the
	 * order of the columns. If it is reordered, nothing else has to be changed.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static enum Column {
		/** The package column. */
		PACKAGE("Package", 200, NovaSpyConstants.IMG_PACKAGE),
		/** The class column. */
		CLASS("Class", 200, NovaSpyConstants.IMG_CLASS),
		/** The method column. */
		METHOD("Method", 300, NovaSpyConstants.IMG_METHOD_PUBLIC),
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
		/** The total exclusive duration column. */
		EXCLUSIVESUM("Total exclusive duration (ms)", 100, null),
		/** The average exclusive duration column. */
		EXCLUSIVEAVERAGE("Average exclusive duration (ms)", 100, null),
		/** The min exclusive duration column. */
		EXCLUSIVEMIN("Min exclusive duration (ms)", 100, null),
		/** The max exclusive duration column. */
		EXCLUSIVEMAX("Max exclusive duration (ms)", 100, null),
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
	 * Data object only used for this class to hold the computation of the
	 * exlusive time.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	@SuppressWarnings("serial")
	private static class ExclusiveTimerData extends TimerData {
		/**
		 * The average exclusive time.
		 */
		private double averageExclusiveTime;

		/**
		 * The minimum exclusive time.
		 */
		private double minExclusiveTime;
		/**
		 * The maximum exclusive time.
		 */
		private double maxExclusiveTime;
		/**
		 * The total exclusive time.
		 */
		private double sumExclusiveTime = 0.0d;

		/**
		 * @return the averageExclusiveTime
		 */
		public double getAverageExclusiveTime() {
			return averageExclusiveTime;
		}

		/**
		 * @param averageExclusiveTime
		 *            the averageExclusiveTime to set
		 */
		public void setAverageExclusiveTime(double averageExclusiveTime) {
			this.averageExclusiveTime = averageExclusiveTime;
		}

		/**
		 * @return the minExclusiveTime
		 */
		public double getMinExclusiveTime() {
			return minExclusiveTime;
		}

		/**
		 * @param minExclusiveTime
		 *            the minExclusiveTime to set
		 */
		public void setMinExclusiveTime(double minExclusiveTime) {
			this.minExclusiveTime = minExclusiveTime;
		}

		/**
		 * @return the maxExclusiveTime
		 */
		public double getMaxExclusiveTime() {
			return maxExclusiveTime;
		}

		/**
		 * @param maxExclusiveTime
		 *            the maxExclusiveTime to set
		 */
		public void setMaxExclusiveTime(double maxExclusiveTime) {
			this.maxExclusiveTime = maxExclusiveTime;
		}

		/**
		 * @param totalExclusiveTime
		 *            The total exlusive time to add.
		 */
		public void addTotalExclusiveTime(double totalExclusiveTime) {
			this.sumExclusiveTime = this.sumExclusiveTime + totalExclusiveTime;
		}

		/**
		 * @return the total exclusive time.
		 */
		public double getTotalExclusiveTime() {
			return this.sumExclusiveTime;
		}

	}

	/**
	 * This data access service is needed because of the ID mappings.
	 */
	private GlobalDataAccessService dataAccessService;

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
	private final class MethodInvocContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<InvocationSequenceData> invocationSequenceDataList = (List<InvocationSequenceData>) inputElement;
			List<ExclusiveTimerData> timerDataList = extractMethodData(invocationSequenceDataList, new ArrayList<ExclusiveTimerData>());
			return timerDataList.toArray();
		}

		@SuppressWarnings("unchecked")
		private List<ExclusiveTimerData> extractMethodData(List<InvocationSequenceData> invocationSequenceDataList, ArrayList<ExclusiveTimerData> timerDataList) {
			for (InvocationSequenceData invocationSequenceData : invocationSequenceDataList) {
				boolean dataFound = false;
				for (ExclusiveTimerData timerData : timerDataList) {
					if (equalsMethods(invocationSequenceData, timerData)) {
						dataFound = true;
						timerData.increaseCount();
						if (null != invocationSequenceData.getTimerData()) {
							timerData.setMin(Math.min(invocationSequenceData.getTimerData().getMin(), timerData.getMin()));
							timerData.setMax(Math.max(invocationSequenceData.getTimerData().getMax(), timerData.getMax()));
							timerData.addDuration(invocationSequenceData.getTimerData().getDuration());
							timerData.setAverage(timerData.getDuration() / timerData.getCount());

							double exclusiveTime = invocationSequenceData.getTimerData().getDuration() - (computeNestedDuration(invocationSequenceData));
							timerData.setAverageExclusiveTime(((timerData.getAverageExclusiveTime() * (timerData.getCount() - 1)) + exclusiveTime) / timerData.getCount());
							timerData.setMinExclusiveTime(Math.min(exclusiveTime, timerData.getMinExclusiveTime()));
							timerData.setMaxExclusiveTime(Math.max(exclusiveTime, timerData.getMaxExclusiveTime()));
							timerData.addTotalExclusiveTime(exclusiveTime);

							if (-1 != invocationSequenceData.getTimerData().getCpuMin()) {
								timerData.setCpuMin(Math.min(invocationSequenceData.getTimerData().getCpuMin(), timerData.getCpuMin()));
								timerData.setCpuMax(Math.max(invocationSequenceData.getTimerData().getCpuMax(), timerData.getCpuMax()));
								timerData.addCpuDuration(invocationSequenceData.getTimerData().getCpuDuration());
								timerData.setCpuAverage(timerData.getCpuDuration() / timerData.getCount());
							}
						}
						break;
					}
				}
				if (!dataFound) {
					ExclusiveTimerData timerData = new ExclusiveTimerData();
					timerData.setMethodIdent(invocationSequenceData.getMethodIdent());
					timerData.setCount(1L);
					timerDataList.add(timerData);

					double time = Double.MIN_VALUE;
					if (null == invocationSequenceData.getParentSequence()) {
						time = invocationSequenceData.getDuration();
					} else if (null != invocationSequenceData.getTimerData()) {
						time = invocationSequenceData.getTimerData().getDuration();
					} else if (null != invocationSequenceData.getSqlStatementData() && 1 == invocationSequenceData.getSqlStatementData().getCount()) {
						time = invocationSequenceData.getSqlStatementData().getDuration();
					}

					if (time != Double.MIN_VALUE) {
						timerData.setMin(time);
						timerData.setMax(time);
						timerData.setDuration(time);
						timerData.setAverage(time);

						double exclusiveTime = time - (computeNestedDuration(invocationSequenceData));
						timerData.setAverageExclusiveTime(exclusiveTime);
						timerData.setMinExclusiveTime(exclusiveTime);
						timerData.setMaxExclusiveTime(exclusiveTime);
						timerData.addTotalExclusiveTime(exclusiveTime);

						if (-1 != invocationSequenceData.getTimerData().getCpuMin()) {
							timerData.setCpuMin(invocationSequenceData.getTimerData().getCpuMin());
							timerData.setCpuMax(invocationSequenceData.getTimerData().getCpuMax());
							timerData.setCpuDuration(invocationSequenceData.getTimerData().getCpuDuration());
							timerData.setCpuAverage(invocationSequenceData.getTimerData().getCpuAverage());
						}
					}
				}
				if (null != invocationSequenceData.getNestedSequences() && !invocationSequenceData.getNestedSequences().isEmpty()) {
					extractMethodData(invocationSequenceData.getNestedSequences(), timerDataList);
				}
			}

			return timerDataList;
		}

		private boolean equalsMethods(MethodSensorData method1, MethodSensorData method2) {
			if (method1.getMethodIdent() != method2.getMethodIdent()) {
				return false;
			}
			return true;
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
			ExclusiveTimerData data = (ExclusiveTimerData) element;
			MethodIdent methodIdent = dataAccessService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, methodIdent, enumId);
		}
	}

	/**
	 * Viewer Comparator used by this input controller to display the contents
	 * of {@link BasicSQLData}.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class MethodInputViewerComparator extends TableViewerComparator<ExclusiveTimerData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int compareElements(Viewer viewer, ExclusiveTimerData timer1, ExclusiveTimerData timer2) {
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
				return Double.compare(timer1.getTotalExclusiveTime(), timer2.getTotalExclusiveTime());
			case EXCLUSIVEAVERAGE:
				return Double.compare(timer1.getAverageExclusiveTime(), timer2.getAverageExclusiveTime());
			case EXCLUSIVEMIN:
				return Double.compare(timer1.getMinExclusiveTime(), timer2.getMinExclusiveTime());
			case EXCLUSIVEMAX:
				return Double.compare(timer1.getMaxExclusiveTime(), timer2.getMaxExclusiveTime());
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
	 * @return The styled string containing the information from the data
	 *         object.
	 */
	private StyledString getStyledTextForColumn(ExclusiveTimerData data, MethodIdent methodIdent, Column enumId) {
		switch (enumId) {
		case PACKAGE:
			return new StyledString(methodIdent.getPackageName());
		case CLASS:
			return new StyledString(methodIdent.getClassName());
		case METHOD:
			return new StyledString(TextFormatter.getMethodWithParameters(methodIdent));
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
		case EXCLUSIVESUM:
			return new StyledString(NumberFormatter.formatDouble(data.getTotalExclusiveTime()));
		case EXCLUSIVEAVERAGE:
			if (data.getMin() != Double.MAX_VALUE) {
				return new StyledString(NumberFormatter.formatDouble(data.getAverageExclusiveTime()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEMIN:
			if (data.getMin() != Double.MAX_VALUE) {
				return new StyledString(NumberFormatter.formatDouble(data.getMinExclusiveTime()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEMAX:
			if (data.getMin() != Double.MAX_VALUE) {
				return new StyledString(NumberFormatter.formatDouble(data.getMaxExclusiveTime()));
			} else {
				return emptyStyledString;
			}
		default:
			return new StyledString("error");
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
	 * {@inheritDoc}
	 */
	public Object getReadableString(Object object) {
		if (object instanceof ExclusiveTimerData) {
			ExclusiveTimerData data = (ExclusiveTimerData) object;
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

}
