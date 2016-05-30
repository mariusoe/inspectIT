/**
 *
 */
package rocks.inspectit.server.anomaly.stream.sink;

import com.lmax.disruptor.RingBuffer;

import rocks.inspectit.server.anomaly.stream.disruptor.events.InvocationSequenceEvent;
import rocks.inspectit.server.anomaly.stream.disruptor.translator.InvocationSequenceTranslator;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class InvocationSequenceSink implements IDataSink<InvocationSequenceData> {

	private final RingBuffer<InvocationSequenceEvent> ringBuffer;

	private final InvocationSequenceTranslator translator = new InvocationSequenceTranslator();

	/**
	 * @param ringBuffer
	 */
	public InvocationSequenceSink(RingBuffer<InvocationSequenceEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(InvocationSequenceData invocationSequence) {
		ringBuffer.publishEvent(translator, invocationSequence);
	}

}
