/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

import rocks.inspectit.server.anomaly.stream.component.impl.BusinessTransactionAlertingComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.BusinessTransactionContextInjectorComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.ConfidenceBandComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.ForecastComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.PercentageRateComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.QuadraticScoreFilterComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.RHoltWintersComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.StandardDeviationComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.TSDBWriterComponent;
import rocks.inspectit.server.anomaly.stream.component.impl.WeightedStandardDeviationComponent;

/**
 * @author Marius Oehler
 *
 */
public abstract class StreamComponentFactory {

	public abstract BusinessTransactionContextInjectorComponent createBusinessTransactionInjector();

	public abstract QuadraticScoreFilterComponent createQuadraticScoreFilter();

	public abstract PercentageRateComponent createPercentageRate();

	public abstract BusinessTransactionAlertingComponent createBusinessTransactionAlerting();

	public abstract TSDBWriterComponent createTSDBWriter();

	public abstract StandardDeviationComponent createStandardDeviation();

	public abstract RHoltWintersComponent createRHoltWinters();

	public abstract ConfidenceBandComponent createConfidenceBand();

	public abstract WeightedStandardDeviationComponent createWeightedStandardDeviation();

	public abstract ForecastComponent createForecastComponent();
}
