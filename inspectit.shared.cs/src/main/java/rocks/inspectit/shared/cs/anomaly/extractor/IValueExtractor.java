package rocks.inspectit.shared.cs.anomaly.extractor;

import java.io.Serializable;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * @author Marius Oehler
 *
 */
public interface IValueExtractor<E extends DefaultData> extends Serializable {

	public String getValueName();

	public double extractValue(E input);

	public Class<?> getDataClass();
}
