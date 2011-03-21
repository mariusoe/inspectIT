package info.novatec.inspectit.rcp.formatter;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageData.StorageState;
import info.novatec.inspectit.storage.WritingStatus;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.BooleanStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractCustomStorageLabelType;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.storage.label.type.impl.AssigneeLabelType;
import info.novatec.inspectit.storage.label.type.impl.CreationDateLabelType;
import info.novatec.inspectit.storage.label.type.impl.ExploredByLabelType;
import info.novatec.inspectit.storage.label.type.impl.RatingLabelType;
import info.novatec.inspectit.storage.label.type.impl.StatusLabelType;
import info.novatec.inspectit.storage.label.type.impl.UseCaseLabelType;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;

/**
 * This class provides some static methods to create some common {@link String} and
 * {@link StyledString} objects.
 * 
 * @author Patrice Bouillet
 * @author Stefan Siegl
 */
public final class TextFormatter {

	/** Logical Name for the font used for the error marker. */
	public static final String FONT_ERROR_MARKER = "de.inspectit.font.errormarker";

	/**
	 * Default size of the font used in the error marker. This will be used if the size of default
	 * system font can not be read.
	 */
	public static final int DEFAULT_FONT_ERROR_SIZE = 10;

	static {
		FontData[] fontData = JFaceResources.getDefaultFontDescriptor().getFontData();
		if (fontData.length > 0) {
			FontData defaultFontData = fontData[0];
			int height = (int) defaultFontData.height;
			JFaceResources.getFontRegistry().put(FONT_ERROR_MARKER, new FontData[] { new FontData("Arial", height, SWT.BOLD | SWT.ITALIC) });
		} else {
			JFaceResources.getFontRegistry().put(FONT_ERROR_MARKER, new FontData[] { new FontData("Arial", DEFAULT_FONT_ERROR_SIZE, SWT.BOLD | SWT.ITALIC) });
		}
	}

	/**
	 * Private constructor. Prevent instantiation.
	 */
	private TextFormatter() {
	}

	/**
	 * Returns a Styled String out of the {@link MethodIdent} objects which looks like:
	 * 'name'('parameter') - 'package'.'class'. Additionally, as this returns a {@link StyledString}
	 * , the last part is colored.
	 * 
	 * @param methodIdent
	 *            The object which contains the information to create the styled method string.
	 * @return The created styled method string.
	 */
	public static StyledString getStyledMethodString(MethodIdent methodIdent) {
		StyledString styledString = new StyledString();

		styledString.append(getMethodWithParameters(methodIdent));
		String decoration;
		if (methodIdent.getPackageName() != null && !methodIdent.getPackageName().equals("")) {
			decoration = MessageFormat.format("- {0}.{1}", new Object[] { methodIdent.getPackageName(), methodIdent.getClassName() });
		} else {
			decoration = MessageFormat.format("- {0}", new Object[] { methodIdent.getClassName() });
		}

		styledString.append(decoration, StyledString.QUALIFIER_STYLER);

		return styledString;
	}

	/**
	 * Returns a method string which is appended by the parameters.
	 * 
	 * @param methodIdent
	 *            The object which contains the information to create the styled method string.
	 * @return The created method + parameters string.
	 */
	public static String getMethodWithParameters(MethodIdent methodIdent) {
		StringBuilder builder = new StringBuilder();
		String parameterText = "";
		if (null != methodIdent.getParameters()) {
			List<String> parameterList = new ArrayList<String>();
			for (String parameter : (List<String>) methodIdent.getParameters()) {
				String[] split = parameter.split("\\.");
				parameterList.add(split[split.length - 1]);
			}

			parameterText = parameterList.toString();
			parameterText = parameterText.substring(1, parameterText.length() - 1);
		}

		builder.append(methodIdent.getMethodName());
		builder.append("(");
		builder.append(parameterText);
		builder.append(") ");

		return builder.toString();
	}

	/**
	 * Returns a String out of the {@link MethodIdent} objects which looks like: 'name'('parameter')
	 * - 'package'.'class'.
	 * 
	 * @param methodIdent
	 *            The object which contains the information to create the method string.
	 * @return The created method string.
	 */
	public static String getMethodString(MethodIdent methodIdent) {
		return getStyledMethodString(methodIdent).getString();
	}

	/**
	 * Returns styled string for invocation affilliation percentage.
	 * 
	 * @param percentage
	 *            Percentage.
	 * @param invocationsNumber
	 *            the number of invocation in total
	 * @return Styled string.
	 */
	public static StyledString getInvocationAffilliationPercentageString(int percentage, int invocationsNumber) {
		StyledString styledString = new StyledString();

		styledString.append(String.valueOf(percentage), StyledString.QUALIFIER_STYLER);
		styledString.append("% (in ", StyledString.QUALIFIER_STYLER);
		styledString.append(String.valueOf(invocationsNumber), StyledString.QUALIFIER_STYLER);
		styledString.append(" inv)", StyledString.QUALIFIER_STYLER);
		return styledString;
	}

	/**
	 * Creates a <code>StyledString</code> containing a warning.
	 * 
	 * @return a <code>StyledString</code> containing a warning.
	 */
	public static StyledString getWarningSign() {
		return new StyledString(" !", new Styler() {

			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.foreground = JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR);
				textStyle.font = JFaceResources.getFont(TextFormatter.FONT_ERROR_MARKER);
			}
		});
	}

	/**
	 * Get the textual representation of objects that will be displayed in the new view.
	 * 
	 * @param invAwareData
	 *            Invocation aware object to get representation for.
	 * @param repositoryDefinition
	 *            Repository definition. Needed for the method name retrival.
	 * @return String.
	 */
	public static String getInvocationAwareDataTextualRepresentation(InvocationAwareData invAwareData, RepositoryDefinition repositoryDefinition) {
		if (invAwareData instanceof SqlStatementData) {
			SqlStatementData sqlData = (SqlStatementData) invAwareData;
			return "SQL: " + sqlData.getSql();
		} else if (invAwareData instanceof HttpTimerData) {
			HttpTimerData timerData = (HttpTimerData) invAwareData;
			// Print either URI or Usecase (tagged value) depending on the situation (which is
			// filled, that is)
			if (!HttpTimerData.UNDEFINED.equals(timerData.getUri())) {
				return "URI: " + timerData.getUri();
			} else {
				return "Usecase: " + timerData.getInspectItTaggingHeaderValue();
			}
		} else if (invAwareData instanceof ExceptionSensorData) {
			ExceptionSensorData exData = (ExceptionSensorData) invAwareData;
			return "Exception: " + exData.getThrowableType();
		} else if (invAwareData instanceof TimerData) {
			TimerData timerData = (TimerData) invAwareData;
			MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(timerData.getMethodIdent());
			return TextFormatter.getMethodString(methodIdent);
		}
		return "";
	}

	/**
	 * Returns the styled string for the storage data and its CMR repository definition.
	 * 
	 * @param storageData
	 *            {@link StorageData}.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @return Styled string for nicer representation.
	 */
	public static StyledString getStyledStorageDataString(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition) {
		StyledString styledString = new StyledString();
		styledString.append(storageData.getName());
		styledString.append(" ");
		styledString.append("[" + cmrRepositoryDefinition.getName() + "]", StyledString.QUALIFIER_STYLER);
		styledString.append(" - ");
		styledString.append(getStorageStateTextualRepresentation(storageData.getState()), StyledString.DECORATIONS_STYLER);
		styledString.append(", " + NumberFormatter.formatBytesToMBytes(storageData.getDiskSize()), StyledString.DECORATIONS_STYLER);
		return styledString;
	}

	/**
	 * @param storageState
	 *            Storage state.
	 * @return Returns the textual representation of the storage state.
	 */
	public static String getStorageStateTextualRepresentation(StorageState storageState) {
		if (storageState == StorageState.CREATED_NOT_OPENED) {
			return "Created";
		} else if (storageState == StorageState.OPENED) {
			return "Writable";
		} else if (storageState == StorageState.CLOSED) {
			return "Readable";
		} else if (storageState == StorageState.RECORDING) {
			return "Recording";
		}
		return "UNKNOWN STATE";
	}

	/**
	 * Returns the name of the label, based on it's type. If label is <code>null</code>, string
	 * "null" will be returned.
	 * 
	 * @param label
	 *            Label to get name for.
	 * @return Returns the name of the label, based on it's class.
	 */
	public static String getLabelName(AbstractStorageLabel<?> label) {
		if (null == label) {
			return "null";
		} else {
			return getLabelName(label.getStorageLabelType());
		}
	}

	/**
	 * Returns the name of the label type. If label type is <code>null</code>, string "null" will be
	 * returned.
	 * 
	 * @param labelType
	 *            Label type to get name for.
	 * @return Returns the name of the label, based on it's class.
	 */
	public static String getLabelName(AbstractStorageLabelType<?> labelType) {
		if (null == labelType) {
			return "null";
		} else if (AssigneeLabelType.class.equals(labelType.getClass())) {
			return "Assignee";
		} else if (CreationDateLabelType.class.equals(labelType.getClass())) {
			return "Creation Date";
		} else if (ExploredByLabelType.class.equals(labelType.getClass())) {
			return "Explored By";
		} else if (RatingLabelType.class.equals(labelType.getClass())) {
			return "Rating";
		} else if (StatusLabelType.class.equals(labelType.getClass())) {
			return "Status";
		} else if (UseCaseLabelType.class.equals(labelType.getClass())) {
			return "Use Case";
		} else if (AbstractCustomStorageLabelType.class.isAssignableFrom(labelType.getClass())) {
			return ((AbstractCustomStorageLabelType<?>) labelType).getName();
		} else {
			return "Unknown Label";
		}
	}

	/**
	 * Returns the class type of the label type.
	 * 
	 * @param labelType
	 *            Label type to get name for.
	 * @return Returns the class type of the label type.
	 */
	public static String getLabelValueType(AbstractStorageLabelType<?> labelType) {
		if (null == labelType) {
			return "null";
		} else if (Boolean.class.equals(labelType.getValueClass())) {
			return "Yes/No";
		} else if (Date.class.equals(labelType.getValueClass())) {
			return "Date";
		} else if (Number.class.equals(labelType.getValueClass())) {
			return "Number";
		} else if (String.class.equals(labelType.getValueClass())) {
			return "Text";
		} else {
			return "Unknown Label Type";
		}
	}

	/**
	 * Returns the name of the label, based on it's class. If label is <code>null</code>, string
	 * "null" will be returned.
	 * 
	 * @param label
	 *            Label to get name for.
	 * @param grouped
	 *            TODO
	 * @return Returns the name of the label, based on it's class.
	 */
	public static String getLabelValue(AbstractStorageLabel<?> label, boolean grouped) {
		if (null == label) {
			return "null";
		} else if (CreationDateLabelType.class.equals(label.getStorageLabelType().getClass())) {
			Date date = (Date) ((AbstractStorageLabel<?>) label).getValue();
			if (grouped) {
				return DateFormat.getDateInstance().format(date);
			} else {
				return DateFormat.getDateTimeInstance().format(date);
			}
		} else if (BooleanStorageLabel.class.equals(label.getClass())) {
			BooleanStorageLabel booleanStorageLabel = (BooleanStorageLabel) label;
			if (booleanStorageLabel.getValue().booleanValue()) {
				return "Yes";
			} else {
				return "No";
			}
		} else {
			return label.getFormatedValue();
		}
	}

	/**
	 * Returns text representation for the {@link WritingStatus} or empty string if status is
	 * <code>null</code>.
	 * 
	 * @param recordingWritingStatus
	 *            Status of writing.
	 * @return String that represent the status.
	 */
	public static String getWritingStatusText(WritingStatus recordingWritingStatus) {
		if (null == recordingWritingStatus) {
			return "";
		}
		switch (recordingWritingStatus) {
		case GOOD:
			return "[GREEN] There are no problems.";
		case MEDIUM:
			return "[YELLOW] Amount of data tried to be recorded is slightly higer than what CMR can support. However, no data loss should be expected.";
		case BAD:
			return "[RED] Amount of data tried to be recorded is too high for CMR to manage. Data loss should be expected.";
		default:
			return "";
		}
	}

}
