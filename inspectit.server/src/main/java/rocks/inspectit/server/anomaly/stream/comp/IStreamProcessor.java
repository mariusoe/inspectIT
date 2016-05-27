/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp;

/**
 * @author Marius Oehler
 *
 */
public abstract class IStreamProcessor<I> {

	/**
	 *
	 */
	public IStreamProcessor(IStreamProcessor<I> nextProcessorA, IStreamProcessor<I> nextProcessorB) {
	}

	public void process(I item) {
		processImpl(item);
	}

	public abstract boolean processImpl(I item);

}
