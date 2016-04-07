/**
 *
 */
package info.novatec.inspectit.cmr.tsdb;

import java.util.Collections;
import java.util.List;

/**
 * @author Marius Oehler
 *
 */
public class DataPoint {

	/**
	 * The data.
	 */
	private final List<Object> data;

	/**
	 * Constructor.
	 *
	 * @param data
	 *            the data
	 */
	DataPoint(List<Object> data) {
		this.data = Collections.unmodifiableList(data);
	}

	/**
	 * Gets {@link #data}.
	 *
	 * @return {@link #data}
	 */
	public List<Object> getData() {
		return data;
	}

	/**
	 * Returns the data of the {@link #data} list with the given index.
	 *
	 * @param index
	 *            index of the data
	 * @return the data object
	 */
	public Object get(int index) {
		return data.get(index);
	}
}
