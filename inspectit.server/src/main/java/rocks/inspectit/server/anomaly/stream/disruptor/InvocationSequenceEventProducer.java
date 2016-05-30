/**
 *
 */
package rocks.inspectit.server.anomaly.stream.disruptor;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class InvocationSequenceEventProducer {

	private final RingBuffer<InvocationSequenceEvent> ringBuffer;

	/**
	 * @param ringBuffer
	 */
	public InvocationSequenceEventProducer(RingBuffer<InvocationSequenceEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	private static final EventTranslatorOneArg<InvocationSequenceEvent, InvocationSequenceData> TRANSLATOR = new EventTranslatorOneArg<InvocationSequenceEvent, InvocationSequenceData>() {
		@Override
		public void translateTo(InvocationSequenceEvent event, long sequence, InvocationSequenceData invocationSequence) {
			event.setData(invocationSequence);
		}
	};

	public void onData(InvocationSequenceData invocationSequence) {
		ringBuffer.publishEvent(TRANSLATOR, invocationSequence);
	}

}
