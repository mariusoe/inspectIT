package info.novatec.inspectit.cmr.util.aop;

import info.novatec.inspectit.cmr.util.Converter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.aop.support.AopUtils;

/**
 * The logging interceptor which will be actived for each method being annotated with @{@link Log}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class LoggingInterceptor implements MethodInterceptor {

	/**
	 * The message printed in the log if the specified duration has been exceeded.
	 */
	private static final String DURATION_THRESHOLD_MSG = "WARNING: Duration threshold (%s ms) exceeded for method '%s': %s ms";

	/**
	 * The message printed in the log if the specified duration has been exceeded.
	 */
	private static final String DURATION_THRESHOLD_MSG_W_TRACE = "    WARNING: Duration threshold (%s ms) exceeded for method '%s': %s ms";

	/**
	 * The log format for the printing of the method duration.
	 */
	private static final String TIME_LOG_FORMAT = "'%s' duration: %s ms";

	/**
	 * The log format for the printing of the method duration if the trace level is active, too.
	 */
	private static final String TIME_LOG_FORMAT_W_TRACE = "    '%s' duration: %s ms";

	/**
	 * The enter format String for the trace level.
	 */
	private static final String TRACE_ENTER_FORMAT = "--> %s#%s()";

	/**
	 * The exit format String for the trace level.
	 */
	private static final String TRACE_EXIT_FORMAT = "<-- %s#%s()";

	/**
	 * The regular expression to split the method names.
	 */
	private static final String SPLIT_METHOD_REGEX = "(?=\\p{Lu})";

	/**
	 * The pre-compiled Pattern object out of the defined regular expression.
	 */
	private static final Pattern SPLIT_METHOD_PATTERN = Pattern.compile(SPLIT_METHOD_REGEX);

	/**
	 * This map holds the mapping between the log levels defined in the aop log and the log4j log
	 * level. Please look at class {@link Log} for the reason for this.
	 */
	private static final Map<Log.Level, Level> LEVELS = new HashMap<Log.Level, Level>(8, 1.0f);

	static {
		// initialize all the available levels
		LEVELS.put(Log.Level.ALL, Level.ALL);
		LEVELS.put(Log.Level.DEBUG, Level.DEBUG);
		LEVELS.put(Log.Level.ERROR, Level.ERROR);
		LEVELS.put(Log.Level.FATAL, Level.FATAL);
		LEVELS.put(Log.Level.INFO, Level.INFO);
		LEVELS.put(Log.Level.OFF, Level.OFF);
		LEVELS.put(Log.Level.TRACE, Level.TRACE);
		LEVELS.put(Log.Level.WARN, Level.WARN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(MethodInvocation invoc) throws Throwable {
		Method mostSpecificMethod = AopUtils.getMostSpecificMethod(invoc.getMethod(), invoc.getThis().getClass());
		Log log = mostSpecificMethod.getAnnotation(Log.class);
		Logger logger = Logger.getLogger(invoc.getThis().getClass());
		Level timeLogLevel = LEVELS.get(log.timeLogLevel());
		Level traceLogLevel = LEVELS.get(log.traceLogLevel());

		if (logger.isEnabledFor(traceLogLevel)) {
			logger.log(traceLogLevel, String.format(TRACE_ENTER_FORMAT, mostSpecificMethod.getDeclaringClass().getName(), mostSpecificMethod.getName()));
		}

		long startTime = System.nanoTime();
		Object object = invoc.proceed();
		long endTime = System.nanoTime();
		double duration = Converter.nanoToMilliseconds(endTime - startTime);

		String methodName = null;
		if (logger.isEnabledFor(timeLogLevel)) {
			methodName = convertMethodName(mostSpecificMethod.getName());
			String formatString;
			if (logger.isEnabledFor(traceLogLevel)) {
				formatString = TIME_LOG_FORMAT_W_TRACE;
			} else {
				formatString = TIME_LOG_FORMAT;
			}
			logger.log(timeLogLevel, String.format(formatString, methodName, duration));
		}

		if (-1 != log.durationLimit() && duration > log.durationLimit()) {
			if (null == methodName) {
				methodName = convertMethodName(mostSpecificMethod.getName());
			}
			String formatString;
			if (logger.isEnabledFor(traceLogLevel)) {
				formatString = DURATION_THRESHOLD_MSG_W_TRACE;
			} else {
				formatString = DURATION_THRESHOLD_MSG;
			}
			logger.log(Level.WARN, String.format(formatString, log.durationLimit(), methodName, duration));
		}

		if (logger.isEnabledFor(traceLogLevel)) {
			logger.log(traceLogLevel, String.format(TRACE_EXIT_FORMAT, mostSpecificMethod.getDeclaringClass().getName(), mostSpecificMethod.getName()));
		}

		return object;
	}

	/**
	 * Converts the method name into something more 'readable'<br>
	 * getMyName: 'Get My Name'<br>
	 * loadAllPersons: 'Load All Persons'
	 * 
	 * @param name
	 *            The original method name.
	 * @return The converted readable name string.
	 */
	private String convertMethodName(String name) {
		// split the name string at each uppercase char
		String[] r = SPLIT_METHOD_PATTERN.split(name, 0);
		StringBuilder builder = new StringBuilder();

		String first = r[0];
		// first character to upper case
		builder.append(Character.toUpperCase(first.charAt(0)));
		builder.append(first.substring(1));
		for (int i = 1; i < r.length; i++) {
			builder.append(" ");
			builder.append(r[i]);
		}

		return builder.toString();
	}

}
