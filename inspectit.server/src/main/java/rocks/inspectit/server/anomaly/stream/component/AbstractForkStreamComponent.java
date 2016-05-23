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
public abstract class AbstractForkStreamComponent<I> implements ISingleInputComponent<I> {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractForkStreamComponent.class);

	private ISingleInputComponent<I> nextComponentOne;

	private ISingleInputComponent<I> nextComponentTwo;

	private IDoubleInputComponent<I> nextComponent;

	/**
	 * Sets {@link #nextComponent}.
	 *
	 * @param nextComponent
	 *            New value for {@link #nextComponent}
	 */
	public void setNextComponent(IDoubleInputComponent<I> nextComponent) {
		this.nextComponentOne = null;
		this.nextComponentTwo = null;
		this.nextComponent = nextComponent;
	}

	/**
	 * Sets {@link #nextComponent}.
	 *
	 * @param nextComponent
	 *            New value for {@link #nextComponent}
	 */
	public void setNextComponent(ISingleInputComponent<I> nextComponentOne, ISingleInputComponent<I> nextComponentTwo) {
		this.nextComponent = null;
		this.nextComponentOne = nextComponentOne;
		this.nextComponentTwo = nextComponentTwo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void print(String prefix, boolean isTail) {
		log.info(prefix + (isTail ? "└── " : "├── ") + getClass().getSimpleName());
		if (nextComponent == null) {
			if (nextComponentOne != null) {
				nextComponentOne.print(prefix + (isTail ? "    " : "│   "), false);
			}
			if (nextComponentTwo != null) {
				nextComponentTwo.print(prefix + (isTail ? "    " : "│   "), true);
			}
		} else {
			nextComponent.print(prefix + (isTail ? "    " : "│   "), true, true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(StreamObject<I> item) {
		EFlowControl flowControl = processImpl(item);

		if (nextComponent != null) {
			if (flowControl == EFlowControl.CONTINUE_ONE) {
				nextComponent.processOne(item);
			} else if (flowControl == EFlowControl.CONTINUE_TWO) {
				nextComponent.processTwo(item);
			}
		} else {
			if (flowControl == EFlowControl.CONTINUE_ONE && nextComponentOne != null) {
				nextComponentOne.process(item);
			} else if (flowControl == EFlowControl.CONTINUE_TWO && nextComponentTwo != null) {
				nextComponentTwo.process(item);
			}
		}
	}

	protected abstract EFlowControl processImpl(StreamObject<I> item);
}
