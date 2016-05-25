/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.stream.comp.IResultProcessor;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
@Component
public class ResultProcessor implements IResultProcessor<InvocationSequenceData> {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(ResultProcessor.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void problem(InvocationSequenceData item) {
		log.info("problem: {}", item.getDuration());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void okay(InvocationSequenceData item) {
		log.info("okay: {}", item.getDuration());
	}
}
