/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractDoubleStreamComponent<I> implements IDoubleInputComponent<I> {

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
