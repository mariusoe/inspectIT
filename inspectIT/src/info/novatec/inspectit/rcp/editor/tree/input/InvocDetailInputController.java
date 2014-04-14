package info.novatec.inspectit.rcp.editor.tree.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodIdentToSensorType;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.InvocationSequenceDataHelper;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.ExceptionImageFactory;
import info.novatec.inspectit.rcp.model.ModifiersImageFactory;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.hibernate.jdbc.util.FormatStyle;
import org.hibernate.jdbc.util.Formatter;

/**
 * This input controller displays the detail contents of {@link InvocationSequenceData} objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InvocDetailInputController extends AbstractTreeInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.tree.invocdetail";

	/**
	 * The total duration of the displayed invocation.
	 */
	private double invocationDuration = 0.0d;

	/**
	 * The resource manager is used for the images etc.
	 */
	private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	/**
	 * The value of the selected sensor types.
	 */
	private Set<SensorTypeEnum> selectedSensorTypes = PreferencesUtils.getObject(PreferencesConstants.INVOCATION_FILTER_SENSOR_TYPES);

	/**
	 * The value for the exclusive time filter.
	 */
	private double defaultExclusiveFilterTime = PreferencesUtils.getDoubleValue(PreferencesConstants.INVOCATION_FILTER_EXCLUSIVE_TIME);

	/**
	 * The value for the total time filter.
	 */
	private double defaultTotalFilterTime = PreferencesUtils.getDoubleValue(PreferencesConstants.INVOCATION_FILTER_TOTAL_TIME);

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static enum Column {
		/** The method column. */
		METHOD("Method", 700, InspectITImages.IMG_CALL_HIERARCHY),
		/** The duration column. */
		DURATION("Duration (ms)", 100, InspectITImages.IMG_LAST_HOUR),
		/** The exclusive duration column. */
		EXCLUSIVE("Exc. duration (ms)", 100, null),
		/** The cpu duration column. */
		CPUDURATION("Cpu Duration (ms)", 100, null),
		/** The time-stamp column. **/
		START_DELTA("Start Delta (ms)", 100, InspectITImages.IMG_TIMER),
		/** The count column. */
		SQL("SQL", 300, InspectITImages.IMG_DATABASE),
		/** The parameter/field contents. */
		PARAMETER("Parameter Content", 200, null);

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
	 * The cached service is needed because of the ID mappings.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * Current input of the tree.
	 */
	private Object input;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);
		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createColumns(TreeViewer treeViewer) {
		for (Column column : Column.values()) {
			TreeViewerColumn viewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText(column.name);
			viewerColumn.getColumn().setWidth(column.width);
			if (null != column.image) {
				viewerColumn.getColumn().setImage(column.image);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTreeInput() {
		return input;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new InvocDetailContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new InvocDetailLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		preferences.add(PreferenceId.FILTERSENSORTYPE);
		preferences.add(PreferenceId.INVOCFILTEREXCLUSIVETIME);
		preferences.add(PreferenceId.INVOCFILTERTOTALTIME);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		switch (preferenceEvent.getPreferenceId()) {
		case FILTERSENSORTYPE:
			SensorTypeEnum sensorType = (SensorTypeEnum) preferenceEvent.getPreferenceMap().get(PreferenceId.SensorTypeSelection.SENSOR_TYPE_SELECTION_ID);
			// add or remove the sensor type from the selected set
			if (selectedSensorTypes.contains(sensorType)) {
				selectedSensorTypes.remove(sensorType);
			} else {
				selectedSensorTypes.add(sensorType);
			}
			break;
		case INVOCFILTEREXCLUSIVETIME:
			defaultExclusiveFilterTime = (Double) preferenceEvent.getPreferenceMap().get(PreferenceId.InvocExclusiveTimeSelection.TIME_SELECTION_ID);
			break;
		case INVOCFILTERTOTALTIME:
			defaultTotalFilterTime = (Double) preferenceEvent.getPreferenceMap().get(PreferenceId.InvocTotalTimeSelection.TIME_SELECTION_ID);
			break;
		default:
			// nothing to do by default
			break;
		}
	}

	/**
	 * Sorts a given collection. As the JDK Collections class only provides to sort for
	 * <code>List</code> classes we initially create a list based on the given collection, sort this
	 * list and return it.
	 * 
	 * @param <T>
	 *            The type
	 * @param c
	 *            The collection to be sorted
	 * @return a sorted <code>ArrayList</code> of the elements contained in the given collection.
	 */
	private <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}

	/**
	 * {@inheritDoc}.
	 * 
	 * @see AbstractTreeInputController#showDetails(Shell, Object)
	 */
	public void showDetails(Shell parent, Object element) {
		final InvocationSequenceData data = (InvocationSequenceData) element;
		final MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());

		int shellStyle = SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE;
		boolean takeFocusOnOpen = true;
		boolean persistSize = true;
		boolean persistLocation = true;
		boolean showDialogMenu = false;
		boolean showPersistActions = true;
		String titleText = TextFormatter.getMethodString(methodIdent);
		String infoText = "Invocation Sequence Details";

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

				if (InvocationSequenceDataHelper.hasTimerData(data)) {
					if (InvocationSequenceDataHelper.hasHttpTimerData(data)) {
						HttpTimerData httpData = (HttpTimerData) data.getTimerData();
						content += "\n";
						content += "URI: " + httpData.getUri() + "\n";
						content += "Request-method: " + httpData.getRequestMethod() + "\n";

						// -- Parameters
						content += "Parameters:";
						Map<String, String[]> paramMap = httpData.getParameters();
						if (null == paramMap) {
							content += " <none>\n";
						} else {
							content += "\n";
							List<String> paramKeys = asSortedList(paramMap.keySet());
							for (String string : paramKeys) {
								String[] value = paramMap.get(string);
								String paramValueDisplay = "";
								for (int i = 0; i < value.length; i++) {
									paramValueDisplay += "[" + value[i] + "]";
									if (i + 1 < value.length) {
										paramValueDisplay += ", ";
									}
								}
								content += "  " + string + ": " + paramValueDisplay + "\n";
							}
						}

						// -- Attributes
						content += "Attributes:";
						Map<String, String> attrMap = httpData.getAttributes();
						if (null == attrMap) {
							content += " <none>\n";
						} else {
							content += "\n";
							List<String> attrKeys = asSortedList(attrMap.keySet());
							for (String string : attrKeys) {
								content += "  " + string + ": " + attrMap.get(string) + "\n";
							}
						}

						// -- Headers
						content += "Headers:";
						Map<String, String> headerMap = httpData.getHeaders();
						if (null == headerMap) {
							content += " <none>\n";
						} else {
							content += "\n";
							List<String> headerKeys = asSortedList(headerMap.keySet());
							for (String string : headerKeys) {
								content += "  " + string + ": " + headerMap.get(string) + "\n";
							}
						}

						// -- Session Attributes
						content += "Session Attributes:";
						Map<String, String> sessionAttributeMap = httpData.getSessionAttributes();
						if (null == sessionAttributeMap) {
							content += " <none>\n";
						} else {
							content += "\n";
							List<String> sessionAttKeys = asSortedList(sessionAttributeMap.keySet());
							for (String string : sessionAttKeys) {
								content += "  " + string + ": " + sessionAttributeMap.get(string) + "\n";
							}
						}

						content += "\n";
					}

					// add data for the "normal" timer data
					TimerData timer = data.getTimerData();
					content += "\n";
					content += "Method duration: " + timer.getDuration() + "\n";
				} else if (InvocationSequenceDataHelper.isRootElementInSequence(data)) {
					content += "\n";
					content += "Invocation duration: " + data.getDuration() + "\n";
				}

				if (InvocationSequenceDataHelper.hasSQLData(data)) {
					SqlStatementData sql = data.getSqlStatementData();
					Formatter sqlFormatter = FormatStyle.BASIC.getFormatter();
					content += "\n";
					if (sql != null) {
						content += "SQL: " + sqlFormatter.format(sql.getSqlWithParameterValues()) + "\n";
						content += "Database URL: " + sql.getDatabaseUrlView() + "\n";
						content += "Database Product: " + sql.getDatabaseProductNameView() + "\n";
						content += "Database Version: " + sql.getDatabaseProductVersionView() + "\n";
					} else {
						content += "SQL: \n";
						content += "Database URL: \n";
						content += "Database Product: \n";
						content += "Database Version: \n";
					}
				}

				// Integrate parameter information from both invocation sequence and timer data
				if (InvocationSequenceDataHelper.hasCapturedParameters(data)) {
					content += "\n";
					content += "Captured parameters:\n";
					Set<ParameterContentData> parameterContents = InvocationSequenceDataHelper.getCapturedParameters(data);

					for (ParameterContentData parameterContentData : parameterContents) {
						switch (parameterContentData.getContentType()) {
						case FIELD:
							content += "Field '" + parameterContentData.getName() + "': " + parameterContentData.getContent() + "\n";
							break;
						case PARAM:
							content += "Method Parameter #" + parameterContentData.getSignaturePosition() + " '" + parameterContentData.getName() + "': " + parameterContentData.getContent() + "\n";
							break;
						case RETURN:
							content += "Return '" + parameterContentData.getName() + "': " + parameterContentData.getContent() + "\n";
							break;
						default:
							break;
						}
					}
				}

				if (InvocationSequenceDataHelper.hasExceptionData(data)) {
					content += "\n";
					content += "Exception Details:\n";
					for (ExceptionSensorData exceptionSensorData : data.getExceptionSensorDataObjects()) {
						content += exceptionSensorData.getExceptionEvent().toString().toLowerCase() + " " + exceptionSensorData.getThrowableType() + "\n";

						String stackTrace = exceptionSensorData.getStackTrace();
						if (null != stackTrace && !"".equals(stackTrace)) {
							content += "\n";
							content += stackTrace;
						}
					}
				}

				text.setText(content);
			}
		};
		dialog.open();
	}

	/**
	 * {@inheritDoc}
	 */
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

		// we are saving the complete duration of this invocation sequence
		invocationDuration = ((InvocationSequenceData) data.get(0)).getDuration();

		return true;
	}

	/**
	 * The invoc detail label provider for this view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class InvocDetailLabelProvider extends StyledCellIndexLabelProvider {

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
			InvocationSequenceData data = (InvocationSequenceData) element;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, methodIdent, enumId);
		}

		/**
		 * Returns the column image for the given element at the given index.
		 * 
		 * @param element
		 *            The element.
		 * @param index
		 *            The index.
		 * @return Returns the Image.
		 */
		@Override
		public Image getColumnImage(Object element, int index) {
			InvocationSequenceData data = (InvocationSequenceData) element;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			switch (enumId) {
			case METHOD:
				ExceptionSensorData exceptionSensorData = null;
				Image image = ModifiersImageFactory.getImage(methodIdent.getModifiers());

				if (InvocationSequenceDataHelper.hasExceptionData(data)) {
					exceptionSensorData = data.getExceptionSensorDataObjects().get(data.getExceptionSensorDataObjects().size() - 1);
					image = ExceptionImageFactory.decorateImageWithException(image, exceptionSensorData, resourceManager);
				}

				return image;
			case DURATION:
				return null;
			case CPUDURATION:
				return null;
			case EXCLUSIVE:
				return null;
			case SQL:
				return null;
			case PARAMETER:
				return null;
			default:
				return null;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Color getBackground(Object element, int index) {
			InvocationSequenceData data = (InvocationSequenceData) element;
			double duration = InvocationSequenceDataHelper.calculateDuration(data);

			if (-1.0d != duration) { // no duration?
				double exclusiveTime = duration - InvocationSequenceDataHelper.computeNestedDuration(data);

				// compute the correct color
				int colorValue = 255 - (int) ((exclusiveTime / invocationDuration) * 100);

				if (colorValue > 255 || colorValue < 0) {
					InspectIT.getDefault().createErrorDialog("The computation of the color value for the detail view returned an invalid value: " + colorValue, null, -1);
					return null;
				}

				Color color = resourceManager.createColor(new RGB(colorValue, colorValue, colorValue));
				return color;
			}

			return null;
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
	private static StyledString getStyledTextForColumn(InvocationSequenceData data, MethodIdent methodIdent, Column enumId) {
		StyledString styledString = null;
		switch (enumId) {
		case METHOD:
			return TextFormatter.getStyledMethodString(methodIdent);
		case START_DELTA:
			InvocationSequenceData root = data;
			while (!InvocationSequenceDataHelper.isRootElementInSequence(root)) {
				root = root.getParentSequence();
			}
			long delta = data.getTimeStamp().getTime() - root.getTimeStamp().getTime();
			return new StyledString(NumberFormatter.formatLong(delta));
		case DURATION:
			styledString = new StyledString();
			double duration = InvocationSequenceDataHelper.calculateDuration(data);
			if (-1.0d != duration) {
				styledString.append(NumberFormatter.formatDouble(duration));
			}
			return styledString;
		case CPUDURATION:
			styledString = new StyledString();
			if (InvocationSequenceDataHelper.hasTimerData(data) && data.getTimerData().isCpuMetricDataAvailable()) {
				styledString.append(NumberFormatter.formatDouble(data.getTimerData().getCpuDuration()));
			}
			return styledString;
		case EXCLUSIVE:
			styledString = new StyledString();
			double dur = InvocationSequenceDataHelper.calculateDuration(data);

			if (-1.0d != dur) {
				double exclusiveTime = dur - InvocationSequenceDataHelper.computeNestedDuration(data);
				styledString.append(NumberFormatter.formatDouble(exclusiveTime));
			}

			return styledString;
		case SQL:
			styledString = new StyledString();
			if (InvocationSequenceDataHelper.hasSQLData(data)) {
				styledString.append(TextFormatter.clearLineBreaks(data.getSqlStatementData().getSqlWithParameterValues()));
			}
			return styledString;
		case PARAMETER:
			styledString = new StyledString();

			if (InvocationSequenceDataHelper.hasHttpTimerData(data)) {
				HttpTimerData httpTimer = (HttpTimerData) data.getTimerData();
				if (null != httpTimer.getUri()) {
					styledString.append("URI: ");
					styledString.append(httpTimer.getUri());
					styledString.append(" | ");
				}
			}

			if (InvocationSequenceDataHelper.hasCapturedParameters(data)) {
				Set<ParameterContentData> parameters = InvocationSequenceDataHelper.getCapturedParameters(data);

				boolean isFirst = true;
				for (ParameterContentData parameterContentData : parameters) {
					// shorten the representation here.
					if (!isFirst) {
						styledString.append(", ");
					} else {
						isFirst = false;
					}
					styledString.append("'");
					styledString.append(parameterContentData.getName());
					styledString.append("': ");
					styledString.append(TextFormatter.clearLineBreaks(parameterContentData.getContent()));
				}
			}

			return styledString;
		default:
			return styledString;
		}
	}

	/**
	 * The invoc detail content provider for this view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class InvocDetailContentProvider implements ITreeContentProvider {

		/**
		 * The deferred manager is used here to update the tree in a concurrent thread so the UI
		 * responds much better if many items are displayed.
		 */
		private DeferredTreeContentManager manager;

		/**
		 * {@inheritDoc}
		 */
		public Object[] getChildren(Object parent) {
			if (manager.isDeferredAdapter(parent)) {
				Object[] children = manager.getChildren(parent);

				return children;
			}

			return new Object[0];
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getParent(Object child) {
			if (child instanceof InvocationSequenceData) {
				InvocationSequenceData invocationSequenceData = (InvocationSequenceData) child;
				return invocationSequenceData.getParentSequence();
			}

			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasChildren(Object parent) {
			if (parent == null) {
				return false;
			}

			if (parent instanceof InvocationSequenceData) {
				InvocationSequenceData invocationSequenceData = (InvocationSequenceData) parent;
				if (!invocationSequenceData.getNestedSequences().isEmpty()) {
					return true;
				}
			}

			return false;
		}

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
			manager = new DeferredTreeContentManager((AbstractTreeViewer) viewer);
			input = newInput;
		}

		/**
		 * {@inheritDoc}
		 */
		public void dispose() {
		}

	}

	@Override
	public ViewerFilter[] getFilters() {
		ViewerFilter sensorTypeFilter = new InvocationViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof InvocationSequenceData) {
					InvocationSequenceData invocationSequenceData = (InvocationSequenceData) element;
					MethodIdent methodIdent = cachedDataService.getMethodIdentForId(invocationSequenceData.getMethodIdent());
					Set<MethodIdentToSensorType> methodIdentToSensorTypes = methodIdent.getMethodIdentToSensorTypes();
					Set<SensorTypeEnum> sensorTypes = SensorTypeEnum.getAllOf(methodIdentToSensorTypes);
					sensorTypes.retainAll(selectedSensorTypes);
					if (sensorTypes.isEmpty()) {
						return false;
					}
				}
				return true;
			}
		};
		ViewerFilter exclusiveTimeFilter = new InvocationViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (Double.isNaN(defaultExclusiveFilterTime)) {
					return true;
				}

				if (element instanceof InvocationSequenceData) {
					InvocationSequenceData invocationSequenceData = (InvocationSequenceData) element;

					// filter by the exclusive duration
					double duration = Double.NaN;
					if (InvocationSequenceDataHelper.hasSQLData(invocationSequenceData)) {
						duration = invocationSequenceData.getSqlStatementData().getDuration();
					} else if (InvocationSequenceDataHelper.hasTimerData(invocationSequenceData)) {
						double totalDuration = invocationSequenceData.getTimerData().getDuration();
						duration = totalDuration - InvocationSequenceDataHelper.computeNestedDuration(invocationSequenceData);
					} else if (InvocationSequenceDataHelper.isRootElementInSequence(invocationSequenceData)) {
						duration = invocationSequenceData.getDuration() - InvocationSequenceDataHelper.computeNestedDuration(invocationSequenceData);
					}

					if (!Double.isNaN(duration) && duration <= defaultExclusiveFilterTime) {
						return false;
					}
				}
				return true;
			}
		};
		ViewerFilter totalTimeFilter = new InvocationViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (Double.isNaN(defaultTotalFilterTime)) {
					return true;
				}

				if (element instanceof InvocationSequenceData) {
					InvocationSequenceData invocationSequenceData = (InvocationSequenceData) element;

					// filter by the exclusive duration
					double duration = InvocationSequenceDataHelper.calculateDuration(invocationSequenceData);
					if (duration != -1.0d && duration <= defaultTotalFilterTime) {
						return false;
					}
				}
				return true;
			}
		};
		// TODO this filter must be removed in the future! ... edit SSL: why?
		ViewerFilter wrapperFilter = new InvocationViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof InvocationSequenceData) {
					InvocationSequenceData invocationSequenceData = (InvocationSequenceData) element;
					MethodIdent methodIdent = cachedDataService.getMethodIdentForId(invocationSequenceData.getMethodIdent());
					Set<MethodIdentToSensorType> methodIdentToSensorTypes = methodIdent.getMethodIdentToSensorTypes();
					Set<SensorTypeEnum> sensorTypes = SensorTypeEnum.getAllOf(methodIdentToSensorTypes);
					if (sensorTypes.contains(SensorTypeEnum.JDBC_PREPARED_STATEMENT)) {
						if (null == invocationSequenceData.getSqlStatementData() || 0 == invocationSequenceData.getSqlStatementData().getCount()) {
							return false;
						}
					}
				}
				return true;
			}
		};
		return new ViewerFilter[] { sensorTypeFilter, exclusiveTimeFilter, totalTimeFilter, wrapperFilter };
	}

	/**
	 * This class is needed to modify the filter method which behaves a little bit differently than
	 * the original one: Instead of filtering out a specific element _and_ all its sub-elements, it
	 * only filters out the specific elements and pushes up the elements which are child-elements of
	 * that one.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private abstract class InvocationViewerFilter extends ViewerFilter {
		/**
		 * The filtering method which tries to push up the child elements if a parent element has to
		 * be filtered out.
		 * 
		 * @param viewer
		 *            The viewer
		 * @param parent
		 *            The parent object
		 * @param elements
		 *            The elements to check if they should be filtered
		 * @return Returns a set of elements which could be now even more than the initial elements
		 */
		@Override
		public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
			List<Object> out = new ArrayList<Object>();
			for (Object element : elements) {
				if (select(viewer, parent, element)) {
					out.add(element);
				} else {
					// This else branch has to be added to not filter out
					// child elements which would pass the filter.
					if (element instanceof InvocationSequenceData) {
						InvocationSequenceData data = (InvocationSequenceData) element;
						if (data.getChildCount() > 0) {
							// the parent object stays the same as this is the
							// graphical representation and not the underlying model
							out.addAll(Arrays.asList(filter(viewer, parent, data.getNestedSequences().toArray())));
						}
					}
				}
			}
			return out.toArray();
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
				sb.append('\t');
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
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object[] getObjectsToSearch(Object treeInput) {
		List<InvocationSequenceData> invocationSequenceDataList = (List<InvocationSequenceData>) treeInput;
		if (!invocationSequenceDataList.isEmpty()) {
			InvocationSequenceData invocation = invocationSequenceDataList.get(0);
			List<InvocationSequenceData> allObjects = new ArrayList<InvocationSequenceData>((int) invocation.getChildCount());
			extractAllChildren(allObjects, invocation);
			return allObjects.toArray();
		}
		return new Object[0];

	}

	/**
	 * Extracts all invocations inside the invocation in one list via reflection.
	 * 
	 * @param resultList
	 *            List to contain all the extracted data.
	 * @param invocation
	 *            Invocation to extract.
	 */
	private void extractAllChildren(List<InvocationSequenceData> resultList, InvocationSequenceData invocation) {
		resultList.add(invocation);
		for (InvocationSequenceData child : invocation.getNestedSequences()) {
			extractAllChildren(resultList, child);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		resourceManager.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SubViewClassification getSubViewClassification() {
		return SubViewClassification.SLAVE;
	}

}
