/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.i.impl;

import org.influxdb.dto.Point;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.comp.i.AbstractDualStream;
import rocks.inspectit.server.anomaly.stream.comp.i.IDoubleInputStream;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class TimeSeriesDatabaseWriter extends AbstractDualStream<InvocationSequenceData, InvocationSequenceData> {

	/**
	 * @param nextStream
	 */
	public TimeSeriesDatabaseWriter(IDoubleInputStream<InvocationSequenceData> nextStream) {
		super(nextStream);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processA(InvocationSequenceData item) {
		SharedStreamProperties.getInfluxService().insert(Point.measurement("stream").addField("duration", item.getDuration()).tag("type", "normal").build());

		nextA(item);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processB(InvocationSequenceData item) {
		SharedStreamProperties.getInfluxService().insert(Point.measurement("stream").addField("duration", item.getDuration()).tag("type", "problem").build());

		nextB(item);
	}

}
