/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.server.anomaly.stream.object.StreamObject;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractSingleStreamComponent<I> implements ISingleInputComponent<I> {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractSingleStreamComponent.class);

	private ISingleInputComponent<I> nextComponent;

	/**
	 *
	 */
	public AbstractSingleStreamComponent() {
	}

	/**
	 *
	 */
	public AbstractSingleStreamComponent(ISingleInputComponent<I> next) {
		nextComponent = next;
	}

	/**
	 * Sets {@link #nextComponent}.
	 *
	 * @param nextComponent
	 *            New value for {@link #nextComponent}
	 */
	public void setNextComponent(ISingleInputComponent<I> nextComponent) {
		this.nextComponent = nextComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void print(String prefix, boolean isTail) {
		log.info(prefix + (isTail ? "└── " : "├── ") + getClass().getSimpleName());
		if (nextComponent != null) {
			nextComponent.print(prefix + (isTail ? "    " : "│   "), true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(StreamObject<I> item) {
		EFlowControl flowControl = processImpl(item);

		if (flowControl != EFlowControl.BREAK && nextComponent != null) {
			nextComponent.process(item);
		}
	}

	protected abstract EFlowControl processImpl(StreamObject<I> item);
}
