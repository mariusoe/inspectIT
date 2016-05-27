/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.i;

/**
 * @author Marius Oehler
 *
 */
public class ASingleOutputStream<I> {

	private final ISingleInputStream<I> nextStream;

	/**
	 *
	 */
	public ASingleOutputStream(ISingleInputStream<I> nextStream) {
		this.nextStream = nextStream;
	}

	protected void next(I item) {
		if (nextStream != null) {
			nextStream.process(item);
		}
	}

}
