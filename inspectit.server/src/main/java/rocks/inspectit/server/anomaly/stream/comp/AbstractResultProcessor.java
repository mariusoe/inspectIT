/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractResultProcessor<I> {

	private AbstractResultProcessor<I> nextProcessor;

	/**
	 * Sets {@link #nextProcessor}.
	 *
	 * @param nextProcessor
	 *            New value for {@link #nextProcessor}
	 */
	public void setNextProcessor(AbstractResultProcessor<I> nextProcessor) {
		this.nextProcessor = nextProcessor;
	}

	public void problem(I item) {
		problemImpl(item);

		if (nextProcessor != null) {
			nextProcessor.problem(item);
		}
	}

	public void okay(I item) {
		okayImpl(item);

		if (nextProcessor != null) {
			nextProcessor.okay(item);
		}
	}

	protected abstract void problemImpl(I item);

	protected abstract void okayImpl(I item);

}
