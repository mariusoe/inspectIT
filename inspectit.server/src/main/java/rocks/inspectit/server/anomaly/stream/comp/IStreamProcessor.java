/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp;

/**
 * @author Marius Oehler
 *
 */
public interface IStreamProcessor<I> {

	void process(I item);

}
