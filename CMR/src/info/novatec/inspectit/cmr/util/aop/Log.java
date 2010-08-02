package info.novatec.inspectit.cmr.util.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for methods that will be logged and/or profiled, By placing this annotation on a method
 * spring will proxy the service and call the interceptor that provides advice to the real method
 * call.
 * 
 * @author Patrice Bouillet
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Log {

	/**
	 * The log level which can be used. The level from log4j cannot be used directly as it is not
	 * allowed as a return type.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public enum Level {
		OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL;
	}

	/**
	 * The defined level on which the time messages shall be printed to.
	 * 
	 * @return the current set time log level.
	 */
	Level timeLogLevel() default Level.DEBUG;

	/**
	 * The defined level on which the trace messages shall be printed to.
	 * 
	 * @return the current set trace log level.
	 */
	Level traceLogLevel() default Level.TRACE;

	/**
	 * Defines a duration limit on this method. If the methods duration exceed the specified one, a
	 * message will be printed into the log.
	 * 
	 * @return the current set duration limit. '-1' means, that the duration limit check is not
	 *         active.
	 */
	long durationLimit() default -1;

}
