/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.i;

/**
 * @author Marius Oehler
 *
 */
public interface ISingleInputStream<I> extends IStream<I> {

	void process(I item);
}
