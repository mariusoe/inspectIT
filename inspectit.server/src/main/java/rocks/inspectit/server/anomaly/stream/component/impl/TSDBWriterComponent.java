/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class TSDBWriterComponent extends AbstractSingleStreamComponent<InvocationSequenceData> {

	@Autowired
	private InfluxDBService influxService;

	private String dataTypeTag;

	/**
	 * Sets {@link #dataTypeTag}.
	 *
	 * @param dataTypeTag
	 *            New value for {@link #dataTypeTag}
	 */
	public void setDataTypeTag(String dataTypeTag) {
		this.dataTypeTag = dataTypeTag;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> streamObject) {
		Builder builder = Point.measurement("stream");
		builder.addField("duration", streamObject.getData().getDuration());
		builder.tag("businessTransaction", streamObject.getContext().getBusinessTransaction());
		builder.tag("type", dataTypeTag);

		influxService.insert(builder.build());

		return EFlowControl.CONTINUE;
	}

}
