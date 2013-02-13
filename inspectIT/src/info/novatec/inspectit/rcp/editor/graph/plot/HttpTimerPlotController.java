package info.novatec.inspectit.rcp.editor.graph.plot;

import info.novatec.inspectit.cmr.service.IHttpTimerDataAccessService;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.HttpTimerDataAggregator;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.HttpChartingInputDefinitionExtra;
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
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
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
 * {@link PlotController} for displaying many Http requests in the graph.
 * 
 * @author Ivan Senic
 * 
 */
public class HttpTimerPlotController extends AbstractPlotController {

	/**
	 * Colors we will use for series.
	 */
	private static final int[] SERIES_COLORS = new int[] { SWT.COLOR_RED, SWT.COLOR_BLUE, SWT.COLOR_DARK_GREEN, SWT.COLOR_DARK_YELLOW, SWT.COLOR_DARK_GRAY, SWT.COLOR_BLACK, SWT.COLOR_DARK_CYAN,
			SWT.COLOR_DARK_BLUE };

	/**
	 * The map containing the weight of the {@link XYPlot}s.
	 */
	private Map<XYPlot, Integer> weights = new HashMap<XYPlot, Integer>();

	/**
	 * Duration series.
	 */
	private List<YIntervalSeriesImproved> durationSeries;

	/**
	 * Count series.
	 */
	private List<TimeSeries> countSeries;

	/**
	 * Templates that will be used for data display. Every template is one line in line chart.
	 */
	private List<HttpTimerData> templates;

	/**
	 * If true tag values from templates will be used in plotting. Otherwise URI is used.
	 */
	private boolean plotByTagValue = false;

	/**
	 * Plot used to display duration of the HTTP requests.
	 */
	private XYPlot durationPlot;

	/**
	 * Plot used to display the count of the HTTP requests.
	 */
	private XYPlot countPlot;

	/**
	 * {@link IHttpTimerDataAccessService}.
	 */
	private IHttpTimerDataAccessService dataAccessService;

	/**
	 * {@link IAggregator}.
	 */
	private IAggregator<HttpTimerData> aggregator;

	/**
	 * List of displayed data.
	 */
	List<HttpTimerData> displayedData = Collections.emptyList();

	/**
	 * Date to display data to.
	 */
	Date toDate = new Date(0);

	/**
	 * Date to display data from.
	 */
	Date fromDate = new Date(Long.MAX_VALUE);

	/**
	 * Date that mark the last displayed data on the grap.
	 */
	Date latestDataDate = new Date(0);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.HTTP_CHARTING_EXTRAS_MARKER)) {
			HttpChartingInputDefinitionExtra inputDefinitionExtra = inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.HTTP_CHARTING_EXTRAS_MARKER);
			templates = inputDefinitionExtra.getTemplates();
			plotByTagValue = inputDefinitionExtra.isPlotByTagValue();
		}

		aggregator = new HttpTimerDataAggregator(true, !plotByTagValue);
		dataAccessService = inputDefinition.getRepositoryDefinition().getHttpTimerDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<XYPlot> getPlots() {
		durationPlot = initializeDurationPlot();
		countPlot = initializeCountPlot();

		weights.put(durationPlot, 2);
		weights.put(countPlot, 1);

		List<XYPlot> list = new ArrayList<XYPlot>();
		Collections.addAll(list, durationPlot, countPlot);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWeight(XYPlot subPlot) {
		return weights.get(subPlot);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean showLegend() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRefresh() {
		DateAxis dateAxis = (DateAxis) durationPlot.getDomainAxis();
		Date from = dateAxis.getMinimumDate();
		Date to = dateAxis.getMaximumDate();

		update(from, to);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Date from, Date to) {
		DateAxis dateAxis = (DateAxis) durationPlot.getDomainAxis();
		dateAxis.setMinimumDate(from);
		dateAxis.setMaximumDate(to);

		// complete load if we have no data, or wanted time range is completly outside the current
		boolean completeLoad = CollectionUtils.isEmpty(displayedData) || fromDate.after(to) || toDate.before(from);
		// left append if currently displayed from date is after the new from date
		boolean leftAppend = fromDate.after(from);
		// right append if the currently displayed to date is before new to date or the date of the
		// last data is before new date
		boolean rightAppend = toDate.before(to) || latestDataDate.before(to);

		if (completeLoad) {
			List<HttpTimerData> httpTimerDatas = dataAccessService.getChartingHttpTimerDataFromDateToDate(templates, from, to, plotByTagValue);
			if (CollectionUtils.isNotEmpty(httpTimerDatas)) {
				fromDate = (Date) from.clone();
				toDate = (Date) to.clone();
			}
			displayedData = httpTimerDatas;
		} else {
			if (rightAppend) {
				Date startingFrom = new Date(latestDataDate.getTime() + 1);
				List<HttpTimerData> httpTimerDatas = dataAccessService.getChartingHttpTimerDataFromDateToDate(templates, startingFrom, to, plotByTagValue);
				if (CollectionUtils.isNotEmpty(httpTimerDatas)) {
					displayedData.addAll(httpTimerDatas);
					toDate = (Date) to.clone();
				}
			}
			if (leftAppend) {
				Date endingTo = new Date(fromDate.getTime() - 1);
				List<HttpTimerData> httpTimerDatas = dataAccessService.getChartingHttpTimerDataFromDateToDate(templates, from, endingTo, plotByTagValue);
				if (CollectionUtils.isNotEmpty(httpTimerDatas)) {
					displayedData.addAll(0, httpTimerDatas);
					fromDate = (Date) from.clone();
				}
			}
		}

		// update the last displayed data
		if (CollectionUtils.isNotEmpty(displayedData)) {
			latestDataDate = new Date(displayedData.get(displayedData.size() - 1).getTimeStamp().getTime());
		}

		Map<Object, List<HttpTimerData>> map = new HashMap<Object, List<HttpTimerData>>();
		for (HttpTimerData data : displayedData) {
			List<HttpTimerData> list = map.get(getSeriesKey(data));
			if (null == list) {
				list = new ArrayList<HttpTimerData>();
				map.put(getSeriesKey(data), list);
			}
			list.add(data);
		}

		for (Entry<Object, List<HttpTimerData>> entry : map.entrySet()) {
			entry.setValue(adjustSamplingRate(entry.getValue(), from, to, aggregator));
		}

		setDurationPlotData(map);
		setCountPlotData(map);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	 * Removes all data from the upper plot and sets the {@link TimerData} objects on the plot.
	 * 
	 * @param map
	 *            The data to set on the plot.
	 */
	private void setDurationPlotData(Map<Object, List<HttpTimerData>> map) {
		for (YIntervalSeriesImproved series : durationSeries) {
			series.clear();
			for (Entry<Object, List<HttpTimerData>> entry : map.entrySet()) {
				if (series.getKey().equals(entry.getKey())) {
					for (HttpTimerData data : entry.getValue()) {
						series.add(data.getTimeStamp().getTime(), data.getAverage(), data.getMin(), data.getMax(), false);
					}
					break;
				}
			}
			series.fireSeriesChanged();
		}
	}

	/**
	 * Removes all data from the upper plot and sets the {@link TimerData} objects on the plot.
	 * 
	 * @param map
	 *            The data to set on the plot.
	 */
	private void setCountPlotData(Map<Object, List<HttpTimerData>> map) {
		for (TimeSeries series : countSeries) {
			series.clear();
			series.setNotify(false);
			for (Entry<Object, List<HttpTimerData>> entry : map.entrySet()) {
				if (series.getKey().equals(entry.getKey())) {
					for (HttpTimerData data : entry.getValue()) {
						series.addOrUpdate(new Millisecond(data.getTimeStamp()), data.getCount());
					}
					break;
				}
			}
			series.setNotify(true);
			series.fireSeriesChanged();
		}
	}

	/**
	 * Initializes the duration plot.
	 * 
	 * @return An instance of {@link XYPlot}.
	 */
	private XYPlot initializeDurationPlot() {
		durationSeries = new ArrayList<YIntervalSeriesImproved>();
		YIntervalSeriesCollection yintervalseriescollection = new YIntervalSeriesCollection();
		for (HttpTimerData httpTimerData : templates) {
			YIntervalSeriesImproved yIntervalSeries = new YIntervalSeriesImproved(getSeriesKey(httpTimerData));
			yintervalseriescollection.addSeries(yIntervalSeries);
			durationSeries.add(yIntervalSeries);
		}

		DeviationRenderer renderer = new DeviationRenderer(true, false);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, DateFormat.getDateTimeInstance(), NumberFormat.getNumberInstance()));
		renderer.setBaseShapesVisible(true);
		renderer.setAlpha(0.1f);
		Display display = Display.getDefault();
		for (int i = 0; i < durationSeries.size(); i++) {
			int color = SERIES_COLORS[i % SERIES_COLORS.length];
			RGB rgb = display.getSystemColor(color).getRGB();
			renderer.setSeriesStroke(i, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			renderer.setSeriesFillPaint(i, new Color(rgb.red, rgb.green, rgb.blue));
			renderer.setSeriesOutlineStroke(i, new BasicStroke(2.0f));
			renderer.setSeriesShape(i, new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0));
		}
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
	 * Initializes the lower plot.
	 * 
	 * @return An instance of {@link XYPlot}
	 */
	private XYPlot initializeCountPlot() {
		countSeries = new ArrayList<TimeSeries>();
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		for (HttpTimerData httpTimerData : templates) {
			TimeSeries timeSeries = new TimeSeries(getSeriesKey(httpTimerData));
			countSeries.add(timeSeries);
			dataset.addSeries(timeSeries);
		}

		// ISE: No idea why we have 30 here, used same value as in other charts
		XYBarDataset ds = new XYBarDataset(dataset, 30);

		XYBarRenderer renderer = new XYBarRenderer();
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, DateFormat.getDateTimeInstance(), NumberFormat.getNumberInstance()));
		renderer.setShadowVisible(false);
		renderer.setMargin(0.1d);
		Display display = Display.getDefault();
		for (int i = 0; i < countSeries.size(); i++) {
			int color = SERIES_COLORS[i % SERIES_COLORS.length];
			RGB rgb = display.getSystemColor(color).getRGB();
			renderer.setSeriesPaint(i, new Color(rgb.red, rgb.green, rgb.blue));
			renderer.setSeriesVisibleInLegend(i, Boolean.FALSE);
		}

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
	 * Returns the series key for the {@link HttpTimerData} object.
	 * 
	 * @param httpTimerData
	 *            {@link HttpTimerData}.
	 * @return Key used to initialize the series and later on compare which series data should be
	 *         added to.
	 */
	private Comparable<?> getSeriesKey(HttpTimerData httpTimerData) {
		if (plotByTagValue) {
			return "Tag: " + httpTimerData.getInspectItTaggingHeaderValue();
		} else {
			return "URI: " + httpTimerData.getUri();
		}
	}
}
