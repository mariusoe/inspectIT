/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component;

/**
 * @author Marius Oehler
 *
 */
public interface ISingleInputComponent<I> extends IStreamComponent<I> {

	void process(I item);

	void print(String prefix, boolean isTail);
}
