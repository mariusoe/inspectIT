package rocks.inspectit.server.anomaly;

import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.data.AnalyzableInvocationSequenceData;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class AnalyzableDataFactory {

	public boolean isAnalyzable(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

	public AnalyzableData<?> createAnalyzable(DefaultData defaultData) {
		if (defaultData instanceof InvocationSequenceData) {
			return new AnalyzableInvocationSequenceData((InvocationSequenceData) defaultData);
		}

		return null;
	}
}
