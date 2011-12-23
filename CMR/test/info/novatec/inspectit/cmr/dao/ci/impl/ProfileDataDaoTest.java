package info.novatec.inspectit.cmr.dao.ci.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import info.novatec.inspectit.cmr.dao.ci.EnvironmentDataDao;
import info.novatec.inspectit.cmr.dao.ci.ProfileDataDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.data.ci.ProfileData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * This class provides test method which are testing the methods provided by the
 * {@link ProfileDataDao}.
 * 
 * @author Matthias Huber
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml" })
public class ProfileDataDaoTest extends AbstractTransactionalTestNGLogSupport {

	/**
	 * The {@link ProfileDataDao} to test.
	 */
	@Autowired
	private ProfileDataDao profileDataDao;

	/**
	 * The {@link EnvironmentDataDao} to add an {@link EnvironmentData} object into the database
	 * during the preparation process of this test.
	 */
	@Autowired
	private EnvironmentDataDao environmentDataDao;

	private EnvironmentData environmentData;

	/**
	 * This method prepares the database so that all needed data for the test is already stored.
	 * 
	 * @throws Exception
	 */
	@BeforeTest
	public void prepareDB() throws Exception {
		// add an environment because a profile will be added to it later on
		environmentData = new EnvironmentData();
		environmentData.setBufferStrategy("bufferStrategy");
		environmentData.setDescription("description");
		environmentData.setName("name");
		environmentData.setSendStrategy("sendStrategy");

		// initializes the environmentDataDao
		springTestContextPrepareTestInstance();

		environmentDataDao.addEnvironment(environmentData);
	}

	/**
	 * Stores a single {@link ProfileData} object into the database. Storing is successful if the
	 * assigned id of the object is greater than 0.
	 */
	@Test
	public void addProfile() {
		ProfileData profileData = new ProfileData();
		profileData.setDescription("description");
		profileData.setName("name");
		profileData.setEnvironmentData(environmentData);

		profileDataDao.addProfile(profileData);
		assertTrue(profileData.getId() > 0);
	}

	/**
	 * Stores a single {@link ProfileData} object into the database. Afterwards the
	 * {@link ProfileData} object is retrieved and the values are compared to those of the original
	 * object.
	 */
	@Test(dependsOnMethods = { "addProfile" })
	public void getProfile() {
		ProfileData profileData = new ProfileData();
		profileData.setDescription("description");
		profileData.setName("name");
		profileData.setEnvironmentData(environmentData);
		profileDataDao.addProfile(profileData);

		ProfileData receivedProfileData = profileDataDao.getProfile(profileData.getId());

		assertNotNull(receivedProfileData);
		assertEquals(receivedProfileData.getDescription(), profileData.getDescription());
		assertEquals(receivedProfileData.getId(), profileData.getId());
		assertEquals(receivedProfileData.getName(), profileData.getName());
		assertFalse(receivedProfileData.isInitialized());
	}

	/**
	 * Stores a single {@link ProfileData} object into the database. Then the {@link ProfileData}
	 * object is changed and an update is performed. Afterwards the {@link ProfileData} object is
	 * retrieved and the values are compared to those of the first stored version and the updated
	 * version.
	 */
	@Test(dependsOnMethods = { "getProfile" })
	public void updateProfile() {
		ProfileData profileData = new ProfileData();
		profileData.setDescription("description");
		profileData.setName("name");
		profileData.setEnvironmentData(environmentData);
		profileDataDao.addProfile(profileData);

		profileData.setDescription("modified description");
		profileData.setName("modified name");

		profileDataDao.updateProfile(profileData);

		ProfileData receivedProfileData = profileDataDao.getProfile(profileData.getId());

		assertNotNull(receivedProfileData);
		// check values against updated version
		assertEquals(receivedProfileData.getDescription(), profileData.getDescription());
		assertEquals(receivedProfileData.getEnvironmentData(), profileData.getEnvironmentData());
		assertEquals(receivedProfileData.getId(), profileData.getId());
		assertEquals(receivedProfileData.getName(), profileData.getName());
		assertEquals(receivedProfileData.isInitialized(), profileData.isInitialized());
		// check values against initial version
		assertTrue(!"description".equals(receivedProfileData.getDescription()));
		assertTrue(!"name".equals(receivedProfileData.getName()));
	}

	/**
	 * Stores a single {@link ProfileData} object into the database and afterwards deletes it twice.
	 * Test is successful if no {@link EntityNotFoundException} is thrown during first deletion and
	 * the exception is thrown during second deletion.
	 * 
	 * @throws EntityNotFoundException
	 *             If object is not found.
	 */
	@Test(dependsOnMethods = { "updateProfile" }, expectedExceptions = EntityNotFoundException.class)
	public void deleteProfile() throws EntityNotFoundException {
		ProfileData profileData = new ProfileData();
		profileData.setDescription("description");
		profileData.setName("name");
		profileData.setEnvironmentData(environmentData);
		profileDataDao.addProfile(profileData);

		// first deletion
		try {
			profileDataDao.deleteProfile(profileData.getId());
		} catch (EntityNotFoundException e) {
			fail("The given profile could not be found inside the database. So deleting was not possible");
		}

		// second deletion
		profileDataDao.deleteProfile(profileData.getId());
	}

	/**
	 * This method tries to retrieve a {@link ProfileData} object which is not available. Test is
	 * successfully if null is the return value.
	 */
	@Test(dependsOnMethods = { "deleteProfile" })
	public void getNonExistingProfile() {
		ProfileData profile = profileDataDao.getProfile(1000);

		assertNull(profile);
	}

	/**
	 * This method tests if a profile is also deleted when its parent, the environment, is deleted.
	 * Therefore the initial added environment will be deleted. Test is successful when null is
	 * returned.
	 * 
	 * @throws EntityNotFoundException
	 *             If the object is not found.
	 */
	@Test(dependsOnMethods = { "getNonExistingProfile" })
	public void casdadeDeleteOfProfile() throws EntityNotFoundException {
		// previously add a profile
		ProfileData profileData = new ProfileData();
		profileData.setDescription("description");
		profileData.setName("name");
		profileData.setEnvironmentData(environmentData);
		profileDataDao.addProfile(profileData);

		// check if profile is stored
		assertNotNull(profileDataDao.getProfile(profileData.getId()));

		environmentDataDao.deleteEnvironment(environmentData.getId());

		List<EnvironmentData> environments = environmentDataDao.getEnvironments();
		assertTrue(environments.isEmpty());

		ProfileData profile = profileDataDao.getProfile(profileData.getId());
		assertNull(profile);
	}

	/**
	 * This method deletes the initial created {@link EnvironmentData} object.
	 * 
	 * @throws EntityNotFoundException
	 *             If the object is not found.
	 */
	@AfterTest(alwaysRun = true)
	public void deleteCreatedObjects() throws EntityNotFoundException {
		environmentDataDao.deleteEnvironment(environmentData.getId());
	}
}
