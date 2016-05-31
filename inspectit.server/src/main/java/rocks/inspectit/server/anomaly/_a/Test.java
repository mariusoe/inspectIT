/**
 *
 */
package rocks.inspectit.server.anomaly._a;

/**
 * @author Marius Oehler
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int[] in = new int[] { 1, 2, 3, 4, 5, 4, 3, 2, 1, 2, 3, 4, 5, 4, 3, 2, 1, 2, 3, 4, 5, 4, 3, 2, 1, 2 };

		System.out.println(in.length);

		double[] forecast = HoltWintersTripleExponentialImpl.forecast(in, 0.5D, 0.5D, 0.5D, 8, 5, true);

	}

}
