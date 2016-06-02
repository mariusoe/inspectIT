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
public abstract class AbstractDoubleStreamComponent<I> implements IDoubleInputComponent<I> {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractDoubleStreamComponent.class);

	private final ISingleInputComponent<I> nextComponentOne;

	private final ISingleInputComponent<I> nextComponentTwo;

	/**
	 * @param nextComponent
	 */
	public AbstractDoubleStreamComponent(ISingleInputComponent<I> nextComponentOne, ISingleInputComponent<I> nextComponentTwo) {
		this.nextComponentOne = nextComponentOne;
		this.nextComponentTwo = nextComponentTwo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void print(String prefix, boolean isTail, boolean doubleInput) {
		String line = doubleInput ? "==" : "──";
		log.info(prefix + (isTail ? "└" + line + " " : "├" + line + " ") + getClass().getSimpleName());
		if (nextComponentOne != null) {
			nextComponentOne.print(prefix + (isTail ? "    " : "│   "), false);
		}
		if (nextComponentTwo != null) {
			nextComponentTwo.print(prefix + (isTail ? "    " : "│   "), true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processOne(I item) {
		EFlowControl flowControl = processOneImpl(item);

		if (flowControl == EFlowControl.CONTINUE_ONE && nextComponentOne != null) {
			nextComponentOne.process(item);
		} else if (flowControl == EFlowControl.CONTINUE_TWO && nextComponentTwo != null) {
			nextComponentTwo.process(item);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processTwo(I item) {
		EFlowControl flowControl = processOneTwo(item);

		if (flowControl == EFlowControl.CONTINUE_ONE) {
			nextComponentOne.process(item);
		} else if (flowControl == EFlowControl.CONTINUE_TWO) {
			nextComponentTwo.process(item);
		}
	}

	protected abstract EFlowControl processOneImpl(I item);

	protected abstract EFlowControl processOneTwo(I item);
}
