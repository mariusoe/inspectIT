package info.novatec.inspectit.cmr.processor.influxdb;

import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.cmr.tsdb.InfluxDBService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
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
public class ExceptionProcessor extends AbstractCmrDataProcessor {

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
		return defaultData instanceof ExceptionSensorData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		ExceptionSensorData data = (ExceptionSensorData) defaultData;

		Point point = Point.measurement("exceptions").time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS).tag("platform_ident", String.valueOf(data.getPlatformIdent()))
				.addField("error_message", data.getErrorMessage()).addField("throwable_type", data.getThrowableType()).build();

		influxDb.insert(point);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isMonitoringProcessor() {
		return true;
	}
}
