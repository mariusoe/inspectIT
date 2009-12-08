package info.novatec.novaspy.rcp.editor.table.input;

import info.novatec.novaspy.communication.DefaultData;
import info.novatec.novaspy.communication.data.TimerData;
import info.novatec.novaspy.rcp.NovaSpy;
import info.novatec.novaspy.rcp.NovaSpyConstants;
import info.novatec.novaspy.rcp.editor.table.TableViewerComparator;
import info.novatec.novaspy.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.novaspy.rcp.formatter.NumberFormatter;

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
 * This input controller displays an aggregated summary of the timer data
 * objects in a table.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class CombinedMetricsInputController extends AbstractTableInputController {

	/**
	 * The private inner enumeration used to define the used IDs which are
	 * mapped into the columns. The order in this enumeration represents the
	 * order of the columns. If it is reordered, nothing else has to be changed.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private static enum Column {
		/** The count column. */
		COUNT("Count", 100, null),
		/** The average column. */
		AVERAGE("Avg (ms)", 100, null),
		/** The minimum column. */
		MIN("Min (ms)", 100, null),
		/** The maximum column. */
		MAX("Max (ms)", 100, null),
		/** The duration column. */
		DURATION("Duration (ms)", 100, null);

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
			if ((i < 0) || (i >= Column.values().length)) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return Column.values()[i];
		}
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
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public TableViewerComparator<? extends DefaultData> getComparator() {
		// no sorting
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new ContentProvider();
	}

	/**
	 * The content provider.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class ContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<TimerData> timerData = (List<TimerData>) inputElement;
			return aggregateData(timerData);
		}

		/**
		 * Aggregates the timer data to one object.
		 * 
		 * @param timerData
		 *            The data to aggregate.
		 * @return The aggregated data.
		 */
		private Object[] aggregateData(List<TimerData> timerData) {
			if (!timerData.isEmpty()) {
				TimerData aggregatedData = new TimerData();
				aggregatedData.setMax(0d);
				aggregatedData.setMin(Double.MAX_VALUE);
				aggregatedData.setDuration(0d);
				aggregatedData.setCount(0L);

				for (TimerData data : timerData) {
					aggregatedData.setMax(Math.max(aggregatedData.getMax(), data.getMax()));
					aggregatedData.setMin(Math.min(aggregatedData.getMin(), data.getMin()));
					aggregatedData.addDuration(data.getDuration());
					aggregatedData.setCount(aggregatedData.getCount() + data.getCount());
				}
				aggregatedData.setAverage(aggregatedData.getDuration() / aggregatedData.getCount());

				Object[] result = new Object[1];
				result[0] = aggregatedData;
				return result;
			} else {
				return new Object[0];
			}
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
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new LabelProvider();
	}

	/**
	 * The label provider.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class LabelProvider extends StyledCellIndexLabelProvider {

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
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, enumId);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canOpenInput(List<? extends DefaultData> data) {
		if (null == data) {
			return false;
		}

		if (!(data.get(0) instanceof TimerData)) {
			return false;
		}

		return true;
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
	private StyledString getStyledTextForColumn(TimerData data, Column enumId) {
		switch (enumId) {
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
			for (Column column : Column.values()) {
				sb.append(getStyledTextForColumn(data, column).toString());
				sb.append("\t");
			}
			return sb.toString();
		}
		throw new RuntimeException("Could not create the human readable string!");
	}

}
