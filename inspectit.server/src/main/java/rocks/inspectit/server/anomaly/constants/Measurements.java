package rocks.inspectit.server.anomaly.constants;

/**
 * @author Marius Oehler
 *
 */
public interface Measurements {

	String MEASUREMENT_PREFIX = "iit_";

	String TAG_CONFIGURATION_GROUP_ID = "configuration_group_id";

	interface Anomalies extends Measurements {

		String NAME = MEASUREMENT_PREFIX + "anomalies";

		String TAG_EVENT = "event";

		String FIELD_ANOMALY_ACTIVE = "anomaly_active";
	}

	interface ProcessingUnitGroupStatistics extends Measurements {

		String NAME = MEASUREMENT_PREFIX + "pug_statistics";

		String TAG_HEALTH_STATUS = "health_status";

		String FIELD_UNKNOWN = "s_unknown";

		String FIELD_NORMAL = "s_normal";

		String FIELD_WARNING = "s_warning";

		String FIELD_CRITICAL = "s_critical";
	}

	interface Data extends Measurements {

		String NAME = MEASUREMENT_PREFIX + "data";

		String TAG_CONFIGURATION_ID = "configuration_id";

		String TAG_HEALTH_STATUS = "health_status";

		String FIELD_LOWER_CRITICAL = "threshold_lower_critical";

		String FIELD_LOWER_WARNING = "threshold_lower_warning";

		String FIELD_UPPER_WARNING = "threshold_upper_warning";

		String FIELD_UPPER_CRITICAL = "threshold_upper_critical";

		String FIELD_BASELINE = "baseline";

		String FIELD_METRIC_AGGREGATION = "metric_aggregation";
	}

	interface PreviewData extends Data {
		String NAME = MEASUREMENT_PREFIX + "preview_data";
	}
}
