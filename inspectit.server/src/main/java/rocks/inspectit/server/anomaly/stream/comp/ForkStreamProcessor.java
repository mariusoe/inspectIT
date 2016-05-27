/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp;

/**
 * @author Marius Oehler
 *
 */
public abstract class ForkStreamProcessor<I> implements IStreamProcessor<I> {

	private final IStreamProcessor<I> nextProcessorA;
	private final IStreamProcessor<I> nextProcessorB;

	/**
	 * @param nextProcessor
	 */
	public ForkStreamProcessor(IStreamProcessor<I> nextProcessorA, IStreamProcessor<I> nextProcessorB) {
		this.nextProcessorA = nextProcessorA;
		this.nextProcessorB = nextProcessorB;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(I item) {
		boolean result = processImpl(item);

		if (result && nextProcessorA != null) {
			nextProcessorA.process(item);
		} else if (!result && nextProcessorB != null) {
			nextProcessorB.process(item);
		}
	}

	protected abstract boolean processImpl(I item);
}
