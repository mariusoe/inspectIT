/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import org.influxdb.dto.Point;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class TSDBWriterComponent extends AbstractSingleStreamComponent<InvocationSequenceData> {

	private final String type;

	/**
	 * @param nextComponent
	 */
	public TSDBWriterComponent(ISingleInputComponent<InvocationSequenceData> nextComponent, String type) {
		super(nextComponent);
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(InvocationSequenceData item) {
		SharedStreamProperties.getInfluxService().insert(Point.measurement("stream").addField("duration", item.getDuration()).tag("type", type).build());

		return EFlowControl.CONTINUE;
	}

}
