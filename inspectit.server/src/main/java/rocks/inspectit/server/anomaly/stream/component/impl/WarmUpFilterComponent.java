/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component.impl;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.anomaly.stream.component.AbstractSingleStreamComponent;
import rocks.inspectit.server.anomaly.stream.component.EFlowControl;
import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
public class WarmUpFilterComponent extends AbstractSingleStreamComponent<InvocationSequenceData> {

	/**
	 * Logger for the class.
	 */
	@Log
	private Logger log;

	private final AtomicLong counter = new AtomicLong(0L);

	@Value("${anomaly.settings.warmupCount}")
	private long warmupCount;

	private boolean isWarmingUp = true;

	/**
	 * Sets {@link #warmupCount}.
	 *
	 * @param warmupCount
	 *            New value for {@link #warmupCount}
	 */
	public void setWarmupCount(long warmupCount) {
		this.warmupCount = warmupCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EFlowControl processImpl(StreamObject<InvocationSequenceData> item) {
		if (isWarmingUp) {
			if (counter.incrementAndGet() > warmupCount) {
				isWarmingUp = false;
				if (log.isInfoEnabled()) {
					log.info("Warm-up phase has finished..");
				}
			}
			return EFlowControl.BREAK;
		} else {
			return EFlowControl.CONTINUE;
		}
	}

}
