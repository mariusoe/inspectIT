package info.novatec.inspectit.cmr.dao.ci.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import info.novatec.inspectit.cmr.dao.ci.EnvironmentDataDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

/**
 * This class provides test method which are testing the methods provided by the
 * {@link EnvironmentDataDao}.
 * 
 * @author Matthias Huber
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-property.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-model.xml", "classpath:spring/spring-context-util.xml" })
public class EnvironmentDataDaoTest extends AbstractTransactionalTestNGLogSupport {

	/**
	 * The {@link EnvironmentDataDao} to test.
	 */
	@Autowired
	private EnvironmentDataDao environmentDataDao;

	/**
	 * Stores a single {@link EnvironmentData} object into the database. Storing
	 * is successful if the assigned id of the object is greater than 0.
	 */
	@Test
	public void addEnvironment() {
		EnvironmentData environmentData = new EnvironmentData();
		environmentData.setBufferStrategy("bufferstrategy");
		environmentData.setDescription("description");
		environmentData.setName("environment");
		environmentData.setSendStrategy("sendstrategy");

		environmentDataDao.addEnvironment(environmentData);
		assertTrue(environmentData.getId() > 0);
	}

	/**
	 * Stores a single {@link EnvironmentData} object into the database.
	 * Afterwards the {@link EnvironmentData} object is retrieved and the values
	 * are compared to those of the original object.
	 */
	@Test(dependsOnMethods = { "addEnvironment" })
	public void getEnvironments() {
		EnvironmentData environmentData = new EnvironmentData();
		environmentData.setBufferStrategy("bufferstrategy");
		environmentData.setDescription("description");
		environmentData.setName("environment");
		environmentData.setSendStrategy("sendstrategy");

		environmentDataDao.addEnvironment(environmentData);

		List<EnvironmentData> environments = environmentDataDao.getEnvironments();

		// check list size; must be of size 1
		assertEquals(environments.size(), 1);

		// check environmentDataOne
		EnvironmentData receivedEnvironmentData = environments.get(0);
		assertEquals(receivedEnvironmentData.getBufferStrategy(), environmentData.getBufferStrategy());
		assertEquals(receivedEnvironmentData.getDescription(), environmentData.getDescription());
		assertEquals(receivedEnvironmentData.getId(), environmentData.getId());
		assertEquals(receivedEnvironmentData.getName(), environmentData.getName());
		assertEquals(receivedEnvironmentData.getSendStrategy(), environmentData.getSendStrategy());
	}

	/**
	 * Stores a single {@link EnvironmentData} object into the database. Then
	 * the {@link EnvironmentData} object is changed and an update is performed.
	 * Afterwards the {@link EnvironmentData} object is retrieved and the values
	 * are compared to those of the first stored version and the updated
	 * version.
	 */
	@Test(dependsOnMethods = { "getEnvironments" })
	public void updateEnvironmentSettings() {
		EnvironmentData environmentData = new EnvironmentData();
		environmentData.setBufferStrategy("bufferstrategy");
		environmentData.setDescription("description");
		environmentData.setName("environment");
		environmentData.setSendStrategy("sendstrategy");
		environmentDataDao.addEnvironment(environmentData);

		// change values and update
		environmentData.setBufferStrategy("updated bufferstrategy");
		environmentData.setDescription("updated description");
		environmentData.setName("updated environment");
		environmentData.setSendStrategy("updated sendstrategy");

		environmentDataDao.updateEnvironmentSettings(environmentData);

		List<EnvironmentData> environments = environmentDataDao.getEnvironments();
		assertEquals(environments.size(), 1);

		EnvironmentData receivedEnvironmentData = environments.get(0);
		// check values against updated version
		assertEquals(receivedEnvironmentData.getBufferStrategy(), environmentData.getBufferStrategy());
		assertEquals(receivedEnvironmentData.getDescription(), environmentData.getDescription());
		assertEquals(receivedEnvironmentData.getId(), environmentData.getId());
		assertEquals(receivedEnvironmentData.getName(), environmentData.getName());
		assertEquals(receivedEnvironmentData.getSendStrategy(), environmentData.getSendStrategy());
		// check values against initial version
		assertTrue(!"bufferstrategy".equals(receivedEnvironmentData.getBufferStrategy()));
		assertTrue(!"description".equals(receivedEnvironmentData.getDescription()));
		assertTrue(!"environment".equals(receivedEnvironmentData.getName()));
		assertTrue(!"sendstrategy".equals(receivedEnvironmentData.getSendStrategy()));
	}

	/**
	 * Stores a single {@link EnvironmentData} object into the database and
	 * afterwards deletes it twice. Test is successful if no
	 * {@link EntityNotFoundException} is thrown during first deletion and the
	 * exception is thrown during second deletion.
	 * 
	 * @throws EntityNotFoundException
	 *             If object is not found.
	 */
	@Test(dependsOnMethods = { "updateEnvironmentSettings" }, expectedExceptions = EntityNotFoundException.class)
	public void deleteEnvironment() throws EntityNotFoundException {
		EnvironmentData environmentData = new EnvironmentData();
		environmentData.setBufferStrategy("bufferstrategy");
		environmentData.setDescription("description");
		environmentData.setName("environment");
		environmentData.setSendStrategy("sendstrategy");
		environmentDataDao.addEnvironment(environmentData);

		// first deletion
		try {
			environmentDataDao.deleteEnvironment(environmentData.getId());
		} catch (EntityNotFoundException e) {
			fail("The given environment could not be found inside the database. So deleting was not possible");
		}

		// second deletion
		environmentDataDao.deleteEnvironment(environmentData.getId());
	}
}
