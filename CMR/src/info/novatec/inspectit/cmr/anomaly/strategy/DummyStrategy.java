package info.novatec.inspectit.cmr.anomaly.strategy;

import java.util.List;

import info.novatec.inspectit.cmr.influxdb.InfluxDbService;

import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

public class DummyStrategy extends AbstractAnomalyDetectionStrategy {

	public DummyStrategy(InfluxDbService influxDb) {
		super(influxDb);
	}

	@Override
	public void detect() {

		QueryResult query = influxDb.query("select MEAN(total_cpu_usage) as mean_total_cpu from cpu_information");

		for (Result r : query.getResults()) {
			List<Series> series = r.getSeries();
			System.out.println("{");
			for (Series s : series) {
				List<String> columns = s.getColumns();
				List<List<Object>> values = s.getValues();
				for (List<Object> valueList : values) {
					System.out.println(" {");
					for (int i = 0; i < columns.size(); i++) {
						System.out.println("  " + columns.get(i) + ": " + valueList.get(i));
					}
					System.out.println(" }");
				}
			}
			System.out.println("}");
		}

	}
}
