/**
 *
 */
package rocks.inspectit.server.anomaly.stream.sink;

/**
 * @author Marius Oehler
 *
 */
public interface IDataSink<I> {

	void process(I item);

}
