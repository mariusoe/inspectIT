/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.i;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractSingleStream<I, O> extends ASingleOutputStream<O> implements ISingleInputStream<I> {

	/**
	 * @param nextStream
	 */
	public AbstractSingleStream(ISingleInputStream<O> nextStream) {
		super(nextStream);
	}

}
