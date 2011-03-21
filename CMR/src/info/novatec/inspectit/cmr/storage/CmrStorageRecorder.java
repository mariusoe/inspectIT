package info.novatec.inspectit.cmr.storage;

import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.StorageRecorder;
import info.novatec.inspectit.storage.StorageWriter;
import info.novatec.inspectit.storage.recording.RecordingProperties;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * CMR extension of {@link StorageRecorder}.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class CmrStorageRecorder extends StorageRecorder {

	/**
	 * The log of this class.
	 */
	@Logger
	Log log;

	/**
	 * CMR storage manager.
	 */
	@Autowired
	private CmrStorageManager cmrStorageManager;

	/**
	 * {@link ExecutorService} for tasks of the tree handling.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService executorService;

	/**
	 * Future for the task of recording stop.
	 */
	private ScheduledFuture<?> stopRecordingFuture;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean startRecording(StorageWriter storageWriter, RecordingProperties recordingProperties) {
		boolean result = super.startRecording(storageWriter, recordingProperties);
		if (result && null != recordingProperties) {
			// set the task for stopping the recording if stop date is provided
			Date stopRecordingDate = recordingProperties.getRecordEndDate();
			if (null != stopRecordingDate) {
				long timeDifferenceInMillis = stopRecordingDate.getTime() - new Date().getTime();
				if (timeDifferenceInMillis > 0) {
					Runnable stopRecordingRunnable = new Runnable() {
						@Override
						public void run() {
							try {
								cmrStorageManager.stopRecording();
							} catch (Exception e) {
								log.warn("Automatic stop of recording failed for the storage: " + getStorageData(), e);
							}
						}
					};
					stopRecordingFuture = executorService.schedule(stopRecordingRunnable, timeDifferenceInMillis, TimeUnit.MILLISECONDS);
				} else {
					log.warn("Recording end date is in past. The recording will not stop automatically for the storage: " + getStorageData());
				}
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void stopRecording() {
		if (null != stopRecordingFuture) {
			if (!stopRecordingFuture.isDone() && !stopRecordingFuture.isCancelled()) {
				stopRecordingFuture.cancel(false);
			}
			stopRecordingFuture = null;
		}

		super.stopRecording();
	}

}
