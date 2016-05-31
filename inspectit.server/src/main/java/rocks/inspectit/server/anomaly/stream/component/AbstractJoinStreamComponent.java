/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractJoinStreamComponent<I> implements IDoubleInputComponent<I> {

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
