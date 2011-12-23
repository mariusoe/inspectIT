package info.novatec.inspectit.cmr.dao.ci.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import info.novatec.inspectit.cmr.dao.ci.EnvironmentDataDao;
import info.novatec.inspectit.cmr.dao.ci.PlatformSensorDefinitionDataDao;
import info.novatec.inspectit.cmr.dao.ci.ProfileDataDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.data.ci.PlatformSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.ProfileData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * This class provides test method which are testing the methods provided by the
 * {@link PlatformSensorDefinitionDataDao}.
 * 
 * @author Matthias Huber
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml" })
public class PlatformSensorDefinitionDataDaoTest extends AbstractTransactionalTestNGLogSupport {

	/**
	 * 
	 * The {@link PlatformSensorDefinitionDataDao} to test.
	 */
	@Autowired
	private PlatformSensorDefinitionDataDao platformSensorDefinitionDataDao;

	/**
	 * The {@link ProfileDataDao} to add an {@link ProfileData} object into the database during the
	 * preparation process of this test.
	 */
	@Autowired
	private ProfileDataDao profileDataDao;

	/**
	 * The {@link EnvironmentDataDao} to add an {@link EnvironmentData} object into the database
	 * during the preparation process of this test.
	 */
	@Autowired
	private EnvironmentDataDao environmentDataDao;

	/**
	 * The {@link SessionFactory} used for flushing the current session.
	 */
	@Autowired
	private SessionFactory sessionFactory;

	private EnvironmentData environmentData;

	private ProfileData profileData;

	/**
	 * This method prepares the database so that all needed data for the test is already stored.
	 * 
	 * @throws Exception
	 */
	@BeforeTest
	public void prepareDB() throws Exception {
		// initializes the daos
		springTestContextPrepareTestInstance();

		// add an environment because a profile will be added to it later on
		environmentData = new EnvironmentData();
		environmentData.setBufferStrategy("bufferStrategy");
		environmentData.setDescription("description");
		environmentData.setName("name");
		environmentData.setSendStrategy("sendStrategy");

		environmentDataDao.addEnvironment(environmentData);

		// add a profile because an exception sensor definition will be added to
		// it later
		profileData = new ProfileData();
		profileData.setDescription("description");
		profileData.setEnvironmentData(environmentData);
		profileData.setName("name");

		profileDataDao.addProfile(profileData);
	}

	@Test
	public void addPlatformSensorDefinition() {
		PlatformSensorDefinitionData platformSensorDefinitionData = new PlatformSensorDefinitionData();
		platformSensorDefinitionData.setActivated(true);
		platformSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		platformSensorDefinitionData.setProfileData(profileData);

		long id = platformSensorDefinitionDataDao.addPlatformSensorDefinition(platformSensorDefinitionData);
		assertTrue(id > 0);
	}

	/**
	 * Stores a single {@link PlatformSensorDefinitionData} object into the database. Afterwards the
	 * {@link PlatformSensorDefinitionData} object is retrieved and the values are compared to those
	 * of the original object.
	 */
	@Test(dependsOnMethods = { "addPlatformSensorDefinition" })
	public void getPlatformSensorDefinition() {
		PlatformSensorDefinitionData platformSensorDefinitionData = new PlatformSensorDefinitionData();
		platformSensorDefinitionData.setActivated(true);
		platformSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		platformSensorDefinitionData.setProfileData(profileData);

		platformSensorDefinitionDataDao.addPlatformSensorDefinition(platformSensorDefinitionData);

		// flush session. this is needed to establish the association between
		// the profile and the platform sensor definition. Otherwise the
		// the platform sensor definition is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		ProfileData receivedProfileData = profileDataDao.getProfile(profileData.getId());
		Set<PlatformSensorDefinitionData> platformSensorDefinitions = receivedProfileData.getPlatformSensorDefinitions();

		assertTrue(!platformSensorDefinitions.isEmpty());

		PlatformSensorDefinitionData receivedPlatformSensorDefinition = null;
		for (PlatformSensorDefinitionData platformSensorDefinition : platformSensorDefinitions) {
			receivedPlatformSensorDefinition = platformSensorDefinition;
		}

		assertNotNull(receivedPlatformSensorDefinition);
		assertEquals(receivedPlatformSensorDefinition.isActivated(), platformSensorDefinitionData.isActivated());
		assertEquals(receivedPlatformSensorDefinition.getFullyQualifiedName(), platformSensorDefinitionData.getFullyQualifiedName());
		assertEquals(receivedPlatformSensorDefinition.getId(), platformSensorDefinitionData.getId());
		assertEquals(receivedPlatformSensorDefinition.getProfileData(), platformSensorDefinitionData.getProfileData());
	}

	/**
	 * Stores a single {@link PlatformSensorDefinitionData} object into the database. Then the
	 * {@link PlatformSensorDefinitionData} object is changed and an update is performed. Afterwards
	 * the {@link PlatformSensorDefinitionData} object is retrieved and the values are compared to
	 * those of the first stored version and the updated version.
	 */
	@Test(dependsOnMethods = { "getPlatformSensorDefinition" })
	public void updatePlatformSensorDefinition() {
		PlatformSensorDefinitionData platformSensorDefinitionData = new PlatformSensorDefinitionData();
		platformSensorDefinitionData.setActivated(true);
		platformSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		platformSensorDefinitionData.setProfileData(profileData);

		platformSensorDefinitionDataDao.addPlatformSensorDefinition(platformSensorDefinitionData);

		platformSensorDefinitionData.setActivated(false);
		platformSensorDefinitionData.setFullyQualifiedName("modified fullyQualifiedName");

		platformSensorDefinitionDataDao.updatePlatformSensorDefinition(platformSensorDefinitionData);

		// flush session. this is needed to establish the association between
		// the profile and the platform sensor definition. Otherwise the
		// the platform sensor definition is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		ProfileData receivedProfileData = profileDataDao.getProfile(profileData.getId());
		Set<PlatformSensorDefinitionData> platformSensorDefinitions = receivedProfileData.getPlatformSensorDefinitions();

		assertTrue(!platformSensorDefinitions.isEmpty());

		PlatformSensorDefinitionData receivedPlatformSensorDefinition = null;
		for (PlatformSensorDefinitionData exceptionSensorDefinition : platformSensorDefinitions) {
			receivedPlatformSensorDefinition = exceptionSensorDefinition;
		}

		assertNotNull(receivedPlatformSensorDefinition);
		// check values against updated version
		assertEquals(receivedPlatformSensorDefinition.isActivated(), platformSensorDefinitionData.isActivated());
		assertEquals(receivedPlatformSensorDefinition.getFullyQualifiedName(), platformSensorDefinitionData.getFullyQualifiedName());
		assertEquals(receivedPlatformSensorDefinition.getId(), platformSensorDefinitionData.getId());
		assertEquals(receivedPlatformSensorDefinition.getProfileData(), platformSensorDefinitionData.getProfileData());
		// check values against initial version
		assertTrue(true != receivedPlatformSensorDefinition.isActivated());
		assertTrue(!"fullyQualifiedName".equals(receivedPlatformSensorDefinition.getFullyQualifiedName()));
	}

	/**
	 * Stores a single {@link PlatformSensorDefinitionData} object into the database and afterwards
	 * deletes it twice. Test is successful if no {@link EntityNotFoundException} is thrown during
	 * first deletion and the exception is thrown during second deletion.
	 * 
	 * @throws EntityNotFoundException
	 *             If object is not found.
	 */
	@Test(dependsOnMethods = { "updatePlatformSensorDefinition" }, expectedExceptions = EntityNotFoundException.class)
	public void deleteExistingPlatformSensorDefinition() throws EntityNotFoundException {
		PlatformSensorDefinitionData platformSensorDefinitionData = new PlatformSensorDefinitionData();
		platformSensorDefinitionData.setActivated(true);
		platformSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		platformSensorDefinitionData.setProfileData(profileData);

		platformSensorDefinitionDataDao.addPlatformSensorDefinition(platformSensorDefinitionData);

		try {
			platformSensorDefinitionDataDao.deletePlatformSensorDefinition(platformSensorDefinitionData.getId());
		} catch (EntityNotFoundException e) {
			fail("The given platform sensor definition could not be found inside the database. So deleting was not possible");
		}

		// second deletion
		platformSensorDefinitionDataDao.deletePlatformSensorDefinition(platformSensorDefinitionData.getId());
	}

	/**
	 * Stores a single {@link PlatformSensorDefinitionData} object into the database. Then the
	 * associated {@link ProfileData} object is deleted. During the deletion the
	 * {@link PlatformSensorDefinitionData} object should also be deleted. The deletion afterwards
	 * of the {@link PlatformSensorDefinitionData} object should cause an
	 * {@link EntityNotFoundException}.
	 * 
	 * @throws EntityNotFoundException
	 *             If object is not found.
	 */
	@Test(dependsOnMethods = { "deleteExistingPlatformSensorDefinition" }, expectedExceptions = EntityNotFoundException.class)
	public void cascadeDeleteIfProfileIsDeleted() throws EntityNotFoundException {
		PlatformSensorDefinitionData platformSensorDefinitionData = new PlatformSensorDefinitionData();
		platformSensorDefinitionData.setActivated(true);
		platformSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		platformSensorDefinitionData.setProfileData(profileData);

		platformSensorDefinitionDataDao.addPlatformSensorDefinition(platformSensorDefinitionData);

		// flush session. this is needed to establish the association between
		// the profile and the platform sensor definition. Otherwise the
		// platform sensor definition is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		profileDataDao.deleteProfile(profileData.getId());

		platformSensorDefinitionDataDao.deletePlatformSensorDefinition(platformSensorDefinitionData.getId());
	}

	/**
	 * Stores a single {@link PlatformSensorDefinitionData} object into the database. Then the
	 * associated {@link EnvironmentData} object is deleted. During the deletion the
	 * {@link PlatformSensorDefinitionData} object should also be deleted. The deletion afterwards
	 * of the {@link PlatformSensorDefinitionData} object should cause an
	 * {@link EntityNotFoundException}.
	 * 
	 * @throws EntityNotFoundException
	 *             If object is not found.
	 */
	@Test(dependsOnMethods = { "cascadeDeleteIfProfileIsDeleted" }, expectedExceptions = EntityNotFoundException.class)
	public void cascadeDeleteIfEnvironmentIsDeleted() throws EntityNotFoundException {
		PlatformSensorDefinitionData platformSensorDefinitionData = new PlatformSensorDefinitionData();
		platformSensorDefinitionData.setActivated(true);
		platformSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		platformSensorDefinitionData.setProfileData(profileData);

		platformSensorDefinitionDataDao.addPlatformSensorDefinition(platformSensorDefinitionData);

		// flush session. this is needed to establish the association between
		// the profile and the platform sensor definition. Otherwise the
		// platform sensor definition is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		environmentDataDao.deleteEnvironment(environmentData.getId());

		platformSensorDefinitionDataDao.deletePlatformSensorDefinition(platformSensorDefinitionData.getId());
	}

	/**
	 * This method deletes the initial created {@link EnvironmentData} and {@link ProfileData}
	 * objects.
	 * 
	 * @throws EntityNotFoundException
	 *             If the object is not found.
	 */
	@AfterTest(alwaysRun = true)
	public void deleteCreatedObjects() throws EntityNotFoundException {
		// deletes also the profileData => the cascade delete is tested
		// before in ProfileDataTest.class
		environmentDataDao.deleteEnvironment(environmentData.getId());
	}
}
