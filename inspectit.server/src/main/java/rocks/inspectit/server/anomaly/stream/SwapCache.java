/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marius Oehler
 *
 */
public class SwapCache {

	private InternalData activeData;

	private final InternalData dataOne;
	private final InternalData dataTwo;

	public SwapCache(int size) {
		dataOne = new InternalData(size);
		dataTwo = new InternalData(size);

		activeData = dataOne;
	}

	public void push(double item) {
		// tolerated data loss
		InternalData iData = activeData;
		iData.data[iData.index.getAndIncrement()] = item;
	}

	public void swap() {
		if (activeData == dataOne) {
			activeData = dataTwo;
		} else {
			activeData = dataOne;
		}
	}

	public InternalData getInactive() {
		if (activeData == dataOne) {
			return dataTwo;
		} else {
			return dataOne;
		}
	}

	public class InternalData {

		/**
		 *
		 */
		public InternalData(int size) {
			data = new double[size];
			index = new AtomicInteger(0);
		}

		double[] data;

		AtomicInteger index;

		public void reset() {
			index.set(0);
		}

		/**
		 * Gets {@link #data}.
		 *
		 * @return {@link #data}
		 */
		public double[] getData() {
			return data;
		}

		/**
		 * Gets {@link #index}.
		 *
		 * @return {@link #index}
		 */
		public AtomicInteger getIndex() {
			return index;
		}

	}
}
