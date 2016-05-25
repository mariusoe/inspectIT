/**
 *
 */
package rocks.inspectit.server.anomaly.stream.comp.impl;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.server.anomaly.stream.SwapCache;
import rocks.inspectit.server.anomaly.stream.SwapCache.InternalData;
import rocks.inspectit.server.anomaly.stream.comp.IResultProcessor;
import rocks.inspectit.server.anomaly.stream.comp.IStreamProcessor;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class BaselineStreamProcessor implements IStreamProcessor<InvocationSequenceData> {

	/**
	 * Logger for the class.
	 */
	private final Logger log = LoggerFactory.getLogger(BaselineStreamProcessor.class);

	/**
	 * The result processor.
	 */
	IResultProcessor<InvocationSequenceData> resultProcessor;

	private final SwapCache swapCache;

	private Double baseline = null;

	private final BaselineUpdater baselineUpdater;

	/**
	 * @param executorService
	 *
	 */
	public BaselineStreamProcessor(int cacheSize, IResultProcessor<InvocationSequenceData> resultProcessor, ScheduledExecutorService executorService) {
		this.resultProcessor = resultProcessor;
		swapCache = new SwapCache(cacheSize);

		baselineUpdater = new BaselineUpdater();
		executorService.scheduleAtFixedRate(baselineUpdater, 5000, 5000, TimeUnit.MILLISECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(InvocationSequenceData item) {
		// store duration
		swapCache.push(item.getDuration());

		if (item.getDuration() > 60) {
			resultProcessor.problem(item);
		} else {
			resultProcessor.okay(item);
		}
	}

	/**
	 * Gets {@link #swapCache}.
	 *
	 * @return {@link #swapCache}
	 */
	public SwapCache getSwapCache() {
		return swapCache;
	}

	class BaselineUpdater implements Runnable {

		/**
		 * Logger for the class.
		 */
		private final Logger log = LoggerFactory.getLogger(BaselineUpdater.class);

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (log.isDebugEnabled()) {
				log.debug("update baseline..");
			}

			// swap cache
			swapCache.swap();

			InternalData cacheData = swapCache.getInactive();

			// calculate new baseline
			int currentIndex = cacheData.getIndex().get();
			if (currentIndex > 0) {
				double newBaseline = 0;

				for (int i = 0; i < currentIndex; i++) {
					newBaseline += cacheData.getData()[i];
				}
				newBaseline /= currentIndex;

				System.out.println(newBaseline);

				// set new baseline
				baseline = newBaseline;
			} else {
				if (log.isDebugEnabled()) {
					log.debug("no data to calculate new baseline");
				}
			}

			// reset old cache data
			cacheData.reset();
		}

	}
}
