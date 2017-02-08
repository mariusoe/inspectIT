package rocks.inspectit.shared.cs.anomaly.classification.classifier;

import rocks.inspectit.shared.cs.anomaly.classification.HealthState;
import rocks.inspectit.shared.cs.anomaly.classification.context.AbstractClassificationContext;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractClassifier<E extends AbstractClassificationContext> {

	private E context;

	/**
	 * Gets {@link #context}.
	 *
	 * @return {@link #context}
	 */
	protected E getContext() {
		return this.context;
	}

	/**
	 * Sets {@link #context}.
	 *
	 * @param context
	 *            New value for {@link #context}
	 */
	public void setContext(E context) {
		this.context = context;
	}

	public abstract HealthState classify(double value);
}
