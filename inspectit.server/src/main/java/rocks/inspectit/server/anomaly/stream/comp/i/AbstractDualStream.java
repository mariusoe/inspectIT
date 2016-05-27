/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.i;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractDualStream<I, O> extends ADoubleOutputStream<O> implements IDoubleInputStream<I> {

	/**
	 * @param nextStream
	 */
	public AbstractDualStream(IDoubleInputStream<O> nextStream) {
		super(nextStream);
	}

}
