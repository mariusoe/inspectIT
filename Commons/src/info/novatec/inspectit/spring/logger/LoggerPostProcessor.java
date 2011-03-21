package info.novatec.inspectit.spring.logger;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/**
 * Component to inject the {@link Log} to each bean that has a {@link Logger} annotation.
 * 
 * @author Patrice Bouillet
 * 
 */
@Component
public class LoggerPostProcessor implements BeanPostProcessor {

	/**
	 * {@inheritDoc}
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		return bean;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object postProcessBeforeInitialization(final Object bean, String beanName) {
		ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {
			public void doWith(Field field) throws IllegalAccessException {
				if (field.getAnnotation(Logger.class) != null) {
					Log log = LogFactory.getLog(bean.getClass());
					ReflectionUtils.makeAccessible(field);
					field.set(bean, log);
				}
			}
		});
		return bean;
	}

}