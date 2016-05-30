/**
 *
 */
package rocks.inspectit.server.anomaly.stream.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * @author Marius Oehler
 *
 */
public class InvocationSequenceEventFactory implements EventFactory<InvocationSequenceEvent> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InvocationSequenceEvent newInstance() {
		return new InvocationSequenceEvent();
	}

}
