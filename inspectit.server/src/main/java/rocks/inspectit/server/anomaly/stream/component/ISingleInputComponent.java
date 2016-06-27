/**
 *
 */
package rocks.inspectit.server.anomaly.stream.component;

import rocks.inspectit.server.anomaly.stream.object.StreamObject;

/**
 * @author Marius Oehler
 *
 */
public interface ISingleInputComponent<I> extends IStreamComponent<I> {

	void process(StreamObject<I> item);

	void print(String prefix, boolean isTail);
}
