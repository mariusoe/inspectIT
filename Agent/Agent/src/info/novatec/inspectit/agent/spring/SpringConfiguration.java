package info.novatec.inspectit.agent.spring;

import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.StrategyConfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Post process configuration storage to define buffer and sending strategy beans.
 * 
 * @author Ivan Senic
 * 
 */
@Configuration
@ComponentScan("info.novatec.inspectit")
public class SpringConfiguration implements BeanDefinitionRegistryPostProcessor {

	/**
	 * Registry to add bean definitions to.
	 */
	private BeanDefinitionRegistry registry;

	/**
	 * Bean factory to force initialization of manually defined beans.
	 */
	private ConfigurableListableBeanFactory beanFactory;

	/**
	 * {@inheritDoc}
	 */
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		this.registry = registry;
	}

	/**
	 * Registers components needed by the configuration to the Spring container.
	 * 
	 * @param configurationStorage
	 *            {@link IConfigurationStorage} with the settings.
	 * @throws Exception
	 *             If exception occurs during the registration.
	 */
	public void registerComponents(IConfigurationStorage configurationStorage) throws Exception {
		// buffer strategy
		String className = configurationStorage.getBufferStrategyConfig().getClazzName();
		String beanName = "bufferStrategy[" + className + "]";
		registerBeanDefinitionAndInitialize(beanName, className);

		// sending strategies
		for (StrategyConfig sendingStrategyConfig : configurationStorage.getSendingStrategyConfigs()) {
			className = sendingStrategyConfig.getClazzName();
			beanName = "sendingStrategy[" + className + "]";
			registerBeanDefinitionAndInitialize(beanName, className);
		}

		// platform sensor types
		for (PlatformSensorTypeConfig platformSensorTypeConfig : configurationStorage.getPlatformSensorTypes()) {
			className = platformSensorTypeConfig.getClassName();
			beanName = "platformSensorType[" + className + "]";
			registerBeanDefinitionAndInitialize(beanName, className);
		}

		// method sensor types
		for (MethodSensorTypeConfig methodSensorTypeConfig : configurationStorage.getMethodSensorTypes()) {
			className = methodSensorTypeConfig.getClassName();
			beanName = "methodSensorType[" + className + "]";
			registerBeanDefinitionAndInitialize(beanName, className);
		}

	}

	/**
	 * Creates bean definition for the given class name, registers the definition in the registry
	 * and immediately invokes the initialization of the bean.
	 * <p>
	 * <i>This is the only way to initialize the bean definitions that no other component has
	 * dependency to, since we add the definitions in the moment when the lookup has been finished
	 * and bean creation has started.</i>
	 * 
	 * @param beanName
	 *            Name of the bean to register.
	 * @param className
	 *            Class name of the bean.
	 * @throws ClassNotFoundException
	 *             If class can not be founded.
	 */
	private void registerBeanDefinitionAndInitialize(String beanName, String className) throws ClassNotFoundException {
		Class<?> clazz = Class.forName(className);
		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setBeanClass(clazz);
		definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
		definition.setAutowireCandidate(true);
		registry.registerBeanDefinition(beanName, definition);
		beanFactory.getBean(beanName, clazz);
	}
}
