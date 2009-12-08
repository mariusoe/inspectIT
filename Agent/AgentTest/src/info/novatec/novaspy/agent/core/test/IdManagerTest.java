package info.novatec.novaspy.agent.core.test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import info.novatec.novaspy.agent.config.IConfigurationStorage;
import info.novatec.novaspy.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.novaspy.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.novaspy.agent.config.impl.RegisteredSensorConfig;
import info.novatec.novaspy.agent.config.impl.RepositoryConfig;
import info.novatec.novaspy.agent.connection.IConnection;
import info.novatec.novaspy.agent.connection.RegistrationException;
import info.novatec.novaspy.agent.connection.ServerUnavailableException;
import info.novatec.novaspy.agent.core.IIdManager;
import info.novatec.novaspy.agent.core.IdNotAvailableException;
import info.novatec.novaspy.agent.core.impl.IdManager;
import info.novatec.novaspy.agent.test.AbstractLogSupport;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IdManagerTest extends AbstractLogSupport {

	@Mock
	private IConfigurationStorage configurationStorage;

	@Mock
	private IConnection connection;

	private IIdManager idManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Level getLogLevel() {
		return Level.OFF;
	}

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		idManager = new IdManager(configurationStorage, connection);
	}

	/**
	 * This method could <b>fail</b> if the testing machine is currently under
	 * heavy load. There is no reliable way to make this test always successful.
	 */
	@Test
	public void startStop() throws ConnectException, InterruptedException, ServerUnavailableException, RegistrationException {
		String host = "localhost";
		int port = 1099;
		RepositoryConfig repositoryConfig = new RepositoryConfig(host, port);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		String agentName = "testagent";
		when(configurationStorage.getAgentName()).thenReturn(agentName);

		idManager.start();

		verify(configurationStorage, times(1)).getMethodSensorTypes();
		verify(configurationStorage, times(1)).getPlatformSensorTypes();
		verify(configurationStorage, times(1)).getExceptionSensorTypes();

		idManager.stop();
	}

	/**
	 * This method could <b>fail</b> if the testing machine is currently under
	 * heavy load. There is no reliable way to make this test always successful.
	 */
	@Test
	public void connected() throws InterruptedException, ServerUnavailableException, RegistrationException {
		when(connection.isConnected()).thenReturn(true);

		String host = "localhost";
		int port = 1099;
		String agentName = "testagent";
		when(configurationStorage.getAgentName()).thenReturn(agentName);
		RepositoryConfig repositoryConfig = new RepositoryConfig(host, port);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);

		idManager.start();

		synchronized (this) {
			this.wait(2000L);
		}

		verify(configurationStorage, times(1)).getMethodSensorTypes();
		verify(configurationStorage, times(1)).getPlatformSensorTypes();
		verify(configurationStorage, times(1)).getExceptionSensorTypes();

		idManager.stop();

		verifyNoMoreInteractions(configurationStorage, connection);
	}

	@Test
	public void connectAndRetrievePlatformId() throws ServerUnavailableException, RegistrationException, IdNotAvailableException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(configurationStorage.getAgentName()).thenReturn("testAgent");

		long fakePlatformId = 7L;
		when(connection.isConnected()).thenReturn(false);
		when(connection.registerPlatform("testAgent")).thenReturn(fakePlatformId);

		idManager.start();
		long platformId = idManager.getPlatformId();
		idManager.stop();

		assertEquals(platformId, fakePlatformId);
	}

	@Test
	public void retrievePlatformId() throws IdNotAvailableException, ServerUnavailableException, RegistrationException, InterruptedException {
		long fakePlatformId = 3L;
		when(connection.isConnected()).thenReturn(true);
		when(connection.registerPlatform("testAgent")).thenReturn(fakePlatformId);
		when(configurationStorage.getAgentName()).thenReturn("testAgent");

		idManager.start();
		long platformId = idManager.getPlatformId();
		idManager.stop();

		assertEquals(platformId, fakePlatformId);
	}

	@Test(expectedExceptions = { IdNotAvailableException.class })
	public void platformIdNotAvailable() throws ConnectException, IdNotAvailableException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(connection.isConnected()).thenReturn(false);
		doThrow(new ConnectException("fake")).when(connection).connect(anyString(), anyInt());

		idManager.start();
		idManager.getPlatformId();
	}

	/**
	 * This method could <b>fail</b> if the testing machine is currently under
	 * heavy load. There is no reliable way to make this test always successful.
	 */
	@Test
	public void registerMethodSensorTypes() throws InterruptedException, IdNotAvailableException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(configurationStorage.getAgentName()).thenReturn("testAgent");
		when(connection.isConnected()).thenReturn(true);

		MethodSensorTypeConfig methodSensorType = mock(MethodSensorTypeConfig.class);
		List<MethodSensorTypeConfig> methodSensorTypes = new ArrayList<MethodSensorTypeConfig>();
		methodSensorTypes.add(methodSensorType);
		when(configurationStorage.getMethodSensorTypes()).thenReturn(methodSensorTypes);

		idManager.start();
		assertEquals(methodSensorType.getId(), 0);

		synchronized (this) {
			this.wait(2000L);
		}

		assertTrue(idManager.getRegisteredSensorTypeId(methodSensorType.getId()) != -1);

		idManager.stop();
	}

	/**
	 * This method could <b>fail</b> if the testing machine is currently under
	 * heavy load. There is no reliable way to make this test always successful.
	 */
	@Test
	public void registerPlatformSensorTypes() throws InterruptedException, IdNotAvailableException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(configurationStorage.getAgentName()).thenReturn("testAgent");
		when(connection.isConnected()).thenReturn(true);

		PlatformSensorTypeConfig platformSensorType = mock(PlatformSensorTypeConfig.class);
		List<PlatformSensorTypeConfig> platformSensorTypes = new ArrayList<PlatformSensorTypeConfig>();
		platformSensorTypes.add(platformSensorType);
		when(configurationStorage.getPlatformSensorTypes()).thenReturn(platformSensorTypes);

		idManager.start();
		assertEquals(platformSensorType.getId(), 0);

		synchronized (this) {
			this.wait(2000L);
		}

		assertEquals(idManager.getRegisteredSensorTypeId(platformSensorType.getId()), 0);

		idManager.stop();
	}

	/**
	 * This method could <b>fail</b> if the testing machine is currently under
	 * heavy load. There is no reliable way to make this test always successful.
	 */
	@Test
	public void registerExceptionSensorTypes() throws InterruptedException, IdNotAvailableException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(configurationStorage.getAgentName()).thenReturn("testAgent");
		when(connection.isConnected()).thenReturn(true);

		MethodSensorTypeConfig exceptionSensorType = mock(MethodSensorTypeConfig.class);
		List<MethodSensorTypeConfig> exceptionSensorTypes = new ArrayList<MethodSensorTypeConfig>();
		exceptionSensorTypes.add(exceptionSensorType);
		when(configurationStorage.getExceptionSensorTypes()).thenReturn(exceptionSensorTypes);

		idManager.start();
		assertEquals(exceptionSensorType.getId(), 0);

		synchronized (this) {
			this.wait(2000L);
		}

		assertEquals(idManager.getRegisteredSensorTypeId(exceptionSensorType.getId()), 0);

		idManager.stop();
	}

	@Test(expectedExceptions = { IdNotAvailableException.class })
	public void sensorTypeIdNotAvailable() throws InterruptedException, IdNotAvailableException, ConnectException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(connection.isConnected()).thenReturn(false);
		doThrow(new ConnectException("fake")).when(connection).connect(anyString(), anyInt());

		MethodSensorTypeConfig methodSensorType = mock(MethodSensorTypeConfig.class);
		List<MethodSensorTypeConfig> methodSensorTypes = new ArrayList<MethodSensorTypeConfig>();
		methodSensorTypes.add(methodSensorType);
		when(configurationStorage.getMethodSensorTypes()).thenReturn(methodSensorTypes);

		idManager.start();
		assertEquals(methodSensorType.getId(), 0);

		idManager.getRegisteredSensorTypeId(methodSensorType.getId());
	}

	@Test
	public void registerMethod() throws ConnectException, ServerUnavailableException, RegistrationException, IdNotAvailableException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(connection.isConnected()).thenReturn(true);

		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);

		idManager.start();

		when(connection.registerMethod(anyInt(), eq(registeredSensorConfig))).thenReturn(7L).thenReturn(13L);
		long id = idManager.registerMethod(registeredSensorConfig);
		assertTrue(id >= 0);
		assertEquals(idManager.getRegisteredMethodId(id), 7L);

		id = idManager.registerMethod(registeredSensorConfig);
		assertTrue(id >= 0);
		assertEquals(idManager.getRegisteredMethodId(id), 13L);

		idManager.stop();
	}

	@Test
	public void testMapping() throws ServerUnavailableException, RegistrationException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(connection.isConnected()).thenReturn(true);

		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		MethodSensorTypeConfig methodSensorType = mock(MethodSensorTypeConfig.class);

		idManager.start();

		when(connection.registerMethod(anyInt(), eq(registeredSensorConfig))).thenReturn(7L);
		when(connection.registerMethodSensorType(anyInt(), eq(methodSensorType))).thenReturn(5L);
		long methodId = idManager.registerMethod(registeredSensorConfig);
		long sensorTypeId = idManager.registerMethodSensorType(methodSensorType);
		idManager.addSensorTypeToMethod(sensorTypeId, methodId);

		idManager.stop();

		verify(connection).addSensorTypeToMethod(5L, 7L);
	}
}
