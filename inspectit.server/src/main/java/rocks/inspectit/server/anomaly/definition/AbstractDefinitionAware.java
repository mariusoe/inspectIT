package rocks.inspectit.server.anomaly.definition;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractDefinitionAware<E extends AbstractDefinition> {

	protected E definition;

	protected abstract void onDefinitionUpdate();

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
		onDefinitionUpdate();
	}

}
