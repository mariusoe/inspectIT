package info.novatec.inspectit.cmr.dao.ci.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import info.novatec.inspectit.cmr.dao.ci.EnvironmentDataDao;
import info.novatec.inspectit.cmr.dao.ci.ProfileDataDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.data.ci.ExceptionSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.MethodSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.PlatformSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.ProfileData;
import info.novatec.inspectit.communication.data.ci.SensorTypeData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import java.util.Set;

import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * This class provides test method which are testing the lazy loading behavior among objects.
 * 
 * @author Matthias Huber
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-property.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-model.xml", "classpath:spring/spring-context-util.xml" })
public class LazyInitializationTest extends AbstractTransactionalTestNGLogSupport {

	/**
	 * The {@link EnvironmentDataDao} to test.
	 */
	@Autowired
	private EnvironmentDataDao environmentDataDao;

	/**
	 * The {@link ProfileDataDao} to test.
	 */
	@Autowired
	private ProfileDataDao profileDataDao;

	/**
	 * Retrieved {@link EnvironmentData} object retrieved as result of calling
	 * {@link EnvironmentDataDao#getEnvironments().
	 */
	private EnvironmentData retrievedEnvironment;

	/**
	 * The {@link ProfileData} object retrieved with the
	 * {@link LazyInitializationTest#retrievedEnvironment}.
	 */
	private ProfileData profileWithoutSensorDefinitions;

	/**
	 * The {@link ProfileData} object retrieved as result of calling
	 * {@link ProfileDataDao#getProfile(long).
	 */
	private ProfileData profileWithSensorDefinitions;

	private long environmentId;

	/**
	 * This method prepares the database so that all needed data for the test is already stored.
	 * Also the variables used for testing are initialized.
	 * 
	 * @throws Exception
	 */
	@BeforeTest
	public void prepareDatabaseAndTestVariables() throws Exception {
		// initializes the two dao's
		springTestContextPrepareTestInstance();

		EnvironmentData environmentData = new EnvironmentData();
		environmentData.setBufferStrategy("bufferStrategy");
		environmentData.setDescription("description");
		environmentData.setName("name");
		environmentData.setSendStrategy("sendStrategy");

		environmentId = environmentDataDao.addEnvironment(environmentData);

		ProfileData profileData = new ProfileData();
		profileData.setDescription("description");
		profileData.setName("name");
		profileData.setEnvironmentData(environmentData);

		profileDataDao.addProfile(profileData);

		// retrieve the currently stored objects
		// environment
		retrievedEnvironment = (EnvironmentData) environmentDataDao.getEnvironments().get(0);

		// profile retrieved with the environment
		Set<ProfileData> profiles = retrievedEnvironment.getProfiles();
		for (ProfileData profile : profiles) {
			profileWithoutSensorDefinitions = profile;
		}

		// whole profile is retrieved
		profileWithSensorDefinitions = profileDataDao.getProfile(profileData.getId());
	}

	/**
	 * The list containing sensor types of a {@link EnvironmentData} object are retrieved too with
	 * the {@link EnvironmentData} object. So no {@link LazyInitializationException} should be
	 * thrown and the list should be empty.
	 */
	@Test
	public void getSensorTypes() {
		try {
			Set<SensorTypeData> sensorTypes = retrievedEnvironment.getSensorTypes();
			assertTrue(sensorTypes.isEmpty());
		} catch (LazyInitializationException e) {
			fail("Sensor types are not loaded immediately with the environment. Check the hibernate mapping for this relationship. Also check the join mode used for retrieving the data");
		}

	}

	/**
	 * The list containing the profiles of a {@link EnvironmentData} object are retrieved too with
	 * the {@link EnvironmentData} object. So no {@link LazyInitializationException} should be
	 * thrown and the Set should not be empty.
	 */
	@Test
	public void getProfiles() {
		try {
			Set<ProfileData> profiles = retrievedEnvironment.getProfiles();
			assertEquals(profiles.size(), 1);
		} catch (LazyInitializationException e) {
			fail("Profiles are not loaded immediately with the environment. Check the hibernate mapping for this relationship. Also check the join mode used for retrieving the data");
		}
	}

	/**
	 * Exception sensor definitions should not be loaded with an environment. So the test is
	 * successful if a {@link LazyInitializationException} is thrown.
	 */
	@Test(expectedExceptions = LazyInitializationException.class)
	public void getExceptionSensorDefinitionsFromEnvironmentProfile() {
		try {
			Set<ExceptionSensorDefinitionData> exceptionSensorDefinitions = profileWithoutSensorDefinitions.getExceptionSensorDefinitions();
			exceptionSensorDefinitions.isEmpty();
		} catch (LazyInitializationException e) {
			throw e;
		}
	}

	/**
	 * Method sensor definitions should not be loaded with an environment. So the test is successful
	 * if a {@link LazyInitializationException} is thrown.
	 */
	@Test(expectedExceptions = LazyInitializationException.class)
	public void getMethodSensorDefinitionsFromEnvironmentProfile() {
		try {
			Set<MethodSensorDefinitionData> methodSensorDefinitions = profileWithoutSensorDefinitions.getMethodSensorDefinitions();
			methodSensorDefinitions.isEmpty();
		} catch (LazyInitializationException e) {
			throw e;
		}
	}

	/**
	 * Platform sensor definitions should not be loaded with an environment. So the test is
	 * successful if a {@link LazyInitializationException} is thrown.
	 */
	@Test(expectedExceptions = LazyInitializationException.class)
	public void getPlatformSensorDefinitionsFromEnvironmentProfile() {
		try {
			Set<PlatformSensorDefinitionData> platformSensorDefinitions = profileWithoutSensorDefinitions.getPlatformSensorDefinitions();
			platformSensorDefinitions.isEmpty();
		} catch (LazyInitializationException e) {
			throw e;
		}
	}

	/**
	 * Exception sensor definitions should be loaded with a profile. So the test is successful if
	 * during the call of the method @see {@link Set#isEmpty()} no
	 * {@link LazyInitializationException} is thrown and the set is empty.
	 */
	@Test
	public void getExceptionSensorDefinitionsFromProfile() {
		Set<ExceptionSensorDefinitionData> exceptionSensorDefinitions = profileWithSensorDefinitions.getExceptionSensorDefinitions();
		assertTrue(exceptionSensorDefinitions.isEmpty());
	}

	/**
	 * Method sensor definitions should be loaded with a profile. So the test is successful if
	 * during the call of the method @see {@link Set#isEmpty()} no
	 * {@link LazyInitializationException} is thrown and the set is empty.
	 */
	@Test
	public void getMethodSensorDefinitionsFromProfile() {
		Set<MethodSensorDefinitionData> methodSensorDefinitions = profileWithSensorDefinitions.getMethodSensorDefinitions();
		assertTrue(methodSensorDefinitions.isEmpty());
	}

	/**
	 * Platform sensor definitions should be loaded with a profile. So the test is successful if
	 * during the call of the method @see {@link Set#isEmpty()} no
	 * {@link LazyInitializationException} is thrown and the set is empty.
	 */
	@Test
	public void getPlatformSensorDefinitionsFromProfile() {
		Set<PlatformSensorDefinitionData> platformSensorDefinitions = profileWithSensorDefinitions.getPlatformSensorDefinitions();
		assertTrue(platformSensorDefinitions.isEmpty());
	}

	/**
	 * Deletes the initially added {@link EnvironmentDataDao} and {@link ProfileDataDao} object.
	 * 
	 * @throws EntityNotFoundException
	 *             If the object is not found.
	 */
	@AfterTest(alwaysRun = true)
	public void rollBack() throws EntityNotFoundException {
		// deletes also the profileData => the cascade delete is tested
		// before in ProfileDataTest.class
		environmentDataDao.deleteEnvironment(environmentId);
	}
}