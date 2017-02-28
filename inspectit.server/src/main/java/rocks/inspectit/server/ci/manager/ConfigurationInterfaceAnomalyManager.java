package rocks.inspectit.server.ci.manager;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBException;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionGroupConfiguration;
import rocks.inspectit.shared.cs.jaxb.ISchemaVersionAware;

/**
 * @author Marius Oehler
 *
 */
@Component
@DependsOn("configurationInterfaceManager")
public class ConfigurationInterfaceAnomalyManager extends AbstractConfigurationInterfaceManager {

	@Log
	private Logger log;

	private ConcurrentHashMap<String, AnomalyDetectionGroupConfiguration> existingGroupConfigurations;

	// ************************************************************
	// ************************************************************
	// ************************************************************

	public List<AnomalyDetectionGroupConfiguration> getAnomalyDetectionGroupConfigurations() {
		if (MapUtils.isEmpty(existingGroupConfigurations)) {
			return Collections.emptyList();
		}
		return new ArrayList<>(existingGroupConfigurations.values());
	}

	public AnomalyDetectionGroupConfiguration createAnomalyDetectionConfigurationGroup(AnomalyDetectionGroupConfiguration groupConfiguration) throws JAXBException, IOException {
		// alertingDefinition.setId(getRandomUUIDString());
		groupConfiguration.setCreatedDate(new Date());

		// existingAlertingDefinitions.put(alertingDefinition.getId(), alertingDefinition);
		saveAnomalyDetectionConfigurationGroup(groupConfiguration);

		// eventPublisher.publishEvent(new
		// AbstractAlertingDefinitionEvent.AlertingDefinitionCreatedEvent(this,
		// alertingDefinition));

		return groupConfiguration;
	}

	// ************************************************************
	// ************************************************************
	// ************************************************************

	private void saveAnomalyDetectionConfigurationGroup(AnomalyDetectionGroupConfiguration configurationGroup) throws JAXBException, IOException {
		transformator.marshall(pathResolver.getAnomalyDetectionConfigurationFilePath(configurationGroup), configurationGroup, getRelativeToSchemaPath(pathResolver.getDefaultCiPath()).toString());
	}

	/**
	 * Loads all existing alerting definitions.
	 */
	public void loadExistingAnomalyDetectionGroupConfigurations() {
		if (existingGroupConfigurations != null) {
			log.info("The existing anomaly detection configurations have been already loaded.");
			return;
		}

		log.info("|-Loading the existing anomaly detection configurations..");
		existingGroupConfigurations = new ConcurrentHashMap<>(16, 0.75f, 2);

		Path path = pathResolver.getAnomalyDetectionConfigurationsPath();

		if (Files.notExists(path)) {
			log.info("Default anomaly detection configuration path does not exists. No configurations are loaded.");
			return;
		}

		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (isXmlFile(file)) {
						try {
							AnomalyDetectionGroupConfiguration groupConfiguration = transformator.unmarshall(file, pathResolver.getSchemaPath(),
									ISchemaVersionAware.ConfigurationInterface.SCHEMA_VERSION, pathResolver.getMigrationPath(), AnomalyDetectionGroupConfiguration.class);
							existingGroupConfigurations.put(groupConfiguration.getId(), groupConfiguration);
						} catch (JAXBException | SAXException e) {
							log.error("Error reading existing anomaly detection configuration file. File path: " + file.toString() + ".", e);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("Error exploring anomaly detection configurations directory. Directory path: " + path.toString() + ".", e);
		}

		if (MapUtils.isEmpty(existingGroupConfigurations)) {
			log.info("No anomaly detection configurations are in the default path.");
		}

		// eventPublisher.publishEvent(new
		// AbstractAlertingDefinitionEvent.AlertingDefinitionLoadedEvent(this, new
		// ArrayList<>(existingAlertingDefinitions.values())));
	}
}
