package rocks.inspectit.server.processor.impl.tsdb;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.CpuInformationData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 *
 * @author Marius Oehler
 *
 */
public class CpuMetricProcessor extends AbstractCmrDataProcessor {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * Instance of the {@link InfluxDBService}.
	 */
	@Autowired
	InfluxDBService influxDb;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof CpuInformationData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		CpuInformationData data = (CpuInformationData) defaultData;

		Point point = Point.measurement("cpu_information").time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS).tag("platform_ident", String.valueOf(data.getPlatformIdent()))
				.addField("process_cpu_time", data.getProcessCpuTime()).addField("total_cpu_usage", data.getTotalCpuUsage()).build();

		influxDb.insert(point);
	}
}
