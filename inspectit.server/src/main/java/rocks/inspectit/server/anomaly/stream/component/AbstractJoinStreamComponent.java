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
public abstract class AbstractJoinStreamComponent<I> implements IDoubleInputComponent<I> {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(AbstractJoinStreamComponent.class);

	private final ISingleInputComponent<I> nextComponent;

	/**
	 * @param nextComponent
	 */
	public AbstractJoinStreamComponent(ISingleInputComponent<I> nextComponent) {
		this.nextComponent = nextComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void print(String prefix, boolean isTail, boolean doubleInput) {
		String line = doubleInput ? "==" : "──";
		log.info(prefix + (isTail ? "└" + line + " " : "├" + line + " ") + getClass().getSimpleName());
		if (nextComponent != null) {
			nextComponent.print(prefix + (isTail ? "    " : "│   "), true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processOne(I item) {
		EFlowControl flowControl = processOneImpl(item);

		if (flowControl != EFlowControl.BREAK) {
			nextComponent.process(item);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processTwo(I item) {
		EFlowControl flowControl = processTwoImpl(item);

		if (flowControl != EFlowControl.BREAK && nextComponent != null) {
			nextComponent.process(item);
		}
	}

	protected abstract EFlowControl processOneImpl(I item);

	protected abstract EFlowControl processTwoImpl(I item);
}
