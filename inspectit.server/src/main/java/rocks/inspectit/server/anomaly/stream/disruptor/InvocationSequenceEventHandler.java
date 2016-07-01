/**
 *
 */
package rocks.inspectit.server.anomaly.stream.disruptor;

import com.lmax.disruptor.EventHandler;

import rocks.inspectit.server.anomaly.stream.component.ISingleInputComponent;
import rocks.inspectit.server.anomaly.stream.disruptor.events.InvocationSequenceEvent;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class InvocationSequenceEventHandler implements EventHandler<InvocationSequenceEvent> {

	private final ISingleInputComponent<InvocationSequenceData> streamEntryComponent;

	/**
	 * @param streamEntryComponent
	 */
	public InvocationSequenceEventHandler(ISingleInputComponent<InvocationSequenceData> streamEntryComponent) {
		this.streamEntryComponent = streamEntryComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEvent(InvocationSequenceEvent event, long sequence, boolean endOfBatch) throws Exception {
		StreamObject<InvocationSequenceData> streamObject = new StreamObject<>(event.getData());
		streamEntryComponent.process(streamObject);
	}

}
