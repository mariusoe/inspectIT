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
public abstract class AbstractDoubleStreamComponent<I> implements IDoubleInputComponent<I> {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractDoubleStreamComponent.class);

	private ISingleInputComponent<I> nextComponentOne;

	private ISingleInputComponent<I> nextComponentTwo;

	/**
	 *
	 */
	public AbstractDoubleStreamComponent() {
	}

	/**
	 * Sets {@link #nextComponentOne}.
	 *
	 * @param nextComponentOne
	 *            New value for {@link #nextComponentOne}
	 */
	public void setNextComponentOne(ISingleInputComponent<I> nextComponentOne) {
		this.nextComponentOne = nextComponentOne;
	}

	/**
	 * Sets {@link #nextComponentTwo}.
	 *
	 * @param nextComponentTwo
	 *            New value for {@link #nextComponentTwo}
	 */
	public void setNextComponentTwo(ISingleInputComponent<I> nextComponentTwo) {
		this.nextComponentTwo = nextComponentTwo;
	}

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
	public void processOne(StreamObject<I> item) {
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
	public void processTwo(StreamObject<I> item) {
		EFlowControl flowControl = processOneTwo(item);

		if (flowControl == EFlowControl.CONTINUE_ONE) {
			nextComponentOne.process(item);
		} else if (flowControl == EFlowControl.CONTINUE_TWO) {
			nextComponentTwo.process(item);
		}
	}

	protected abstract EFlowControl processOneImpl(StreamObject<I> item);

	protected abstract EFlowControl processOneTwo(StreamObject<I> item);
}
