/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractForkStreamComponent<I> implements ISingleInputComponent<I> {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractForkStreamComponent.class);

	private final ISingleInputComponent<I> nextComponentOne;

	private final ISingleInputComponent<I> nextComponentTwo;

	private final IDoubleInputComponent<I> nextComponent;

	/**
	 * @param nextComponent
	 */
	public AbstractForkStreamComponent(IDoubleInputComponent<I> nextComponent) {
		this.nextComponent = nextComponent;
		nextComponentOne = null;
		nextComponentTwo = null;
	}

	/**
	 * @param nextComponent
	 */
	public AbstractForkStreamComponent(ISingleInputComponent<I> nextComponentOne, ISingleInputComponent<I> nextComponentTwo) {
		this.nextComponentOne = nextComponentOne;
		this.nextComponentTwo = nextComponentTwo;
		nextComponent = null;
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
	public void process(I item) {
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

	protected abstract EFlowControl processImpl(I item);
}
