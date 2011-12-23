package info.novatec.inspectit.cmr.service;

import java.awt.Component;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated class is a "Service" interface (e.g. a business service facade).
 * 
 * <p>
 * This annotation serves as a specialization of {@link Component @Component}, allowing for
 * interface classes to be autodetected through classpath scanning.
 * 
 * @author Henno Vermeulen
 * @author Patrice Bouillet
 * @see Component
 * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceInterface {

	/**
	 * The value may indicate a suggestion for a logical component name, to be turned into a Spring
	 * bean in case of an autodetected component.
	 * 
	 * @return the suggested component name, if any
	 */
	String name() default "";

	/**
	 * Defines the exporter used for exposing this service. Valid values are defined in the
	 * enumeration {@link ServiceExporterType}.
	 * 
	 * @return the defined exporter type.
	 */
	ServiceExporterType exporter();

	/**
	 * The service port if type {@link ServiceExporterType#RMI} is used.
	 * 
	 * @return the specified service port.
	 */
	String servicePort() default "";

	/**
	 * The registry port if type {@link ServiceExporterType#RMI} is used.
	 * 
	 * @return the specified registry port.
	 */
	String registryPort() default "";

}