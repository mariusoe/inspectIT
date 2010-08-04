package info.novatec.inspectit.cmr.dao.ci.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import info.novatec.inspectit.cmr.dao.ci.EnvironmentDataDao;
import info.novatec.inspectit.cmr.dao.ci.ExceptionSensorDefinitionDataDao;
import info.novatec.inspectit.cmr.dao.ci.ProfileDataDao;
import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.data.ci.ExceptionSensorDefinitionData;
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
 * {@link ExceptionSensorDefinitionDataDao}.
 * 
 * @author Matthias Huber
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-model.xml" })
public class ExceptionSensorDefinitionDataDaoTest extends AbstractLogSupport {

	/**
	 * The {@link ExceptionSensorDefinitionDataDao} to test.
	 */
	@Autowired
	private ExceptionSensorDefinitionDataDao exceptionSensorDefinitionDataDao;

	/**
	 * The {@link ProfileDataDao} to add an {@link ProfileData} object into the
	 * database during the preparation process of this test.
	 */
	@Autowired
	private ProfileDataDao profileDataDao;

	/**
	 * The {@link EnvironmentDataDao} to add an {@link EnvironmentData} object
	 * into the database during the preparation process of this test.
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
	 * This method prepares the database so that all needed data for the test is
	 * already stored.
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

	/**
	 * Stores a single {@link ExceptionSensorDefinitionData} object into the
	 * database. Storing is successful if the assigned id of the object is
	 * greater than 0.
	 */
	@Test
	public void addExceptionSensorDefinition() {
		ExceptionSensorDefinitionData exceptionSensorDefinitionData = new ExceptionSensorDefinitionData();
		exceptionSensorDefinitionData.setActivated(true);
		exceptionSensorDefinitionData.setDescription("description");
		exceptionSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		exceptionSensorDefinitionData.setSensorOption(1);
		exceptionSensorDefinitionData.setProfileData(profileData);

		exceptionSensorDefinitionDataDao.addExceptionSensorDefinition(exceptionSensorDefinitionData);
		assertTrue(exceptionSensorDefinitionData.getId() > 0);
	}

	/**
	 * Stores a single {@link ExceptionSensorDefinitionData} object into the
	 * database. Afterwards the {@link ExceptionSensorDefinitionData} object is
	 * retrieved and the values are compared to those of the original object.
	 */
	@Test(dependsOnMethods = { "addExceptionSensorDefinition" })
	public void getExceptionSensorDefinition() {
		ExceptionSensorDefinitionData exceptionSensorDefinitionData = new ExceptionSensorDefinitionData();
		exceptionSensorDefinitionData.setActivated(true);
		exceptionSensorDefinitionData.setDescription("description");
		exceptionSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		exceptionSensorDefinitionData.setSensorOption(ExceptionSensorDefinitionData.NO_SENSOR_OPTION);
		exceptionSensorDefinitionData.setProfileData(profileData);

		exceptionSensorDefinitionDataDao.addExceptionSensorDefinition(exceptionSensorDefinitionData);

		// flush session. this is needed to establish the association between
		// the profile and the exception sensor definition. Otherwise the
		// exception sensor definition is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		ProfileData receivedProfileData = profileDataDao.getProfile(profileData.getId());
		Set<ExceptionSensorDefinitionData> exceptionSensorDefinitions = receivedProfileData.getExceptionSensorDefinitions();

		assertTrue(!exceptionSensorDefinitions.isEmpty());

		ExceptionSensorDefinitionData receivedExceptionSensorDefinition = null;
		for (ExceptionSensorDefinitionData exceptionSensorDefinition : exceptionSensorDefinitions) {
			receivedExceptionSensorDefinition = exceptionSensorDefinition;
		}

		assertNotNull(receivedExceptionSensorDefinition);
		assertEquals(receivedExceptionSensorDefinition.isActivated(), exceptionSensorDefinitionData.isActivated());
		assertEquals(receivedExceptionSensorDefinition.getDescription(), exceptionSensorDefinitionData.getDescription());
		assertEquals(receivedExceptionSensorDefinition.getFullyQualifiedName(), exceptionSensorDefinitionData.getFullyQualifiedName());
		assertEquals(receivedExceptionSensorDefinition.getId(), exceptionSensorDefinitionData.getId());
		assertEquals(receivedExceptionSensorDefinition.getProfileData(), exceptionSensorDefinitionData.getProfileData());
		assertEquals(receivedExceptionSensorDefinition.getSensorOption(), exceptionSensorDefinitionData.getSensorOption());
	}

	/**
	 * Stores a single {@link ExceptionSensorDefinitionData} object into the
	 * database. Then the {@link ExceptionSensorDefinitionData} object is
	 * changed and an update is performed. Afterwards the
	 * {@link ExceptionSensorDefinitionData} object is retrieved and the values
	 * are compared to those of the first stored version and the updated
	 * version.
	 */
	@Test(dependsOnMethods = { "getExceptionSensorDefinition" })
	public void updateExceptionSensorDefinition() {
		ExceptionSensorDefinitionData exceptionSensorDefinitionData = new ExceptionSensorDefinitionData();
		exceptionSensorDefinitionData.setActivated(true);
		exceptionSensorDefinitionData.setDescription("description");
		exceptionSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		exceptionSensorDefinitionData.setSensorOption(ExceptionSensorDefinitionData.NO_SENSOR_OPTION);
		exceptionSensorDefinitionData.setProfileData(profileData);

		exceptionSensorDefinitionDataDao.addExceptionSensorDefinition(exceptionSensorDefinitionData);

		exceptionSensorDefinitionData.setActivated(false);
		exceptionSensorDefinitionData.setDescription("modified description");
		exceptionSensorDefinitionData.setFullyQualifiedName("modified fullyQualifiedName");
		exceptionSensorDefinitionData.setSensorOption(ExceptionSensorDefinitionData.INTERFACE);

		exceptionSensorDefinitionDataDao.updateExceptionSensorDefinition(exceptionSensorDefinitionData);

		// flush session. this is needed to establish the association between
		// the profile and the exception sensor definition. Otherwise the
		// exception sensor definition is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		ProfileData receivedProfileData = profileDataDao.getProfile(profileData.getId());
		Set<ExceptionSensorDefinitionData> exceptionSensorDefinitions = receivedProfileData.getExceptionSensorDefinitions();

		assertTrue(!exceptionSensorDefinitions.isEmpty());

		ExceptionSensorDefinitionData receivedExceptionSensorDefinition = null;
		for (ExceptionSensorDefinitionData exceptionSensorDefinition : exceptionSensorDefinitions) {
			receivedExceptionSensorDefinition = exceptionSensorDefinition;
		}

		assertNotNull(receivedExceptionSensorDefinition);
		// check values against updated version
		assertEquals(receivedExceptionSensorDefinition.isActivated(), exceptionSensorDefinitionData.isActivated());
		assertEquals(receivedExceptionSensorDefinition.getDescription(), exceptionSensorDefinitionData.getDescription());
		assertEquals(receivedExceptionSensorDefinition.getFullyQualifiedName(), exceptionSensorDefinitionData.getFullyQualifiedName());
		assertEquals(receivedExceptionSensorDefinition.getId(), exceptionSensorDefinitionData.getId());
		assertEquals(receivedExceptionSensorDefinition.getProfileData(), exceptionSensorDefinitionData.getProfileData());
		assertEquals(receivedExceptionSensorDefinition.getSensorOption(), exceptionSensorDefinitionData.getSensorOption());
		assertEquals(receivedExceptionSensorDefinition.getSensorOption(), ExceptionSensorDefinitionData.INTERFACE);
		// check values against initial version
		assertTrue(true != receivedExceptionSensorDefinition.isActivated());
		assertTrue(!"description".equals(receivedExceptionSensorDefinition.getDescription()));
		assertTrue(!"fullyQualifiedName".equals(receivedExceptionSensorDefinition.getFullyQualifiedName()));
		assertTrue(!(ExceptionSensorDefinitionData.NO_SENSOR_OPTION == receivedExceptionSensorDefinition.getSensorOption()));
	}

	/**
	 * Stores a single {@link ExceptionSensorDefinitionData} object into the
	 * database and afterwards deletes it twice. Test is successful if no
	 * {@link EntityNotFoundException} is thrown during first deletion and the
	 * exception is thrown during second deletion.
	 * 
	 * @throws EntityNotFoundException
	 *             If object is not found.
	 */
	@Test(dependsOnMethods = { "updateExceptionSensorDefinition" }, expectedExceptions = EntityNotFoundException.class)
	public void deleteExistingExceptionSensorDefinition() throws EntityNotFoundException {
		ExceptionSensorDefinitionData exceptionSensorDefinitionData = new ExceptionSensorDefinitionData();
		exceptionSensorDefinitionData.setActivated(true);
		exceptionSensorDefinitionData.setDescription("description");
		exceptionSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		exceptionSensorDefinitionData.setSensorOption(ExceptionSensorDefinitionData.NO_SENSOR_OPTION);
		exceptionSensorDefinitionData.setProfileData(profileData);

		exceptionSensorDefinitionDataDao.addExceptionSensorDefinition(exceptionSensorDefinitionData);

		try {
			exceptionSensorDefinitionDataDao.deleteExceptionSensorDefinition(exceptionSensorDefinitionData.getId());
		} catch (EntityNotFoundException e) {
			fail("The given exception sensor definition could not be found inside the database. So deleting was not possible");
		}

		// second deletion
		exceptionSensorDefinitionDataDao.deleteExceptionSensorDefinition(exceptionSensorDefinitionData.getId());
	}

	/**
	 * Stores a single {@link ExceptionSensorDefinitionData} object into the
	 * database. Then the associated {@link ProfileData} object is deleted.
	 * During the deletion the {@link ExceptionSensorDefinitionData} object
	 * should also be deleted. The deletion afterwards of the
	 * {@link ExceptionSensorDefinitionData} object should cause an
	 * {@link EntityNotFoundException}.
	 * 
	 * @throws EntityNotFoundException
	 *             If object is not found.
	 */
	@Test(dependsOnMethods = { "deleteExistingExceptionSensorDefinition" }, expectedExceptions = EntityNotFoundException.class)
	public void cascadeDeleteIfProfileIsDeleted() throws EntityNotFoundException {
		ExceptionSensorDefinitionData exceptionSensorDefinitionData = new ExceptionSensorDefinitionData();
		exceptionSensorDefinitionData.setActivated(true);
		exceptionSensorDefinitionData.setDescription("description");
		exceptionSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		exceptionSensorDefinitionData.setSensorOption(ExceptionSensorDefinitionData.NO_SENSOR_OPTION);
		exceptionSensorDefinitionData.setProfileData(profileData);

		exceptionSensorDefinitionDataDao.addExceptionSensorDefinition(exceptionSensorDefinitionData);

		// flush session. this is needed to establish the association between
		// the profile and the exception sensor definition. Otherwise the
		// exception sensor definition is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		profileDataDao.deleteProfile(profileData.getId());

		exceptionSensorDefinitionDataDao.deleteExceptionSensorDefinition(exceptionSensorDefinitionData.getId());
	}

	/**
	 * Stores a single {@link ExceptionSensorDefinitionData} object into the
	 * database. Then the associated {@link EnvironmentData} object is deleted.
	 * During the deletion the {@link ExceptionSensorDefinitionData} object
	 * should also be deleted. The deletion afterwards of the
	 * {@link ExceptionSensorDefinitionData} object should cause an
	 * {@link EntityNotFoundException}.
	 * 
	 * @throws EntityNotFoundException
	 *             If object is not found.
	 */
	@Test(dependsOnMethods = { "cascadeDeleteIfProfileIsDeleted" }, expectedExceptions = EntityNotFoundException.class)
	public void cascadeDeleteIfEnvironmentIsDeleted() throws EntityNotFoundException {
		ExceptionSensorDefinitionData exceptionSensorDefinitionData = new ExceptionSensorDefinitionData();
		exceptionSensorDefinitionData.setActivated(true);
		exceptionSensorDefinitionData.setDescription("description");
		exceptionSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		exceptionSensorDefinitionData.setSensorOption(ExceptionSensorDefinitionData.NO_SENSOR_OPTION);
		exceptionSensorDefinitionData.setProfileData(profileData);

		exceptionSensorDefinitionDataDao.addExceptionSensorDefinition(exceptionSensorDefinitionData);

		// flush session. this is needed to establish the association between
		// the profile and the exception sensor definition. Otherwise the
		// exception sensor definition is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		environmentDataDao.deleteEnvironment(environmentData.getId());

		exceptionSensorDefinitionDataDao.deleteExceptionSensorDefinition(exceptionSensorDefinitionData.getId());
	}

	/**
	 * This method deletes the initial created {@link EnvironmentData} and
	 * {@link ProfileData} objects.
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
