/**
 *
 */
package rocks.inspectit.server.anomaly.stream.disruptor.translator;

import com.lmax.disruptor.EventTranslatorOneArg;

import rocks.inspectit.server.anomaly.stream.disruptor.events.InvocationSequenceEvent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class InvocationSequenceTranslator implements EventTranslatorOneArg<InvocationSequenceEvent, InvocationSequenceData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void translateTo(InvocationSequenceEvent event, long sequence, InvocationSequenceData data) {
		event.setData(data);
	}

}
