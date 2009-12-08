package info.novatec.novaspy.rcp.editor.exception.input;

import info.novatec.novaspy.cmr.model.MethodIdent;
import info.novatec.novaspy.cmr.service.IExceptionDataAccessService;
import info.novatec.novaspy.communication.DefaultData;
import info.novatec.novaspy.communication.ExceptionEventEnum;
import info.novatec.novaspy.communication.data.ExceptionSensorData;
import info.novatec.novaspy.rcp.NovaSpy;
import info.novatec.novaspy.rcp.NovaSpyConstants;
import info.novatec.novaspy.rcp.editor.InputDefinition;
import info.novatec.novaspy.rcp.editor.preferences.PreferenceId;
import info.novatec.novaspy.rcp.editor.table.TableViewerComparator;
import info.novatec.novaspy.rcp.editor.table.input.AbstractTableInputController;
import info.novatec.novaspy.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.novaspy.rcp.repository.service.GlobalDataAccessService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class ExceptionOverviewInputController extends AbstractTableInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "novaspy.subview.table.exceptionoverview";

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
		CLASS("Class", 250, NovaSpyConstants.IMG_CLASS),
		/** The package column. */
		PACKAGE("Package", 400, NovaSpyConstants.IMG_PACKAGE),
		/** The CREATED column. */
		CREATED("Created", 80, null),
		/** The RETHROWN column. */
		RETHROWN("Rethrown", 80, null),
		/** The HANDLED column. */
		HANDLED("Handled", 80, null);

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
	 * The template object which is send to the server.
	 */
	private ExceptionSensorData template;

	/**
	 * The list of invocation sequence data objects which is displayed.
	 */
	private List<ExtendedExceptionSensorData> exceptionSensorData = new ArrayList<ExtendedExceptionSensorData>();

	/**
	 * The limit of the result set.
	 */
	private int limit = 10;

	/**
	 * This data access service is needed because of the ID mappings.
	 */
	private GlobalDataAccessService globalDataAccessService;

	/**
	 * The data access service to access the data on the CMR.
	 */
	private IExceptionDataAccessService dataAccessService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new ExceptionSensorData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
		template.setMethodIdent(inputDefinition.getIdDefinition().getMethodId());

		globalDataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
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
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTableInput() {
		// this list will be filled with data
		return exceptionSensorData;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new ExceptionOverviewContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new ExceptionOverviewLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public TableViewerComparator<? extends DefaultData> getComparator() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		preferences.add(PreferenceId.LIVEMODE);
		preferences.add(PreferenceId.UPDATE);
		preferences.add(PreferenceId.ITEMCOUNT);
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
		monitor.beginTask("Updating Exception Overview", IProgressMonitor.UNKNOWN);
		monitor.subTask("Retrieving the Exception Overview from the CMR");
		List<ExceptionSensorData> exceptionData = dataAccessService.getExceptionTreeOverview(template);
		List<ExceptionSensorData> exceptionDataDetails = Collections.EMPTY_LIST;
		List<ExtendedExceptionSensorData> extendedExceptionData = new ArrayList<ExtendedExceptionSensorData>();
		List<ExtendedExceptionSensorData> result = new ArrayList<ExtendedExceptionSensorData>();
		Map<String, ExtendedExceptionSensorData> resultMap = new HashMap<String, ExtendedExceptionSensorData>();

		// TODO ET: check whether this can be implemented with Hibernate
		// Projections
		for (ExceptionSensorData data : exceptionData) {
			exceptionDataDetails = dataAccessService.getExceptionTreeDetails(data);
			ExtendedExceptionSensorData resultObject = new ExtendedExceptionSensorData();

			for (ExceptionSensorData exObject : exceptionDataDetails) {
				String eventType = exObject.getExceptionEventString();
				if (eventType.equalsIgnoreCase(ExceptionEventEnum.CREATED.toString())) {
					fillWithInformation(resultObject, exObject);
					resultObject.setCreatedCounter(resultObject.getCreatedCounter() + 1);
				} else if (eventType.equals(ExceptionEventEnum.RETHROWN.toString())) {
					resultObject.setRethrownCounter(resultObject.getRethrownCounter() + 1);
				} else if (eventType.equals(ExceptionEventEnum.HANDLED.toString())) {
					resultObject.setHandledCounter(resultObject.getHandledCounter() + 1);
				}
			}
			extendedExceptionData.add(resultObject);
		}

		// now summarize the exception classes
		if ((null != extendedExceptionData) && !extendedExceptionData.isEmpty()) {
			for (ExtendedExceptionSensorData extendedExceptionSensorData : extendedExceptionData) {
				if (resultMap.containsKey(extendedExceptionSensorData.getThrowableType())) {
					ExtendedExceptionSensorData data = resultMap.get(extendedExceptionSensorData.getThrowableType());
					data.setCreatedCounter(data.getCreatedCounter() + extendedExceptionSensorData.getCreatedCounter());
					data.setRethrownCounter(data.getRethrownCounter() + extendedExceptionSensorData.getRethrownCounter());
					data.setHandledCounter(data.getHandledCounter() + extendedExceptionSensorData.getHandledCounter());
				} else {
					resultMap.put(extendedExceptionSensorData.getThrowableType(), extendedExceptionSensorData);
				}
			}

			// list is reused for copying the values of the resultMap into it
			extendedExceptionData.clear();
			extendedExceptionData.addAll(resultMap.values());

			// result contains limit elements.
			int limitCounter = 0;
			while ((limitCounter < extendedExceptionData.size()) && (limitCounter < limit)) {
				result.add(extendedExceptionData.get(limitCounter));
				limitCounter++;
			}
		}

		if ((null != result) && !result.isEmpty()) {
			exceptionSensorData.clear();
			monitor.subTask("Displaying the Exception Overview");
			exceptionSensorData.addAll(result);
		}

		monitor.done();
	}

	/**
	 * This method simply gets information from the exObject and sets it on the
	 * resultObject.
	 * 
	 * @param resultObject
	 *            The resulting object where to set the information.
	 * @param exObject
	 *            The object where the information is copied from.
	 */
	private void fillWithInformation(ExtendedExceptionSensorData resultObject, ExceptionSensorData exObject) {
		resultObject.setExceptionEventString(exObject.getExceptionEventString());
		resultObject.setId(exObject.getId());
		resultObject.setMethodIdent(exObject.getMethodIdent());
		resultObject.setPlatformIdent(exObject.getPlatformIdent());
		resultObject.setSensorTypeIdent(exObject.getSensorTypeIdent());
		resultObject.setThrowableType(exObject.getThrowableType());
		resultObject.setTimeStamp(exObject.getTimeStamp());
	}

	/**
	 * The label provider for this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class ExceptionOverviewLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StyledString getStyledText(Object element, int index) {
			ExtendedExceptionSensorData data = (ExtendedExceptionSensorData) element;
			MethodIdent methodIdent = globalDataAccessService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, methodIdent, enumId);
		}

	}

	/**
	 * The content provider for this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private static final class ExceptionOverviewContentProvider implements IStructuredContentProvider {

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
	private StyledString getStyledTextForColumn(ExtendedExceptionSensorData data, MethodIdent methodIdent, Column enumId) {
		switch (enumId) {
		case PACKAGE:
			return new StyledString(methodIdent.getPackageName());
		case CLASS:
			return new StyledString(methodIdent.getClassName());
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
	 * Data object only for this class which counts exception events.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	@SuppressWarnings("serial")
	private class ExtendedExceptionSensorData extends ExceptionSensorData {

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
		 * The three arg constructor.
		 * 
		 * @param timeStamp
		 *            The timestamp to set.
		 * @param platformIdent
		 *            The platformIdent to set.
		 * @param sensorTypeIdent
		 *            The sensorTypeIdent to set.
		 * @param methodIdent
		 *            The methodIdent to set.
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

	}

}
