package info.novatec.inspectit.cmr.dao.ci.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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
@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-model.xml" })
@SuppressWarnings("PMD")
public class EnvironmentDataDaoTest extends AbstractTransactionalTestNGLogSupport {

	/**
	 * The {@link EnvironmentDataDao} to test.
	 */
	@Autowired
	private EnvironmentDataDao environmentDataDao;

	/**
	 * Stores a single {@link EnvironmentData} object into the database. Storing is successful if
	 * the assigned id of the object is greater than 0.
	 */
	@Test
	public void addEnvironment() {
		EnvironmentData environmentData = new EnvironmentData();
		environmentData.setBufferStrategy("bufferstrategy");
		environmentData.setDescription("description");
		environmentData.setName("environment");
		environmentData.setSendStrategy("sendstrategy");

		environmentDataDao.addEnvironment(environmentData);
		assertThat(environmentData.getId(), is(greaterThan(0L)));
	}

	/**
	 * Stores a single {@link EnvironmentData} object into the database. Afterwards the
	 * {@link EnvironmentData} object is retrieved and the values are compared to those of the
	 * original object.
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
		assertThat(environments.size(), is(equalTo(1)));

		// check environmentDataOne
		EnvironmentData receivedEnvironmentData = environments.get(0);
		assertThat(receivedEnvironmentData.getBufferStrategy(), is(equalTo(environmentData.getBufferStrategy())));
		assertThat(receivedEnvironmentData.getDescription(), is(equalTo(environmentData.getDescription())));
		assertThat(receivedEnvironmentData.getId(), is(equalTo(environmentData.getId())));
		assertThat(receivedEnvironmentData.getName(), is(equalTo(environmentData.getName())));
		assertThat(receivedEnvironmentData.getSendStrategy(), is(equalTo(environmentData.getSendStrategy())));
	}

	/**
	 * Stores a single {@link EnvironmentData} object into the database. Then the
	 * {@link EnvironmentData} object is changed and an update is performed. Afterwards the
	 * {@link EnvironmentData} object is retrieved and the values are compared to those of the first
	 * stored version and the updated version.
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
		assertThat(environments.size(), is(equalTo(1)));

		EnvironmentData receivedEnvironmentData = environments.get(0);
		// check values against updated version
		assertThat(receivedEnvironmentData.getBufferStrategy(), is(equalTo(environmentData.getBufferStrategy())));
		assertThat(receivedEnvironmentData.getDescription(), is(equalTo(environmentData.getDescription())));
		assertThat(receivedEnvironmentData.getId(), is(equalTo(environmentData.getId())));
		assertThat(receivedEnvironmentData.getName(), is(equalTo(environmentData.getName())));
		assertThat(receivedEnvironmentData.getSendStrategy(), is(equalTo(environmentData.getSendStrategy())));
		// check values against initial version
		assertThat(receivedEnvironmentData.getBufferStrategy(), is(not(equalTo("bufferstrategy"))));
		assertThat(receivedEnvironmentData.getBufferStrategy(), is(not(equalTo("description"))));
		assertThat(receivedEnvironmentData.getName(), is(not(equalTo("environment"))));
		assertThat(receivedEnvironmentData.getSendStrategy(), is(not(equalTo("sendstrategy"))));
	}

	/**
	 * Stores a single {@link EnvironmentData} object into the database and afterwards deletes it
	 * twice. Test is successful if no {@link EntityNotFoundException} is thrown during first
	 * deletion and the exception is thrown during second deletion.
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
