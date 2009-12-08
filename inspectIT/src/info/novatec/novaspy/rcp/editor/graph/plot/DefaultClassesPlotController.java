package info.novatec.novaspy.rcp.editor.graph.plot;

import info.novatec.novaspy.communication.data.ClassLoadingInformationData;
import info.novatec.novaspy.rcp.editor.InputDefinition;
import info.novatec.novaspy.rcp.editor.preferences.PreferenceId;
import info.novatec.novaspy.rcp.repository.service.GlobalDataAccessService;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.RangeType;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * This class creates a {@link XYPlot} containing the
 * {@link ClassLoadingInformationData} informations.
 * 
 * @author Eduard Tudenhoefner
 * @author Patrice Bouillet
 * 
 */
public class DefaultClassesPlotController extends AbstractPlotController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "novaspy.subview.graph.classes";

	/**
	 * The template of the {@link ClassLoadingInformationData} object.
	 */
	private ClassLoadingInformationData template;

	/**
	 * Indicates the weight of the upper {@link XYPlot}.
	 */
	private static final int WEIGHT_UPPER_PLOT = 1;

	/**
	 * The upper {@link XYPlot} containing the graphical view.
	 */
	private XYPlot upperPlot;

	/**
	 * The map containing the weights of the {@link XYPlot}s.
	 */
	private Map<XYPlot, Integer> weights = new HashMap<XYPlot, Integer>();

	/**
	 * The {@link YIntervalSeries}.
	 */
	private YIntervalSeriesImproved loadedClasses;

	/**
	 * The {@link YIntervalSeries}.
	 */
	private YIntervalSeriesImproved totalLoadedClasses;

	/**
	 * The data access service to access the data on the CMR.
	 */
	private GlobalDataAccessService dataAccessService;

	/**
	 * Old list containing some data objects which could be reused.
	 */
	private List<ClassLoadingInformationData> oldData = Collections.emptyList();

	/**
	 * The old from date.
	 */
	private Date oldFromDate = new Date(Long.MAX_VALUE);

	/**
	 * The old to date.
	 */
	private Date oldToDate = new Date(0);

	/**
	 * This represents the date of one of the objects which was received at some
	 * time in the past but was the one with the newest date. This is needed for
	 * not requesting some data of the CMR sometimes.
	 */
	private Date newestDate = new Date(0);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new ClassLoadingInformationData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
		template.setId(-1L);

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<XYPlot> getPlots() {
		upperPlot = initializeUpperPlot();

		List<XYPlot> plots = new ArrayList<XYPlot>(1);
		plots.add(upperPlot);
		weights.put(upperPlot, WEIGHT_UPPER_PLOT);

		return plots;
	}

	/**
	 * Initializes the upper plot.
	 * 
	 * @return An instance of {@link XYPlot}.
	 */
	private XYPlot initializeUpperPlot() {
		loadedClasses = new YIntervalSeriesImproved("loaded classes");
		totalLoadedClasses = new YIntervalSeriesImproved("total loaded classes");

		YIntervalSeriesCollection yintervalseriescollection = new YIntervalSeriesCollection();
		yintervalseriescollection.addSeries(loadedClasses);
		yintervalseriescollection.addSeries(totalLoadedClasses);

		DeviationRenderer renderer = new DeviationRenderer(true, false);
		renderer.setBaseShapesVisible(true);
		renderer.setSeriesStroke(0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		renderer.setSeriesFillPaint(0, new Color(255, 200, 200));
		renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0));
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

		final NumberAxis rangeAxis = new NumberAxis("Classes");
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRangeMinimumSize(2000.0d);
		rangeAxis.setRangeType(RangeType.POSITIVE);
		rangeAxis.setAutoRangeIncludesZero(true);

		final XYPlot subplot = new XYPlot(yintervalseriescollection, null, rangeAxis, renderer);
		subplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		subplot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		subplot.setRangeCrosshairVisible(true);

		return subplot;
	}

	/**
	 * Updates the upper plot with the given input data.
	 * 
	 * @param classLoadingData
	 *            The input data.
	 */
	private void addUpperPlotData(List<ClassLoadingInformationData> classLoadingData) {
		for (ClassLoadingInformationData data : classLoadingData) {
			int loadedClassAverage = data.getTotalLoadedClassCount() / data.getCount();
			long totalLoadedClassAverage = data.getTotalTotalLoadedClassCount() / data.getCount();
			loadedClasses.add(data.getTimeStamp().getTime(), loadedClassAverage, data.getMinLoadedClassCount(), data.getMaxLoadedClassCount(), false);
			totalLoadedClasses.add(data.getTimeStamp().getTime(), totalLoadedClassAverage, data.getMinTotalLoadedClassCount(), data.getMaxTotalLoadedClassCount(), false);
		}
		loadedClasses.fireSeriesChanged();
		totalLoadedClasses.fireSeriesChanged();
	}

	/**
	 * Removes all data from the upper plot and sets the
	 * {@link ClassLoadingInformationData} objects on the plot.
	 * 
	 * @param classLoadingData
	 *            The data to set on the plot.
	 */
	private void setUpperPlotData(List<ClassLoadingInformationData> classLoadingData) {
		loadedClasses.clear();
		totalLoadedClasses.clear();
		addUpperPlotData(classLoadingData);
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
		if (!oldData.isEmpty()) {
			dataNewestDate = oldData.get(oldData.size() - 1).getTimeStamp();
		}
		boolean leftAppend = from.before(oldFromDate);
		// boolean rightAppend = to.after(dataNewestDate) &&
		// (to.equals(newestDate) || to.after(newestDate));
		boolean rightAppend = to.after(newestDate) || oldToDate.before(to);

		List<ClassLoadingInformationData> adjustedTimerData = Collections.emptyList();

		if (oldData.isEmpty() || to.before(oldFromDate) || from.after(dataNewestDate)) {
			// the old data is empty or the range does not fit, thus we need
			// to access the whole range
			List<ClassLoadingInformationData> data = (List<ClassLoadingInformationData>) dataAccessService.getDataObjectsFromToDate(template, from, to);

			if (!data.isEmpty()) {
				adjustedTimerData = (List<ClassLoadingInformationData>) adjustSamplingRate(data, from, to);

				// we got some data, thus we can set the date
				oldFromDate = from;
				oldToDate = to;
				if (newestDate.before(data.get(data.size() - 1).getTimeStamp())) {
					newestDate = new Date(data.get(data.size() - 1).getTimeStamp().getTime());
				}
			}
			oldData = data;
		} else if (leftAppend && rightAppend) {
			// we have some data in between, but we need to append something
			// to the start and to the end
			Date rightDate = new Date(newestDate.getTime() + 1);
			Date leftDate = new Date(oldFromDate.getTime() - 1);

			List<ClassLoadingInformationData> rightData = (List<ClassLoadingInformationData>) dataAccessService.getDataObjectsFromToDate(template, rightDate, to);
			List<ClassLoadingInformationData> leftData = (List<ClassLoadingInformationData>) dataAccessService.getDataObjectsFromToDate(template, from, leftDate);

			if (!leftData.isEmpty()) {
				oldData.addAll(0, leftData);
				oldFromDate = from;
			}

			if (!rightData.isEmpty()) {
				oldData.addAll(rightData);
				oldToDate = to;
				if (newestDate.before(rightData.get(rightData.size() - 1).getTimeStamp())) {
					newestDate = new Date(rightData.get(rightData.size() - 1).getTimeStamp().getTime());
				}
			}

			adjustedTimerData = (List<ClassLoadingInformationData>) adjustSamplingRate(oldData, from, to);
		} else if (rightAppend) {
			// just append something on the right
			Date rightDate = new Date(newestDate.getTime() + 1);

			List<ClassLoadingInformationData> timerData = (List<ClassLoadingInformationData>) dataAccessService.getDataObjectsFromToDate(template, rightDate, to);

			if (!timerData.isEmpty()) {
				oldData.addAll(timerData);
				oldToDate = to;
				if (newestDate.before(timerData.get(timerData.size() - 1).getTimeStamp())) {
					newestDate = new Date(timerData.get(timerData.size() - 1).getTimeStamp().getTime());
				}
			}

			adjustedTimerData = (List<ClassLoadingInformationData>) adjustSamplingRate(oldData, from, to);
		} else if (leftAppend) {
			// just append something on the left
			Date leftDate = new Date(oldFromDate.getTime() - 1);

			List<ClassLoadingInformationData> timerData = (List<ClassLoadingInformationData>) dataAccessService.getDataObjectsFromToDate(template, from, leftDate);

			if (!timerData.isEmpty()) {
				oldData.addAll(timerData);
				oldFromDate = from;
			}

			adjustedTimerData = (List<ClassLoadingInformationData>) adjustSamplingRate(oldData, from, to);
		} else {
			// No update is needed here because we already have all the
			// needed data
			adjustedTimerData = (List<ClassLoadingInformationData>) adjustSamplingRate(oldData, from, to);
		}

		setUpperPlotData(adjustedTimerData);
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
		preferenceIds.add(PreferenceId.TIMELINE);
		preferenceIds.add(PreferenceId.SAMPLINGRATE);
		preferenceIds.add(PreferenceId.LIVEMODE);
		preferenceIds.add(PreferenceId.UPDATE);
		return preferenceIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
	}

}
