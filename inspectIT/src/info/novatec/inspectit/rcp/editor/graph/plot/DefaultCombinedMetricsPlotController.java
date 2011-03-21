package info.novatec.inspectit.rcp.editor.graph.plot;

import info.novatec.inspectit.cmr.service.ICombinedMetricsDataAccessService;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.CombinedMetricsInputDefinitionExtra;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.RangeType;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * This class creates a {@link XYPlot} containing the {@link TimerData} informations.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class DefaultCombinedMetricsPlotController extends AbstractPlotController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.graph.combinedmetrics";

	/**
	 * The template of the {@link TimerData} object.
	 */
	private TimerData template;

	/**
	 * Indicates the weight of the upper {@link XYPlot}.
	 */
	private static final int WEIGHT_UPPER_PLOT = 2;

	/**
	 * Indicates the weight of the lower {@link XYPlot}.
	 */
	private static final int WEIGHT_LOWER_PLOT = 1;

	/**
	 * The upper {@link XYPlot}.
	 */
	private XYPlot upperPlot;

	/**
	 * The lower {@link XYPlot}.
	 */
	private XYPlot lowerPlot;

	/**
	 * The map containing the weight of the {@link XYPlot}s.
	 */
	private Map<XYPlot, Integer> weights = new HashMap<XYPlot, Integer>();

	/**
	 * The {@link YIntervalSeriesImproved}.
	 */
	private YIntervalSeriesImproved yIntervalSeries;

	/**
	 * The {@link TimeSeries} for the count.
	 */
	private TimeSeries countPop;

	/**
	 * The data access service to access the data on the CMR.
	 */
	private ICombinedMetricsDataAccessService dataAccessService;

	/**
	 * Old list containing some data objects which could be reused.
	 */
	private List<TimerData> oldTimerData = Collections.emptyList();

	/**
	 * The old from date.
	 */
	private Date oldFromDate = new Date(Long.MAX_VALUE);

	/**
	 * The old to date.
	 */
	private Date oldToDate = new Date(0);

	/**
	 * This represents the date of one of the objects which was received at some time in the past
	 * but was the one with the newest date. This is needed for not requesting some data of the CMR
	 * sometimes.
	 */
	private Date newestDate = new Date(0);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new TimerData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
		template.setMethodIdent(inputDefinition.getIdDefinition().getMethodId());
		template.setId(-1L);

		dataAccessService = inputDefinition.getRepositoryDefinition().getCombinedMetricsDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<XYPlot> getPlots() {
		upperPlot = initializeUpperPlot();
		lowerPlot = initializeLowerPlot();

		List<XYPlot> plots = new ArrayList<XYPlot>(2);
		plots.add(upperPlot);
		plots.add(lowerPlot);
		weights.put(upperPlot, WEIGHT_UPPER_PLOT);
		weights.put(lowerPlot, WEIGHT_LOWER_PLOT);

		return plots;
	}

	/**
	 * Initializes the upper plot.
	 * 
	 * @return An instance of {@link XYPlot}.
	 */
	private XYPlot initializeUpperPlot() {
		yIntervalSeries = new YIntervalSeriesImproved("Series 1");

		YIntervalSeriesCollection yintervalseriescollection = new YIntervalSeriesCollection();
		yintervalseriescollection.addSeries(yIntervalSeries);

		DeviationRenderer renderer = new DeviationRenderer(true, false);
		renderer.setBaseShapesVisible(true);
		renderer.setSeriesStroke(0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		renderer.setSeriesFillPaint(0, new Color(255, 200, 200));
		renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0));
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, DateFormat.getDateTimeInstance(), NumberFormat.getNumberInstance()));

		NumberAxis rangeAxis = new NumberAxis("ms");
		rangeAxis.setAutoRangeMinimumSize(100.0d);
		rangeAxis.setRangeType(RangeType.POSITIVE);
		rangeAxis.setAutoRangeIncludesZero(true);

		XYPlot subplot = new XYPlot(yintervalseriescollection, null, rangeAxis, renderer);
		subplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		subplot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT, false);
		subplot.setRangeCrosshairVisible(true);

		return subplot;
	}

	/**
	 * Removes all data from the upper plot and sets the {@link TimerData} objects on the plot.
	 * 
	 * @param timerData
	 *            The data to set on the plot.
	 */
	private void setUpperPlotData(List<TimerData> timerData) {
		yIntervalSeries.clear();
		addUpperPlotData(timerData);
	}

	/**
	 * Updates the upper plot.
	 * 
	 * @param timerData
	 *            the input data.
	 */
	private void addUpperPlotData(List<TimerData> timerData) {
		for (TimerData data : timerData) {
			yIntervalSeries.add(data.getTimeStamp().getTime(), data.getAverage(), data.getMin(), data.getMax(), false);
		}
		yIntervalSeries.fireSeriesChanged();
	}

	/**
	 * Initializes the lower plot.
	 * 
	 * @return An instance of {@link XYPlot}
	 */
	private XYPlot initializeLowerPlot() {
		countPop = new TimeSeries("Count");

		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(countPop);

		XYBarDataset ds = new XYBarDataset(dataset, 30);

		XYBarRenderer renderer = new XYBarRenderer();
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, DateFormat.getDateTimeInstance(), NumberFormat.getNumberInstance()));

		NumberAxis rangeAxis = new NumberAxis("Count");
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRange(true);
		rangeAxis.setRangeType(RangeType.POSITIVE);

		XYPlot subplot = new XYPlot(ds, null, rangeAxis, renderer);
		subplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		subplot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

		return subplot;
	}

	/**
	 * Removes all data from the lower plot and sets the {@link TimerData} objects on the plot.
	 * 
	 * @param timerData
	 *            The data to set on the plot.
	 */
	private void setLowerPlotData(List<TimerData> timerData) {
		countPop.clear();
		addLowerPlotData(timerData);
	}

	/**
	 * Updates the lower plot.
	 * 
	 * @param timerData
	 *            the input data.
	 */
	private void addLowerPlotData(List<TimerData> timerData) {
		countPop.setNotify(false);
		for (TimerData data : timerData) {
			countPop.addOrUpdate(new Millisecond(data.getTimeStamp()), data.getCount());
		}
		countPop.setNotify(true);
		countPop.fireSeriesChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		DateAxis dateAxis = (DateAxis) upperPlot.getDomainAxis();
		Date from = dateAxis.getMinimumDate();
		Date to = dateAxis.getMaximumDate();

		update(from, to);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void update(Date from, Date to) {
		DateAxis dateAxis = (DateAxis) upperPlot.getDomainAxis();
		dateAxis.setMinimumDate(from);
		dateAxis.setMaximumDate(to);

		Date dataNewestDate = new Date(0);
		if (!oldTimerData.isEmpty()) {
			dataNewestDate = oldTimerData.get(oldTimerData.size() - 1).getTimeStamp();
		}
		boolean leftAppend = from.before(oldFromDate);
		// boolean rightAppend = to.after(dataNewestDate) &&
		// (to.equals(newestDate) || to.after(newestDate));
		boolean rightAppend = to.after(newestDate) || oldToDate.before(to);

		List<TimerData> adjustedTimerData = Collections.emptyList();

		InputDefinition inputDefinition = getInputDefinition();
		String workflowName = "";
		String activityName = "";
		if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.COMBINED_METRICS_EXTRAS_MARKER)) {
			CombinedMetricsInputDefinitionExtra combinedMetricsExtra = inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.COMBINED_METRICS_EXTRAS_MARKER);
			workflowName = combinedMetricsExtra.getWorkflow();
			activityName = combinedMetricsExtra.getActivity();
		}

		if (oldTimerData.isEmpty() || to.before(oldFromDate) || from.after(dataNewestDate)) {
			// the old data is empty or the range does not fit, thus we need
			// to access the whole range
			List<TimerData> timerData = dataAccessService.getCombinedMetricsFromToDate(template, from, to, workflowName, activityName);

			if (!timerData.isEmpty()) {
				adjustedTimerData = (List<TimerData>) adjustSamplingRate(timerData, from, to);

				// we got some data, thus we can set the date
				oldFromDate = (Date) from.clone();
				oldToDate = (Date) to.clone();
				if (newestDate.before(timerData.get(timerData.size() - 1).getTimeStamp())) {
					newestDate = new Date(timerData.get(timerData.size() - 1).getTimeStamp().getTime());
				}
			}
			oldTimerData = timerData;
		} else if (leftAppend && rightAppend) {
			// we have some data in between, but we need to append something
			// to the start and to the end
			Date rightDate = new Date(newestDate.getTime() + 1);
			Date leftDate = new Date(oldFromDate.getTime() - 1);

			List<TimerData> rightData = dataAccessService.getCombinedMetricsFromToDate(template, rightDate, to, workflowName, activityName);
			List<TimerData> leftData = dataAccessService.getCombinedMetricsFromToDate(template, from, leftDate, workflowName, activityName);

			if (!leftData.isEmpty()) {
				oldTimerData.addAll(0, leftData);
				oldFromDate = (Date) from.clone();
			}

			if (!rightData.isEmpty()) {
				oldTimerData.addAll(rightData);
				oldToDate = (Date) to.clone();
				if (newestDate.before(rightData.get(rightData.size() - 1).getTimeStamp())) {
					newestDate = new Date(rightData.get(rightData.size() - 1).getTimeStamp().getTime());
				}
			}

			adjustedTimerData = (List<TimerData>) adjustSamplingRate(oldTimerData, from, to);
		} else if (rightAppend) {
			// just append something on the right
			Date rightDate = new Date(newestDate.getTime() + 1);

			List<TimerData> timerData = dataAccessService.getCombinedMetricsFromToDate(template, rightDate, to, workflowName, activityName);

			if (!timerData.isEmpty()) {
				oldTimerData.addAll(timerData);
				oldToDate = (Date) to.clone();
				if (newestDate.before(timerData.get(timerData.size() - 1).getTimeStamp())) {
					newestDate = new Date(timerData.get(timerData.size() - 1).getTimeStamp().getTime());
				}
			}

			adjustedTimerData = (List<TimerData>) adjustSamplingRate(oldTimerData, from, to);
		} else if (leftAppend) {
			// just append something on the left
			Date leftDate = new Date(oldFromDate.getTime() - 1);

			List<TimerData> timerData = dataAccessService.getCombinedMetricsFromToDate(template, from, leftDate, workflowName, activityName);

			if (!timerData.isEmpty()) {
				oldTimerData.addAll(timerData);
				oldFromDate = (Date) from.clone();
			}

			adjustedTimerData = (List<TimerData>) adjustSamplingRate(oldTimerData, from, to);
		} else {
			// No update is needed here because we already have all the
			// needed data
			adjustedTimerData = (List<TimerData>) adjustSamplingRate(oldTimerData, from, to);
		}

		setUpperPlotData(adjustedTimerData);
		setLowerPlotData(adjustedTimerData);

		int start = -1;
		int end = -1;
		for (int i = 0; i < oldTimerData.size(); i++) {
			TimerData data = oldTimerData.get(i);
			if (!data.getTimeStamp().before(from)) {
				start = i;
				break;
			}
		}
		for (int i = oldTimerData.size(); i > 0; i--) {
			TimerData data = oldTimerData.get(i - 1);
			if (!data.getTimeStamp().after(to)) {
				end = i;
				break;
			}
		}

		if ((start != -1) && (end != -1)) {
			final List<TimerData> dataToSet = oldTimerData.subList(start, end);

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					getRootEditor().setDataInput(dataToSet);
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getWeight(XYPlot subPlot) {
		return weights.get(subPlot);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferenceIds = EnumSet.noneOf(PreferenceId.class);
		if (getInputDefinition().getRepositoryDefinition() instanceof CmrRepositoryDefinition) {
			preferenceIds.add(PreferenceId.LIVEMODE);
		}
		preferenceIds.add(PreferenceId.TIMELINE);
		preferenceIds.add(PreferenceId.SAMPLINGRATE);
		preferenceIds.add(PreferenceId.UPDATE);
		return preferenceIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		super.dispose();
		oldTimerData.clear();
	}
}
