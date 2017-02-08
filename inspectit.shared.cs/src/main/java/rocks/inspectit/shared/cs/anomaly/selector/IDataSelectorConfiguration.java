package rocks.inspectit.shared.cs.anomaly.selector;

import java.io.Serializable;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * @author Marius Oehler
 *
 */
public interface IDataSelectorConfiguration extends Serializable {

	Class<? extends AbstractDataSelector<? extends DefaultData, ?>> getDataSelectorClass();

}
