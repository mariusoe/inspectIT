/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component;

/**
 * @author Marius Oehler
 *
 */
public interface IDoubleInputComponent<I> extends IStreamComponent<I> {

	void processOne(I item);

	void processTwo(I item);

	void print(String prefix, boolean isTail, boolean doubleInput);
}
