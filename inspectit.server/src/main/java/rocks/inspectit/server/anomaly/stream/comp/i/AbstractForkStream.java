/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.i;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractForkStream<I, O> extends ADoubleOutputStream<O> implements ISingleInputStream<I> {

	/**
	 * @param nextStream
	 */
	public AbstractForkStream(IDoubleInputStream<O> nextStream) {
		super(nextStream);
	}

}
