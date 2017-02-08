package rocks.inspectit.shared.cs.anomaly.extractor.impl;

import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.anomaly.extractor.IValueExtractor;

/**
 * @author Marius Oehler
 *
 */
@Component
public class InvocationSequenceDurationExtractor implements IValueExtractor<InvocationSequenceData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getValueName() {
		return "Invocation Sequence Duration";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double extractValue(InvocationSequenceData input) {
		return input.getDuration();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getDataClass() {
		return InvocationSequenceData.class;
	}
}
