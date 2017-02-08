package rocks.inspectit.server;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.processor.impl.AnomalyDetectionProcessor;
import rocks.inspectit.shared.all.cmr.service.IAgentStorageService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * @author Marius Oehler
 *
 */
@Component
public class DummyAgent implements Runnable {

	@Autowired
	IRegistrationService registrationService;

	@Autowired
	AnomalyDetectionProcessor adp;

	@Autowired
	IAgentStorageService ass;

	private long platformIdent;

	private long sensorTypeIdent;

	private long methodIdent;

	public void start() {

		try {
			platformIdent = registrationService.registerPlatformIdent(Arrays.asList("127.0.0.1"), "DummyAgent", "v1");

			Map<String, Object> parameter = new HashMap<>();
			parameter.put("stringLength", 100);
			sensorTypeIdent = registrationService.registerMethodSensorTypeIdent(platformIdent, "rocks.inspectit.agent.java.sensor.method.invocationsequence.InvocationSequenceSensor", parameter);
			methodIdent = registrationService.registerMethodIdent(platformIdent, "package.name", "MyClass", "methodName", Arrays.asList("parameter.Class"), "return.Class", 1);

			System.out.println(String.format("platform: %d // sensor: %d // method: %d", platformIdent, sensorTypeIdent, methodIdent));

			ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
			pool.scheduleWithFixedDelay(this, 0L, 1000, TimeUnit.MILLISECONDS);

		} catch (BusinessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		InvocationSequenceData data = new InvocationSequenceData();

		data.setPlatformIdent(platformIdent);
		data.setSensorTypeIdent(sensorTypeIdent);
		data.setMethodIdent(methodIdent);

		data.setTimeStamp(new Timestamp(System.currentTimeMillis()));

		data.setDuration(50D);

		ass.addDataObjects(Arrays.asList(data));
	}
}
