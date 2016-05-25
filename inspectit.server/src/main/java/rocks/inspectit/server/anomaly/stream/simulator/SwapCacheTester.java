/**
 *
 */
package rocks.inspectit.server.anomaly.stream.simulator;

import rocks.inspectit.server.anomaly.stream.SwapCache;
import rocks.inspectit.server.anomaly.stream.SwapCache.InternalData;

/**
 * @author Marius Oehler
 *
 */
public class SwapCacheTester extends Thread {

	static SwapCache cache = new SwapCache(10000);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// push data
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					cache.push(Math.random() * 100D);

					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		new SwapCacheTester().start();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		while (true) {
			try {
				System.out.println("Run");

				cache.swap();

				InternalData data = cache.getInactive();

				double mean = 0;
				for (int i = 0; i < data.getIndex().get(); i++) {
					mean += data.getData()[i];
				}
				mean /= data.getIndex().get();

				System.out.println(mean);

				data.reset();

				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
