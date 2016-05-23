package rocks.inspectit.server.processor.impl.tsdb;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.spring.logger.Log;

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
}
