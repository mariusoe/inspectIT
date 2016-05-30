/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractSingleStreamComponent<I> implements ISingleInputComponent<I> {

	private final ISingleInputComponent<I> nextComponent;

	/**
	 * @param nextComponent
	 */
	public AbstractSingleStreamComponent(ISingleInputComponent<I> nextComponent) {
		this.nextComponent = nextComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(I item) {
		EFlowControl flowControl = processImpl(item);

		if (flowControl != EFlowControl.BREAK && nextComponent != null) {
			nextComponent.process(item);
		}
	}

	protected abstract EFlowControl processImpl(I item);
}
