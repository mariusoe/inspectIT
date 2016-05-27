/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.impl;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.server.anomaly.stream.comp.AbstractResultProcessor;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class TSDBWriter extends AbstractResultProcessor<InvocationSequenceData> {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(TSDBWriter.class);

	private final InfluxDBService influx;

	/**
	 * @param influx
	 */
	public TSDBWriter(InfluxDBService influx) {
		this.influx = influx;
	}

	/**
	 * Instance of the {@link InfluxDBService}.
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void problemImpl(InvocationSequenceData item) {
		log.info("problem: {}", item.getDuration());

		influx.insert(Point.measurement("stream").addField("duration", item.getDuration()).tag("type", "problem").build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void okayImpl(InvocationSequenceData item) {
		log.info("okay: {}", item.getDuration());

		influx.insert(Point.measurement("stream").addField("duration", item.getDuration()).tag("type", "normal").build());
	}
}
