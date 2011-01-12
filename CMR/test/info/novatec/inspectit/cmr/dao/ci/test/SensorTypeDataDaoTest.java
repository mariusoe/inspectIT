package info.novatec.inspectit.cmr.dao.ci.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import info.novatec.inspectit.cmr.dao.ci.EnvironmentDataDao;
import info.novatec.inspectit.cmr.dao.ci.SensorTypeDataDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.data.ci.SensorTypeData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * This class provides test method which are testing the methods provided by the
 * {@link SensorTypeDataDao}.
 * 
 * @author Matthias Huber
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-property.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-model.xml" })
public class SensorTypeDataDaoTest extends AbstractTransactionalTestNGLogSupport {

	/**
	 * The {@link SensorTypeDataDao} to test.
	 */
	@Autowired
	private SensorTypeDataDao sensorTypeDataDao;

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

	/**
	 * This method prepares the database so that all needed data for the test is
	 * already stored.
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
	 * Stores a single {@link SensorTypeData} object into the database. Storing
	 * is successful if the assigned id of the object is greater than 0.
	 */
	@Test
	public void addSensorType() {
		SensorTypeData sensorTypeData = new SensorTypeData();
		sensorTypeData.setDescription("description");
		sensorTypeData.setFullyQualifiedName("fullyQualifiedName");
		sensorTypeData.setName("name");
		sensorTypeData.setPriority(SensorTypeData.LOW);
		sensorTypeData.setTypeOption(SensorTypeData.NO_TYPE_OPTION);
		sensorTypeData.setEnvironmentData(environmentData);

		sensorTypeDataDao.addSensorType(sensorTypeData);
		assertTrue(sensorTypeData.getId() > 0);
	}

	/**
	 * Stores a single {@link SensorTypeData} object into the database.
	 * Afterwards the {@link SensorTypeData} object is retrieved and the values
	 * are compared to those of the original object.
	 */
	@Test(dependsOnMethods = { "addSensorType" })
	public void getSensorType() {
		SensorTypeData sensorTypeData = new SensorTypeData();
		sensorTypeData.setDescription("description");
		sensorTypeData.setFullyQualifiedName("fullyQualifiedName");
		sensorTypeData.setName("name");
		sensorTypeData.setPriority(SensorTypeData.LOW);
		sensorTypeData.setTypeOption(SensorTypeData.NO_TYPE_OPTION);
		sensorTypeData.setEnvironmentData(environmentData);

		sensorTypeDataDao.addSensorType(sensorTypeData);

		// flush session. this is needed to establish the association between
		// the profile and the sensor type. Otherwise the
		// sensor type is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		List<EnvironmentData> environments = environmentDataDao.getEnvironments();
		Set<SensorTypeData> sensorTypes = environments.get(0).getSensorTypes();

		assertTrue(!sensorTypes.isEmpty());

		SensorTypeData receivedSensorTypeData = null;
		for (SensorTypeData sensorType : sensorTypes) {
			receivedSensorTypeData = sensorType;
		}

		assertNotNull(receivedSensorTypeData);
		assertEquals(receivedSensorTypeData.getDescription(), sensorTypeData.getDescription());
		assertEquals(receivedSensorTypeData.getEnvironmentData(), sensorTypeData.getEnvironmentData());
		assertEquals(receivedSensorTypeData.getFullyQualifiedName(), sensorTypeData.getFullyQualifiedName());
		assertEquals(receivedSensorTypeData.getId(), sensorTypeData.getId());
		assertEquals(receivedSensorTypeData.getName(), sensorTypeData.getName());
		assertEquals(receivedSensorTypeData.getPriority(), SensorTypeData.LOW);
		assertEquals(receivedSensorTypeData.getTypeOption(), SensorTypeData.NO_TYPE_OPTION);
	}

	/**
	 * Stores a single {@link SensorTypeData} object into the database. Then the
	 * {@link SensorTypeData} object is changed and an update is performed.
	 * Afterwards the {@link SensorTypeData} object is retrieved and the values
	 * are compared to those of the first stored version and the updated
	 * version.
	 */
	@Test(dependsOnMethods = { "getSensorType" })
	public void updateSensorType() {
		SensorTypeData sensorTypeData = new SensorTypeData();
		sensorTypeData.setDescription("description");
		sensorTypeData.setFullyQualifiedName("fullyQualifiedName");
		sensorTypeData.setName("name");
		sensorTypeData.setPriority(SensorTypeData.LOW);
		sensorTypeData.setTypeOption(SensorTypeData.NO_TYPE_OPTION);
		sensorTypeData.setEnvironmentData(environmentData);

		sensorTypeDataDao.addSensorType(sensorTypeData);

		sensorTypeData.setDescription("modified description");
		sensorTypeData.setFullyQualifiedName("modified fullyQualifiedName");
		sensorTypeData.setName("modified name");
		sensorTypeData.setPriority(SensorTypeData.MAX);
		sensorTypeData.setTypeOption(SensorTypeData.AGGREGATE);

		sensorTypeDataDao.updateSensorType(sensorTypeData);

		// flush session. this is needed to establish the association between
		// the profile and the sensor type. Otherwise the
		// sensor type is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		List<EnvironmentData> environments = environmentDataDao.getEnvironments();
		Set<SensorTypeData> sensorTypes = environments.get(0).getSensorTypes();

		assertTrue(!sensorTypes.isEmpty());

		SensorTypeData receivedSensorTypeData = null;
		for (SensorTypeData sensorType : sensorTypes) {
			receivedSensorTypeData = sensorType;
		}

		assertNotNull(receivedSensorTypeData);
		// check values against updated version
		assertEquals(receivedSensorTypeData.getDescription(), sensorTypeData.getDescription());
		assertEquals(receivedSensorTypeData.getEnvironmentData(), sensorTypeData.getEnvironmentData());
		assertEquals(receivedSensorTypeData.getFullyQualifiedName(), sensorTypeData.getFullyQualifiedName());
		assertEquals(receivedSensorTypeData.getId(), sensorTypeData.getId());
		assertEquals(receivedSensorTypeData.getName(), sensorTypeData.getName());
		assertEquals(receivedSensorTypeData.getPriority(), SensorTypeData.MAX);
		assertEquals(receivedSensorTypeData.getTypeOption(), SensorTypeData.AGGREGATE);
		// check values against initial version
		assertTrue(!"description".equals(receivedSensorTypeData.getDescription()));
		assertTrue(!"fullyQualifiedName".equals(receivedSensorTypeData.getFullyQualifiedName()));
		assertTrue(!"name".equals(receivedSensorTypeData.getName()));
		assertTrue(!(SensorTypeData.LOW == receivedSensorTypeData.getPriority()));
		assertTrue(!(SensorTypeData.NO_TYPE_OPTION == receivedSensorTypeData.getTypeOption()));
	}

	/**
	 * Stores a single {@link SensorTypeData} object into the database. Then the
	 * associated {@link EnvironmentData} object is deleted. During the deletion
	 * the {@link SensorTypeData} object should also be deleted. The deletion
	 * afterwards of the {@link SensorTypeData} object should cause an
	 * {@link EntityNotFoundException}.
	 * 
	 * @throws EntityNotFoundException
	 *             If the object is not found.
	 */
	@Test(dependsOnMethods = { "updateSensorType" }, expectedExceptions = EntityNotFoundException.class)
	public void casdadeDeleteOfSensorType() throws EntityNotFoundException {
		SensorTypeData sensorTypeData = new SensorTypeData();
		sensorTypeData.setDescription("description");
		sensorTypeData.setFullyQualifiedName("fullyQualifiedName");
		sensorTypeData.setName("name");
		sensorTypeData.setPriority(SensorTypeData.LOW);
		sensorTypeData.setTypeOption(SensorTypeData.NO_TYPE_OPTION);
		sensorTypeData.setEnvironmentData(environmentData);

		sensorTypeDataDao.addSensorType(sensorTypeData);

		// flush session. this is needed to establish the association between
		// the profile and the sensor type. Otherwise the
		// sensor type is not related to the profile.
		sessionFactory.getCurrentSession().flush();

		environmentDataDao.deleteEnvironment(environmentData.getId());

		sensorTypeDataDao.deleteSensorType(sensorTypeData.getId());
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
