/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.i;

/**
 * @author Marius Oehler
 *
 */
public interface IDoubleInputStream<I> extends IStream<I> {

	void processA(I item);

	void processB(I item);

}
