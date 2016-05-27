/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp;

/**
 * @author Marius Oehler
 *
 */
public abstract class OneWayStreamProcessor<I> implements IStreamProcessor<I> {

	private final IStreamProcessor<I> nextProcessor;

	/**
	 * @param nextProcessor
	 */
	public OneWayStreamProcessor(IStreamProcessor<I> nextProcessor) {
		this.nextProcessor = nextProcessor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(I item) {
		processImpl(item);

		if (nextProcessor != null) {
			nextProcessor.process(item);
		}
	}

	protected abstract void processImpl(I item);
}
