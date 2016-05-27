/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.i;

/**
 * @author Marius Oehler
 *
 */
public class ADoubleOutputStream<I> {

	private final IDoubleInputStream<I> nextStream;

	/**
	 *
	 */
	public ADoubleOutputStream(IDoubleInputStream<I> nextStream) {
		this.nextStream = nextStream;
	}

	protected void nextA(I item) {
		if (nextStream != null) {
			nextStream.processA(item);
		}
	}

	protected void nextB(I item) {
		if (nextStream != null) {
			nextStream.processB(item);
		}
	}

}
