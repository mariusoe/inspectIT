package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.formatter.ColorFormatter;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.mutable.MutableDouble;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * The small summary below the SQL invocation overview.
 * 
 * @author Ivan Senic
 * 
 */
public class SqlInvocSummaryTextInputController extends AbstractTextInputController {

	/**
	 * Slowest 80/20 string.
	 */
	private static final String SLOWEST_80_20 = "Slowest 80%/20%:";

	/**
	 * SQLs duration in invocation string.
	 */
	private static final String SQLS_DURATION_IN_INVOCATION = "SQLs duration in invocation:";

	/**
	 * Total duration string.
	 */
	private static final String TOTAL_DURATION = "Total duration:";

	/**
	 * Total SQLs string.
	 */
	private static final String TOTAL_SQLS = "Total SQLs:";

	/**
	 * System {@link SWT#COLOR_DARK_GREEN} color.
	 */
	private static final RGB GREEN_RGB;

	/**
	 * System {@link SWT#COLOR_DARK_YELLOW} color.
	 */
	private static final RGB YELLOW_RGB;

	/**
	 * System {@link SWT#COLOR_RED} color.
	 */
	private static final RGB RED_RGB;

	/**
	 * Local resource manager for color creation.
	 */
	private ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	/**
	 * Main composite.
	 */
	private Composite main;

	/**
	 * Total count SQL field.
	 */
	private StyledText totalSql;

	/**
	 * Total SQL duration field.
	 */
	private StyledText totalDuration;

	/**
	 * Percentage in invocation.
	 */
	private StyledText percentageOfDuration;

	/**
	 * Slowest 80%/20% count.
	 */
	private StyledText slowestCount;

	/**
	 * Style Range for total SQLs.
	 */
	private StyleRange totalSqlStyle;

	/**
	 * Style Range for total duration.
	 */
	private StyleRange totalDurationStyle;

	/**
	 * Style Range for percentage.
	 */
	private StyleRange percentageOfDurationStyle;

	/**
	 * Style Range for 80/15/5.
	 */
	private StyleRange slowestCountStyle;

	static {
		Display display = Display.getDefault();
		GREEN_RGB = display.getSystemColor(SWT.COLOR_DARK_GREEN).getRGB();
		YELLOW_RGB = display.getSystemColor(SWT.COLOR_DARK_YELLOW).getRGB();
		RED_RGB = display.getSystemColor(SWT.COLOR_RED).getRGB();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		main = toolkit.createComposite(parent, SWT.BORDER);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout gl = new GridLayout(8, false);
		main.setLayout(gl);

		toolkit.createLabel(main, null).setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE));

		totalSql = new StyledText(main, SWT.READ_ONLY | SWT.WRAP);
		totalSql.setToolTipText("Total amount of SQL Statements executed in the invocation");
		totalSql.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(main, null).setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_LAST_HOUR));

		totalDuration = new StyledText(main, SWT.READ_ONLY | SWT.WRAP);
		totalDuration.setToolTipText("Duration sum of all SQL Statements executed in the invocation");
		totalDuration.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(main, null).setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INVOCATION));

		percentageOfDuration = new StyledText(main, SWT.READ_ONLY | SWT.WRAP);
		percentageOfDuration.setToolTipText("Percentage of the time spent in the invocation on SQL Statements execution");
		percentageOfDuration.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(main, null).setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_LIGHT_BULB_ON));

		slowestCount = new StyledText(main, SWT.READ_ONLY | SWT.WRAP);
		slowestCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		slowestCount.setToolTipText("Amount of slowest SQL Statements that take 80%/20% time of total SQL execution duration");

		// remove left and right margins from the parent
		Layout parentLayout = parent.getLayout();
		if (parentLayout instanceof GridLayout) {
			((GridLayout) parentLayout).marginWidth = 0;
			((GridLayout) parentLayout).marginHeight = 0;
		}

		setDefaultText();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setDataInput(List<? extends DefaultData> data) {
		if (CollectionUtils.isNotEmpty(data)) {
			DefaultData defaultData = data.get(0);
			if (defaultData instanceof InvocationSequenceData) {
				updateRepresentation((List<InvocationSequenceData>) data);
			}
		} else {
			setDefaultText();
		}
	}

	/**
	 * Updates the representation of the text form.
	 * 
	 * @param invocations
	 *            Invocations to display.
	 */
	private void updateRepresentation(List<InvocationSequenceData> invocations) {
		MutableDouble duration = new MutableDouble(0d);
		List<SqlStatementData> sqlList = new ArrayList<SqlStatementData>();
		processInputList(invocations, sqlList, duration);
		double totalInvocationsDuration = 0d;
		for (InvocationSequenceData inv : invocations) {
			totalInvocationsDuration += inv.getDuration();
		}
		double percentage = duration.toDouble() / totalInvocationsDuration * 100;
		int slowest80 = getSlowestSqlCount(duration.toDouble(), sqlList, 0.8d);

		totalSql.setText(TOTAL_SQLS + " " + sqlList.size());
		totalSql.setStyleRange(totalSqlStyle);

		totalDuration.setText(TOTAL_DURATION + " " + NumberFormatter.formatDouble(duration.doubleValue()) + " ms");
		totalDuration.setStyleRange(totalDurationStyle);

		String formatedPercentage = NumberFormatter.formatDouble(percentage, 1);
		percentageOfDuration.setText(SQLS_DURATION_IN_INVOCATION + " " + formatedPercentage + "%");
		percentageOfDuration.setStyleRange(percentageOfDurationStyle);
		if (CollectionUtils.isNotEmpty(sqlList)) {
			StyleRange colorPercentageStyle = new StyleRange();
			colorPercentageStyle.start = SQLS_DURATION_IN_INVOCATION.length() + 1;
			colorPercentageStyle.length = formatedPercentage.length() + 1;
			colorPercentageStyle.foreground = ColorFormatter.getPerformanceColor(GREEN_RGB, YELLOW_RGB, RED_RGB, percentage, 20d, 80d, resourceManager);
			percentageOfDuration.setStyleRange(colorPercentageStyle);
		}

		String slowestString = getCountAndPercentage(slowest80, sqlList.size()) + " / " + getCountAndPercentage(sqlList.size() - slowest80, sqlList.size());
		slowestCount.setText(SLOWEST_80_20 + " " + slowestString);
		slowestCount.setStyleRange(slowestCountStyle);

		if (CollectionUtils.isNotEmpty(sqlList)) {
			StyleRange color8020Style = new StyleRange();
			color8020Style.start = SLOWEST_80_20.length() + 1;
			color8020Style.length = slowestString.length();
			double slowest80Percentage = (double) slowest80 / sqlList.size() * 100;
			if (Double.isNaN(slowest80Percentage)) {
				slowest80Percentage = 0;
			}
			color8020Style.foreground = ColorFormatter.getPerformanceColor(GREEN_RGB, YELLOW_RGB, RED_RGB, slowest80Percentage, 70d, 10d, resourceManager);
			slowestCount.setStyleRange(color8020Style);
		}

		main.layout();
	}

	/**
	 * Sets default text that has no informations displayed.
	 */
	private void setDefaultText() {
		totalSql.setText(TOTAL_SQLS);
		totalSqlStyle = new StyleRange();
		totalSqlStyle.fontStyle = SWT.BOLD;
		totalSqlStyle.length = TOTAL_SQLS.length();
		totalSql.setStyleRange(totalSqlStyle);

		totalDuration.setText(TOTAL_DURATION);
		totalDurationStyle = new StyleRange();
		totalDurationStyle.fontStyle = SWT.BOLD;
		totalDurationStyle.length = TOTAL_DURATION.length();
		totalDuration.setStyleRange(totalDurationStyle);

		percentageOfDuration.setText(SQLS_DURATION_IN_INVOCATION);
		percentageOfDurationStyle = new StyleRange();
		percentageOfDurationStyle.fontStyle = SWT.BOLD;
		percentageOfDurationStyle.length = SQLS_DURATION_IN_INVOCATION.length();
		percentageOfDuration.setStyleRange(percentageOfDurationStyle);

		slowestCount.setText(SLOWEST_80_20);
		slowestCountStyle = new StyleRange();
		slowestCountStyle.fontStyle = SWT.BOLD;
		slowestCountStyle.length = SLOWEST_80_20.length();
		slowestCount.setStyleRange(slowestCountStyle);
	}

	/**
	 * Returns string representation of count and percentage.
	 * 
	 * @param count
	 *            Count.
	 * @param totalCount
	 *            Total count.
	 * @return {@link String} representation.
	 */
	private String getCountAndPercentage(int count, int totalCount) {
		if (0 == totalCount) {
			return "0(0%)";
		}
		return count + "(" + NumberFormatter.formatDouble((double) count / totalCount * 100, 0) + "%)";
	}

	/**
	 * Calculates how much slowest SQL can fit into the given percentage of total duration.
	 * 
	 * @param totalDuration
	 *            Total duration of all SQLs.
	 * @param sqlStatementDataList
	 *            List of SQL. Note that there is a side effect of list sorting.
	 * @param percentage
	 *            Wanted percentages to be calculated.
	 * @return Return the count of SQL.
	 */
	private int getSlowestSqlCount(double totalDuration, List<SqlStatementData> sqlStatementDataList, double percentage) {
		// sort first
		Collections.sort(sqlStatementDataList, new Comparator<SqlStatementData>() {
			@Override
			public int compare(SqlStatementData o1, SqlStatementData o2) {
				return ObjectUtils.compare(o2.getDuration(), o1.getDuration());
			}
		});

		int result = 0;
		double currentDurationSum = 0;
		for (SqlStatementData sqlStatementData : sqlStatementDataList) {
			if (currentDurationSum / totalDuration < percentage) {
				result++;
			} else {
				break;
			}
			currentDurationSum += sqlStatementData.getDuration();
		}
		return result;
	}

	/**
	 * Returns the raw list, with no aggregation.
	 * 
	 * @param invocationSequenceDataList
	 *            Input as list of invocations
	 * @param sqlStatementDataList
	 *            List where results will be stored. Needed because of reflection. Note that this
	 *            list will be returned as the result.
	 * @param totalDuration
	 *            {@link MutableDouble} where total duration will be stored.
	 */
	private void processInputList(List<InvocationSequenceData> invocationSequenceDataList, List<SqlStatementData> sqlStatementDataList, MutableDouble totalDuration) {
		for (InvocationSequenceData invocationSequenceData : invocationSequenceDataList) {
			if (null != invocationSequenceData.getSqlStatementData()) {
				sqlStatementDataList.add(invocationSequenceData.getSqlStatementData());
				totalDuration.add(invocationSequenceData.getSqlStatementData().getDuration());
			}
			if (CollectionUtils.isNotEmpty(invocationSequenceDataList)) {
				processInputList(invocationSequenceData.getNestedSequences(), sqlStatementDataList, totalDuration);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		resourceManager.dispose();
		super.dispose();
	}

}
