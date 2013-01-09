package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.InvocationSequenceDataHelper;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId.LiveMode;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.service.cache.CachedDataService;
import info.novatec.inspectit.rcp.util.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ContentViewer;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * This input controller displays an overview of {@link InvocationSequenceData} objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InvocOverviewInputController extends AbstractTableInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.invocoverview";

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static enum Column {
		/** The time column. */
		NESTED_DATA("", 40, null),
		/** The time column. */
		TIME("Start Time", 150, InspectITImages.IMG_TIMER),
		/** The method column. */
		METHOD("Method", 550, InspectITImages.IMG_METHOD_PUBLIC),
		/** The duration column. */
		DURATION("Duration (ms)", 100, InspectITImages.IMG_LAST_HOUR),
		/** The count column. */
		COUNT("Child Count", 100, null),
		/** The URI column. */
		URI("URI", 150, null),
		/** The Use case column. */
		USE_CASE("Use case", 100, null);

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
		 *            The name of the image. Names are defined in {@link InspectITImages}.
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
	 * The template object which is send to the server.
	 */
	private InvocationSequenceData template;

	/**
	 * The list of invocation sequence data objects which is displayed.
	 */
	private List<InvocationSequenceData> invocationSequenceData = new ArrayList<InvocationSequenceData>();

	/**
	 * The limit of the result set.
	 */
	private int limit = PreferencesUtils.getIntValue(PreferencesConstants.ITEMS_COUNT_TO_SHOW);

	/**
	 * The used data access service to access the data on the CMR.
	 */
	private IInvocationDataAccessService dataAccessService;

	/**
	 * The cached service is needed because of the ID mappings.
	 */
	private CachedDataService cachedDataService;

	/**
	 * Date to display invocations from.
	 */
	private Date fromDate = null;

	/**
	 * Date to display invocations to.
	 */
	private Date toDate = null;

	/**
	 * Are we in live mode.
	 */
	private boolean autoUpdate = LiveMode.ACTIVE_DEFAULT;

	/**
	 * Empty styled string.
	 */
	private final StyledString emptyStyledString = new StyledString();

	/**
	 * The resource manager is used for the images etc.
	 */
	private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	/**
	 * 
	 * @return Returns list of invocation sequence data that represents a table input.
	 */
	protected List<InvocationSequenceData> getInvocationSequenceData() {
		return invocationSequenceData;
	}

	/**
	 * Returns data access service for retrieving the data from the server.
	 * 
	 * @return Returns data access service.
	 */
	protected IInvocationDataAccessService getDataAccessService() {
		return dataAccessService;
	}

	/**
	 * Returns current view item count limit defined for the view.
	 * 
	 * @return Returns current view item count limit.
	 */
	protected int getLimit() {
		return limit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new InvocationSequenceData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
		template.setMethodIdent(inputDefinition.getIdDefinition().getMethodId());

		dataAccessService = inputDefinition.getRepositoryDefinition().getInvocationDataAccessService();
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
			viewerColumn.getColumn().setWidth(column.width);
			if (null != column.image) {
				viewerColumn.getColumn().setImage(column.image);
			}
			mapTableViewerColumn(column, viewerColumn);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTableInput() {
		// this list will be filled with data
		return invocationSequenceData;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new InvocOverviewContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new InvocOverviewLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public TableViewerComparator<? extends DefaultData> getComparator() {
		InvocOverviewViewerComparator invocOverviewViewerComparator = new InvocOverviewViewerComparator();
		for (Column column : Column.values()) {
			invocOverviewViewerComparator.addColumn(getMappedTableViewerColumn(column).getColumn(), column);
		}

		return invocOverviewViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		if (getInputDefinition().getRepositoryDefinition() instanceof CmrRepositoryDefinition) {
			preferences.add(PreferenceId.CLEAR_BUFFER);
			preferences.add(PreferenceId.LIVEMODE);
		}
		preferences.add(PreferenceId.UPDATE);
		preferences.add(PreferenceId.ITEMCOUNT);
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
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeLine.FROM_DATE_ID)) {
				fromDate = (Date) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeLine.FROM_DATE_ID);
			}
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeLine.TO_DATE_ID)) {
				toDate = (Date) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeLine.TO_DATE_ID);
			}
			break;
		case LIVEMODE:
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.LiveMode.BUTTON_LIVE_ID)) {
				autoUpdate = (Boolean) preferenceEvent.getPreferenceMap().get(PreferenceId.LiveMode.BUTTON_LIVE_ID);
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
	public boolean canOpenInput(List<? extends DefaultData> data) {
		if (data.isEmpty()) {
			return true;
		}
		return false;
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
	public void doRefresh(IProgressMonitor monitor) {
		monitor.beginTask("Updating Invocation Overview", IProgressMonitor.UNKNOWN);
		monitor.subTask("Retrieving the Invocation Overview");
		List<InvocationSequenceData> invocData;

		if (!autoUpdate) {
			if (template.getMethodIdent() != IdDefinition.ID_NOT_USED) {
				invocData = dataAccessService.getInvocationSequenceOverview(template.getPlatformIdent(), template.getMethodIdent(), limit, fromDate, toDate);
			} else {
				invocData = dataAccessService.getInvocationSequenceOverview(template.getPlatformIdent(), limit, fromDate, toDate);
			}
		} else {
			if (template.getMethodIdent() != IdDefinition.ID_NOT_USED) {
				invocData = dataAccessService.getInvocationSequenceOverview(template.getPlatformIdent(), template.getMethodIdent(), limit);
			} else {
				invocData = dataAccessService.getInvocationSequenceOverview(template.getPlatformIdent(), limit);
			}
		}

		// why this? so only update with new data if returned collection is not empty, i would say
		// with every update, if it is empty, then there is nothing to display
		// then i also done need the clearInvocationFlag
		// I changed here, .clear() is now out of if clause

		invocationSequenceData.clear();
		if (!invocData.isEmpty()) {
			monitor.subTask("Displaying the Invocation Overview");
			invocationSequenceData.addAll(invocData);
		}

		monitor.done();
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
						monitor.beginTask("Retrieving Invocation detail data", IProgressMonitor.UNKNOWN);
						InvocationSequenceData invocationSequenceData = (InvocationSequenceData) selection.getFirstElement();
						InvocationSequenceData data = (InvocationSequenceData) dataAccessService.getInvocationSequenceDetail(invocationSequenceData);
						final List<InvocationSequenceData> invocationSequenceDataList = new ArrayList<InvocationSequenceData>();
						invocationSequenceDataList.add(data);
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
								IWorkbenchPage page = window.getActivePage();
								IRootEditor rootEditor = (IRootEditor) page.getActiveEditor();
								rootEditor.setDataInput(invocationSequenceDataList);
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
	 * @author Patrice Bouillet
	 * 
	 */
	private final class InvocOverviewLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StyledString getStyledText(Object element, int index) {
			InvocationSequenceData data = (InvocationSequenceData) element;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, methodIdent, enumId);
		}

		/**
		 * 
		 * {@inheritDoc}
		 */
		@Override
		protected Image getColumnImage(Object element, int index) {
			InvocationSequenceData data = (InvocationSequenceData) element;
			Column enumId = Column.fromOrd(index);

			switch (enumId) {
			case NESTED_DATA:
				if (InvocationSequenceDataHelper.hasNestedSqlStatements(data) && InvocationSequenceDataHelper.hasNestedExceptions(data)) {
					return ImageFormatter.getCombinedImage(resourceManager, SWT.HORIZONTAL, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_DATABASE), InspectIT.getDefault()
							.getImageDescriptor(InspectITImages.IMG_EXCEPTION_SENSOR));
				} else if (InvocationSequenceDataHelper.hasNestedSqlStatements(data)) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE);
				} else if (InvocationSequenceDataHelper.hasNestedExceptions(data)) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_EXCEPTION_SENSOR);
				} else {
					return super.getColumnImage(element, index);
				}
			default:
				return super.getColumnImage(element, index);
			}

		}
	}

	/**
	 * The content provider for this view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static final class InvocOverviewContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<InvocationSequenceData> invocationSequenceData = (List<InvocationSequenceData>) inputElement;
			return invocationSequenceData.toArray();
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
	 * Viewer Comparator used by this input controller to display the contents of
	 * {@link BasicSQLData}.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static final class InvocOverviewViewerComparator extends TableViewerComparator<InvocationSequenceData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int compareElements(Viewer viewer, InvocationSequenceData invoc1, InvocationSequenceData invoc2) {
			switch ((Column) getEnumSortColumn()) {
			case NESTED_DATA:
				int invNested1 = 0;
				if (InvocationSequenceDataHelper.hasNestedSqlStatements(invoc1)) {
					invNested1 += 2;
				}
				if (InvocationSequenceDataHelper.hasNestedExceptions(invoc1)) {
					invNested1++;
				}
				int invNested2 = 0;
				if (InvocationSequenceDataHelper.hasNestedSqlStatements(invoc2)) {
					invNested2 += 2;
				}
				if (InvocationSequenceDataHelper.hasNestedExceptions(invoc2)) {
					invNested2++;
				}
				return invNested1 - invNested2;
			case TIME:
				return invoc1.getTimeStamp().compareTo(invoc2.getTimeStamp());
			case METHOD:
				IBaseLabelProvider baseLabelProvider = ((ContentViewer) viewer).getLabelProvider();
				InvocOverviewLabelProvider invocLabelProvider = (InvocOverviewLabelProvider) baseLabelProvider;
				String text1 = invocLabelProvider.getStyledText(invoc1, Column.METHOD.ordinal()).getString();
				String text2 = invocLabelProvider.getStyledText(invoc2, Column.METHOD.ordinal()).getString();
				return text1.compareTo(text2);
			case DURATION:
				if (InvocationSequenceDataHelper.hasTimerData(invoc1) && InvocationSequenceDataHelper.hasTimerData(invoc2)) {
					return Double.compare(invoc1.getTimerData().getDuration(), invoc2.getTimerData().getDuration());
				} else {
					return Double.compare(invoc1.getDuration(), invoc2.getDuration());
				}
			case COUNT:
				return Long.valueOf(invoc1.getChildCount()).compareTo(Long.valueOf(invoc2.getChildCount()));
			case URI:
				if (isHttpDataBounded(invoc1) && isHttpDataBounded(invoc2)) {
					String uri1 = ((HttpTimerData) invoc1.getTimerData()).getUri();
					String uri2 = ((HttpTimerData) invoc2.getTimerData()).getUri();
					return ObjectUtils.compare(uri1, uri2);
				} else if (isHttpDataBounded(invoc1)) {
					return 1;
				} else if (isHttpDataBounded(invoc2)) {
					return -1;
				} else {
					return 0;
				}
			case USE_CASE:
				if (isHttpDataBounded(invoc1) && isHttpDataBounded(invoc2)) {
					String useCase1 = ((HttpTimerData) invoc1.getTimerData()).getInspectItTaggingHeaderValue();
					String useCase2 = ((HttpTimerData) invoc2.getTimerData()).getInspectItTaggingHeaderValue();
					return ObjectUtils.compare(useCase1, useCase2);
				} else if (isHttpDataBounded(invoc1)) {
					return 1;
				} else if (isHttpDataBounded(invoc2)) {
					return -1;
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
	 * @param methodIdent
	 *            The method ident object.
	 * @param enumId
	 *            The enumeration ID.
	 * @return The styled string containing the information from the data object.
	 */
	private StyledString getStyledTextForColumn(InvocationSequenceData data, MethodIdent methodIdent, Column enumId) {
		switch (enumId) {
		case NESTED_DATA:
			return emptyStyledString;
		case TIME:
			return new StyledString(NumberFormatter.formatTimeWithMillis(data.getTimeStamp()));
		case METHOD:
			return TextFormatter.getStyledMethodString(methodIdent);
		case DURATION:
			if (InvocationSequenceDataHelper.hasTimerData(data)) {
				return new StyledString(NumberFormatter.formatDouble(data.getTimerData().getDuration()));
			} else {
				// this duration is always available but could differ from
				// the timer data duration as these measures are taken
				// separately.
				return new StyledString(NumberFormatter.formatDouble(data.getDuration()));
			}
		case COUNT:
			return new StyledString(NumberFormatter.formatLong(data.getChildCount()));
		case URI:
			if (isHttpDataBounded(data)) {
				String uri = ((HttpTimerData) data.getTimerData()).getUri();
				if (null != uri) {
					return new StyledString(uri);
				} else {
					return emptyStyledString;
				}
			} else {
				return emptyStyledString;
			}
		case USE_CASE:
			if (isHttpDataBounded(data)) {
				String useCase = ((HttpTimerData) data.getTimerData()).getInspectItTaggingHeaderValue();
				if (null != useCase) {
					return new StyledString(useCase);
				} else {
					return emptyStyledString;
				}
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
		if (object instanceof InvocationSequenceData) {
			InvocationSequenceData data = (InvocationSequenceData) object;
			StringBuilder sb = new StringBuilder();
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
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
	public List<String> getColumnValues(Object object) {
		if (object instanceof InvocationSequenceData) {
			InvocationSequenceData data = (InvocationSequenceData) object;
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
	 * Returns if the given invocation sequence has {@link HttpTimerData} bounded.
	 * 
	 * @param invocationSequenceData
	 *            Invocation to check.
	 * @return True if {@link HttpTimerData} is available.
	 */
	private static boolean isHttpDataBounded(InvocationSequenceData invocationSequenceData) {
		return invocationSequenceData.getTimerData() instanceof HttpTimerData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		resourceManager.dispose();
	}
}
