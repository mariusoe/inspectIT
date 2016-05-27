/**
 *
 */
package rocks.inspectit.server.anomaly.stream.transfer;

/**
 * @author Marius Oehler
 *
 */
public class TransferFunction {

	private TransferFunction() {
	}

	public final static ITransferFunction QUADRATIC = new ITransferFunction() {
		@Override
		public double transfer(double input) {
			return input * input;
		}
	};

}
