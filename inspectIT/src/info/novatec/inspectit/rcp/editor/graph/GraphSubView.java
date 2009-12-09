package info.novatec.inspectit.rcp.editor.graph;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.AbstractSubView;
import info.novatec.inspectit.rcp.editor.graph.plot.DateAxisZoomNotify;
import info.novatec.inspectit.rcp.editor.graph.plot.PlotController;
import info.novatec.inspectit.rcp.editor.graph.plot.ZoomListener;
import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;

import java.awt.Color;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.experimental.chart.swt.ChartComposite;

/**
 * This sub-view can create charts which can contain themselves some plots. The
 * plots are defined by {@link PlotController}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class GraphSubView extends AbstractSubView {

	/**
	 * The composite used to draw the items to.
	 */
	private Composite composite;

	/**
	 * The {@link JFreeChart} chart.
	 */
	private JFreeChart chart;

	/**
	 * The plot controller defines the visualized plots in the chart.
	 */
	private PlotController plotController;

	/**
	 * If we are in the auto update mode.
	 */
	private boolean autoUpdate = true;

	/**
	 * One minute in milliseconds.
	 */
	private static final long ONE_MINUTE = 60000L;

	/**
	 * Ten minutes in milliseconds.
	 */
	private static final long TEN_MINUTES = ONE_MINUTE * 10;

	/**
	 * The chart composite frame.
	 */
	private ChartComposite frame;

	/**
	 * The zoom listener.
	 */
	private ZoomListener zoomListener;

	/**
	 * Defines if this view is disposed.
	 */
	private boolean isDisposed = false;

	/**
	 * The constructor taking one parameter and creating a
	 * {@link PlotController}.
	 * 
	 * @param fqn
	 *            The fully-qualified-name of the corresponding sensor type.
	 */
	public GraphSubView(String fqn) {
		this.plotController = PlotFactory.createDefaultPlotController(fqn);
	}

	/**
	 * The constructor taking one parameter and creating a
	 * {@link PlotController}.
	 * 
	 * @param sensorTypeEnum
	 *            The sensor type enumeration of the corresponding sensor type.
	 */
	public GraphSubView(SensorTypeEnum sensorTypeEnum) {
		this.plotController = PlotFactory.createDefaultPlotController(sensorTypeEnum);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		// set the input definition
		plotController.setInputDefinition(getRootEditor().getInputDefinition());
		plotController.setRootEditor(getRootEditor());

		// create the composite
		composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(1, false));

		// create the chart
		chart = createChart();
		chart.removeLegend();
		Color color = new Color(toolkit.getColors().getBackground().getRed(), toolkit.getColors().getBackground().getGreen(), toolkit.getColors().getBackground().getBlue());
		chart.setBackgroundPaint(color);

		frame = new ChartComposite(composite, SWT.NONE, chart, true);
		// frame.setRangeZoomable(false);
		frame.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	/**
	 * Creates and returns a {@link JFreeChart} chart.
	 * 
	 * @return The {@link JFreeChart} chart.
	 */
	private JFreeChart createChart() {
		DateAxisZoomNotify domainAxis = new DateAxisZoomNotify();
		domainAxis.setLowerMargin(0.0d);
		domainAxis.setAutoRangeMinimumSize(100000.0d);
		long now = System.currentTimeMillis();
		domainAxis.setRange(new Range(now - TEN_MINUTES, now + ONE_MINUTE), true, false);

		addZoomListener(domainAxis);

		CombinedDomainXYPlot plot = new CombinedDomainXYPlot(domainAxis);
		plot.setGap(10.0);

		// add the subplots...
		List<XYPlot> subPlots = plotController.getPlots();
		for (XYPlot subPlot : subPlots) {
			plot.add(subPlot, plotController.getWeight(subPlot));
		}

		plot.setOrientation(PlotOrientation.VERTICAL);

		// return a new chart containing the overlaid plot...
		return new JFreeChart(plot);
	}

	/**
	 * Adds the zoom listener to the domain axis.
	 * 
	 * @param domainAxis
	 *            The domain axis.
	 */
	private void addZoomListener(DateAxisZoomNotify domainAxis) {
		if (null == zoomListener) {
			zoomListener = new ZoomListener() {
				public void zoomOccured() {
					if (autoUpdate) {
						autoUpdate = false;
						getRootEditor().getPreferencePanel().disableLiveMode();
					}
					doRefresh();
				}
			};
		}
		domainAxis.addZoomListener(zoomListener);
	}

	/**
	 * Removes the zoom listener from the domain axis.
	 * 
	 * @param domainAxis
	 *            The domain axis.
	 */
	private void removeZoomListener(DateAxisZoomNotify domainAxis) {
		domainAxis.removeZoomListener(zoomListener);
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDataInput(List<? extends DefaultData> data) {
		// nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PreferenceId> getPreferenceIds() {
		return plotController.getPreferenceIds();
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		if (PreferenceId.TIMELINE.equals(preferenceEvent.getPreferenceId())) {
			XYPlot plot = (XYPlot) chart.getPlot();
			DateAxis axis = (DateAxis) plot.getDomainAxis();

			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();
			if (preferenceMap.containsKey(PreferenceId.TimeLine.TO_DATE_ID)) {
				Date toDate = (Date) preferenceMap.get(PreferenceId.TimeLine.TO_DATE_ID);
				axis.setMaximumDate(toDate);
			}
			if (preferenceMap.containsKey(PreferenceId.TimeLine.FROM_DATE_ID)) {
				Date fromDate = (Date) preferenceMap.get(PreferenceId.TimeLine.FROM_DATE_ID);
				axis.setMinimumDate(fromDate);
			}
		}

		if (PreferenceId.LIVEMODE.equals(preferenceEvent.getPreferenceId())) {
			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();
			if (preferenceMap.containsKey(PreferenceId.LiveMode.BUTTON_LIVE_ID)) {
				autoUpdate = (Boolean) preferenceMap.get(PreferenceId.LiveMode.BUTTON_LIVE_ID);
				if (autoUpdate) {
					XYPlot plot = (XYPlot) chart.getPlot();
					DateAxisZoomNotify domainAxis = (DateAxisZoomNotify) plot.getDomainAxis();
					removeZoomListener(domainAxis);
					frame.restoreAutoBounds();
					addZoomListener(domainAxis);
					doRefresh();
				}
			}
		}

		plotController.preferenceEventFired(preferenceEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		if (!isDisposed) {
			XYPlot plot = (XYPlot) chart.getPlot();
			if (autoUpdate) {
				long now = System.currentTimeMillis();
				plot.getDomainAxis().setRange(new Range(now - TEN_MINUTES, now + ONE_MINUTE));
				plotController.doRefresh();
			} else {
				DateAxis axis = (DateAxis) plot.getDomainAxis();
				Date minDate = axis.getMinimumDate();
				Date maxDate = axis.getMaximumDate();
				plotController.update(minDate, maxDate);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		isDisposed = true;
		plotController.dispose();
	}

}
