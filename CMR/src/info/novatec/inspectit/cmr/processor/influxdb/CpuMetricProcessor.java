package info.novatec.inspectit.cmr.processor.influxdb;

import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.cmr.tsdb.InfluxDBService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.CpuInformationData;
import info.novatec.inspectit.spring.logger.Log;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

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
				.field("process_cpu_time", data.getProcessCpuTime()).field("total_cpu_usage", data.getTotalCpuUsage()).build();

		influxDb.insert(point);
	}

}
