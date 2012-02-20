package info.novatec.inspectit.rcp.editor.graph.plot;

import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.control.SamplingRateControl.Sensitivity;
import info.novatec.inspectit.rcp.editor.preferences.control.samplingrate.SamplingRateMode;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.jfree.chart.plot.XYPlot;

/**
 * The interface for all plot controller.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface PlotController {

	/**
	 * Sets the input definition of this controller.
	 * 
	 * @param inputDefinition
	 *            The input definition.
	 */
	void setInputDefinition(InputDefinition inputDefinition);

	/**
	 * Sets the root editor.
	 * 
	 * @param rootEditor
	 *            The rootEditor to set.
	 */
	void setRootEditor(IRootEditor rootEditor);

	/**
	 * This method is used to retrieve the plots which are used in the whole
	 * chart. No data has to be requested from the server here, this is done in
	 * the {@link #update()} method.
	 * 
	 * @return A list containing {@link XYPlot} classes which are used by
	 *         JFreeChart to initialize the chart.
	 */
	List<XYPlot> getPlots();

	/**
	 * Returns the weight of a specific plot.
	 * 
	 * @param subPlot
	 *            The plot to calculate the weight.
	 * @return Returns the weight of the plot.
	 */
	int getWeight(XYPlot subPlot);

	/**
	 * The do refresh method is called at least one time to fill the plots with
	 * some initial data. It depends on several settings if this method is
	 * called repeatedly.
	 */
	void doRefresh();

	/**
	 * This method obtains historical information from the DB for the timeframe
	 * denoted by its parameters.
	 * <p>
	 * After fetching the historical data the upper and lower plot graphs get
	 * updated.
	 * 
	 * @param from
	 *            the timeframe's start date.
	 * @param to
	 *            the timeframes's end date.
	 */
	void update(Date from, Date to);

	/**
	 * Sets the sampling rate.
	 * 
	 * @param mode
	 *            The mode.
	 * @param sensitivity
	 *            The sensitivity of the mode.
	 */
	void setSamplingRate(SamplingRateMode mode, Sensitivity sensitivity);

	/**
	 * Returns all needed preference IDs.
	 * 
	 * @return A {@link Set} containing all {@link PreferenceId}. Returning
	 *         <code>null</code> is not permitted here. At least a
	 *         {@link Collections#EMPTY_SET} should be returned.
	 */
	Set<PreferenceId> getPreferenceIds();

	/**
	 * This method is called whenever something is changed in one of the
	 * preferences.
	 * 
	 * @param preferenceEvent
	 *            The event object containing the changed objects.
	 */
	void preferenceEventFired(PreferenceEvent preferenceEvent);

	/**
	 * Disposes all plots etc.
	 */
	void dispose();

}
