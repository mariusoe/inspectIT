package info.novatec.inspectit.cmr.util;

import info.novatec.inspectit.spring.logger.Logger;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.springframework.stereotype.Component;

/**
 * This component can shutdown or restart the CMR.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class ShutdownService {

	/**
	 * Code that JVM will exit if restart is required. Note that this code must be max in 8bits size
	 * and changing it requires the change of the CMR startup script.
	 */
	private static final int RESTART_EXIT_CODE = 10;

	/**
	 * The logger of this class.
	 */
	@Logger
	Log log;

	/**
	 * Flag for shutdown initialization.
	 */
	private volatile boolean isShutdown = false;

	/**
	 * Executor service for executing restart/shutdown. We need any thread created to be daemon.
	 * We'll use the executor to asynchronously restart so that the methods can return.
	 */
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {

		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		}
	});

	/**
	 * {@inheritDoc}
	 */
	public void restart() {
		synchronized (this) {
			if (this.isShutdown) {
				return;
			}
			this.isShutdown = true;
		}
		doShutdown(true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutdown() {
		synchronized (this) {
			if (this.isShutdown) {
				return;
			}
			this.isShutdown = true;
		}
		doShutdown(false);

	}

	/**
	 * Executes shutdown. If restart is true, there will be a shutdown hook added that will execute
	 * the {@link #restartCommand} so that new CMR is launched.
	 * 
	 * @param restart
	 *            If new CMR should be launched.
	 */
	private void doShutdown(final boolean restart) {
		if (restart) {
			log.info("Restart initialized");
		} else {
			log.info("Shutdown initialized");
		}

		Runnable shutdownRunnable = new Runnable() {
			@Override
			public void run() {
				if (restart) {
					System.exit(RESTART_EXIT_CODE);
				} else {
					System.exit(0);
				}
			}
		};
		// we execute the shutdown in 500ms so that this method can return
		executorService.schedule(shutdownRunnable, 500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Loads restart command from startup file.
	 * 
	 * @throws IOException
	 *             If {@link IOException} occurs during reading.
	 */
	@PostConstruct
	public void postConstruct() throws IOException {
		if (log.isInfoEnabled()) {
			log.info("|-Shutdown Service active...");
		}
	}

}
