package info.novatec.inspectit.cmr.property;

import info.novatec.inspectit.cmr.property.configuration.Configuration;
import info.novatec.inspectit.cmr.property.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.validation.ValidationError;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Properties manager bean that controls all properties specified in the configuration files and
 * provides the {@link Properties} object as a bean for the Spring context property-placeholder.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class PropertyManager {

	/**
	 * The logger of this class.
	 * <p>
	 * Must be declared manually since loading of the properties is done before beans
	 * post-processing.
	 */
	private static final Log LOG = LogFactory.getLog(PropertyManager.class);

	/**
	 * Name of the local properties bean that will be created.
	 */
	public static final String LOCAL_PROPERTIES_BEAN_NAME = "localPropertiesBean";

	/**
	 * Directory where configuration files are places.
	 */
	private static final String CONFIG_DIR = "config";

	/**
	 * File name where default configuration is stored.
	 */
	private static final String DEFAULT_CONFIG_FILE = "default.xml";

	/**
	 * Default configuration.
	 */
	private Configuration defaultConfiguration;

	/**
	 * Returns {@link Properties} containing key/value property pairs defined in CMR configuration.
	 * 
	 * @return Returns {@link Properties} containing key/value property pairs defined in CMR
	 *         configuration.
	 */
	@Bean(name = { LOCAL_PROPERTIES_BEAN_NAME })
	public Properties getProperties() {
		try {
			loadDefaultConfiguration();
		} catch (JAXBException | IOException e) {
			LOG.warn("|-Default CMR configuration can not be loaded.", e);
			return new Properties();
		}

		// validate configuration
		Map<AbstractProperty, PropertyValidation> validationMap = defaultConfiguration.validate();

		// if we have some validation problems log them
		if (MapUtils.isNotEmpty(validationMap)) {
			for (Entry<AbstractProperty, PropertyValidation> entry : validationMap.entrySet()) {
				LOG.warn("|-Property '" + entry.getKey().getName() + "' has not pass validation test because of the " + entry.getValue().getErrorCount() + " errors:");
				for (ValidationError validationError : entry.getValue().getErrors()) {
					LOG.warn("||- " + validationError.getMessage());
				}
			}
		} else {
			LOG.info("|-Default configuration verified with no errors");
		}

		// create properties from correct ones
		Properties properties = new Properties();
		for (AbstractProperty property : defaultConfiguration.getAllProperties()) {
			if (!validationMap.containsKey(property)) {
				property.register(properties);
			}
		}
		return properties;
	}

	/**
	 * Unmarshalls the given file. The root class of the XML must be given.
	 * 
	 * @param <T>
	 *            Type of root object.
	 * @param path
	 *            Path to file to unmarshall.
	 * @param rootClass
	 *            Root class of the XML document.
	 * @return Unmarshalled object.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during loading.
	 * @throws IOException
	 *             If {@link IOException} occurs during loading.
	 */
	@SuppressWarnings("unchecked")
	private <T> T unmarshall(Path path, Class<T> rootClass) throws JAXBException, IOException {
		JAXBContext context = JAXBContext.newInstance(rootClass);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		if (Files.notExists(path) || Files.isDirectory(path)) {
			return null;
		}

		try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
			return (T) unmarshaller.unmarshal(inputStream);
		}
	}

	/**
	 * Loads the default configuration if it is not already loaded. Is successfully loaded
	 * configuration will be placed in the {@link #defaultConfiguration} field.
	 * 
	 * 
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during loading.
	 * @throws IOException
	 *             If {@link IOException} occurs during loading.
	 */
	@PostConstruct
	void loadDefaultConfiguration() throws JAXBException, IOException {
		LOG.info("|-Loading the default CMR configuration");
		defaultConfiguration = unmarshall(Paths.get(CONFIG_DIR, DEFAULT_CONFIG_FILE), Configuration.class);
	}
}
