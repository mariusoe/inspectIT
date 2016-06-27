/**
 *
 */
package rocks.inspectit.server.anomaly.stream.object;

/**
 * @author Marius Oehler
 *
 */
public class StreamObject<I> {

	private final I data;

	/**
	 * @param data
	 */
	public StreamObject(I data) {
		this.data = data;
	}

	/**
	 * Gets {@link #data}.
	 *
	 * @return {@link #data}
	 */
	public I getData() {
		return data;
	}
}
