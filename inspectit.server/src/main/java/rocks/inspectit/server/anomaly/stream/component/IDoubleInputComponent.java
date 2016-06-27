/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component;

import rocks.inspectit.server.anomaly.stream.object.StreamObject;

/**
 * @author Marius Oehler
 *
 */
public interface IDoubleInputComponent<I> extends IStreamComponent<I> {

	void processOne(StreamObject<I> item);

	void processTwo(StreamObject<I> item);

	void print(String prefix, boolean isTail, boolean doubleInput);
}
