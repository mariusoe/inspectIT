package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Simple {@link TextInputController} that display the text of the SQL query with the '?' characters
 * bold.
 * 
 * @author Ivan Senic
 * 
 */
public class SqlStatementTextInputController extends AbstractTextInputController {

	/**
	 * SQL to display.
	 */
	private SqlStatementData dataToDisplay;

	/**
	 * Form text to display the data.
	 */
	private FormText formText;

	/**
	 * Main composite.
	 */
	private Composite main;

	/**
	 * {@link ScrolledComposite} that will hold the main composite.
	 */
	private ScrolledComposite scrollComposite;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		scrollComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollComposite.setBackground(toolkit.getColors().getBackground());

		main = toolkit.createComposite(scrollComposite);
		GridLayout gl = new GridLayout(2, false);
		gl.horizontalSpacing = 10;
		main.setLayout(gl);

		Label img = toolkit.createLabel(main, null, SWT.NONE);
		img.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE));
		img.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, true));

		formText = toolkit.createFormText(main, false);
		formText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

		scrollComposite.setContent(main);
		scrollComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				fitSizeOfScrolledContent();
			}
		});

		// remove margins from the parent
		Layout parentLayout = parent.getLayout();
		if (parentLayout instanceof GridLayout) {
			((GridLayout) parentLayout).marginHeight = 0;
			((GridLayout) parentLayout).marginWidth = 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataInput(List<? extends DefaultData> data) {
		if (null != data) {
			if (!data.isEmpty()) {
				DefaultData defaultData = data.get(0);
				if (defaultData instanceof SqlStatementData) {
					dataToDisplay = (SqlStatementData) defaultData;
				}
			} else {
				dataToDisplay = null;
			}
		} else {
			dataToDisplay = null;
		}
		updateRepresentation();
	}

	/**
	 * Updates the representation of the text form.
	 */
	private void updateRepresentation() {
		if (null != dataToDisplay) {
			String boldSql = StringUtils.replaceEach(dataToDisplay.getSql(), new String[] { "?" }, new String[] { "<b>?</b>" });
			if (CollectionUtils.isNotEmpty(dataToDisplay.getParameterValues())) {
				int index = 0;
				StringBuilder stringBuilder = new StringBuilder(boldSql.length());
				for (int i = 0; i < boldSql.length(); i++) {
					char c = boldSql.charAt(i);
					if ('?' == c) {
						String parameter = dataToDisplay.getParameterValues().get(index);
						if ((null == parameter) || "".equals(parameter.trim())) {
							stringBuilder.append(c);
						} else {
							stringBuilder.append(parameter);
						}
						index = index + 1;
					} else {
						stringBuilder.append(c);
					}
				}
				boldSql = stringBuilder.toString();
			}
			formText.setText("<form><p>" + boldSql + "</p></form>", true, false);
		} else {
			formText.setText("", false, false);
		}
		main.layout();
		fitSizeOfScrolledContent();
	}

	/**
	 * Fits the width of the main composite to the same width scrolled composite was given.
	 */
	private void fitSizeOfScrolledContent() {
		Point p = scrollComposite.getSize();
		main.setSize(main.computeSize(p.x, SWT.DEFAULT));
	}
}
