package info.novatec.inspectit.cmr.processor.influxdb;

import info.novatec.inspectit.cmr.influxdb.InfluxDbService;
import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.spring.logger.Log;

import java.util.concurrent.TimeUnit;

import org.hibernate.StatelessSession;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Marius Oehler
 *
 */
public class ThreadMetricProcessor extends AbstractCmrDataProcessor {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * Instance of the {@link InfluxDbService}.
	 */
	@Autowired
	InfluxDbService influxDb;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, StatelessSession session) {
		ThreadInformationData data = (ThreadInformationData) defaultData;

		Point point = Point.measurement("thread_information").time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS).tag("platform_ident", String.valueOf(data.getPlatformIdent()))
				.field("thread_count", data.getTotalThreadCount()).field("daemon_thread_count", data.getTotalDaemonThreadCount()).build();

		influxDb.write(point);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof ThreadInformationData;
	}
}
