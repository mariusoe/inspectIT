package info.novatec.inspectit.agent.sensor.method.timer.test;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.sensor.method.timer.ITimerStorage;
import info.novatec.inspectit.agent.sensor.method.timer.PlainTimerStorage;
import info.novatec.inspectit.agent.sensor.method.timer.TimerHook;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.communication.valueobject.TimerRawVO;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.mockito.ArgumentMatcher;
import org.mockito.MockitoAnnotations.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TimerHookTest extends AbstractLogSupport {

	@Mock
	private Timer timer;

	@Mock
	private IIdManager idManager;

	@Mock
	private IPropertyAccessor propertyAccessor;

	@Mock
	private ICoreService coreService;

	@Mock
	private RegisteredSensorConfig registeredSensorConfig;

	@Mock
	private ThreadMXBean threadMXBean;

	private TimerHook timerHook;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("mode", "raw");
		stub(threadMXBean.isThreadCpuTimeEnabled()).toReturn(true);
		stub(threadMXBean.isThreadCpuTimeSupported()).toReturn(true);
		timerHook = new TimerHook(timer, idManager, propertyAccessor, settings, threadMXBean);
	}

	@Test
	public void oneRecord() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		Long firstCpuTimerValue = 5000L;
		Long secondCpuTimerValue = 6872L;

		stub(timer.getCurrentTime()).toReturn(firstTimerValue).toReturn(secondTimerValue);
		stub(threadMXBean.getCurrentThreadCpuTime()).toReturn(firstCpuTimerValue).toReturn(secondCpuTimerValue);
		stub(idManager.getPlatformId()).toReturn(platformId);
		stub(idManager.getRegisteredMethodId(methodId)).toReturn(registeredMethodId);
		stub(idManager.getRegisteredSensorTypeId(sensorTypeId)).toReturn(registeredSensorTypeId);

		timerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(1)).getCurrentTime();

		timerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(timer, times(2)).getCurrentTime();

		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(idManager).getPlatformId();
		verify(idManager).getRegisteredMethodId(methodId);
		verify(idManager).getRegisteredSensorTypeId(sensorTypeId);
		verify(coreService).getObjectStorage(sensorTypeId, methodId, null);
		verify(registeredSensorConfig).isPropertyAccess();

		PlainTimerStorage plainTimerStorage = new PlainTimerStorage(null, platformId, registeredSensorTypeId, registeredMethodId, null);
		plainTimerStorage.addData(secondTimerValue - firstTimerValue, (secondCpuTimerValue - firstCpuTimerValue) / 1000000.0d);
		verify(coreService).addObjectStorage(eq(sensorTypeId), eq(methodId), (String) eq(null), argThat(new PlainTimerStorageVerifier(plainTimerStorage)));

		verifyNoMoreInteractions(timer, idManager, coreService, registeredSensorConfig);
		verifyZeroInteractions(propertyAccessor, object, result);
	}

	/**
	 * Inner class used to verify the contents of PlainTimerData objects.
	 */
	private static class PlainTimerStorageVerifier extends ArgumentMatcher<PlainTimerStorage> {
		private final ITimerStorage timerStorage;

		public PlainTimerStorageVerifier(PlainTimerStorage timerStorage) {
			this.timerStorage = timerStorage;
		}

		@Override
		public boolean matches(Object object) {
			if (!PlainTimerStorage.class.isInstance(object)) {
				return false;
			}
			PlainTimerStorage otherPlainTimerStorage = (PlainTimerStorage) object;
			// we receive the raw vo by calling finalize on the object storage!
			TimerRawVO timerRawVO = (TimerRawVO) timerStorage.finalizeDataObject();
			TimerRawVO otherTimerRawVO = (TimerRawVO) otherPlainTimerStorage.finalizeDataObject();
			if (timerRawVO.getPlatformIdent() != otherTimerRawVO.getPlatformIdent()) {
				return false;
			} else if (timerRawVO.getMethodIdent() != otherTimerRawVO.getMethodIdent()) {
				return false;
			} else if (timerRawVO.getSensorTypeIdent() != otherTimerRawVO.getSensorTypeIdent()) {
				return false;
			} else if (!timerRawVO.getParameterContentData().equals(otherTimerRawVO.getParameterContentData())) {
				return false;
			} else if (!timerRawVO.getData().equals(otherTimerRawVO.getData())) {
				return false;
			}
			return true;
		}
	}

	@Test
	public void twoRecords() throws IdNotAvailableException {
		long platformId = 1L;
		long methodIdOne = 3L;
		long registeredMethodIdOne = 13L;
		long methodIdTwo = 9L;
		long registeredMethodIdTwo = 15L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;
		Double thirdTimerValue = 1578.92d;
		Double fourthTimerValue = 2319.712d;

		Long firstCpuTimerValue = 5000L;
		Long secondCpuTimerValue = 6872L;
		Long thirdCpuTimerValue = 8412L;
		Long fourthCpuTimerValue = 15932L;

		stub(timer.getCurrentTime()).toReturn(firstTimerValue).toReturn(secondTimerValue).toReturn(thirdTimerValue).toReturn(fourthTimerValue);
		stub(threadMXBean.getCurrentThreadCpuTime()).toReturn(firstCpuTimerValue).toReturn(secondCpuTimerValue).toReturn(thirdCpuTimerValue).toReturn(fourthCpuTimerValue);
		stub(idManager.getPlatformId()).toReturn(platformId);
		stub(idManager.getRegisteredMethodId(methodIdOne)).toReturn(registeredMethodIdOne);
		stub(idManager.getRegisteredMethodId(methodIdTwo)).toReturn(registeredMethodIdTwo);
		stub(idManager.getRegisteredSensorTypeId(sensorTypeId)).toReturn(registeredSensorTypeId);

		timerHook.beforeBody(methodIdOne, sensorTypeId, object, parameters, registeredSensorConfig);
		timerHook.beforeBody(methodIdTwo, sensorTypeId, object, parameters, registeredSensorConfig);

		timerHook.firstAfterBody(methodIdTwo, sensorTypeId, object, parameters, result, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodIdTwo, sensorTypeId, object, parameters, result, registeredSensorConfig);
		PlainTimerStorage plainTimerStorageTwo = new PlainTimerStorage(null, platformId, registeredSensorTypeId, registeredMethodIdTwo, null);
		plainTimerStorageTwo.addData(thirdTimerValue - secondTimerValue, (thirdCpuTimerValue - secondCpuTimerValue) / 1000000.0d);
		verify(coreService).addObjectStorage(eq(sensorTypeId), eq(methodIdTwo), (String) eq(null), argThat(new PlainTimerStorageVerifier(plainTimerStorageTwo)));

		timerHook.firstAfterBody(methodIdOne, sensorTypeId, object, parameters, result, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodIdOne, sensorTypeId, object, parameters, result, registeredSensorConfig);
		PlainTimerStorage plainTimerStorageOne = new PlainTimerStorage(null, platformId, registeredSensorTypeId, registeredMethodIdOne, null);
		plainTimerStorageOne.addData(fourthTimerValue - firstTimerValue, (fourthCpuTimerValue - firstCpuTimerValue) / 1000000.0d);
		verify(coreService).addObjectStorage(eq(sensorTypeId), eq(methodIdOne), (String) eq(null), argThat(new PlainTimerStorageVerifier(plainTimerStorageOne)));
	}

}
