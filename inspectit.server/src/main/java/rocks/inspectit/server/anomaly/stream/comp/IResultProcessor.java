/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp;

/**
 * @author Marius Oehler
 *
 */
public interface IResultProcessor<I> {

	void problem(I item);

	void okay(I item);

}
