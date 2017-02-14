package rocks.inspectit.server.anomaly.baseline;

import rocks.inspectit.server.anomaly.definition.AbstractDefinitionAware;
import rocks.inspectit.server.anomaly.processing.AnomalyProcessingContext;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractBaseline extends AbstractDefinitionAware<BaselineDefinition> {

	private E definition;

	/**
	 * Gets {@link #definition}.
	 *
	 * @return {@link #definition}
	 */
	public E getBaselineDefinition() {
		return this.definition;
	}

	/**
	 * Sets {@link #definition}.
	 *
	 * @param definition
	 *            New value for {@link #definition}
	 */
	public void setBaselineDefinition(E definition) {
		this.definition = definition;

		onDefinitionUpdate();
	}

	@Override
	protected abstract void onDefinitionUpdate();

	public abstract void process(AnomalyProcessingContext context, long time);

	public abstract double getBaseline();
}
