/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractForkStreamComponent<I> implements ISingleInputComponent<I> {

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
