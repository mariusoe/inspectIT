package info.novatec.inspectit.agent.sensor.method.http.test;

import static org.mockito.Mockito.stub;
import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.sensor.method.http.HttpHook;
import info.novatec.inspectit.agent.test.AbstractLogSupport;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ThreadMXBean;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ObjectUtils;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HttpHookTest extends AbstractLogSupport {

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

	@Mock
	private Object result;

	@Mock
	private HttpServlet servlet;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Mock
	private ServletRequest servletRequest;

	@Mock
	private HttpSession session;

	private HttpHook httpHook;

	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("sessioncapture", "false");
		stub(threadMXBean.isThreadCpuTimeEnabled()).toReturn(true);
		stub(threadMXBean.isThreadCpuTimeSupported()).toReturn(true);

		httpHook = new HttpHook(timer, idManager, new HashMap<String, String>() {
			{
				put("sessioncapture", "true");
			}
		}, threadMXBean);
	}

	@Test
	public void oneRecordThatIsHttpWithoutReadingData() throws IdNotAvailableException {
		// initialize the ids
		long platformId = 1L;
		long methodId = 1L;
		long sensorTypeId = 3L;
		long registeredMethodId = 13L;
		long registeredSensorTypeId = 7L;

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		Long firstCpuTimerValue = 5000L;
		Long secondCpuTimerValue = 6872L;

		MethodSensorData data = new HttpTimerData(null, platformId, registeredSensorTypeId, registeredMethodId);

		stub(timer.getCurrentTime()).toReturn(firstTimerValue).toReturn(secondTimerValue);
		stub(threadMXBean.getCurrentThreadCpuTime()).toReturn(firstCpuTimerValue).toReturn(secondCpuTimerValue);
		stub(idManager.getPlatformId()).toReturn(platformId);
		stub(idManager.getRegisteredMethodId(methodId)).toReturn(registeredMethodId);
		stub(idManager.getRegisteredSensorTypeId(sensorTypeId)).toReturn(registeredSensorTypeId);

		Object[] parameters = new Object[] { httpServletRequest };

		httpHook.beforeBody(methodId, sensorTypeId, servlet, parameters, registeredSensorConfig);

		httpHook.firstAfterBody(methodId, sensorTypeId, servlet, parameters, result, registeredSensorConfig);

		httpHook.secondAfterBody(coreService, methodId, sensorTypeId, servlet, parameters, parameters, registeredSensorConfig);

		Mockito.verify(coreService).addMethodSensorData(Mockito.eq(registeredSensorTypeId), Mockito.eq(registeredMethodId), (String) Mockito.eq(null),
				Mockito.argThat(new HttpTimerDataVerifier((HttpTimerData) data)));

		Mockito.verifyZeroInteractions(result);
	}

	@Test
	public void oneRecordThatIsHttpReadingData() throws IdNotAvailableException {
		// initialize the ids
		long platformId = 1L;
		long methodId = 1L;
		long sensorTypeId = 3L;
		long registeredMethodId = 13L;
		long registeredSensorTypeId = 7L;

		final String uri = "URI";
		final String method = "GET";

		final String param1 = "p1";
		final String param2 = "p2";
		final String param1VReal = "value1";
		final String param2VReal1 = "value5";
		final String param2VReal2 = "value6";
		final String[] param1V = new String[] { param1VReal };
		final String[] param2V = new String[] { param2VReal1, param2VReal2 };
		final Map<String, String[]> parameterMap = new HashMap<String, String[]>() {
			{
				put(param1, param1V);
				put(param2, param2V);
			}
		};

		final String att1 = "a1";
		final String att2 = "a2";
		final String att1Value = "aValue1";
		final String att2Value = "aValue2";
		final Vector<String> attributesList = new Vector<String>() {
			{
				add(att1);
				add(att2);
			}
		};
		final Enumeration<String> attributes = attributesList.elements();

		final String h1 = "h1";
		final String h2 = "h2";
		final String h1Value = "hValue1";
		final String h2Value = "hValue2";
		final Vector<String> headersList = new Vector<String>() {
			{
				add(h1);
				add(h2);
			}
		};
		final Enumeration<String> headers = headersList.elements();

		final String sa1 = "sa1";
		final String sa2 = "sa2";
		final String sa1Value = "saValue1";
		final String sa2Value = "saValue2";
		final Vector<String> sessionAttributesList = new Vector<String>() {
			{
				add(sa1);
				add(sa2);
			}
		};
		final Enumeration<String> sessionAttributes = sessionAttributesList.elements();

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		Long firstCpuTimerValue = 5000L;
		Long secondCpuTimerValue = 6872L;

		HttpTimerData tmp = new HttpTimerData(null, platformId, registeredSensorTypeId, registeredMethodId);
		tmp.setRequestMethod(method);
		tmp.setUri(uri);
		tmp.setAttributes(new HashMap<String, String>() {
			{
				put(att1, att1Value);
				put(att2, att2Value);
			}
		});
		tmp.setParameters(new HashMap<String, String[]>() {
			{
				put(param1, param1V);
				put(param2, param2V);
			}
		});
		tmp.setHeaders(new HashMap<String, String>() {
			{
				put(h1, h1Value);
				put(h2, h2Value);
			}
		});
		tmp.setSessionAttributes(new HashMap<String, String>() {
			{
				put(sa1, sa1Value);
				put(sa2, sa2Value);
			}
		});
		MethodSensorData data = tmp;

		stub(timer.getCurrentTime()).toReturn(firstTimerValue).toReturn(secondTimerValue);
		stub(threadMXBean.getCurrentThreadCpuTime()).toReturn(firstCpuTimerValue).toReturn(secondCpuTimerValue);
		stub(idManager.getPlatformId()).toReturn(platformId);
		stub(idManager.getRegisteredMethodId(methodId)).toReturn(registeredMethodId);
		stub(idManager.getRegisteredSensorTypeId(sensorTypeId)).toReturn(registeredSensorTypeId);

		stub(httpServletRequest.getMethod()).toReturn(method);
		stub(httpServletRequest.getRequestURI()).toReturn(uri);
		stub(httpServletRequest.getParameterMap()).toReturn(parameterMap);
		stub(httpServletRequest.getAttributeNames()).toReturn(attributes);
		Mockito.when(httpServletRequest.getAttribute(att1)).thenReturn(att1Value);
		Mockito.when(httpServletRequest.getAttribute(att2)).thenReturn(att2Value);
		stub(httpServletRequest.getHeaderNames()).toReturn(headers);
		Mockito.when(httpServletRequest.getHeader(h1)).thenReturn(h1Value);
		Mockito.when(httpServletRequest.getHeader(h2)).thenReturn(h2Value);

		stub(session.getAttributeNames()).toReturn(sessionAttributes);
		stub(session.getAttribute(sa1)).toReturn(sa1Value);
		stub(session.getAttribute(sa2)).toReturn(sa2Value);
		Mockito.when(httpServletRequest.getSession(false)).thenReturn(session);

		// Object servlet = (Object) new MyTestServlet();
		Object[] parameters = new Object[] { httpServletRequest };

		httpHook.beforeBody(methodId, sensorTypeId, servlet, parameters, registeredSensorConfig);

		httpHook.firstAfterBody(methodId, sensorTypeId, servlet, parameters, result, registeredSensorConfig);

		httpHook.secondAfterBody(coreService, methodId, sensorTypeId, servlet, parameters, parameters, registeredSensorConfig);

		Mockito.verify(coreService).addMethodSensorData(Mockito.eq(registeredSensorTypeId), Mockito.eq(registeredMethodId), (String) Mockito.eq(null),
				Mockito.argThat(new HttpTimerDataVerifier((HttpTimerData) data)));

		Mockito.verifyZeroInteractions(result);
	}

	@Test
	public void oneRecordThatIsHttpReadingDataSessionNotThere() throws IdNotAvailableException {
		// initialize the ids
		long platformId = 1L;
		long methodId = 1L;
		long sensorTypeId = 3L;
		long registeredMethodId = 13L;
		long registeredSensorTypeId = 7L;

		final String uri = "URI";
		final String method = "GET";

		final String sa1 = "sa1";
		final String sa2 = "sa2";
		final String sa1Value = "saValue1";
		final String sa2Value = "saValue2";
		final Vector<String> sessionAttributesList = new Vector<String>() {
			{
				add(sa1);
				add(sa2);
			}
		};
		final Enumeration<String> sessionAttributes = sessionAttributesList.elements();

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		Long firstCpuTimerValue = 5000L;
		Long secondCpuTimerValue = 6872L;

		HttpTimerData tmp = new HttpTimerData(null, platformId, registeredSensorTypeId, registeredMethodId);
		tmp.setRequestMethod(method);
		tmp.setUri(uri);

		MethodSensorData data = tmp;

		stub(timer.getCurrentTime()).toReturn(firstTimerValue).toReturn(secondTimerValue);
		stub(threadMXBean.getCurrentThreadCpuTime()).toReturn(firstCpuTimerValue).toReturn(secondCpuTimerValue);
		stub(idManager.getPlatformId()).toReturn(platformId);
		stub(idManager.getRegisteredMethodId(methodId)).toReturn(registeredMethodId);
		stub(idManager.getRegisteredSensorTypeId(sensorTypeId)).toReturn(registeredSensorTypeId);

		stub(httpServletRequest.getMethod()).toReturn(method);
		stub(httpServletRequest.getRequestURI()).toReturn(uri);

		stub(session.getAttributeNames()).toReturn(sessionAttributes);
		stub(session.getAttribute(sa1)).toReturn(sa1Value);
		stub(session.getAttribute(sa2)).toReturn(sa2Value);
		Mockito.when(httpServletRequest.getSession(false)).thenReturn(null);

		// Object servlet = (Object) new MyTestServlet();
		Object[] parameters = new Object[] { httpServletRequest };

		httpHook.beforeBody(methodId, sensorTypeId, servlet, parameters, registeredSensorConfig);

		httpHook.firstAfterBody(methodId, sensorTypeId, servlet, parameters, result, registeredSensorConfig);

		httpHook.secondAfterBody(coreService, methodId, sensorTypeId, servlet, parameters, parameters, registeredSensorConfig);

		Mockito.verify(coreService).addMethodSensorData(Mockito.eq(registeredSensorTypeId), Mockito.eq(registeredMethodId), (String) Mockito.eq(null),
				Mockito.argThat(new HttpTimerDataVerifier((HttpTimerData) data)));

		Mockito.verifyZeroInteractions(result);
	}

	@Test
	public void oneRecordThatIsNotHttp() throws IdNotAvailableException {
		// initialize the ids
		long platformId = 1L;
		long methodId = 1L;
		long sensorTypeId = 3L;
		long registeredMethodId = 13L;
		long registeredSensorTypeId = 7L;

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		Long firstCpuTimerValue = 5000L;
		Long secondCpuTimerValue = 6872L;

		MethodSensorData data = new HttpTimerData(null, platformId, registeredSensorTypeId, registeredMethodId);

		stub(timer.getCurrentTime()).toReturn(firstTimerValue).toReturn(secondTimerValue);

		stub(threadMXBean.getCurrentThreadCpuTime()).toReturn(firstCpuTimerValue).toReturn(secondCpuTimerValue);
		stub(idManager.getPlatformId()).toReturn(platformId);
		stub(idManager.getRegisteredMethodId(methodId)).toReturn(registeredMethodId);
		stub(idManager.getRegisteredSensorTypeId(sensorTypeId)).toReturn(registeredSensorTypeId);

		Object[] parameters = new Object[] { servletRequest };

		httpHook.beforeBody(methodId, sensorTypeId, servlet, parameters, registeredSensorConfig);

		httpHook.firstAfterBody(methodId, sensorTypeId, servlet, parameters, result, registeredSensorConfig);

		httpHook.secondAfterBody(coreService, methodId, sensorTypeId, servlet, parameters, parameters, registeredSensorConfig);

		// Data must not be pushed!
		Mockito.verifyNoMoreInteractions(coreService);
		Mockito.verifyZeroInteractions(result);
	}

	@Test
	public void twoInvocationsAfterEachOther() throws IdNotAvailableException {
		// Idea: Is it also working for two invocations after each other. Is the marker reset
		// correctly?

		// First invocation:
		// a) has no http
		// b) has http

		// Seconf invocation:
		// a) has http
		// b) has no http

		// initialize the ids
		long platformId = 1L;
		long sensorTypeId = 3L;
		long registeredSensorTypeId = 7L;

		long methodId11 = 1L;
		long methodId12 = 2L;
		long methodId21 = 3L;
		long methodId22 = 4L;

		long registeredMethodId11 = 11L;
		long registeredMethodId12 = 12L;
		long registeredMethodId21 = 13L;
		long registeredMethodId22 = 14L;

		Double timerS11 = 1000d;
		Double timerS12 = 1500d;
		Double timerE12 = 2000d;
		Double timerE11 = 2500d;

		Double timerS21 = 2000d;
		Double timerS22 = 2500d;
		Double timerE22 = 3000d;
		Double timerE21 = 3500d;

		Long cpuS11 = 11000L;
		Long cpuS12 = 21500L;
		Long cpuE12 = 34500L;
		Long cpuE11 = 45000L;

		Long cpuS21 = 52000L;
		Long cpuS22 = 62500L;
		Long cpuE22 = 73500L;
		Long cpuE21 = 84000L;

		// The second one should have the results!
		MethodSensorData data1 = new HttpTimerData(null, platformId, registeredSensorTypeId, registeredMethodId12);
		MethodSensorData data2 = new HttpTimerData(null, platformId, registeredSensorTypeId, registeredMethodId21);

		stub(timer.getCurrentTime()).toReturn(timerS11).toReturn(timerS12).toReturn(timerE12).toReturn(timerE11).toReturn(timerS21).toReturn(timerS22).toReturn(timerE22).toReturn(timerE21);
		stub(threadMXBean.getCurrentThreadCpuTime()).toReturn(cpuS11).toReturn(cpuS12).toReturn(cpuE12).toReturn(cpuE11).toReturn(cpuS21).toReturn(cpuS22).toReturn(cpuE22).toReturn(cpuE21);
		stub(idManager.getPlatformId()).toReturn(platformId);
		stub(idManager.getRegisteredMethodId(methodId11)).toReturn(registeredMethodId11);
		stub(idManager.getRegisteredMethodId(methodId12)).toReturn(registeredMethodId12);
		stub(idManager.getRegisteredMethodId(methodId21)).toReturn(registeredMethodId21);
		stub(idManager.getRegisteredMethodId(methodId22)).toReturn(registeredMethodId22);
		stub(idManager.getRegisteredSensorTypeId(sensorTypeId)).toReturn(registeredSensorTypeId);

		Object[] parametersNoHttp = new Object[] { servletRequest };
		Object[] parametersHttp = new Object[] { httpServletRequest };

		httpHook.beforeBody(methodId11, sensorTypeId, servlet, parametersNoHttp, registeredSensorConfig);
		httpHook.beforeBody(methodId12, sensorTypeId, servlet, parametersHttp, registeredSensorConfig);

		httpHook.firstAfterBody(methodId12, sensorTypeId, servlet, parametersNoHttp, result, registeredSensorConfig);
		httpHook.secondAfterBody(coreService, methodId12, sensorTypeId, servlet, parametersNoHttp, result, registeredSensorConfig);

		httpHook.firstAfterBody(methodId11, sensorTypeId, servlet, parametersHttp, result, registeredSensorConfig);
		httpHook.secondAfterBody(coreService, methodId11, sensorTypeId, servlet, parametersHttp, result, registeredSensorConfig);

		Mockito.verify(coreService).addMethodSensorData(Mockito.eq(registeredSensorTypeId), Mockito.eq(registeredMethodId12), (String) Mockito.eq(null),
				Mockito.argThat(new HttpTimerDataVerifier((HttpTimerData) data1)));

		httpHook.beforeBody(methodId21, sensorTypeId, servlet, parametersHttp, registeredSensorConfig);
		httpHook.beforeBody(methodId22, sensorTypeId, servlet, parametersNoHttp, registeredSensorConfig);

		httpHook.firstAfterBody(methodId22, sensorTypeId, servlet, parametersHttp, result, registeredSensorConfig);
		httpHook.secondAfterBody(coreService, methodId22, sensorTypeId, servlet, parametersHttp, result, registeredSensorConfig);

		httpHook.firstAfterBody(methodId21, sensorTypeId, servlet, parametersNoHttp, result, registeredSensorConfig);
		httpHook.secondAfterBody(coreService, methodId21, sensorTypeId, servlet, parametersNoHttp, result, registeredSensorConfig);

		Mockito.verify(coreService).addMethodSensorData(Mockito.eq(registeredSensorTypeId), Mockito.eq(registeredMethodId21), (String) Mockito.eq(null),
				Mockito.argThat(new HttpTimerDataVerifier((HttpTimerData) data2)));

		// ensure that there are no exceptions (like "NoSuchElement" which means that before or
		// after did not push a timer object)

		// No other data must not be pushed!
		Mockito.verifyNoMoreInteractions(coreService);

		Mockito.verifyZeroInteractions(result);

	}

	@Test
	public void multipleRecordsWithHttp() throws IdNotAvailableException {
		// Idea:
		// 1.element -> not http -> no data
		// 2.element -> http -> measurement
		// 3.element -> not http -> no data
		// 4.element -> http -> no data (already have a measurement)

		// initialize the ids
		long platformId = 1L;
		long sensorTypeId = 3L;
		long registeredSensorTypeId = 7L;

		long methodId1 = 1L;
		long methodId2 = 2L;
		long methodId3 = 3L;
		long methodId4 = 4L;

		long registeredMethodId1 = 11L;
		long registeredMethodId2 = 12L;
		long registeredMethodId3 = 13L;
		long registeredMethodId4 = 14L;

		Double timerS1 = 1000d;
		Double timerS2 = 1500d;
		Double timerS3 = 2000d;
		Double timerS4 = 2500d;
		Double timerE4 = 3500d;
		Double timerE3 = 4000d;
		Double timerE2 = 4500d;
		Double timerE1 = 5000d;

		Long cpuS1 = 11000L;
		Long cpuS2 = 21500L;
		Long cpuS3 = 32000L;
		Long cpuS4 = 42500L;
		Long cpuE4 = 53500L;
		Long cpuE3 = 64000L;
		Long cpuE2 = 74500L;
		Long cpuE1 = 85000L;

		// The second one should have the results!
		MethodSensorData data = new HttpTimerData(null, platformId, registeredSensorTypeId, registeredMethodId2);

		stub(timer.getCurrentTime()).toReturn(timerS1).toReturn(timerS2).toReturn(timerS3).toReturn(timerS4).toReturn(timerE4).toReturn(timerE3).toReturn(timerE2).toReturn(timerE1);
		stub(threadMXBean.getCurrentThreadCpuTime()).toReturn(cpuS1).toReturn(cpuS2).toReturn(cpuS3).toReturn(cpuS4).toReturn(cpuE4).toReturn(cpuE3).toReturn(cpuE2).toReturn(cpuE1);
		stub(idManager.getPlatformId()).toReturn(platformId);
		stub(idManager.getRegisteredMethodId(methodId1)).toReturn(registeredMethodId1);
		stub(idManager.getRegisteredMethodId(methodId2)).toReturn(registeredMethodId2);
		stub(idManager.getRegisteredMethodId(methodId3)).toReturn(registeredMethodId3);
		stub(idManager.getRegisteredMethodId(methodId4)).toReturn(registeredMethodId4);
		stub(idManager.getRegisteredSensorTypeId(sensorTypeId)).toReturn(registeredSensorTypeId);

		Object[] parameters1 = new Object[] { servletRequest };
		Object[] parameters2 = new Object[] { httpServletRequest };
		Object[] parameters3 = new Object[] { "Ich bin ein String und keine http information" };
		Object[] parameters4 = new Object[] { httpServletRequest };

		httpHook.beforeBody(methodId1, sensorTypeId, servlet, parameters1, registeredSensorConfig);
		httpHook.beforeBody(methodId2, sensorTypeId, servlet, parameters2, registeredSensorConfig);
		httpHook.beforeBody(methodId3, sensorTypeId, servlet, parameters3, registeredSensorConfig);
		httpHook.beforeBody(methodId4, sensorTypeId, servlet, parameters4, registeredSensorConfig);

		httpHook.firstAfterBody(methodId4, sensorTypeId, servlet, parameters4, result, registeredSensorConfig);
		httpHook.secondAfterBody(coreService, methodId4, sensorTypeId, servlet, parameters4, result, registeredSensorConfig);

		httpHook.firstAfterBody(methodId3, sensorTypeId, servlet, parameters3, result, registeredSensorConfig);
		httpHook.secondAfterBody(coreService, methodId3, sensorTypeId, servlet, parameters3, result, registeredSensorConfig);

		httpHook.firstAfterBody(methodId2, sensorTypeId, servlet, parameters2, result, registeredSensorConfig);
		httpHook.secondAfterBody(coreService, methodId2, sensorTypeId, servlet, parameters2, result, registeredSensorConfig);

		httpHook.firstAfterBody(methodId1, sensorTypeId, servlet, parameters1, result, registeredSensorConfig);
		httpHook.secondAfterBody(coreService, methodId1, sensorTypeId, servlet, parameters1, result, registeredSensorConfig);

		Mockito.verify(coreService).addMethodSensorData(Mockito.eq(registeredSensorTypeId), Mockito.eq(registeredMethodId2), (String) Mockito.eq(null),
				Mockito.argThat(new HttpTimerDataVerifier((HttpTimerData) data)));

		// No other data must not be pushed!
		Mockito.verifyNoMoreInteractions(coreService);

		Mockito.verifyZeroInteractions(result);
	}

	/**
	 * Inner class used to verify the contents of PlainTimerData objects.
	 */
	private static class HttpTimerDataVerifier extends ArgumentMatcher<HttpTimerData> {
		private final HttpTimerData data;

		public HttpTimerDataVerifier(HttpTimerData data) {
			this.data = data;
		}

		@Override
		public boolean matches(Object object) {
			if (!HttpTimerData.class.isInstance(object)) {
				return false;
			}
			HttpTimerData other = (HttpTimerData) object;

			if (!ObjectUtils.equals(data.getAttributes(), other.getAttributes())) {
				return false;
			} else if (!ObjectUtils.equals(data.getParameters(), other.getParameters())) {
				return false;
			} else if (!ObjectUtils.equals(data.getHeaders(), other.getHeaders())) {
				return false;
			} else if (!ObjectUtils.equals(data.getRequestMethod(), other.getRequestMethod())) {
				return false;
			} else if (!ObjectUtils.equals(data.getSessionAttributes(), other.getSessionAttributes())) {
				return false;
			} else if (!ObjectUtils.equals(data.getUri(), other.getUri())) {
				return false;
			}

			return true;
		}
	}
}
