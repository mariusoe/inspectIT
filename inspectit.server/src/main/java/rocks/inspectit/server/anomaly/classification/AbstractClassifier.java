package rocks.inspectit.server.anomaly.classification;

import rocks.inspectit.server.anomaly.processing.AnomalyProcessingContext;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractClassifier<E extends IClassifierDefinition> {

	private E definition;

	/**
	 * Gets {@link #definition}.
	 *
	 * @return {@link #definition}
	 */
	public E getDefinition() {
		return this.definition;
	}

	/**
	 * Sets {@link #definition}.
	 *
	 * @param definition
	 *            New value for {@link #definition}
	 */
	public void setDefinition(E definition) {
		this.definition = definition;
	}

	public abstract void process(AnomalyProcessingContext context, long time);
}
