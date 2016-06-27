/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import rocks.inspectit.server.anomaly.stream.SharedStreamProperties;
import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.object.InvocationStreamObject;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class TSDBWriterComponent extends AbstractSingleStreamComponent<InvocationSequenceData> {

	private final String type;

	/**
	 * @param nextComponent
	 * @param businessService2
	 * @param businessContextManagementService
	 */
	public TSDBWriterComponent(ISingleInputComponent<InvocationSequenceData> nextComponent, String type) {
		super(nextComponent);
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> streamObject) {
		InvocationStreamObject invocationStreamObject = (InvocationStreamObject) streamObject;

		Builder builder = Point.measurement("stream");
		builder.addField("duration", streamObject.getData().getDuration());
		builder.tag("businessTransaction", invocationStreamObject.getBusinessTransaction());
		builder.tag("type", type);

		SharedStreamProperties.getInfluxService().insert(builder.build());

		return EFlowControl.CONTINUE;
	}

}
