package info.novatec.inspectit.storage.processor;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.storage.StorageWriter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This is a processor that has additional processors connected to it. Thus, this kind of processors
 * will based on the implementation of {@link #shouldBePassedToChainedProcessors(DefaultData)} only
 * pass (or not) the data to other processors.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractChainedDataProcessor extends AbstractDataProcessor {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 4799747830133168646L;

	/**
	 * List of chained processors.
	 */
	private List<AbstractDataProcessor> dataProcessors;

	/**
	 * Default constructor.
	 */
	public AbstractChainedDataProcessor() {
		dataProcessors = new ArrayList<AbstractDataProcessor>();
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param dataProcessors
	 *            List of chained processors.
	 */
	public AbstractChainedDataProcessor(List<AbstractDataProcessor> dataProcessors) {
		this.dataProcessors = dataProcessors;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData) {
		if (shouldBePassedToChainedProcessors(defaultData)) {
			passToChainedProcessors(defaultData);
		}
	}

	/**
	 * Passed the default data to all chained processors.
	 * 
	 * @param defaultData
	 *            Data to pass.
	 */
	protected void passToChainedProcessors(DefaultData defaultData) {
		if (null != dataProcessors) {
			for (AbstractDataProcessor dataProcessor : dataProcessors) {
				dataProcessor.process(defaultData);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Flushes all chained data processors.
	 */
	@Override
	public void flush() {
		for (AbstractDataProcessor abstractDataProcessor : dataProcessors) {
			abstractDataProcessor.flush();
		}
	}

	/**
	 * Should the data be passed to the chained processors.
	 * 
	 * @param defaultData
	 *            {@link DefaultData}.
	 * @return True if it should be passed, false otherwise.
	 */
	protected abstract boolean shouldBePassedToChainedProcessors(DefaultData defaultData);

	/**
	 * {@inheritDoc}
	 * <P>
	 * Passes the writer to all chained processors.
	 */
	@Override
	public void setStorageWriter(StorageWriter storageWriter) {
		super.setStorageWriter(storageWriter);
		for (AbstractDataProcessor processor : dataProcessors) {
			processor.setStorageWriter(storageWriter);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder =  new ToStringBuilder(this);
		toStringBuilder.append("chainedProcessors", dataProcessors);
		return toStringBuilder.toString();
	}
}
