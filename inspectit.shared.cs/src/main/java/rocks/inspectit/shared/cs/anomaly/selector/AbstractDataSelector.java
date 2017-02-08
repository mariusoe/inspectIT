package rocks.inspectit.shared.cs.anomaly.selector;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractDataSelector<M extends DefaultData, E extends IDataSelectorConfiguration> {

	private E configuration;

	/**
	 * Sets {@link #configuration}.
	 *
	 * @param configuration
	 *            New value for {@link #configuration}
	 */
	public void setConfiguration(E configuration) {
		this.configuration = configuration;
	}

	protected E getConfiguration() {
		return configuration;
	}

	public abstract boolean select(M defaultData);
}
