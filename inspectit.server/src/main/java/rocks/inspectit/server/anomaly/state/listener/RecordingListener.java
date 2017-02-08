package rocks.inspectit.server.anomaly.state.listener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.processing.ProcessingUnitGroupContext;
import rocks.inspectit.server.anomaly.state.IAnomalyStateListener;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;
import rocks.inspectit.shared.all.communication.data.CpuInformationData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.util.TimeFrame;
import rocks.inspectit.shared.cs.cmr.service.IStorageService;
import rocks.inspectit.shared.cs.communication.data.cmr.RecordingData;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.SqlStatementDataAggregator;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.TimerDataAggregator;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.label.DateStorageLabel;
import rocks.inspectit.shared.cs.storage.label.ObjectStorageLabel;
import rocks.inspectit.shared.cs.storage.label.type.impl.CreationDateLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.DataTimeFrameLabelType;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.DataAggregatorProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.DataSaverProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.InvocationClonerDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.InvocationExtractorDataProcessor;
import rocks.inspectit.shared.cs.storage.recording.RecordingProperties;

/**
 * @author Marius Oehler
 *
 */
@Component
public class RecordingListener implements IAnomalyStateListener {

	/**
	 * The logger of this class.
	 */
	@Log
	private Logger log;

	@Autowired
	private IStorageService storageService;

	private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy - hh:mm:ss");

	private int recordingCounter = 0;

	private long startTime;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart(ProcessingUnitGroupContext groupContext) {
		if (!groupContext.getGroupConfiguration().isRecordDataDuringAnomaly()) {
			return;
		}

		synchronized (storageService) {
			if (recordingCounter > 0) {
				if (log.isInfoEnabled()) {
					log.info("Recording has not been started because there is already an recording active.");
				}
				return;
			}

			if (log.isInfoEnabled()) {
				log.info("Start recording..");
			}

			StorageData storageData = new StorageData();
			storageData.setName("Automated Anomaly Recording - " + format.format(new Date(groupContext.getAnomalyStateContext().getCurrentAnomaly().getStartTime())));
			storageData.setDescription(createDescription(groupContext));

			try {
				storageData = storageService.createAndOpenStorage(storageData);
			} catch (BusinessException e) {
				if (log.isErrorEnabled()) {
					log.error("Could not create a new storage for recording..", e);
				}
				return;
			}

			RecordingProperties recordingProperties = getRecordingProperties();
			recordingProperties.setAutoFinalize(true);
			recordingProperties.setStartDelay(0);
			recordingProperties.setRecordStartDate(new Date(groupContext.getAnomalyStateContext().getCurrentAnomaly().getStartTime()));

			try {
				storageService.startOrScheduleRecording(storageData, recordingProperties);
				recordingCounter++;

				startTime = groupContext.getAnomalyStateContext().getCurrentAnomaly().getStartTime();
			} catch (BusinessException e) {
				if (log.isErrorEnabled()) {
					log.error("Recording could not be started..", e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onUpgrade(ProcessingUnitGroupContext groupContext) {
		// do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDowngrade(ProcessingUnitGroupContext groupContext) {
		// do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnd(ProcessingUnitGroupContext groupContext) {
		if (!groupContext.getGroupConfiguration().isRecordDataDuringAnomaly()) {
			return;
		}

		synchronized (storageService) {
			recordingCounter--;

			if (recordingCounter > 0) {
				if (log.isInfoEnabled()) {
					log.info("Recording has not been stopped because there are still anomalies active.");
				}
				return;
			}

			if (log.isInfoEnabled()) {
				log.info("Stop recording..");
			}

			RecordingData recordingData = storageService.getRecordingData();

			if (recordingData == null) {
				return;
			}

			DateStorageLabel createdLabel = new DateStorageLabel();
			createdLabel.setStorageLabelType(new CreationDateLabelType());
			createdLabel.setDateValue(new Date());

			TimeFrame timeFrame = new TimeFrame(new Date(startTime), new Date(groupContext.getAnomalyStateContext().getCurrentAnomaly().getEndTime()));
			ObjectStorageLabel<TimeFrame> timeframeLabel = new ObjectStorageLabel<>(timeFrame, new DataTimeFrameLabelType());

			storageService.getRecordingData().getRecordingStorage().addLabel(createdLabel, true);
			storageService.getRecordingData().getRecordingStorage().addLabel(timeframeLabel, true);

			try {
				storageService.stopRecording();
			} catch (BusinessException e) {
				if (log.isErrorEnabled()) {
					log.error("Recording could not be stopped..", e);
				}
			}
		}
	}

	private String createDescription(ProcessingUnitGroupContext groupContext) {
		return "dummy description";
	}

	/**
	 * Returns the recording properties with correctly set default set of
	 * {@link AbstractDataProcessor}s.
	 *
	 * @param extractInvocations
	 *            If invocations should be extracted.
	 * @return {@link RecordingProperties}.
	 */
	private RecordingProperties getRecordingProperties() {
		RecordingProperties recordingProperties = new RecordingProperties();

		List<AbstractDataProcessor> normalProcessors = new ArrayList<>();

		// data saver
		List<Class<? extends DefaultData>> classesToSave = new ArrayList<>();
		Collections.addAll(classesToSave, InvocationSequenceData.class, HttpTimerData.class, ExceptionSensorData.class, MemoryInformationData.class, CpuInformationData.class,
				ClassLoadingInformationData.class, ThreadInformationData.class, SystemInformationData.class);
		DataSaverProcessor dataSaverProcessor = new DataSaverProcessor(classesToSave, true);
		normalProcessors.add(dataSaverProcessor);

		// data aggregators
		normalProcessors.add(new DataAggregatorProcessor<>(TimerData.class, 5000, new TimerDataAggregator(), true));
		normalProcessors.add(new DataAggregatorProcessor<>(SqlStatementData.class, 5000, new SqlStatementDataAggregator(true), true));

		// invocations support
		List<AbstractDataProcessor> chainedProcessorsForExtractor = new ArrayList<>();
		chainedProcessorsForExtractor.addAll(normalProcessors);
		InvocationExtractorDataProcessor invocationExtractorDataProcessor = new InvocationExtractorDataProcessor(chainedProcessorsForExtractor);
		normalProcessors.add(invocationExtractorDataProcessor);

		normalProcessors.add(new InvocationClonerDataProcessor());

		recordingProperties.setRecordingDataProcessors(normalProcessors);

		return recordingProperties;
	}
}
