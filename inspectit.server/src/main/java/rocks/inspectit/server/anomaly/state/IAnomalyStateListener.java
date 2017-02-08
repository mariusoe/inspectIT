package rocks.inspectit.server.anomaly.state;

import rocks.inspectit.server.anomaly.processing.ProcessingUnitGroupContext;

/**
 * @author Marius Oehler
 *
 */
public interface IAnomalyStateListener {

	void onStart(ProcessingUnitGroupContext groupContext);

	void onUpgrade(ProcessingUnitGroupContext groupContext);

	void onDowngrade(ProcessingUnitGroupContext groupContext);

	void onEnd(ProcessingUnitGroupContext groupContext);

}
