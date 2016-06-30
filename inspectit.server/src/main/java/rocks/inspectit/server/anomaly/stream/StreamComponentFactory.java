/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

import rocks.inspectit.server.anomaly.stream.component.impl.WarmUpFilterComponent;

/**
 * @author Marius Oehler
 *
 */
public abstract class StreamComponentFactory {

	public abstract WarmUpFilterComponent createWarmUpFilter();

}
