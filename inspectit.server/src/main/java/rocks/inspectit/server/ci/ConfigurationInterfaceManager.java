package rocks.inspectit.server.ci;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import rocks.inspectit.server.ci.event.AbstractAlertingDefinitionEvent;
import rocks.inspectit.server.ci.event.AgentMappingsUpdateEvent;
import rocks.inspectit.server.ci.event.BusinessContextDefinitionUpdateEvent;
import rocks.inspectit.server.ci.event.EnvironmentUpdateEvent;
import rocks.inspectit.server.ci.event.ProfileUpdateEvent;
import rocks.inspectit.server.ci.manager.ConfigurationInterfaceAnomalyManager;
import rocks.inspectit.server.util.CollectionSubtractUtils;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.AlertErrorCodeEnum;
import rocks.inspectit.shared.all.exception.enumeration.ConfigurationInterfaceErrorCodeEnum;
import rocks.inspectit.shared.all.serializer.impl.SerializationManager;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.AgentMapping;
import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.shared.cs.ci.BusinessContextDefinition;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.alerting.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.export.ConfigurationInterfaceImportData;
import rocks.inspectit.shared.cs.jaxb.ISchemaVersionAware;
import rocks.inspectit.shared.cs.jaxb.JAXBTransformator;

/**
 * Manages all configuration interface operations.
 *
 * @author Ivan Senic
 * @author Marius Oehler
 *
 */
@SuppressWarnings({ "PMD.ExcessiveClassLength" })
@Component
public class ConfigurationInterfaceManager {

	/**
	 * The logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * Path resolver.
	 */
	@Autowired
	private ConfigurationInterfacePathResolver pathResolver;

	/**
	 * Spring {@link ApplicationEventPublisher} for publishing the events.
	 */
	@Autowired
	private ApplicationEventPublisher eventPublisher;

	/**
	 * The used {@link SerializationManager}.
	 */
	@Autowired
	SerializationManager serializationManager;

	/**
	 * {@link JAXBTransformator}.
	 */
	private final JAXBTransformator transformator = new JAXBTransformator();

	/**
	 * Existing profiles in the system mapped by the id.
	 */
	private ConcurrentHashMap<String, Profile> existingProfiles;

	/**
	 * Existing environments in the system mapped by the id.
	 */
	private ConcurrentHashMap<String, Environment> existingEnvironments;

	/**
	 * Existing environments in the system mapped by the id.
	 */
	private ConcurrentHashMap<String, AlertingDefinition> existingAlertingDefinitions;

	/**
	 * Currently used agent mapping.
	 */
	private final AtomicReference<AgentMappings> agentMappingsReference = new AtomicReference<>();

	/**
	 * Business context definition.
	 */
	private final AtomicReference<BusinessContextDefinition> businessContextDefinitionReference = new AtomicReference<>();

	@Autowired
	private ConfigurationInterfaceAnomalyManager anomalyManager;

	/**
	 * Returns all existing profiles.
	 *
	 * @return Returns all existing profiles.
	 */
	public List<Profile> getAllProfiles() {
		return new ArrayList<>(existingProfiles.values());
	}

	/**
	 * Returns the profile with the given id.
	 *
	 * @param id
	 *            Id of profile.
	 * @return {@link Profile}
	 * @throws BusinessException
	 *             If profile with given id does not exist.
	 */
	public Profile getProfile(String id) throws BusinessException {
		Profile profile = existingProfiles.get(id);
		if (null == profile) {
			throw new BusinessException("Load profile with the id=" + id + ".", ConfigurationInterfaceErrorCodeEnum.PROFILE_DOES_NOT_EXIST);
		}
		return profile;
	}

	/**
	 * Creates new profile.
	 *
	 * @param profile
	 *            Profile template.
	 * @return Returns created profile with correctly set id.
	 * @throws BusinessException
	 *             If attempt is made to create common profile.
	 * @throws IOException
	 *             If {@link IOException} occurs during save.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during save.
	 */
	public Profile createProfile(Profile profile) throws BusinessException, JAXBException, IOException {
		if (null == profile.getProfileData()) {
			throw new BusinessException("Create new profile.", ConfigurationInterfaceErrorCodeEnum.PROFILE_DOES_NOT_HAVE_CORRECT_PROFILE_DATA);
		}

		profile.setId(getRandomUUIDString());
		profile.setCreatedDate(new Date());
		existingProfiles.put(profile.getId(), profile);
		saveProfile(profile);
		return profile;
	}

	/**
	 * Imports the profile. Note that if profile with the same id already exists it will be
	 * overwritten.
	 *
	 * @param profile
	 *            Profile.
	 * @return Returns created/updated profile depending if the overwrite was executed.
	 * @throws BusinessException
	 *             If attempt is made to import common profile or profile without the id.
	 * @throws IOException
	 *             If {@link IOException} occurs during save.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during save.
	 */
	public Profile importProfile(Profile profile) throws BusinessException, JAXBException, IOException {
		if (null == profile.getId()) {
			throw new BusinessException("Import the profile '" + profile.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.IMPORT_DATA_NOT_VALID);
		}

		profile.setImportDate(new Date());
		if (existingProfiles.containsKey(profile.getId())) {
			Profile old = existingProfiles.replace(profile.getId(), profile);
			Files.deleteIfExists(pathResolver.getProfileFilePath(old));
		} else {
			existingProfiles.put(profile.getId(), profile);
		}

		saveProfile(profile);
		return profile;
	}

	/**
	 * Updates the given profile and saves it to the disk. Update will fail with an Exception if:
	 * <ul>
	 * <li>Attempt is made to update default profile.
	 * <li>Profile does not exists on the CMR.
	 * <li>Profile revision sequence does not match the current sequence.
	 * </ul>
	 *
	 * @param profile
	 *            Profile to update.
	 * @return updated profile instance
	 * @throws BusinessException
	 *             If update of common profile is attempted.
	 * @throws IOException
	 *             If {@link IOException} occurs during update.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during update.
	 */
	public synchronized Profile updateProfile(Profile profile) throws BusinessException, JAXBException, IOException {
		if (profile.isCommonProfile()) {
			throw new BusinessException("Update the profile '" + profile.getName() + ".", ConfigurationInterfaceErrorCodeEnum.COMMON_PROFILE_CAN_NOT_BE_ALTERED);
		}

		return updateProfileInternal(profile);
	}

	/**
	 * Deletes the existing profile.
	 *
	 * @param profile
	 *            Profile to delete.
	 * @throws IOException
	 *             If {@link IOException} occurs during update.
	 * @throws BusinessException
	 *             If profile is common profile.
	 */
	public void deleteProfile(Profile profile) throws BusinessException, IOException {
		if (profile.isCommonProfile()) {
			throw new BusinessException("Delete the profile '" + profile.getName() + ".", ConfigurationInterfaceErrorCodeEnum.COMMON_PROFILE_CAN_NOT_BE_ALTERED);
		}

		String id = profile.getId();
		Profile local = existingProfiles.get(id);
		if (null != local) {
			Files.deleteIfExists(pathResolver.getProfileFilePath(local));

			for (Environment environment : existingEnvironments.values()) {
				if (CollectionUtils.isEmpty(environment.getProfileIds())) {
					continue;
				}

				if (environment.getProfileIds().contains(id)) {
					environment = copy(environment);
					environment.getProfileIds().remove(id);
					try {
						updateEnvironment(environment, false);
					} catch (Exception e) {
						log.error("Update of the environment on the profile deletion failed.", e);
					}
				}
			}
			existingProfiles.remove(id);
		}
	}

	/**
	 * Returns all existing environment.
	 *
	 * @return Returns all existing environment.
	 */
	public Collection<Environment> getAllEnvironments() {
		return new ArrayList<>(existingEnvironments.values());
	}

	/**
	 * Returns the environment with the given id.
	 *
	 * @param id
	 *            Id of environment.
	 * @return {@link Environment}
	 * @throws BusinessException
	 *             If environment with given id does not exist.
	 */
	public Environment getEnvironment(String id) throws BusinessException {
		Environment environment = existingEnvironments.get(id);
		if (null == environment) {
			throw new BusinessException("Load environemnt with the id=" + id + ".", ConfigurationInterfaceErrorCodeEnum.ENVIRONMENT_DOES_NOT_EXIST);
		}
		return environment;
	}

	/**
	 * Creates new environment.
	 *
	 * @param environment
	 *            Environment template.
	 * @return Returns created environment with correctly set id
	 * @throws IOException
	 *             If {@link IOException} occurs during create.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during create.
	 */
	public Environment createEnvironment(Environment environment) throws JAXBException, IOException {
		environment.setId(getRandomUUIDString());

		// add the default include profiles
		Set<String> profileIds = new HashSet<>();
		for (Profile profile : existingProfiles.values()) {
			if (profile.isDefaultProfile()) {
				profileIds.add(profile.getId());
			}
		}
		environment.setProfileIds(profileIds);
		environment.setCreatedDate(new Date());

		existingEnvironments.put(environment.getId(), environment);
		saveEnvironment(environment);
		return environment;
	}

	/**
	 * Imports the environment. Note that if environment with the same id already exists it will be
	 * overwritten.
	 *
	 * @param environment
	 *            Environment.
	 * @return Returns created/updated environment depending if the overwrite was executed.
	 * @throws BusinessException
	 *             If attempt is made to import environment without the id.
	 * @throws IOException
	 *             If {@link IOException} occurs during save.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during save.
	 */
	public Environment importEnvironment(Environment environment) throws BusinessException, JAXBException, IOException {
		if (null == environment.getId()) {
			throw new BusinessException("Import the environment '" + environment.getName() + "'.", ConfigurationInterfaceErrorCodeEnum.IMPORT_DATA_NOT_VALID);
		}

		environment.setImportDate(new Date());
		if (existingEnvironments.containsKey(environment.getId())) {
			Environment old = existingEnvironments.replace(environment.getId(), environment);
			Files.deleteIfExists(pathResolver.getEnvironmentFilePath(old));
		} else {
			existingEnvironments.put(environment.getId(), environment);
		}

		checkProfiles(environment);
		saveEnvironment(environment);
		return environment;
	}

	/**
	 * Updates the given environment and saves it to the disk. Update will fail with an Exception
	 * if:
	 * <ul>
	 * <li>Environment does not exists on the CMR.
	 * <li>Environment revision sequence does not match the current sequence.
	 * </ul>
	 *
	 * @param environment
	 *            Environment to update.
	 * @param checkProfiles
	 *            if environment should be checked for non existing profiles
	 * @return updated environment instance
	 * @throws IOException
	 *             If {@link IOException} occurs during update.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during update.
	 * @throws BusinessException
	 *             If environment does not exists or the revision check failed.
	 */
	public synchronized Environment updateEnvironment(Environment environment, boolean checkProfiles) throws BusinessException, JAXBException, IOException {
		if (checkProfiles) {
			checkProfiles(environment);
		}

		String id = environment.getId();
		environment.setRevision(environment.getRevision() + 1);
		Environment local = existingEnvironments.replace(id, environment);
		if (null == local) {
			existingEnvironments.remove(id);
			throw new BusinessException("Update of the environment '" + environment.getName() + ".", ConfigurationInterfaceErrorCodeEnum.ENVIRONMENT_DOES_NOT_EXIST);
		} else if ((local != environment) && ((local.getRevision() + 1) != environment.getRevision())) { // NOPMD
			// == check here if same object is used
			existingEnvironments.replace(id, local);
			BusinessException e = new BusinessException("Update of the environment '" + environment.getName() + ".", ConfigurationInterfaceErrorCodeEnum.REVISION_CHECK_FAILED);
			environment.setRevision(environment.getRevision() - 1);
			throw e;
		}
		environment.setUpdatedDate(new Date());
		saveEnvironment(environment);

		// if the name changes we should also delete local from disk
		if (!Objects.equals(environment.getName(), local.getName())) {
			Files.deleteIfExists(pathResolver.getEnvironmentFilePath(local));
		}

		publishEnvironmentUpdateEvent(local, environment);

		return environment;
	}

	/**
	 * Deletes the existing environment.
	 *
	 * @param environment
	 *            Environment to delete.
	 * @throws IOException
	 *             If {@link IOException} occurs during delete.
	 */
	public void deleteEnvironment(Environment environment) throws IOException {
		String id = environment.getId();
		Environment local = existingEnvironments.remove(id);
		if (null != local) {
			Files.deleteIfExists(pathResolver.getEnvironmentFilePath(local));

			AgentMappings agentMappings = agentMappingsReference.get();
			if (checkEnvironments(agentMappings)) {
				try {
					saveAgentMappings(agentMappings, false);
				} catch (Exception e) {
					log.error("Update of the agent mappings on the environment deletion failed.", e);
				}
			}
		}
	}

	/**
	 * Returns the currently used agent mappings.
	 *
	 * @return Returns the currently used agent mappings.
	 */
	public AgentMappings getAgentMappings() {
		return agentMappingsReference.get();
	}

	/**
	 * Sets the agent mappings to be used.
	 *
	 * @param agentMappings
	 *            {@link AgentMappings}
	 * @param checkEnvironments
	 *            if mapping should be checked for non existing environments
	 * @return updated {@link AgentMappings} instance
	 * @throws IOException
	 *             If {@link IOException} occurs during update.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during update.
	 * @throws BusinessException
	 *             If revision check fails.
	 */
	public AgentMappings saveAgentMappings(AgentMappings agentMappings, boolean checkEnvironments) throws BusinessException, JAXBException, IOException {
		// check environment
		if (checkEnvironments) {
			checkEnvironments(agentMappings);
		}

		// ensure there is not overwrite

		AgentMappings current;
		do {
			current = agentMappingsReference.get();
			if (current.getRevision() != agentMappings.getRevision()) {
				throw new BusinessException("Update of the agent mappings.", ConfigurationInterfaceErrorCodeEnum.REVISION_CHECK_FAILED);
			}
		} while (!agentMappingsReference.compareAndSet(current, agentMappings));

		agentMappings.setRevision(agentMappings.getRevision() + 1);
		saveAgentMapping(agentMappings);

		publishAgentMappingsUpdateEvent();

		return agentMappings;
	}

	/**
	 * Returns the current business context definition.
	 *
	 * @return Returns the current business context definition.
	 */
	public BusinessContextDefinition getBusinessconContextDefinition() {
		return businessContextDefinitionReference.get();
	}

	/**
	 * Updates and stores new definition of the business context.
	 *
	 * @param businessContextDefinition
	 *            New {@link IBusinessContextDefinition} to use.
	 * @return the updated {@link BusinessContextDefinition} instance.
	 * @throws BusinessException
	 *             If updating business context fails.
	 * @throws IOException
	 *             If {@link IOException} occurs during update.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during update.
	 */
	public synchronized BusinessContextDefinition updateBusinessContextDefinition(BusinessContextDefinition businessContextDefinition) throws BusinessException, JAXBException, IOException {
		businessContextDefinition.setRevision(businessContextDefinition.getRevision() + 1);
		BusinessContextDefinition currentBusinessContextDefinition = businessContextDefinitionReference.get();
		if ((currentBusinessContextDefinition != businessContextDefinition) && ((currentBusinessContextDefinition.getRevision() + 1) != businessContextDefinition.getRevision())) { // NOPMD
			throw new BusinessException("Update of the business context.", ConfigurationInterfaceErrorCodeEnum.REVISION_CHECK_FAILED);
		}
		saveBusinessContext(businessContextDefinition);

		eventPublisher.publishEvent(new BusinessContextDefinitionUpdateEvent(this, businessContextDefinition));

		return businessContextDefinition;
	}

	/**
	 * Returns all existing alerting definitions.
	 *
	 * @return {@link List} containing all {@link AlertingDefinition}s.
	 */
	public List<AlertingDefinition> getAlertingDefinitions() {
		return new ArrayList<>(existingAlertingDefinitions.values());
	}

	/**
	 * Returns the {@link AlertingDefinition} for the given id.
	 *
	 * @param id
	 *            the identifier of the {@link AlertingDefinition}
	 * @return {@link AlertingDefinition} of the given id
	 * @throws BusinessException
	 *             if no {@link AlertingDefinition} exists for the given id
	 */
	public AlertingDefinition getAlertingDefinition(String id) throws BusinessException {
		AlertingDefinition alertingDefinition = existingAlertingDefinitions.get(id);
		if (null == alertingDefinition) {
			throw new BusinessException("Load alerting definition with the id=" + id + ".", AlertErrorCodeEnum.ALERTING_DEFINITION_DOES_NOT_EXIST);
		}
		return alertingDefinition;
	}

	/**
	 * Creates a new {@link AlertingDefinition}.
	 *
	 * @param alertingDefinition
	 *            {@link AlertingDefinition} template
	 * @return the created {@link AlertingDefinition}
	 * @throws IOException
	 *             if {@link IOException} occurs during create
	 * @throws JAXBException
	 *             if {@link JAXBException} occurs during create
	 */
	public AlertingDefinition createAlertingDefinition(AlertingDefinition alertingDefinition) throws JAXBException, IOException {
		alertingDefinition.setId(getRandomUUIDString());
		alertingDefinition.setCreatedDate(new Date());

		existingAlertingDefinitions.put(alertingDefinition.getId(), alertingDefinition);
		saveAlertingDefinition(alertingDefinition);

		eventPublisher.publishEvent(new AbstractAlertingDefinitionEvent.AlertingDefinitionCreatedEvent(this, alertingDefinition));

		return alertingDefinition;
	}

	/**
	 * Updates the given {@link AlertingDefinition}.
	 *
	 * @param alertingDefinition
	 *            {@link AlertingDefinition} to update
	 * @return the updated {@link AlertingDefinition}
	 * @throws BusinessException
	 *             if {@link BusinessException} occurs during create
	 * @throws JAXBException
	 *             if {@link JAXBException} occurs during create
	 * @throws IOException
	 *             if {@link IOException} occurs during create
	 */
	public synchronized AlertingDefinition updateAlertingDefinition(AlertingDefinition alertingDefinition) throws BusinessException, JAXBException, IOException {
		String id = alertingDefinition.getId();
		if (id == null) {
			throw new BusinessException("Update of an uncreated alerting definition.", AlertErrorCodeEnum.MISSING_ID);
		}

		alertingDefinition.setRevision(alertingDefinition.getRevision() + 1);

		AlertingDefinition local = existingAlertingDefinitions.replace(id, alertingDefinition);
		if (null == local) {
			existingAlertingDefinitions.remove(id);
			throw new BusinessException("Update of the alerting definition '" + alertingDefinition.getName() + ".", AlertErrorCodeEnum.ALERTING_DEFINITION_DOES_NOT_EXIST);
		} else if ((local != alertingDefinition) && ((local.getRevision() + 1) != alertingDefinition.getRevision())) { // NOPMD
			existingAlertingDefinitions.replace(id, local);
			BusinessException e = new BusinessException("Update of the alerting definition '" + alertingDefinition.getName() + ".", AlertErrorCodeEnum.REVISION_CHECK_FAILED);
			alertingDefinition.setRevision(alertingDefinition.getRevision() - 1);
			throw e;
		}
		Date currentDate = new Date();

		alertingDefinition.setUpdatedDate(currentDate);

		// if the name changes we should also delete local from disk
		if (!Objects.equals(alertingDefinition.getName(), local.getName())) {
			Files.deleteIfExists(pathResolver.getAlertingDefinitionFilePath(local));
		}

		saveAlertingDefinition(alertingDefinition);

		eventPublisher.publishEvent(new AbstractAlertingDefinitionEvent.AlertingDefinitionUpdateEvent(this, alertingDefinition));

		return alertingDefinition;
	}

	/**
	 * Deletes the alerting definition.
	 *
	 * @param alertingDefinition
	 *            AlertingDefinition to delete.
	 * @throws IOException
	 *             If {@link IOException} occurs during delete.
	 */
	public void deleteAlertingDefinition(AlertingDefinition alertingDefinition) throws IOException {
		String id = alertingDefinition.getId();
		AlertingDefinition local = existingAlertingDefinitions.remove(id);
		if (local != null) {
			Files.deleteIfExists(pathResolver.getAlertingDefinitionFilePath(local));

			eventPublisher.publishEvent(new AbstractAlertingDefinitionEvent.AlertingDefinitionDeletedEvent(this, local));
		}
	}

	/**
	 * Returns the bytes for the given import data consisted out of given environments and profiles.
	 * These bytes can be saved directly to export file.
	 *
	 * @param environments
	 *            Environments to export.
	 * @param profiles
	 *            Profiles to export.
	 * @return Byte array.
	 * @throws IOException
	 *             If {@link IOException} occurs during marshall.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during marshall.
	 */
	public byte[] getExportData(Collection<Environment> environments, Collection<Profile> profiles) throws JAXBException, IOException {
		ConfigurationInterfaceImportData importData = new ConfigurationInterfaceImportData();

		if (CollectionUtils.isNotEmpty(environments)) {
			Collection<Environment> exportedEnvironments = new ArrayList<>(environments.size());
			for (Environment environment : environments) {
				try {
					exportedEnvironments.add(getEnvironment(environment.getId()));
				} catch (BusinessException e) {
					log.warn("Environment trying to export does not exists.", e);
				}
			}
			importData.setEnvironments(exportedEnvironments);
		}

		if (CollectionUtils.isNotEmpty(profiles)) {
			Collection<Profile> exportedProfiles = new ArrayList<>(profiles.size());
			for (Profile profile : profiles) {
				try {
					exportedProfiles.add(getProfile(profile.getId()));
				} catch (BusinessException e) {
					log.warn("Profile trying to export does not exists.", e);
				}
			}
			importData.setProfiles(exportedProfiles);
		}

		return transformator.marshall(importData, null);
	}

	/**
	 * Returns the {@link ConfigurationInterfaceImportData} from the given import data bytes.
	 *
	 * @param importData
	 *            bytes that were exported.
	 * @return {@link ConfigurationInterfaceImportData}.
	 * @throws SAXException
	 *             IF {@link SAXException} occurs during unmarshall.
	 * @throws IOException
	 *             If {@link IOException} occurs during unmarshall.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during unmarshall.
	 */
	public ConfigurationInterfaceImportData getImportData(byte[] importData) throws JAXBException, IOException, SAXException {
		return transformator.unmarshall(importData, pathResolver.getSchemaPath(), ISchemaVersionAware.ConfigurationInterface.SCHEMA_VERSION, pathResolver.getMigrationPath(),
				ConfigurationInterfaceImportData.class);
	}

	/**
	 * Internal process of updating the profile.
	 *
	 * @param profile
	 *            Profile being updated.
	 * @return Updated instance.
	 * @throws IOException
	 *             If {@link IOException} occurs during save.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during save.
	 * @throws BusinessException
	 *             If profile does not exist or revision check fails.
	 */
	private Profile updateProfileInternal(Profile profile) throws BusinessException, JAXBException, IOException {
		String id = profile.getId();
		profile.setRevision(profile.getRevision() + 1);
		Profile local = existingProfiles.replace(id, profile);
		if (null == local) {
			existingProfiles.remove(id);
			throw new BusinessException("Update of the profile '" + profile.getName() + ".", ConfigurationInterfaceErrorCodeEnum.PROFILE_DOES_NOT_EXIST);
		} else if ((local != profile) && ((local.getRevision() + 1) != profile.getRevision())) { // NOPMD
			// == check here if same object is used
			existingProfiles.replace(id, local);
			BusinessException e = new BusinessException("Update of the profile '" + profile.getName() + ".", ConfigurationInterfaceErrorCodeEnum.REVISION_CHECK_FAILED);
			profile.setRevision(profile.getRevision() - 1);
			throw e;
		}
		profile.setUpdatedDate(new Date());
		saveProfile(profile);

		// if the name changes we should also delete local from disk
		if (!Objects.equals(profile.getName(), local.getName())) {
			Files.deleteIfExists(pathResolver.getProfileFilePath(local));
		}

		// notify listeners
		publishProfileUpdateEvent(local, profile);

		return profile;
	}

	/**
	 * Notifies listeners about profile update.
	 *
	 * @param old
	 *            Old profile instance.
	 * @param updated
	 *            Updated profile instance.
	 */
	private void publishProfileUpdateEvent(Profile old, Profile updated) {
		ProfileUpdateEvent profileUpdateEvent = new ProfileUpdateEvent(this, old, updated);
		eventPublisher.publishEvent(profileUpdateEvent);
	}

	/**
	 * Notifies listeners about environment update.
	 *
	 * @param old
	 *            Old environment instance.
	 * @param updated
	 *            Updated environment instance.
	 */
	private void publishEnvironmentUpdateEvent(Environment old, Environment updated) {
		Collection<Profile> removedProfiles = getProfileDifference(old, updated);
		Collection<Profile> addedProfiles = getProfileDifference(updated, old);

		EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, addedProfiles, removedProfiles);
		eventPublisher.publishEvent(event);
	}

	/**
	 * Returns profile differences between two profiles. The result collection will contain profiles
	 * that exists in the e1 and do not exist in e2.
	 *
	 * @param e1
	 *            First environment.
	 * @param e2
	 *            Second environment.
	 * @return Collection of profiles existing in first environment and not in the second.
	 */
	private Collection<Profile> getProfileDifference(Environment e1, Environment e2) {
		Collection<Profile> profiles = new ArrayList<>();
		Collection<String> profilesIds = CollectionSubtractUtils.subtractSafe(e1.getProfileIds(), e2.getProfileIds());
		for (String id : profilesIds) {
			try {
				profiles.add(getProfile(id));
			} catch (BusinessException e) {
				if (log.isDebugEnabled()) {
					log.debug("Profile with id " + id + " ignored during profile difference calculation as it does not exist anymore.", e);
				}
				continue;
			}
		}
		return profiles;
	}

	/**
	 * Notifies listeners about mappings update.
	 */
	private void publishAgentMappingsUpdateEvent() {
		AgentMappingsUpdateEvent event = new AgentMappingsUpdateEvent(this);
		eventPublisher.publishEvent(event);
	}

	/**
	 * Cleans the non-existing profiles from the {@link Environment}.
	 *
	 * @param environment
	 *            {@link Environment}.
	 * @return if environment was changed during the check process
	 */
	private boolean checkProfiles(Environment environment) {
		boolean changed = false;
		if (CollectionUtils.isNotEmpty(environment.getProfileIds())) {
			for (Iterator<String> it = environment.getProfileIds().iterator(); it.hasNext();) {
				String profileId = it.next();
				if (!existingProfiles.containsKey(profileId)) {
					it.remove();
					changed = true;
				}
			}
		}
		return changed;
	}

	/**
	 * Cleans the non-existing environments from the {@link AgentMappings}.
	 *
	 * @param agentMappings
	 *            {@link AgentMappings}.
	 * @return if mappings where changed during the check process
	 */
	private boolean checkEnvironments(AgentMappings agentMappings) {
		boolean changed = false;
		if (CollectionUtils.isNotEmpty(agentMappings.getMappings())) {
			for (Iterator<AgentMapping> it = agentMappings.getMappings().iterator(); it.hasNext();) {
				AgentMapping agentMapping = it.next();
				if (!existingEnvironments.containsKey(agentMapping.getEnvironmentId())) {
					it.remove();
					changed = true;
				}
			}
		}
		return changed;
	}

	/**
	 * Saves profile and persists it to the list.
	 *
	 * @param profile
	 *            Profile to be saved.
	 * @throws IOException
	 *             If {@link IOException} occurs during save.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs during save.
	 * @throws BusinessException
	 *             If saving of the common profile is requested.
	 */
	private void saveProfile(Profile profile) throws BusinessException, JAXBException, IOException {
		if (profile.isCommonProfile()) {
			throw new BusinessException("Save the profile '" + profile.getName() + " to disk.", ConfigurationInterfaceErrorCodeEnum.COMMON_PROFILE_CAN_NOT_BE_ALTERED);
		}
		transformator.marshall(pathResolver.getProfileFilePath(profile), profile, getRelativeToSchemaPath(pathResolver.getProfilesPath()).toString(),
				ISchemaVersionAware.ConfigurationInterface.SCHEMA_VERSION);
	}

	/**
	 * Saves {@link Environment} to the disk.
	 *
	 * @param environment
	 *            {@link Environment} to save.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs. If saving fails.
	 */
	private void saveEnvironment(Environment environment) throws JAXBException, IOException {
		transformator.marshall(pathResolver.getEnvironmentFilePath(environment), environment, getRelativeToSchemaPath(pathResolver.getEnvironmentPath()).toString(),
				ISchemaVersionAware.ConfigurationInterface.SCHEMA_VERSION);
	}

	/**
	 * Saves agent mapping.
	 *
	 * @param agentMappings
	 *            To save
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs. If saving fails.
	 */
	private void saveAgentMapping(AgentMappings agentMappings) throws JAXBException, IOException {
		transformator.marshall(pathResolver.getAgentMappingFilePath(), agentMappings, getRelativeToSchemaPath(pathResolver.getDefaultCiPath()).toString(),
				ISchemaVersionAware.ConfigurationInterface.SCHEMA_VERSION);
	}

	/**
	 * Saves the passed {@link IBusinessContextDefinition}.
	 *
	 * @param businessContextDefinition
	 *            {@link IBusinessContextDefinition} to save
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs. If saving fails.
	 */
	private void saveBusinessContext(BusinessContextDefinition businessContextDefinition) throws JAXBException, IOException {
		businessContextDefinitionReference.set(businessContextDefinition);
		transformator.marshall(pathResolver.getBusinessContextFilePath(), businessContextDefinition, getRelativeToSchemaPath(pathResolver.getDefaultCiPath()).toString(),
				ISchemaVersionAware.ConfigurationInterface.SCHEMA_VERSION);
	}

	/**
	 * Save the given {@link AlertingDefinition}.
	 *
	 * @param alertingDefinition
	 *            the {@link AlertingDefinition} to save
	 * @throws IOException
	 *             if {@link IOException} occurs
	 * @throws JAXBException
	 *             if {@link JAXBException} occurs. If saving fails
	 */
	private void saveAlertingDefinition(AlertingDefinition alertingDefinition) throws JAXBException, IOException {
		transformator.marshall(pathResolver.getAlertingDefinitionFilePath(alertingDefinition), alertingDefinition, getRelativeToSchemaPath(pathResolver.getDefaultCiPath()).toString());
	}



	/**
	 * Returns given path relative to schema part.
	 *
	 * @param path
	 *            path to relativize
	 * @return path relative to schema part
	 * @see #getSchemaPath()
	 */
	private Path getRelativeToSchemaPath(Path path) {
		return path.relativize(pathResolver.getSchemaPath());
	}

	/**
	 * Creates a deep copy of the given object.
	 *
	 * @param object
	 *            object to clone
	 * @param <T>
	 *            the class of the given object
	 * @return the new object
	 */
	private synchronized <T> T copy(T object) {
		return serializationManager.copy(object);
	}

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		// WARNING loading must be done in this order
		loadExistingProfiles();
		loadExistingEnvironments();
		loadAgentMappings();
		loadBusinessContextDefinition();
		loadExistingAlertingDefinitions();
		anomalyManager.loadExistingAnomalyDetectionGroupConfigurations();
	}

	/**
	 * Loads all existing profiles.
	 */
	private void loadExistingProfiles() {
		log.info("|-Loading the existing Configuration interface profiles..");
		existingProfiles = new ConcurrentHashMap<>(16, 0.75f, 2);

		Path path = pathResolver.getProfilesPath();
		final Path schemaPath = pathResolver.getSchemaPath();

		if (Files.notExists(path)) {
			log.info("Default configuration interface profiles path does not exists. No profile is loaded.");
			return;
		}

		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (isXmlFile(file)) {
						try {
							Profile profile = transformator.unmarshall(file, schemaPath, ISchemaVersionAware.ConfigurationInterface.SCHEMA_VERSION, pathResolver.getMigrationPath(), Profile.class);
							existingProfiles.put(profile.getId(), profile);
						} catch (JAXBException | SAXException e) {
							log.error("Error reading existing Configuration interface profile file. File path: " + file.toString() + ".", e);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("Error exploring Configuration interface profiles directory. Directory path: " + path.toString() + ".", e);
		}

		if (MapUtils.isEmpty(existingProfiles)) {
			log.info("No profile exists in the default profiles path.");
		}
	}

	/**
	 * Loads all existing profiles.
	 */
	private void loadExistingEnvironments() {
		log.info("|-Loading the existing Configuration interface environments..");
		existingEnvironments = new ConcurrentHashMap<>(16, 0.75f, 2);

		Path path = pathResolver.getEnvironmentPath();
		final Path schemaPath = pathResolver.getSchemaPath();

		if (Files.notExists(path)) {
			// create at least one default environment on start-up
			log.info("||-Default configuration interface environment path does not exists. Creating default environment.");
			Environment environment = new Environment();
			environment.setName("Default Environment");
			environment.setDescription("Environment that contains the default inspectIT monitoring settings and all default profiles.");
			try {
				createEnvironment(environment);
			} catch (Exception e) {
				log.error("Error creating default Configuration interface environment on the CMR.", e);
			}
			return;
		}

		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (isXmlFile(file)) {
						try {
							Environment environment = transformator.unmarshall(file, schemaPath, ISchemaVersionAware.ConfigurationInterface.SCHEMA_VERSION, pathResolver.getMigrationPath(), Environment.class);
							existingEnvironments.put(environment.getId(), environment);

							// if checking of the profile made a change, save it
							if (checkProfiles(environment)) {
								try {
									saveEnvironment(environment);
									log.info("Environment '" + environment.getName() + "' was auto-updated as it was referencing the non-existing profile(s).");
								} catch (IOException | JAXBException e) {
									log.error("Error updating existing Configuration interface environment file. File path: " + file.toString() + ".", e);
								}
							}
						} catch (JAXBException | SAXException e) {
							log.error("Error reading existing Configuration interface environment file. File path: " + file.toString() + ".", e);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("Error exploring Configuration interface environments directory. Directory path: " + path.toString() + ".", e);
		}
	}

	/**
	 * Loads currently used agent mapping file.
	 */
	private void loadAgentMappings() {
		log.info("|-Loading the existing Configuration interface agent mappings..");

		AgentMappings agentMappings;
		Path path = pathResolver.getAgentMappingFilePath();
		if (Files.notExists(path)) {
			log.info("||-The agent mappings file does not exists. Creating default mapping.");
			agentMappings = new AgentMappings(Collections.<AgentMapping> emptyList());

			if (MapUtils.isNotEmpty(existingEnvironments)) {
				Environment environment = existingEnvironments.values().iterator().next();
				if (null != environment) {
					// we expect only one mapping here - the default one
					AgentMapping mapping = new AgentMapping("*", "*");
					mapping.setEnvironmentId(environment.getId());
					Collection<AgentMapping> mappings = new ArrayList<>();
					mappings.add(mapping);
					agentMappings.setMappings(mappings);
				}
			}
		} else {
			try {
				agentMappings = transformator.unmarshall(path, pathResolver.getSchemaPath(), ISchemaVersionAware.ConfigurationInterface.SCHEMA_VERSION, pathResolver.getMigrationPath(), AgentMappings.class);
			} catch (JAXBException | IOException | SAXException e) {
				agentMappings = new AgentMappings(Collections.<AgentMapping> emptyList());
				log.error("Error loading Configuration interface agent mappings file. File path: " + path.toString() + ".", e);
			}
		}

		// set atomic reference
		agentMappingsReference.set(agentMappings);

		// check that mapped Environment exists
		if (checkEnvironments(agentMappings)) {
			try {
				saveAgentMapping(agentMappings);
				log.info("Agent mappings configuration is auto-updated as it was referencing the non-existing environment(s).");
			} catch (JAXBException | IOException e) {
				log.error("Error save Configuration interface agent mappings file. File path: " + path.toString() + ".", e);
			}
		}
	}

	/**
	 * Loads the business context definition if it is not already loaded. If successfully loaded
	 * definition will be placed in the {@link #businessContextDefinition} field.
	 */
	private void loadBusinessContextDefinition() {
		log.info("|-Loading the business context definition");
		Path path = pathResolver.getBusinessContextFilePath();
		BusinessContextDefinition businessContextDefinition = null;
		if (Files.exists(path)) {
			try {
				businessContextDefinition = transformator.unmarshall(path, pathResolver.getSchemaPath(), ISchemaVersionAware.ConfigurationInterface.SCHEMA_VERSION, pathResolver.getMigrationPath(),
						BusinessContextDefinition.class);
			} catch (JAXBException | IOException | SAXException e) {
				log.error("Error loading Configuration interface business context file. File path: " + path.toString() + ".", e);
			}
		}
		if (null == businessContextDefinition) {
			businessContextDefinition = new BusinessContextDefinition();
			try {
				saveBusinessContext(businessContextDefinition);
			} catch (JAXBException | IOException e) {
				log.error("Error saving Configuration interface business context file. File path: " + path.toString() + ".", e);
			}
		}
		businessContextDefinitionReference.set(businessContextDefinition);
	}

	/**
	 * Loads all existing alerting definitions.
	 */
	private void loadExistingAlertingDefinitions() {
		log.info("|-Loading the existing alerting definitions..");
		existingAlertingDefinitions = new ConcurrentHashMap<>(16, 0.75f, 2);

		Path path = pathResolver.getAlertingDefinitionsPath();

		if (Files.notExists(path)) {
			log.info("Default alerting definitions path does not exists. No alerting definitions are loaded.");
			return;
		}

		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (isXmlFile(file)) {
						try {
							AlertingDefinition alertingDefinition = transformator.unmarshall(file, pathResolver.getSchemaPath(), ISchemaVersionAware.ConfigurationInterface.SCHEMA_VERSION,
									pathResolver.getMigrationPath(), AlertingDefinition.class);
							existingAlertingDefinitions.put(alertingDefinition.getId(), alertingDefinition);
						} catch (JAXBException | SAXException e) {
							log.error("Error reading existing alerting definition file. File path: " + file.toString() + ".", e);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("Error exploring alerting definitions directory. Directory path: " + path.toString() + ".", e);
		}

		if (MapUtils.isEmpty(existingAlertingDefinitions)) {
			log.info("No alerting definitions are in the default path.");
		}

		eventPublisher.publishEvent(new AbstractAlertingDefinitionEvent.AlertingDefinitionLoadedEvent(this, new ArrayList<>(existingAlertingDefinitions.values())));
	}

	/**
	 * If path is a file that ends with the <i>.xml</i> extension.
	 *
	 * @param path
	 *            Path to the file.
	 * @return If path is a file that ends with the <i>.xml</i> extension.
	 */
	private boolean isXmlFile(Path path) {
		return !Files.isDirectory(path) && path.toString().endsWith(".xml");
	}

	/**
	 * Returns the unique String that will be used for IDs.
	 *
	 * @return Returns unique string based on the {@link UUID}.
	 */
	private String getRandomUUIDString() {
		return UUID.randomUUID().toString();
	}
}
