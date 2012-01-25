package info.novatec.inspectit.agent.sensor.method.http;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sensor.method.timer.TimerHook;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.util.StringConstraint;
import info.novatec.inspectit.util.ThreadLocalStack;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ThreadMXBean;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;
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
	private final ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

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
	private final ThreadLocalStack<Long> threadCpuTimeStack = new ThreadLocalStack<Long>();

	/**
	 * Extractor for Http parameters.
	 */
	private HttpRequestParameterExtractor extractor;

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
	private static final CopyOnWriteArrayList<Class<?>> WHITE_LIST = new CopyOnWriteArrayList<Class<?>>();

	/**
	 * Blacklist that contains all classes that we already checked if they provide
	 * HttpServletMetrics and do not. We are talking about the class of the ServletRequest here.
	 * This list is extended if a new Class that does not provides this interface is found.
	 */
	private static final CopyOnWriteArrayList<Class<?>> BLACK_LIST = new CopyOnWriteArrayList<Class<?>>();

	/**
	 * Helps us to ensure that we only store on http metric per request.
	 */
	private final StartEndMarker refMarker = new StartEndMarker();

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
	public HttpHook(Timer timer, IIdManager idManager, Map<String, Object> parameters, ThreadMXBean threadMXBean) {
		this.timer = timer;
		this.idManager = idManager;
		this.threadMXBean = threadMXBean;
		this.extractor = new HttpRequestParameterExtractor(new StringConstraint(parameters));

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
				data.setUri(extractor.getRequestUri(servletRequestClass, httpServletRequest));
				data.setRequestMethod(extractor.getRequestMethod(servletRequestClass, httpServletRequest));
				data.setParameters(extractor.getParameterMap(servletRequestClass, httpServletRequest));
				data.setAttributes(extractor.getAttributes(servletRequestClass, httpServletRequest));
				data.setHeaders(extractor.getHeaders(servletRequestClass, httpServletRequest));
				if (captureSessionData) {
					data.setSessionAttributes(extractor.getSessionAttributes(servletRequestClass, httpServletRequest));
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
		if (WHITE_LIST.contains(c)) {
			return true;
		}
		if (BLACK_LIST.contains(c)) {
			return false;
		}
		boolean realizesInterface = checkForInterface(c, HTTP_SERVLET_REQUEST_CLASS);
		if (realizesInterface) {
			WHITE_LIST.add(c);
		} else {
			BLACK_LIST.add(c);
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

}
