/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.i;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractJoinStream<I, O> extends ADoubleOutputStream<O> implements IDoubleInputStream<I> {

	/**
	 * @param nextStream
	 */
	public AbstractJoinStream(IDoubleInputStream<O> nextStream) {
		super(nextStream);
	}

}
