package info.novatec.novaspy.agent.sensor.method.timer.test;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import info.novatec.novaspy.agent.config.IPropertyAccessor;
import info.novatec.novaspy.agent.config.impl.RegisteredSensorConfig;
import info.novatec.novaspy.agent.core.ICoreService;
import info.novatec.novaspy.agent.core.IIdManager;
import info.novatec.novaspy.agent.core.IObjectStorage;
import info.novatec.novaspy.agent.core.IdNotAvailableException;
import info.novatec.novaspy.agent.sensor.method.timer.AggregateTimerStorage;
import info.novatec.novaspy.agent.sensor.method.timer.ITimerStorage;
import info.novatec.novaspy.agent.sensor.method.timer.OptimizedTimerStorage;
import info.novatec.novaspy.agent.sensor.method.timer.PlainTimerStorage;
import info.novatec.novaspy.agent.sensor.method.timer.TimerHook;
import info.novatec.novaspy.agent.test.AbstractLogSupport;
import info.novatec.novaspy.communication.data.TimerData;
import info.novatec.novaspy.communication.valueobject.TimerRawVO;
import info.novatec.novaspy.communication.valueobject.TimerRawVO.TimerRawContainer;
import info.novatec.novaspy.util.Timer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
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
		timerHook = new TimerHook(timer, idManager, propertyAccessor, settings);
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

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

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
		plainTimerStorage.addData(secondTimerValue - firstTimerValue);
		verify(coreService).addObjectStorage(eq(sensorTypeId), eq(methodId), (String) eq(null), argThat(new PlainTimerStorageVerifier(plainTimerStorage)));

		verifyNoMoreInteractions(timer, idManager, coreService, registeredSensorConfig);
		verifyZeroInteractions(propertyAccessor, object, result);
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

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue).thenReturn(thirdTimerValue).thenReturn(fourthTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodIdOne)).thenReturn(registeredMethodIdOne);
		when(idManager.getRegisteredMethodId(methodIdTwo)).thenReturn(registeredMethodIdTwo);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

		timerHook.beforeBody(methodIdOne, sensorTypeId, object, parameters, registeredSensorConfig);
		timerHook.beforeBody(methodIdTwo, sensorTypeId, object, parameters, registeredSensorConfig);

		timerHook.firstAfterBody(methodIdTwo, sensorTypeId, object, parameters, result, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodIdTwo, sensorTypeId, object, parameters, result, registeredSensorConfig);
		PlainTimerStorage plainTimerStorageTwo = new PlainTimerStorage(null, platformId, registeredSensorTypeId, registeredMethodIdTwo, null);
		plainTimerStorageTwo.addData(thirdTimerValue - secondTimerValue);
		verify(coreService).addObjectStorage(eq(sensorTypeId), eq(methodIdTwo), (String) eq(null), argThat(new PlainTimerStorageVerifier(plainTimerStorageTwo)));

		timerHook.firstAfterBody(methodIdOne, sensorTypeId, object, parameters, result, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodIdOne, sensorTypeId, object, parameters, result, registeredSensorConfig);
		PlainTimerStorage plainTimerStorageOne = new PlainTimerStorage(null, platformId, registeredSensorTypeId, registeredMethodIdOne, null);
		plainTimerStorageOne.addData(fourthTimerValue - firstTimerValue);
		verify(coreService).addObjectStorage(eq(sensorTypeId), eq(methodIdOne), (String) eq(null), argThat(new PlainTimerStorageVerifier(plainTimerStorageOne)));
	}

	@Test
	public void sameMethodTwice() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.0d;
		Double secondTimerValue = 1323.0d;
		Double thirdTimerValue = 1894.0d;
		Double fourthTimerValue = 2812.0d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue).thenReturn(thirdTimerValue).thenReturn(fourthTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

		// First call
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
		plainTimerStorage.addData(secondTimerValue - firstTimerValue);
		verify(coreService).addObjectStorage(eq(sensorTypeId), eq(methodId), (String) eq(null), argThat(new PlainTimerStorageVerifier(plainTimerStorage)));

		// second one
		timerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(3)).getCurrentTime();

		timerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(timer, times(4)).getCurrentTime();

		when(coreService.getObjectStorage(sensorTypeId, methodId, null)).thenReturn(plainTimerStorage);
		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(coreService, times(2)).getObjectStorage(sensorTypeId, methodId, null);
		verify(registeredSensorConfig, times(2)).isPropertyAccess();

		TimerRawVO timerRawVO = (TimerRawVO) plainTimerStorage.finalizeDataObject();
		assertEquals(timerRawVO.getPlatformIdent(), platformId);
		assertEquals(timerRawVO.getMethodIdent(), registeredMethodId);
		assertEquals(timerRawVO.getSensorTypeIdent(), registeredSensorTypeId);
		assertEquals(((TimerRawContainer) timerRawVO.getData().get(0)).getData()[0], secondTimerValue - firstTimerValue);
		assertEquals(((TimerRawContainer) timerRawVO.getData().get(0)).getData()[1], fourthTimerValue - thirdTimerValue);

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
	public void platformIdNotAvailable() throws IdNotAvailableException {
		// set up data
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		doThrow(new IdNotAvailableException("")).when(idManager).getPlatformId();

		timerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		timerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(coreService, never()).addObjectStorage(anyLong(), anyLong(), anyString(), (IObjectStorage) isNull());
	}

	@Test
	public void methodIdNotAvailable() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		doThrow(new IdNotAvailableException("")).when(idManager).getRegisteredMethodId(methodId);

		timerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		timerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(coreService, never()).addObjectStorage(anyLong(), anyLong(), anyString(), (IObjectStorage) isNull());
	}

	@Test
	public void sensorTypeIdNotAvailable() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		doThrow(new IdNotAvailableException("")).when(idManager).getRegisteredSensorTypeId(sensorTypeId);

		timerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		timerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(coreService, never()).addObjectStorage(anyLong(), anyLong(), anyString(), (IObjectStorage) isNull());
	}

	@Test
	public void propertyAccess() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[2];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		doThrow(new IdNotAvailableException("")).when(idManager).getRegisteredSensorTypeId(sensorTypeId);
		when(registeredSensorConfig.isPropertyAccess()).thenReturn(true);

		timerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		timerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(registeredSensorConfig, times(1)).isPropertyAccess();
		verify(propertyAccessor, times(1)).getParameterContentData(registeredSensorConfig.getPropertyAccessorList(), object, parameters);
	}

	@Test
	public void aggregateStorage() throws IdNotAvailableException {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("mode", "aggregate");
		timerHook = new TimerHook(timer, idManager, propertyAccessor, settings);

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

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

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

		AggregateTimerStorage aggregateTimerStorage = new AggregateTimerStorage(null, platformId, registeredSensorTypeId, registeredMethodId, null);
		aggregateTimerStorage.addData(secondTimerValue - firstTimerValue);
		verify(coreService).addObjectStorage(eq(sensorTypeId), eq(methodId), (String) eq(null), argThat(new AggregateTimerStorageVerifier(aggregateTimerStorage)));

		verifyNoMoreInteractions(timer, idManager, coreService, registeredSensorConfig);
		verifyZeroInteractions(propertyAccessor, object, result);
	}

	/**
	 * Inner class used to verify the contents of AggregateTimerStorage objects.
	 */
	private static class AggregateTimerStorageVerifier extends ArgumentMatcher<AggregateTimerStorage> {
		private final ITimerStorage timerStorage;

		public AggregateTimerStorageVerifier(AggregateTimerStorage timerStorage) {
			this.timerStorage = timerStorage;
		}

		@Override
		public boolean matches(Object object) {
			if (!AggregateTimerStorage.class.isInstance(object)) {
				return false;
			}

			try {
				AggregateTimerStorage otherAggregateTimerStorage = (AggregateTimerStorage) object;
				// we have to use reflection here
				Field field = AggregateTimerStorage.class.getDeclaredField("timerRawVO");
				field.setAccessible(true);
				TimerRawVO timerRawVO = (TimerRawVO) field.get(timerStorage);
				TimerRawVO otherTimerRawVO = (TimerRawVO) field.get(otherAggregateTimerStorage);
				if (timerRawVO.getPlatformIdent() != otherTimerRawVO.getPlatformIdent()) {
					return false;
				} else if (timerRawVO.getMethodIdent() != otherTimerRawVO.getMethodIdent()) {
					return false;
				} else if (timerRawVO.getSensorTypeIdent() != otherTimerRawVO.getSensorTypeIdent()) {
					return false;
				} else if (!timerRawVO.getParameterContentData().equals(otherTimerRawVO.getParameterContentData())) {
					return false;
				}
				return true;
			} catch (Exception exception) {
				exception.printStackTrace();
				return false;
			}
		}
	}

	@Test
	public void optimizedStorage() throws IdNotAvailableException {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("mode", "optimized");
		timerHook = new TimerHook(timer, idManager, propertyAccessor, settings);

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

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

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

		OptimizedTimerStorage optimizedTimerStorage = new OptimizedTimerStorage(null, platformId, registeredSensorTypeId, registeredMethodId, null);
		optimizedTimerStorage.addData(secondTimerValue - firstTimerValue);
		verify(coreService).addObjectStorage(eq(sensorTypeId), eq(methodId), (String) eq(null), argThat(new OptimizedTimerStorageVerifier(optimizedTimerStorage)));

		verifyNoMoreInteractions(timer, idManager, coreService, registeredSensorConfig);
		verifyZeroInteractions(propertyAccessor, object, result);
	}

	/**
	 * Inner class used to verify the contents of AggregateTimerStorage objects.
	 */
	private static class OptimizedTimerStorageVerifier extends ArgumentMatcher<OptimizedTimerStorage> {
		private final ITimerStorage timerStorage;

		public OptimizedTimerStorageVerifier(OptimizedTimerStorage timerStorage) {
			this.timerStorage = timerStorage;
		}

		@Override
		public boolean matches(Object object) {
			if (!OptimizedTimerStorage.class.isInstance(object)) {
				return false;
			}
			TimerData timerData = (TimerData) timerStorage.finalizeDataObject();
			TimerData otherTimerData = (TimerData) ((OptimizedTimerStorage) object).finalizeDataObject();
			if (timerData.getPlatformIdent() != otherTimerData.getPlatformIdent()) {
				return false;
			} else if (timerData.getMethodIdent() != otherTimerData.getMethodIdent()) {
				return false;
			} else if (timerData.getSensorTypeIdent() != otherTimerData.getSensorTypeIdent()) {
				return false;
			} else if (timerData.getCount() != otherTimerData.getCount()) {
				return false;
			} else if (timerData.getDuration() != otherTimerData.getDuration()) {
				return false;
			} else if (timerData.getMax() != otherTimerData.getMax()) {
				return false;
			} else if (timerData.getMin() != otherTimerData.getMin()) {
				return false;
			} else if (timerData.getAverage() != otherTimerData.getAverage()) {
				return false;
			} else if (!timerData.getParameterContentData().equals(otherTimerData.getParameterContentData())) {
				return false;
			} else if (timerData.getVariance() != otherTimerData.getVariance()) {
				return false;
			}
			return true;
		}
	}

}
