package info.novatec.inspectit.rcp.formatter;

import info.novatec.inspectit.cmr.model.MethodIdent;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StyledString;

/**
 * This class provides some static methods to create some common {@link String}
 * and {@link StyledString} objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public final class TextFormatter {

	/**
	 * Private constructor. Prevent instantiation.
	 */
	private TextFormatter() {
	}

	/**
	 * Returns a Styled String out of the {@link MethodIdent} objects which
	 * looks like: 'name'('parameter') - 'package'.'class'. Additionally, as
	 * this returns a {@link StyledString}, the last part is colored.
	 * 
	 * @param methodIdent
	 *            The object which contains the information to create the styled
	 *            method string.
	 * @return The created styled method string.
	 */
	public static StyledString getStyledMethodString(MethodIdent methodIdent) {
		StyledString styledString = new StyledString();

		styledString.append(getMethodWithParameters(methodIdent));

		String decoration = MessageFormat.format("- {0}.{1}", new Object[] { methodIdent.getPackageName(), methodIdent.getClassName() });
		styledString.append(decoration, StyledString.QUALIFIER_STYLER);

		return styledString;
	}

	/**
	 * Returns a method string which is appended by the parameters.
	 * 
	 * @param methodIdent
	 *            The object which contains the information to create the styled
	 *            method string.
	 * @return The created method + parameters string.
	 */
	@SuppressWarnings("unchecked")
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
	 * Returns a String out of the {@link MethodIdent} objects which looks like:
	 * 'name'('parameter') - 'package'.'class'.
	 * 
	 * @param methodIdent
	 *            The object which contains the information to create the method
	 *            string.
	 * @return The created method string.
	 */
	public static String getMethodString(MethodIdent methodIdent) {
		return getStyledMethodString(methodIdent).getString();
	}

}
