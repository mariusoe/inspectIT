package info.novatec.inspectit.rcp.editor.graph.plot;

import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.service.IHttpTimerDataAccessService;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.HttpTimerDataAggregator;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.HttpChartingInputDefinitionExtra;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import info.novatec.inspectit.rcp.util.data.RegExAggregatedHttpTimerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;

/**
 * {@link PlotController} for displaying many Http requests in the graph.
 * 
 * @author Ivan Senic
 * 
 */
public class HttpTimerPlotController extends AbstractTimerDataPlotController<HttpTimerData> {

	/**
	 * Templates that will be used for data display. Every template is one line in line chart.
	 */
	private List<HttpTimerData> templates;

	/**
	 * If true tag values from templates will be used in plotting. Otherwise URI is used.
	 */
	private boolean plotByTagValue = false;

	/**
	 * If true than regular expression transformation will be performed on the template URIs.
	 */
	private boolean regExTransformation = false;

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
	 * HTTP sensor type ident.
	 */
	private MethodSensorTypeIdent httpSensorTypeIdent;

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
			regExTransformation = inputDefinitionExtra.isRegExTransformation();
		}

		aggregator = new HttpTimerDataAggregator(true, !plotByTagValue);
		dataAccessService = inputDefinition.getRepositoryDefinition().getHttpTimerDataAccessService();

		if (0 != inputDefinition.getIdDefinition().getSensorTypeId()) {
			httpSensorTypeIdent = (MethodSensorTypeIdent) inputDefinition.getRepositoryDefinition().getCachedDataService().getSensorTypeIdentForId(inputDefinition.getIdDefinition().getSensorTypeId());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean showLegend() {
		return templates.size() > 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Date from, Date to) {
		super.update(from, to);

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
	 * Returns the series key for the {@link HttpTimerData} object.
	 * 
	 * @param httpTimerData
	 *            {@link HttpTimerData}.
	 * @return Key used to initialize the series and later on compare which series data should be
	 *         added to.
	 */
	protected Comparable<?> getSeriesKey(HttpTimerData httpTimerData) {
		if (plotByTagValue) {
			return "Tag: " + httpTimerData.getInspectItTaggingHeaderValue();
		} else if (regExTransformation) {
			return "Transformed URI: " + RegExAggregatedHttpTimerData.getTransformedUri(httpTimerData, httpSensorTypeIdent);
		} else {
			return "URI: " + httpTimerData.getUri();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<HttpTimerData> getTemplates() {
		return templates;
	}
}
