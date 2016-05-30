/**
 *
 */
package rocks.inspectit.server.anomaly.stream.disruptor;

import com.lmax.disruptor.EventHandler;

/**
 * @author Marius Oehler
 *
 */
public class InvocationSequenceEventHandler implements EventHandler<InvocationSequenceEvent> {

	long i = 0L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEvent(InvocationSequenceEvent event, long sequence, boolean endOfBatch) throws Exception {
		System.out.println(event.getData() + " " + i++);
	}

}
