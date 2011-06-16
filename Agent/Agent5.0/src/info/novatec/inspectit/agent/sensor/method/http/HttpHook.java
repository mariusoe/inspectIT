package info.novatec.inspectit.agent.sensor.method.http;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sensor.method.timer.TimerHook;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.util.ThreadLocalStack;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The hook implementation for the http sensor. It uses the {@link ThreadLocalStack} class to save
 * the time when the method was called.
 * <p>
 * This hook measures timer data like the {@link TimerHook} but in addition provides Http
 * information. Another difference is that we ensure that only one Http metric per request is
 * created.
 * 
 * @author Stefan Siegl
 * 
 */
public class HttpHook implements IMethodHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(HttpHook.class.getName());

	/**
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack timeStack = new ThreadLocalStack();

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The ID manager.
	 */
	private final IIdManager idManager;

	/**
	 * The URI.
	 */
	private ThreadLocal<HttpTimerData> httpTimerData = new ThreadLocal<HttpTimerData>();

	/**
	 * The thread MX bean.
	 */
	private ThreadMXBean threadMXBean;

	/**
	 * Defines if the thread CPU time is supported.
	 */
	private boolean threadCPUTimeJMXAvailable = false;

	/**
	 * Defines if the thread CPU time is enabled.
	 */
	private boolean threadCPUTimeEnabled = false;

	/**
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack threadCpuTimeStack = new ThreadLocalStack();

	/**
	 * Keeps track of already looked up <code>Method</code> objects for faster access. Get and Put
	 * operations are synchronized by the concurrent hash map.
	 */
	private ConcurrentHashMap<HttpMethods, Method> methodCache = new ConcurrentHashMap<HttpMethods, Method>(9);

	/**
	 * Configuration setting if session data should be captured.
	 */
	private final boolean captureSessionData;

	/**
	 * Expected name of the HttpServletRequest interface.
	 */
	private static final String HTTP_SERVLET_REQUEST_CLASS = "javax.servlet.http.HttpServletRequest";

	/**
	 * Name of Object class. Stored to reduce number of created String objects during comparison.
	 */
	private static final String OBJECT_CLASS = Object.class.getName();

	/**
	 * Whitelist that contains all classes that we already checked if they provide
	 * HttpServletMetrics and do. We are talking about the class of the ServletRequest here. This
	 * list is extended if a new Class that provides this interface is found.
	 */
	private static final CopyOnWriteArrayList<Class<?>> whitelist = new CopyOnWriteArrayList<Class<?>>();

	/**
	 * Blacklist that contains all classes that we already checked if they provide
	 * HttpServletMetrics and do not. We are talking about the class of the ServletRequest here.
	 * This list is extended if a new Class that does not provides this interface is found.
	 */
	private static final CopyOnWriteArrayList<Class<?>> blacklist = new CopyOnWriteArrayList<Class<?>>();

	/**
	 * Helps us to ensure that we only store on http metric per request.
	 */
	private final StartEndMarker refMarker = new StartEndMarker();

	/**
	 * Structure to store all necessary methods that we can invoke to get http information. These
	 * objects are also used to cache the <code>Method</code> object in a cache.
	 * 
	 * @author Stefan Siegl
	 */
	private enum HttpMethods {
		/** Request URI in Servlet */
		SERVLET_REQUEST_URI("getRequestURI", (Class<?>[]) null),
		/** Parameter map */
		SERVLET_GET_PARAMETER_MAP("getParameterMap", (Class<?>[]) null),
		/** Gets all attributes names */
		SERVLET_GET_ATTRIBUTE_NAMES("getAttributeNames", (Class<?>[]) null),
		/** Gets a given attributes name value */
		SERVLET_GET_ATTRIBUTE("getAttribute", new Class[] { String.class }),
		/** Gets all header names */
		SERVLET_GET_HEADER_NAMES("getHeaderNames", (Class<?>[]) null),
		/** Gets the value of one given header */
		SERVLET_GET_HEADER("getHeader", new Class[] { String.class }),
		/** Gets the session */
		SERVLET_GET_SESSION("getSession", new Class[] { boolean.class }),
		/** Reads the request method */
		SERVLET_GET_METHOD("getMethod", (Class<?>[]) null),
		/** Gets all attribute names in the session */
		SESSION_GET_ATTRIBUTE_NAMES("getAttributeNames", (Class<?>[]) null),
		/** Gets the value of a session attribute */
		SESSION_GET_ATTRIBUTE("getAttribute", new Class[] { String.class });

		private HttpMethods(String methodName, Class<?>[] parameters) {
			this.methodName = methodName;
			this.parameters = parameters;
		}

		private String methodName;
		private Class<?>[] parameters;
	}

	/**
	 * Constructor
	 * 
	 * @param timer
	 *            The timer
	 * @param idManager
	 *            The id manager
	 * @param threadMXBean
	 *            the threadMx Bean for cpu timing
	 */
	public HttpHook(Timer timer, IIdManager idManager, Map<String, String> parameters, ThreadMXBean threadMXBean) {
		this.timer = timer;
		this.idManager = idManager;
		this.threadMXBean = threadMXBean;

		if (null != parameters && "true".equals(parameters.get("sessioncapture"))) {
			LOGGER.finer("Enabling session capturing for the http sensor");
			captureSessionData = true;
		} else {
			captureSessionData = false;
		}

		try {
			// if it is even supported by this JVM
			threadCPUTimeJMXAvailable = threadMXBean.isThreadCpuTimeSupported();
			if (threadCPUTimeJMXAvailable) {
				// check if its enabled
				threadCPUTimeEnabled = threadMXBean.isThreadCpuTimeEnabled();
				if (!threadCPUTimeEnabled) {
					// try to enable it
					threadMXBean.setThreadCpuTimeEnabled(true);
					// check again now if it is enabled now
					threadCPUTimeEnabled = threadMXBean.isThreadCpuTimeEnabled();
				}
			}
		} catch (RuntimeException e) {
			// catching the runtime exceptions which could be thrown by the
			// above statements.
			LOGGER.warning("Your environment does not support to capture CPU timings.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		if (!refMarker.isMarkerSet()) {

			// We expect the first parameter to be of the type javax.servlet.ServletRequest
			// If this is not the case then the configuration was wrong. For performance sake
			// we will not check each any everytime if the parameter is available but will on
			// purpose break here with an IndexOutOfBoundException.

			Object httpServletRequest = parameters[0];
			Class<?> servletRequestClass = httpServletRequest.getClass();

			if (providesHttpMetrics(servletRequestClass)) {

				// We must take the time as soon as we know that we are dealing with an http
				// timer. We cannot do that after we read the information from the request object
				// because these methods could be instrumented and thus the whole http timer
				// would be off - resulting in very strange results.
				timeStack.push(new Double(timer.getCurrentTime()));
				if (threadCPUTimeEnabled) {
					threadCpuTimeStack.push(new Long(threadMXBean.getCurrentThreadCpuTime()));
				}

				refMarker.markCall();

				// Keep the TimerData already thread locally so that we can already add the data
				// prior to the call.
				HttpTimerData data = new HttpTimerData();
				httpTimerData.set(data);

				// Include additional http information
				includeRequestUri(servletRequestClass, httpServletRequest, data);
				includeRequestMethod(servletRequestClass, httpServletRequest, data);
				includeParameterMap(servletRequestClass, httpServletRequest, data);
				includeAttributes(servletRequestClass, httpServletRequest, data);
				includeHeaders(servletRequestClass, httpServletRequest, data);
				if (captureSessionData) {
					includeSessionAttributes(servletRequestClass, httpServletRequest, data);
				}
			}
		} else {
			refMarker.markCall();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		if (!refMarker.isMarkerSet())
			return;

		refMarker.markEndCall();
		if (refMarker.matchesFirst()) {
			// Get the timer and store it.
			timeStack.push(new Double(timer.getCurrentTime()));
			if (threadCPUTimeEnabled) {
				threadCpuTimeStack.push(new Long(threadMXBean.getCurrentThreadCpuTime()));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		if (refMarker.isMarkerSet() && refMarker.matchesFirst()) {
			double endTime = ((Double) timeStack.pop()).doubleValue();
			double startTime = ((Double) timeStack.pop()).doubleValue();
			double duration = endTime - startTime;

			// default setting to a negative number
			double cpuDuration = -1.0d;
			if (threadCPUTimeEnabled) {
				long cpuEndTime = ((Long) threadCpuTimeStack.pop()).longValue();
				long cpuStartTime = ((Long) threadCpuTimeStack.pop()).longValue();
				cpuDuration = (cpuEndTime - cpuStartTime) / 1000000.0d;
			}

			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				long registeredMethodId = idManager.getRegisteredMethodId(methodId);
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

				HttpTimerData data = (HttpTimerData) httpTimerData.get();
				data.setPlatformIdent(platformId);
				data.setMethodIdent(registeredMethodId);
				data.setSensorTypeIdent(registeredSensorTypeId);
				data.setTimeStamp(timestamp);

				data.setDuration(duration);
				data.calculateMin(duration);
				data.calculateMax(duration);
				data.setCpuDuration(cpuDuration);
				data.calculateCpuMax(cpuDuration);
				data.calculateCpuMin(cpuDuration);
				data.setCount(1L);

				coreService.addMethodSensorData(registeredSensorTypeId, registeredMethodId, null, data);

				httpTimerData.remove();
				refMarker.remove();

			} catch (IdNotAvailableException e) {
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer("Could not save the timer data because of an unavailable id. " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Checks if the given Class is realizing the HttpServletRequest interface directly or
	 * indirectly. Only if this interface is realized, we can get Http metric information.
	 * 
	 * @param c
	 *            The class to check
	 * @return whether or not the HttpServletRequest interface is realized.
	 */
	private boolean providesHttpMetrics(Class<?> c) {
		if (whitelist.contains(c)) {
			return true;
		}
		if (blacklist.contains(c)) {
			return false;
		}
		boolean realizesInterface = checkForInterface(c, HTTP_SERVLET_REQUEST_CLASS);
		if (realizesInterface) {
			whitelist.add(c);
		} else {
			blacklist.add(c);
		}
		return realizesInterface;
	}

	/**
	 * recursively checks if the given <code>Class</code> object realizes a given interface. This is
	 * done by recursively checking the implementing interfaces of the current class, then jump to
	 * the superclass and repeat. If you reach the java.lang.Object class we know that we can stop
	 * 
	 * @param c
	 *            the <code>Class</code> object to search for
	 * @param interfaceName
	 *            the name of the interface that should be searched
	 * @return whether the given class realizes the given interface.
	 */
	private boolean checkForInterface(Class<?> c, String interfaceName) {
		if (c.getName().equals(OBJECT_CLASS)) {
			return false;
		}

		for (Class<?> clazz : c.getInterfaces()) {
			if (clazz.getName().equals(interfaceName)) {
				return true;
			}
		}

		return checkForInterface(c.getSuperclass(), interfaceName);
	}

	/**
	 * Reads the request URI from the given <code>HttpServletRequest</code> object and stores it
	 * with the given <code>HttpTimerData</code> object.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @param data
	 *            the timer data that this uri should be attached to.
	 */
	private void includeRequestUri(Class<?> httpServletRequestClass, Object httpServletRequest, HttpTimerData data) {
		Method m = retrieveMethod(HttpMethods.SERVLET_REQUEST_URI, httpServletRequestClass);
		if (null == m) {
			return;
		}

		try {
			String uri = (String) m.invoke(httpServletRequest, (Object[]) null);
			if (null != uri) {
				data.setUri(uri);
			} else {
				data.setUri(HttpTimerData.UNDEFINED);
			}
		} catch (Exception e) {
			LOGGER.severe("Invocation on given object failed. Exception was: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Reads the request URI from the given <code>HttpServletRequest</code> object and stores it
	 * with the given <code>HttpTimerData</code> object.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @param data
	 *            the timer data that this uri should be attached to.
	 */
	private void includeRequestMethod(Class<?> httpServletRequestClass, Object httpServletRequest, HttpTimerData data) {
		Method m = retrieveMethod(HttpMethods.SERVLET_GET_METHOD, httpServletRequestClass);
		if (null == m) {
			return;
		}

		try {
			String requestMethod = (String) m.invoke(httpServletRequest, (Object[]) null);
			if (null != requestMethod) {
				data.setRequestMethod(requestMethod);
			} else {
				data.setRequestMethod(HttpTimerData.UNDEFINED);
			}
		} catch (Exception e) {
			LOGGER.severe("Invocation on given object failed. Exception was: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Reads all request parameters from the given <code>HttpServletRequest</code> object and stores
	 * them with the given <code>HttpTimerData</code> object.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @param data
	 *            the timer data that this uri should be attached to.
	 */
	private void includeParameterMap(Class<?> httpServletRequestClass, Object httpServletRequest, HttpTimerData data) {
		Method m = retrieveMethod(HttpMethods.SERVLET_GET_PARAMETER_MAP, httpServletRequestClass);
		if (null == m) {
			return;
		}

		try {
			@SuppressWarnings("unchecked")
			Map<String, String[]> parameterMap = (Map<String, String[]>) m.invoke(httpServletRequest, (Object[]) null);

			if (null == parameterMap || parameterMap.isEmpty()) {
				return;
			}

			// The returned map is potentially a concrete class that is not
			// available at the CMR, thus we need to copy the information
			Map<String, String[]> internMap = new HashMap<String, String[]>(parameterMap.size());

			for (Object e : parameterMap.entrySet()) {
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) e;
				if (null == entry.getKey()) {
					continue;
				}

				Object value = entry.getValue();
				String[] convertedValue = null;
				if (null == value) {
					convertedValue = new String[1];
					convertedValue[0] = "<notset>";
				} else {
					// We know that we are dealing with String[] values, but to be
					// sure and to get better exceptions if this is not the case we
					// will check the cast
					try {
						convertedValue = (String[]) value;
					} catch (ClassCastException cce) {
						LOGGER.warning("Casting value of parameter map to String[] failed. Class is  " + value.getClass().getCanonicalName()
								+ ". For you as user this means, that you do not get the attribute value of the attribute called " + entry.getKey()
								+ ". Please report this to the inspectit team. Exception was: " + cce.getMessage());
						continue;
					}
				}
				internMap.put((String) entry.getKey(), convertedValue);
			}
			data.setParameters(internMap);
		} catch (Exception e) {
			LOGGER.severe("Invocation on given object failed. Exception was: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Reads all request attributes from the given <code>HttpServletRequest</code> object and stores
	 * them with the given <code>HttpTimerData</code> object.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @param data
	 *            the timer data that this uri should be attached to.
	 */
	private void includeAttributes(Class<?> httpServletRequestClass, Object httpServletRequest, HttpTimerData data) {
		Method attributesMethod = retrieveMethod(HttpMethods.SERVLET_GET_ATTRIBUTE_NAMES, httpServletRequestClass);
		if (null == attributesMethod) {
			return;
		}

		Method attributeValue = retrieveMethod(HttpMethods.SERVLET_GET_ATTRIBUTE, httpServletRequestClass);
		if (null == attributeValue) {
			return;
		}

		try {
			@SuppressWarnings("unchecked")
			Enumeration<String> params = (Enumeration<String>) attributesMethod.invoke(httpServletRequest, (Object[]) null);
			Map<String, String> attributes = new HashMap<String, String>();
			if (null == params) {
				LOGGER.finer("Attribute enumeration was <null>");
				return;
			}
			while (params.hasMoreElements()) {
				String attrName = params.nextElement();
				Object value = attributeValue.invoke(httpServletRequest, new Object[] { attrName });
				if (null != value) {
					attributes.put(attrName, value.toString());
				} else {
					attributes.put(attrName, "<null>");
				}

			}
			data.setAttributes(attributes);
		} catch (Exception e) {
			LOGGER.severe("Invocation of " + attributesMethod.getName() + " to get attributes on given object failed. Exception was: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Reads all headers from the given <code>HttpServletRequest</code> object and stores them with
	 * the given <code>HttpTimerData</code> object.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @param data
	 *            the timer data that this uri should be attached to.
	 */
	private void includeHeaders(Class<?> httpServletRequestClass, Object httpServletRequest, HttpTimerData data) {
		Method headerNamesMethod = retrieveMethod(HttpMethods.SERVLET_GET_HEADER_NAMES, httpServletRequestClass);
		if (null == headerNamesMethod) {
			return;
		}

		Method headerValueMethod = retrieveMethod(HttpMethods.SERVLET_GET_HEADER, httpServletRequestClass);
		if (null == headerValueMethod) {
			return;
		}

		try {
			@SuppressWarnings("unchecked")
			Enumeration<String> headers = (Enumeration<String>) headerNamesMethod.invoke(httpServletRequest, (Object[]) null);
			Map<String, String> headersResult = new HashMap<String, String>();
			if (headers != null) {
				while (headers.hasMoreElements()) {
					String headerName = (String) headers.nextElement();
					String headerValue = (String) headerValueMethod.invoke(httpServletRequest, new Object[] { headerName });
					headersResult.put(headerName, headerValue);
				}
				data.setHeaders(headersResult);
			}
		} catch (Exception e) {
			LOGGER.severe("Invocation of to get attributes on given object failed. Exception was: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Reads all session attributes from the <code>HttpSession</code> of the given
	 * <code>HttpServletRequest</code> object and stores them with the given
	 * <code>HttpTimerData</code> object. This method ensures that no new session will be created.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @param data
	 *            the timer data that this uri should be attached to.
	 */
	@SuppressWarnings("unchecked")
	private void includeSessionAttributes(Class<?> httpServletRequestClass, Object httpServletRequest, HttpTimerData data) {
		Method getSessionMethod = retrieveMethod(HttpMethods.SERVLET_GET_SESSION, httpServletRequestClass);

		if (null == getSessionMethod) { // Could not retrieve method
			return;
		}

		Object httpSession;
		Class<?> httpSessionClass;
		try {
			httpSession = getSessionMethod.invoke(httpServletRequest, new Object[] { Boolean.FALSE });
			if (httpSession == null) {
				// Currently we do not have a session and thus cannot get any session attributes
				LOGGER.finer("No session can be found");
				return;
			}
			httpSessionClass = httpSession.getClass();

		} catch (Exception e) {
			LOGGER.severe("Invocation of to get attributes on given object failed. Exception was: " + e.getMessage());
			e.printStackTrace();
			// we cannot go on!
			return;
		}

		Method getAttributeNamesSession = retrieveMethod(HttpMethods.SESSION_GET_ATTRIBUTE_NAMES, httpSessionClass);
		if (null == getAttributeNamesSession) {
			return;
		}

		Method getAttributeValueSession = retrieveMethod(HttpMethods.SESSION_GET_ATTRIBUTE, httpSessionClass);
		if (null == getAttributeValueSession) {
			return;
		}

		try {
			Enumeration<String> sessionAttr = (Enumeration<String>) getAttributeNamesSession.invoke(httpSession, (Object[]) null);
			Map<String, Object> sessionAttributes = new HashMap<String, Object>();

			if (null != sessionAttr) {
				while (sessionAttr.hasMoreElements()) {
					String sessionAtt = sessionAttr.nextElement();
					Object sessionValue = (Object) getAttributeValueSession.invoke(httpSession, sessionAtt);
					if (null != sessionValue) {
						sessionAttributes.put(sessionAtt, sessionValue.toString());
					} else {
						sessionAttributes.put(sessionAtt, "<notset>");
					}
				}
				data.setSessionAttributes(sessionAttributes);
			}
		} catch (Exception e) {
			LOGGER.severe("Invocation of to get attributes on given object failed. Exception was: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Tries a lookup in the cache first, then tries to get the <code>Method</code> object via
	 * reflection.
	 * 
	 * @param httpMethod
	 *            the Method to lookup
	 * @param clazzUsedToLookup
	 *            the class to use if reflection lookup is necessary (if it is not already in the
	 *            cache)
	 * @return the <code>Method</code> object or <code>null</code> if the method cannot be found.
	 */
	private Method retrieveMethod(HttpMethods httpMethod, Class<?> clazzUsedToLookup) {
		Method m = methodCache.get(httpMethod);

		if (null == m) {
			// We do not yet have the method in the Cache
			try {
				m = clazzUsedToLookup.getMethod(httpMethod.methodName, httpMethod.parameters);
				methodCache.put(httpMethod, m);
			} catch (Exception e) {
				LOGGER.severe("The provided class " + clazzUsedToLookup.getCanonicalName() + " did not provide the desired method. Exception was: " + e.getMessage());
				e.printStackTrace();

				// Do not try to look up every time.
				methodCache.put(httpMethod, null);
			}
		}

		return m;
	}
}
