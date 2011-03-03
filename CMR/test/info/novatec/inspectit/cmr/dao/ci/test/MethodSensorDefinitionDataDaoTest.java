package info.novatec.inspectit.cmr.dao.ci.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import info.novatec.inspectit.cmr.dao.ci.EnvironmentDataDao;
import info.novatec.inspectit.cmr.dao.ci.MethodSensorDefinitionDataDao;
import info.novatec.inspectit.cmr.dao.ci.ProfileDataDao;
import info.novatec.inspectit.cmr.dao.ci.SensorTypeDataDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.data.ci.MethodSensorDefinitionData;
import info.novatec.inspectit.communication.data.ci.ProfileData;
import info.novatec.inspectit.communication.data.ci.SensorTypeData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * This class provides test method which are testing the methods provided by the
 * {@link MethodSensorDefinitionDataDao}.
 * 
 * @author Matthias Huber
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-property.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-model.xml", "classpath:spring/spring-context-util.xml" })
public class MethodSensorDefinitionDataDaoTest extends AbstractTransactionalTestNGLogSupport {

	/**
	 * The {@link MethodSensorDefinitionDataDao} to test.
	 */
	@Autowired
	private MethodSensorDefinitionDataDao methodSensorDefinitionDataDao;

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
	 * The {@link SensorTypeData} to add an {@link SensorTypeData} object into
	 * the database.
	 */
	@Autowired
	private SensorTypeDataDao sensorTypeDataDao;

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
	 * Stores a single {@link MethodSensorDefinitionData} object into the
	 * database. Storing is successful if the assigned id of the object is
	 * greater than 0.
	 */
	@Test
	public void addMethodSensorDefinition() {
		MethodSensorDefinitionData methodSensorDefinitionData = new MethodSensorDefinitionData();
		methodSensorDefinitionData.setActivated(true);
		methodSensorDefinitionData.setDescription("description");
		methodSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		methodSensorDefinitionData.setName("name");
		methodSensorDefinitionData.setProfileData(profileData);

		methodSensorDefinitionDataDao.addMethodSensorDefinition(methodSensorDefinitionData);
		assertTrue(methodSensorDefinitionData.getId() > 0);
	}

	/**
	 * Stores a single {@link MethodSensorDefinitionData} object into the
	 * database. Afterwards the {@link MethodSensorDefinitionData} object is
	 * retrieved and the values are compared to those of the original object.
	 */
	@Test(dependsOnMethods = { "addMethodSensorDefinition" })
	public void getMethodSensorDefinition() {
		MethodSensorDefinitionData methodSensorDefinitionData = new MethodSensorDefinitionData();
		methodSensorDefinitionData.setActivated(true);
		methodSensorDefinitionData.setDescription("description");
		methodSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		methodSensorDefinitionData.setName("name");
		methodSensorDefinitionData.setProfileData(profileData);
		methodSensorDefinitionData.setSensorOption(MethodSensorDefinitionData.NO_SENSOR_OPTION);

		methodSensorDefinitionDataDao.addMethodSensorDefinition(methodSensorDefinitionData);

		// flush session. this is needed to establish the association between
		// the profile and the method sensor definition. Otherwise the
		// method sensor definition is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		ProfileData receivedProfileData = profileDataDao.getProfile(profileData.getId());
		Set<MethodSensorDefinitionData> methodSensorDefinitions = receivedProfileData.getMethodSensorDefinitions();

		assertTrue(!methodSensorDefinitions.isEmpty());

		MethodSensorDefinitionData receivedMethodSensorDefinition = null;
		for (MethodSensorDefinitionData methodSensorDefinition : methodSensorDefinitions) {
			receivedMethodSensorDefinition = methodSensorDefinition;
		}

		assertNotNull(receivedMethodSensorDefinition);
		assertEquals(receivedMethodSensorDefinition.isActivated(), methodSensorDefinitionData.isActivated());
		assertEquals(receivedMethodSensorDefinition.getDescription(), methodSensorDefinitionData.getDescription());
		assertEquals(receivedMethodSensorDefinition.getFullyQualifiedName(), methodSensorDefinitionData.getFullyQualifiedName());
		assertEquals(receivedMethodSensorDefinition.getName(), methodSensorDefinitionData.getName());
		assertEquals(receivedMethodSensorDefinition.getId(), methodSensorDefinitionData.getId());
		assertEquals(receivedMethodSensorDefinition.getProfileData(), methodSensorDefinitionData.getProfileData());
		assertEquals(receivedMethodSensorDefinition.getSensorOption(), methodSensorDefinitionData.getSensorOption());
	}

	/**
	 * Stores a single {@link MethodSensorDefinitionData} object into the
	 * database. Then the {@link MethodSensorDefinitionData} object is changed
	 * and an update is performed. Afterwards the
	 * {@link MethodSensorDefinitionData} object is retrieved and the values are
	 * compared to those of the first stored version and the updated version.
	 */
	@Test(dependsOnMethods = { "getMethodSensorDefinition" })
	public void updateMethodSensorDefinition() {
		MethodSensorDefinitionData methodSensorDefinitionData = new MethodSensorDefinitionData();
		methodSensorDefinitionData.setActivated(true);
		methodSensorDefinitionData.setDescription("description");
		methodSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		methodSensorDefinitionData.setName("name");
		methodSensorDefinitionData.setProfileData(profileData);
		methodSensorDefinitionData.setSensorOption(MethodSensorDefinitionData.NO_SENSOR_OPTION);

		methodSensorDefinitionDataDao.addMethodSensorDefinition(methodSensorDefinitionData);

		methodSensorDefinitionData.setActivated(false);
		methodSensorDefinitionData.setDescription("modified description");
		methodSensorDefinitionData.setFullyQualifiedName("modified fullyQualifiedName");
		methodSensorDefinitionData.setName("modified name");
		methodSensorDefinitionData.setSensorOption(MethodSensorDefinitionData.INTERFACE);

		methodSensorDefinitionDataDao.updateMethodSensorDefinition(methodSensorDefinitionData);

		// flush session. this is needed to establish the association between
		// the profile and the method sensor definition. Otherwise the
		// method sensor definition is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		ProfileData receivedProfileData = profileDataDao.getProfile(profileData.getId());
		Set<MethodSensorDefinitionData> methodSensorDefinitions = receivedProfileData.getMethodSensorDefinitions();

		assertTrue(!methodSensorDefinitions.isEmpty());

		MethodSensorDefinitionData receivedMethodSensorDefinition = null;
		for (MethodSensorDefinitionData methodSensorDefinition : methodSensorDefinitions) {
			receivedMethodSensorDefinition = methodSensorDefinition;
		}

		assertNotNull(receivedMethodSensorDefinition);
		// check values against updated version
		assertEquals(receivedMethodSensorDefinition.isActivated(), methodSensorDefinitionData.isActivated());
		assertEquals(receivedMethodSensorDefinition.getDescription(), methodSensorDefinitionData.getDescription());
		assertEquals(receivedMethodSensorDefinition.getFullyQualifiedName(), methodSensorDefinitionData.getFullyQualifiedName());
		assertEquals(receivedMethodSensorDefinition.getName(), methodSensorDefinitionData.getName());
		assertEquals(receivedMethodSensorDefinition.getId(), methodSensorDefinitionData.getId());
		assertEquals(receivedMethodSensorDefinition.getProfileData(), methodSensorDefinitionData.getProfileData());
		assertEquals(receivedMethodSensorDefinition.getSensorOption(), methodSensorDefinitionData.getSensorOption());
		// check values against initial version
		assertTrue(true != receivedMethodSensorDefinition.isActivated());
		assertTrue(!"description".equals(receivedMethodSensorDefinition.getDescription()));
		assertTrue(!"fullyQualifiedName".equals(receivedMethodSensorDefinition.getFullyQualifiedName()));
		assertTrue(!"name".equals(receivedMethodSensorDefinition.getName()));
		assertTrue(!(MethodSensorDefinitionData.NO_SENSOR_OPTION == receivedMethodSensorDefinition.getSensorOption()));
	}

	/**
	 * Stores a single {@link MethodSensorDefinitionData} object into the
	 * database and afterwards deletes it twice. Test is successful if no
	 * {@link EntityNotFoundException} is thrown during first deletion and the
	 * exception is thrown during second deletion.
	 * 
	 * @throws EntityNotFoundException
	 *             If object is not found.
	 */
	@Test(dependsOnMethods = { "updateMethodSensorDefinition" }, expectedExceptions = EntityNotFoundException.class)
	public void deleteExistingMethodSensorDefinition() throws EntityNotFoundException {
		MethodSensorDefinitionData methodSensorDefinitionData = new MethodSensorDefinitionData();
		methodSensorDefinitionData.setActivated(true);
		methodSensorDefinitionData.setDescription("description");
		methodSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		methodSensorDefinitionData.setName("name");
		methodSensorDefinitionData.setProfileData(profileData);
		methodSensorDefinitionData.setSensorOption(MethodSensorDefinitionData.NO_SENSOR_OPTION);

		methodSensorDefinitionDataDao.addMethodSensorDefinition(methodSensorDefinitionData);

		try {
			methodSensorDefinitionDataDao.deleteMethodSensorDefinition(methodSensorDefinitionData.getId());
		} catch (EntityNotFoundException e) {
			fail("The given method sensor definition could not be found inside the database. So deleting was not possible");
		}

		// second deletion
		methodSensorDefinitionDataDao.deleteMethodSensorDefinition(methodSensorDefinitionData.getId());
	}

	/**
	 * Stores a single {@link SensorTypeData} object into the database.
	 * Afterwards this object is retrieved an assigned to a
	 * {@link MethodSensorDefinitionData} object which is also stored. Then the
	 * {@link MethodSensorDefinitionData} object is retrieved and the values of
	 * the assigned {@link SensorTypeData} object is compared to the values of
	 * the initially created {@link SensorTypeData} object. These values should
	 * be equal for a successful test.
	 */
	@Test(dependsOnMethods = { "deleteExistingMethodSensorDefinition" })
	public void getSensorType() {
		SensorTypeData sensorTypeData = new SensorTypeData();
		sensorTypeData.setDescription("description");
		sensorTypeData.setFullyQualifiedName("fullyQualifiedName");
		sensorTypeData.setName("name");
		sensorTypeData.setPriority(SensorTypeData.LOW);
		sensorTypeData.setTypeOption(SensorTypeData.NO_TYPE_OPTION);
		sensorTypeData.setEnvironmentData(environmentData);

		sensorTypeDataDao.addSensorType(sensorTypeData);

		// flush session. this is needed to establish the association the
		// associations between the object
		sessionFactory.getCurrentSession().flush();

		// get the sensor types from the environment
		EnvironmentData receivedEnvironmentData = environmentDataDao.getEnvironments().get(0);
		Set<SensorTypeData> receivedSensorTypes = receivedEnvironmentData.getSensorTypes();
		assertTrue(!receivedSensorTypes.isEmpty());

		MethodSensorDefinitionData methodSensorDefinitionData = new MethodSensorDefinitionData();
		methodSensorDefinitionData.setActivated(true);
		methodSensorDefinitionData.setDescription("description");
		methodSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		methodSensorDefinitionData.setName("name");
		methodSensorDefinitionData.setProfileData(profileData);
		methodSensorDefinitionData.setSensorOption(MethodSensorDefinitionData.NO_SENSOR_OPTION);

		Set<SensorTypeData> sensorTypes = new HashSet<SensorTypeData>();
		sensorTypes.addAll(receivedSensorTypes);
		methodSensorDefinitionData.setSensorTypes(sensorTypes);
		methodSensorDefinitionDataDao.addMethodSensorDefinition(methodSensorDefinitionData);

		// flush session. this is needed to establish the association the
		// associations between the object
		sessionFactory.getCurrentSession().flush();

		// get the profile with the contained method sensor definition
		ProfileData receivedProfileData = profileDataDao.getProfile(profileData.getId());
		Set<MethodSensorDefinitionData> methodSensorDefinitions = receivedProfileData.getMethodSensorDefinitions();

		MethodSensorDefinitionData receivedMethodSensorDefinition = null;
		for (MethodSensorDefinitionData methodSensorDefinition : methodSensorDefinitions) {
			receivedMethodSensorDefinition = methodSensorDefinition;
		}

		assertNotNull(receivedMethodSensorDefinition);
		Set<SensorTypeData> assignedSensorTypes = receivedMethodSensorDefinition.getSensorTypes();
		assertTrue(!assignedSensorTypes.isEmpty());

		SensorTypeData receivedSensorTypeData = null;
		for (SensorTypeData sensorType : assignedSensorTypes) {
			receivedSensorTypeData = sensorType;
		}

		assertNotNull(receivedSensorTypeData);
		// check values against added sensor type
		assertEquals(receivedSensorTypeData.getDescription(), sensorTypeData.getDescription());
		assertEquals(receivedSensorTypeData.getEnvironmentData(), sensorTypeData.getEnvironmentData());
		assertEquals(receivedSensorTypeData.getFullyQualifiedName(), sensorTypeData.getFullyQualifiedName());
		assertEquals(receivedSensorTypeData.getId(), sensorTypeData.getId());
		assertEquals(receivedSensorTypeData.getName(), sensorTypeData.getName());
		assertEquals(receivedSensorTypeData.getPriority(), sensorTypeData.getPriority());
		assertEquals(receivedSensorTypeData.getTypeOption(), sensorTypeData.getTypeOption());
	}

	/**
	 * Stores a single {@link MethodSensorDefinitionData} object into the
	 * database. Then the associated {@link ProfileData} object is deleted.
	 * During the deletion the {@link MethodSensorDefinitionData} object should
	 * also be deleted. The deletion afterwards of the
	 * {@link MethodSensorDefinitionData} object should cause an
	 * {@link EntityNotFoundException}.
	 * 
	 * @throws EntityNotFoundException
	 *             If object is not found.
	 */
	@Test(dependsOnMethods = { "getSensorType" }, expectedExceptions = EntityNotFoundException.class)
	public void cascadeDeleteIfProfileIsDeleted() throws EntityNotFoundException {
		MethodSensorDefinitionData methodSensorDefinitionData = new MethodSensorDefinitionData();
		methodSensorDefinitionData.setActivated(true);
		methodSensorDefinitionData.setDescription("description");
		methodSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		methodSensorDefinitionData.setName("name");
		methodSensorDefinitionData.setProfileData(profileData);
		methodSensorDefinitionData.setSensorOption(MethodSensorDefinitionData.NO_SENSOR_OPTION);

		methodSensorDefinitionDataDao.addMethodSensorDefinition(methodSensorDefinitionData);

		// flush session. this is needed to establish the association between
		// the profile and the method sensor definition. Otherwise the
		// method sensor definition is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		profileDataDao.deleteProfile(profileData.getId());

		methodSensorDefinitionDataDao.deleteMethodSensorDefinition(methodSensorDefinitionData.getId());
	}

	/**
	 * Stores a single {@link MethodSensorDefinitionData} object into the
	 * database. Then the associated {@link EnvironmentData} object is deleted.
	 * During the deletion the {@link MethodSensorDefinitionData} object should
	 * also be deleted. The deletion afterwards of the
	 * {@link MethodSensorDefinitionData} object should cause an
	 * {@link EntityNotFoundException}.
	 * 
	 * @throws EntityNotFoundException
	 *             If object is not found.
	 */
	@Test(dependsOnMethods = { "cascadeDeleteIfProfileIsDeleted" }, expectedExceptions = EntityNotFoundException.class)
	public void cascadeDeleteIfEnvironmentIsDeleted() throws EntityNotFoundException {
		MethodSensorDefinitionData methodSensorDefinitionData = new MethodSensorDefinitionData();
		methodSensorDefinitionData.setActivated(true);
		methodSensorDefinitionData.setDescription("description");
		methodSensorDefinitionData.setFullyQualifiedName("fullyQualifiedName");
		methodSensorDefinitionData.setName("name");
		methodSensorDefinitionData.setProfileData(profileData);
		methodSensorDefinitionData.setSensorOption(MethodSensorDefinitionData.NO_SENSOR_OPTION);

		methodSensorDefinitionDataDao.addMethodSensorDefinition(methodSensorDefinitionData);

		// flush session. this is needed to establish the association between
		// the profile and the exception sensor definition. Otherwise the
		// exception sensor definition is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		environmentDataDao.deleteEnvironment(environmentData.getId());

		methodSensorDefinitionDataDao.deleteMethodSensorDefinition(methodSensorDefinitionData.getId());
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
