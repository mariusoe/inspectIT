package info.novatec.inspectit.cmr.anomaly.strategy;

import info.novatec.inspectit.cmr.influxdb.InfluxDbService;

import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;

public class DummyStrategy extends AbstractAnomalyDetectionStrategy {

	public DummyStrategy(InfluxDbService influxDb) {
		super(influxDb);
	}

	@Override
	public void detect() {

		QueryResult query = influxDb.query("select MEAN(total_cpu_usage) as mean_total_cpu from cpu_information");

		for (Result r : query.getResults()) {
			System.out.println(r.toString());
		}

	}

}
