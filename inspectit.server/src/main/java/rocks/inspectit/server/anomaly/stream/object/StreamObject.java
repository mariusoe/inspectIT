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

	private StreamContext context;

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

	/**
	 * Gets {@link #context}.
	 *
	 * @return {@link #context}
	 */
	public StreamContext getContext() {
		return context;
	}

	/**
	 * Sets {@link #context}.
	 *
	 * @param context
	 *            New value for {@link #context}
	 */
	public void setContext(StreamContext context) {
		this.context = context;
	}

}
