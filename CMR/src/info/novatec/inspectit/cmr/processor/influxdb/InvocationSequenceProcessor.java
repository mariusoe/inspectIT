/**
 *
 */
package info.novatec.inspectit.cmr.processor.influxdb;

import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.cmr.tsdb.InfluxDBService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.spring.logger.Log;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Marius Oehler
 *
 */
public class InvocationSequenceProcessor extends AbstractCmrDataProcessor {

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
		return defaultData instanceof InvocationSequenceData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		InvocationSequenceData data = (InvocationSequenceData) defaultData;

		Builder builder = Point.measurement("invocation_sequences").time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS).tag("platform_ident", String.valueOf(data.getPlatformIdent()))
				.addField("duration", data.getDuration()).addField("child_count", data.getChildCount());

		if (data.getTimerData() != null && data.getTimerData() instanceof HttpTimerData) {
			String useCase = ((HttpTimerData) data.getTimerData()).getHttpInfo().getInspectItTaggingHeaderValue();
			if (useCase != null) {
				builder.tag("useCase", useCase);
			}
		}

		influxDb.insert(builder.build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isMonitoringProcessor() {
		return true;
	}
}
