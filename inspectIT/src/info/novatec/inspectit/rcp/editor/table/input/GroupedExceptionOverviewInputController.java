package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEventEnum;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class GroupedExceptionOverviewInputController extends AbstractTableInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.groupedexceptionoverview";

	/**
	 * The private inner enumeration used to define the used IDs which are
	 * mapped into the columns. The order in this enumeration represents the
	 * order of the columns. If it is reordered, nothing else has to be changed.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private static enum Column {
		/** The class column. */
		FQN("Fully-Qualified Name", 450, InspectITConstants.IMG_CLASS),
		/** The CREATED column. */
		CREATED("Created", 70, null),
		/** The RETHROWN column. */
		RETHROWN("Rethrown", 70, null),
		/** The HANDLED column. */
		HANDLED("Handled", 70, null);

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
			if ((i < 0) || (i >= Column.values().length)) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return Column.values()[i];
		}
	}

	/**
	 * The template object which is send to the server.
	 */
	private ExceptionSensorData template;

	/**
	 * Indicates from which point in time data should be shown.
	 */
	private Date fromDate;

	/**
	 * Indicates until which point in time data should be shown.
	 */
	private Date toDate;

	/**
	 * The list of {@link ExceptionSensorData} objects which is displayed.
	 */
	private List<ExtendedExceptionSensorData> exceptionSensorDataList = new ArrayList<ExtendedExceptionSensorData>();

	/**
	 * This map holds all objects that are needed to be represented in this
	 * view. It uses the fqn of an exception as the key. It contains as value
	 * the objects that are belonging to a specific exception class.
	 */
	private Map<String, List<ExtendedExceptionSensorData>> overviewMap;

	/**
	 * The data access service to access the data on the CMR.
	 */
	private IExceptionDataAccessService dataAccessService;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new ExceptionSensorData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
		template.setMethodIdent(inputDefinition.getIdDefinition().getMethodId());

		dataAccessService = inputDefinition.getRepositoryDefinition().getExceptionDataAccessService();
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
		// this list will be filled with data
		return exceptionSensorDataList;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new GroupedExceptionOverviewContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new GroupedExceptionOverviewLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public TableViewerComparator<? extends DefaultData> getComparator() {
		GroupedExceptionOverviewViewerComparator exceptionOverviewViewerComparator = new GroupedExceptionOverviewViewerComparator();
		for (Column column : Column.values()) {
			exceptionOverviewViewerComparator.addColumn(column.column.getColumn(), column);
		}

		return exceptionOverviewViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		preferences.add(PreferenceId.LIVEMODE);
		preferences.add(PreferenceId.UPDATE);
		preferences.add(PreferenceId.TIMELINE);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		switch (preferenceEvent.getPreferenceId()) {
		case TIMELINE:
			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();
			if (preferenceMap.containsKey(PreferenceId.TimeLine.FROM_DATE_ID)) {
				fromDate = (Date) preferenceMap.get(PreferenceId.TimeLine.FROM_DATE_ID);
			}
			if (preferenceMap.containsKey(PreferenceId.TimeLine.TO_DATE_ID)) {
				toDate = (Date) preferenceMap.get(PreferenceId.TimeLine.TO_DATE_ID);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void doRefresh(IProgressMonitor monitor) {
		monitor.beginTask("Updating Grouped Exception Overview", IProgressMonitor.UNKNOWN);
		monitor.subTask("Retrieving the Grouped Exception Overview data from the CMR");
		List<ExceptionSensorData> ungroupedList = null;

		// if fromDate and toDate are set, then we retrieve only the data for
		// this time interval
		if (null != fromDate && null != toDate) {
			ungroupedList = dataAccessService.getDataForGroupedExceptionOverview(template, fromDate, toDate);
		} else {
			ungroupedList = dataAccessService.getDataForGroupedExceptionOverview(template);
		}

		List<ExtendedExceptionSensorData> groupedOverviewList = new ArrayList<ExtendedExceptionSensorData>();
		overviewMap = new HashMap<String, List<ExtendedExceptionSensorData>>();

		for (ExceptionSensorData ungroupedObject : ungroupedList) {
			List<ExtendedExceptionSensorData> groupedObjects = Collections.EMPTY_LIST;
			ExtendedExceptionSensorData data = copyInformation(ungroupedObject);

			if (!overviewMap.containsKey(ungroupedObject.getThrowableType())) {
				// map doesn't contain the actual exception class, so we create
				// and add a new list for exception classes of the same type
				groupedObjects = new ArrayList<ExtendedExceptionSensorData>();

				// updating the counter values of newly created object based on
				// its event type and adding it to the list
				data.updateCounterForEventType(data.getExceptionEventString(), (int) data.getThrowableIdentityHashCode());
				groupedObjects.add(data);
				overviewMap.put(ungroupedObject.getThrowableType(), groupedObjects);
			} else {
				// map contains the actual exception class, so we get the list
				// and search for the object within the list where the counter
				// values must be updated
				groupedObjects = overviewMap.get(ungroupedObject.getThrowableType());

				if (groupedObjects.contains(data)) {
					// updating the counter values of already saved object
					// based on the actual event type
					ExtendedExceptionSensorData nestedData = groupedObjects.get(groupedObjects.indexOf(data));
					nestedData.updateCounterForEventType(data.getExceptionEventString(), (int) data.getThrowableIdentityHashCode());
				} else {
					// updating the counter values of newly created object based
					// on its event type and adding it to the list
					data.updateCounterForEventType(data.getExceptionEventString(), (int) data.getThrowableIdentityHashCode());
					groupedObjects.add(data);
				}
			}
		}

		// we are creating the list that contains all object to be shown in the
		// overview
		for (Map.Entry<String, List<ExtendedExceptionSensorData>> entry : overviewMap.entrySet()) {
			String throwableType = entry.getKey();
			ExtendedExceptionSensorData data = createObjectForOverview(throwableType, overviewMap.get(throwableType));
			groupedOverviewList.add(data);
		}

		if ((null != groupedOverviewList)) {
			exceptionSensorDataList.clear();
			monitor.subTask("Displaying the Exception Overview");
			exceptionSensorDataList.addAll(groupedOverviewList);
		}

		monitor.done();
	}

	/**
	 * Method is used to create {@link ExtendedExceptionSensorData} object that
	 * are used for the overview of this view. The overview basically shows the
	 * type of the exception (class name) and the additional information about
	 * how often an exceptional event was caused.
	 * 
	 * @param throwableType
	 *            The fqn of the exception class.
	 * @param dataList
	 *            The list containing {@link ExtendedExceptionSensorData} object
	 *            of the same throwableType.
	 * @return An instance of {@link ExtendedExceptionSensorData} that is used
	 *         for the overview of this view and contains simply the fqn with
	 *         additional information about exceptional events.
	 */
	private ExtendedExceptionSensorData createObjectForOverview(String throwableType, List<ExtendedExceptionSensorData> dataList) {
		ExtendedExceptionSensorData data = new ExtendedExceptionSensorData();
		data.setThrowableType(throwableType);

		for (ExtendedExceptionSensorData object : dataList) {
			data.setCreatedCounter(data.getCreatedCounter() + object.getCreatedCounter());
			data.setRethrownCounter(data.getRethrownCounter() + object.getRethrownCounter());
			data.setHandledCounter(data.getHandledCounter() + object.getHandledCounter());
		}

		return data;
	}

	/**
	 * This method simply gets information from the {@link ExceptionSensorData}
	 * object and creates a new object of type
	 * {@link ExtendedExceptionSensorData} with the same data.
	 * 
	 * @param data
	 *            The object where the information is copied from.
	 */
	private ExtendedExceptionSensorData copyInformation(ExceptionSensorData data) {
		ExtendedExceptionSensorData resultObject = new ExtendedExceptionSensorData();
		resultObject.setId(data.getId());
		resultObject.setPlatformIdent(data.getPlatformIdent());
		resultObject.setSensorTypeIdent(data.getSensorTypeIdent());
		resultObject.setErrorMessage(data.getErrorMessage());
		resultObject.setExceptionEventString(data.getExceptionEventString());
		resultObject.setMethodIdent(data.getMethodIdent());
		resultObject.setThrowableIdentityHashCode(data.getThrowableIdentityHashCode());
		resultObject.setThrowableType(data.getThrowableType());
		resultObject.setStackTrace(data.getStackTrace());
		resultObject.setCause(data.getCause());

		return resultObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		final StructuredSelection selection = (StructuredSelection) event.getSelection();
		if (!selection.isEmpty()) {
			try {
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
					public void run(final IProgressMonitor monitor) {
						monitor.beginTask("Retrieving Exception Messages", IProgressMonitor.UNKNOWN);
						ExtendedExceptionSensorData data = (ExtendedExceptionSensorData) selection.getFirstElement();
						final List<ExtendedExceptionSensorData> dataList = (List<ExtendedExceptionSensorData>) overviewMap.get(data.getThrowableType());

						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
								IWorkbenchPage page = window.getActivePage();
								IRootEditor rootEditor = (IRootEditor) page.getActiveEditor();
								rootEditor.setDataInput(dataList);
							}
						});
						monitor.done();
					}
				});
			} catch (InvocationTargetException e) {
				MessageDialog.openError(Display.getDefault().getActiveShell().getShell(), "Error", e.getCause().toString());
			} catch (InterruptedException e) {
				MessageDialog.openInformation(Display.getDefault().getActiveShell().getShell(), "Cancelled", e.getCause().toString());
			}
		}
	}

	/**
	 * The label provider for this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class GroupedExceptionOverviewLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
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
	private static final class GroupedExceptionOverviewContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<ExtendedExceptionSensorData> exceptionSensorData = (List<ExtendedExceptionSensorData>) inputElement;
			return exceptionSensorData.toArray();
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
		case FQN:
			return new StyledString(data.getThrowableType());
		case CREATED:
			return new StyledString("" + data.getCreatedCounter());
		case RETHROWN:
			return new StyledString("" + data.getRethrownCounter());
		case HANDLED:
			return new StyledString("" + data.getHandledCounter());
		default:
			return new StyledString("error");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getReadableString(Object object) {
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
	 * Data object that contains exceptional events and should be used only for
	 * this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	@SuppressWarnings("serial")
	public static class ExtendedExceptionSensorData extends ExceptionSensorData {

		/**
		 * The inputDefinition.
		 */
		private InputDefinition inputDefinition;

		/**
		 * The created counter.
		 */
		private int createdCounter = 0;

		/**
		 * The rethrown counter.
		 */
		private int rethrownCounter = 0;

		/**
		 * The handled counter.
		 */
		private int handledCounter = 0;

		/**
		 * Default no-arg constructor
		 */
		public ExtendedExceptionSensorData() {
			super();
		}

		/**
		 * {@inheritDoc}
		 */
		public ExtendedExceptionSensorData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
			super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
		}

		public int getCreatedCounter() {
			return createdCounter;
		}

		public void setCreatedCounter(int createdCounter) {
			this.createdCounter = createdCounter;
		}

		public int getRethrownCounter() {
			return rethrownCounter;
		}

		public void setRethrownCounter(int rethrownCounter) {
			this.rethrownCounter = rethrownCounter;
		}

		public int getHandledCounter() {
			return handledCounter;
		}

		public void setHandledCounter(int handledCounter) {
			this.handledCounter = handledCounter;
		}

		public InputDefinition getInputDefinition() {
			return inputDefinition;
		}

		public void setInputDefinition(InputDefinition inputDefinition) {
			this.inputDefinition = inputDefinition;
		}

		/**
		 * The respective counter is updated based on the eventType.
		 * 
		 * @param eventType
		 *            The event type of the exception.
		 * @param value
		 *            The value to be added to the respective counter.
		 */
		public void updateCounterForEventType(String eventType, int value) {
			// all other event types are ignored as we are only interested in
			// these event types
			if (eventType.equalsIgnoreCase(ExceptionEventEnum.CREATED.toString())) {
				setCreatedCounter(getCreatedCounter() + value);
			} else if (eventType.equals(ExceptionEventEnum.RETHROWN.toString())) {
				setRethrownCounter(getRethrownCounter() + value);
			} else if (eventType.equals(ExceptionEventEnum.HANDLED.toString())) {
				setHandledCounter(getHandledCounter() + value);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + createdCounter;
			result = prime * result + handledCounter;
			result = prime * result + rethrownCounter;
			return result;
		}

		/**
		 * Be very careful, as only {@link #getErrorMessage()} and the
		 * {@link #getThrowableType()} are used for equality in this context.
		 */
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ExtendedExceptionSensorData other = (ExtendedExceptionSensorData) obj;

			if (getClass() != obj.getClass()) {
				return false;
			}

			if (super.getErrorMessage() == null) {
				if (other.getErrorMessage() != null) {
					return false;
				}
			} else if (!super.getErrorMessage().equals(other.getErrorMessage())) {
				return false;
			}
			if (super.getThrowableType() == null) {
				if (other.getThrowableType() != null) {
					return false;
				}
			} else if (!super.getThrowableType().equals(other.getThrowableType())) {
				return false;
			}
			return true;
		}

	}

	/**
	 * Viewer Comparator used by this input controller to display the contents.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class GroupedExceptionOverviewViewerComparator extends TableViewerComparator<ExtendedExceptionSensorData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int compareElements(Viewer viewer, ExtendedExceptionSensorData data1, ExtendedExceptionSensorData data2) {
			switch ((Column) getEnumSortColumn()) {
			case FQN:
				return data1.getThrowableType().compareTo(data2.getThrowableType());
			case CREATED:
				return Integer.valueOf(data1.getCreatedCounter()).compareTo(Integer.valueOf(data2.getCreatedCounter()));
			case RETHROWN:
				return Integer.valueOf(data1.getRethrownCounter()).compareTo(Integer.valueOf(data2.getRethrownCounter()));
			case HANDLED:
				return Integer.valueOf(data1.getHandledCounter()).compareTo(Integer.valueOf(data2.getHandledCounter()));
			default:
				return 0;
			}
		}
	}

}
