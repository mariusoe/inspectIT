package info.novatec.inspectit.rcp.storage.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.springframework.beans.factory.FactoryBean;

/**
 * Bean factory for providing the executor service.
 * <P>
 * The exactly same class exists in the CommonsCS project. However that class can not be used, since
 * Spring will not recognize it is a factory due to the different class loaders. When Spring on the
 * UI is fixed, this class can be removed.
 * 
 * @author Ivan Senic
 * 
 */
public class ExecutorServiceFactory implements FactoryBean<ExecutorService> {

	/**
	 * Number of threads in the executor.
	 */
	private int executorThreads;

	/**
	 * Will this factory create a singleton.
	 */
	private boolean isBeanSingleton;

	/**
	 * Should the provided executor be {@link ScheduledExecutorService}.
	 */
	private boolean isScheduledExecutor;

	/**
	 * {@inheritDoc}
	 */
	public ExecutorService getObject() throws Exception {
		if (!isScheduledExecutor) {
			return Executors.newFixedThreadPool(executorThreads);
		} else {
			// I set remove on cancel policy, because i don't want to have the canceled tasks still
			// in the queue
			ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(executorThreads);
			scheduledExecutor.setRemoveOnCancelPolicy(true);
			return scheduledExecutor;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<?> getObjectType() {
		if (!isScheduledExecutor) {
			return ExecutorService.class;
		} else {
			return ScheduledExecutorService.class;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSingleton() {
		return isBeanSingleton;
	}

	/**
	 * @param executorThreads
	 *            the executorThreads to set
	 */
	public void setExecutorThreads(int executorThreads) {
		this.executorThreads = executorThreads;
	}

	/**
	 * @param isBeanSingleton
	 *            the isBeanSingleton to set
	 */
	public void setBeanSingleton(boolean isBeanSingleton) {
		this.isBeanSingleton = isBeanSingleton;
	}

	/**
	 * @param isScheduledExecutor
	 *            the isScheduledExecutor to set
	 */
	public void setScheduledExecutor(boolean isScheduledExecutor) {
		this.isScheduledExecutor = isScheduledExecutor;
	}

}
