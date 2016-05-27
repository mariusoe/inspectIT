/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractStreamProcessor<I> {

	private AbstractStreamProcessor<I> nextProcessor;

	/**
	 * Sets {@link #nextProcessor}.
	 *
	 * @param nextProcessor
	 *            New value for {@link #nextProcessor}
	 */
	public void setNextProcessor(AbstractStreamProcessor<I> nextProcessor) {
		this.nextProcessor = nextProcessor;
	}

	public void process(I item) {
		processImpl(item);

		if (nextProcessor != null) {
			nextProcessor.process(item);
		}
	}

	/**
	 * @param item
	 */
	protected abstract void processImpl(I item);

}
