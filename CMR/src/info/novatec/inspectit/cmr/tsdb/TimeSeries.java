/**
 *
 */
package info.novatec.inspectit.cmr.tsdb;

import java.util.List;

/**
 * This generic representation of a time series.
 *
 * @author Marius Oehler
 *
 */
public class TimeSeries {

	/**
	 * The names of the columns.
	 */
	private final List<String> columns;

	/**
	 * The data.
	 */
	private final List<DataPoint> data;

	/**
	 * Constructor.
	 *
	 * @param columns
	 *            the columns
	 * @param data
	 *            the data
	 */
	TimeSeries(List<String> columns, List<DataPoint> data) {
		this.columns = columns;
		this.data = data;
	}

	/**
	 * f Gets {@link #data}.
	 *
	 * @return {@link #data}
	 */
	public List<DataPoint> getData() {
		return data;
	}

	/**
	 * Gets {@link #columns}.
	 *
	 * @return {@link #columns}
	 */
	public List<String> getColumns() {
		return columns;
	}

	/**
	 * Returns whether the time series contains any data.
	 *
	 * @return true if the series contains any data
	 */
	public boolean hasData() {
		return data != null && !data.isEmpty();
	}

	/**
	 * Returns the first {@link DataPoint} of this time series.
	 *
	 * @return the first {@link DataPoint}
	 */
	public DataPoint getFirst() {
		if (data.isEmpty()) {
			return null;
		} else {
			return data.get(0);
		}
	}
}
