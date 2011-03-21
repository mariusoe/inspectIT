package info.novatec.inspectit.rcp.storage.util;

import info.novatec.inspectit.spring.logger.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/**
 * This class solves a problem with Spring loading with classes coming from two different bundles.
 * It is necessary to load a org.apache.commons.logging.LogFactory from the same class-loader as the
 * class/field that has to be enriched, because setting of the field will fail.
 * 
 * @author Ivan Senic
 * 
 */
public class InspectITLoggerPostProcessor implements BeanPostProcessor {

	/**
	 * Log factory class FQN.
	 */
	private static final String LOG_FACTORY_CLASS = "org.apache.commons.logging.LogFactory";

	/**
	 * Method name for getting the logger.
	 */
	private static final String GET_LOG_METHOD = "getLog";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object postProcessBeforeInitialization(final Object bean, String beanName) throws BeansException {
		ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {
			public void doWith(Field field) throws IllegalAccessException {
				if (field.getAnnotation(Logger.class) != null) {
					ClassLoader cl = field.getDeclaringClass().getClassLoader();
					try {
						Class<?> clazz = cl.loadClass(LOG_FACTORY_CLASS);
						Method m = clazz.getDeclaredMethod(GET_LOG_METHOD, new Class[] { String.class });
						Object logger = m.invoke(null, bean.getClass().getName());
						ReflectionUtils.makeAccessible(field);
						field.set(bean, logger);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		return bean;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
