package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;
import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.StorageManager;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link ICmrManagementService}. Provides general management of the CMR.
 * 
 * @author Ivan Senic
 * 
 */
@Service
public class CmrManagementService implements ICmrManagementService {

	/** The logger of this class. */
	@Logger
	Log log;

	/**
	 * Buffer data dao.
	 */
	@Autowired
	private IBuffer<DefaultData> buffer;

	/**
	 * {@link StorageManager}.
	 */
	@Autowired
	private StorageManager storageManager;

	/**
	 * Count of dropped data due to high volume of incoming data objects.
	 */
	private int droppedDataCount = 0;

	/**
	 * Time in milliseconds when the CMR has started.
	 */
	private long timeStarted;

	/**
	 * Date when the CMR has started.
	 */
	private Date dateStarted;

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void clearBuffer() {
		buffer.clearAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public CmrStatusData getCmrStatusData() {
		// cmr status data should always report in bytes!
		CmrStatusData cmrStatusData = new CmrStatusData();
		cmrStatusData.setCurrentBufferSize(buffer.getCurrentSize());
		cmrStatusData.setMaxBufferSize(buffer.getMaxSize());
		cmrStatusData.setBufferOldestElement(buffer.getOldestElement());
		cmrStatusData.setBufferNewestElement(buffer.getNewestElement());
		cmrStatusData.setStorageDataSpaceLeft(storageManager.getBytesHardDriveOccupancyLeft());
		cmrStatusData.setStorageMaxDataSpace(storageManager.getMaxBytesHardDriveOccupancy());
		cmrStatusData.setWarnSpaceLeftActive(storageManager.isSpaceWarnActive());
		cmrStatusData.setCanWriteMore(storageManager.canWriteMore());
		cmrStatusData.setUpTime(System.currentTimeMillis() - timeStarted);
		cmrStatusData.setDateStarted(dateStarted);
		return cmrStatusData;
	}

	/**
	 * Reports that an amount of data has been dropped.
	 * 
	 * @param count
	 *            Dropped amount.
	 */
	public void addDroppedDataCount(int count) {
		droppedDataCount += count;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getDroppedDataCount() {
		return droppedDataCount;
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		timeStarted = System.currentTimeMillis();
		dateStarted = new Date(timeStarted);
		if (log.isInfoEnabled()) {
			log.info("|-CMR Management Service active...");
		}
	}

}
