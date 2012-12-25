package info.novatec.inspectit.cmr.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.dao.impl.MethodIdentDaoImpl;
import info.novatec.inspectit.cmr.dao.impl.MethodSensorTypeIdentDaoImpl;
import info.novatec.inspectit.cmr.dao.impl.PlatformIdentDaoImpl;
import info.novatec.inspectit.cmr.dao.impl.PlatformSensorTypeIdentDaoImpl;
import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;
import info.novatec.inspectit.cmr.service.exception.ServiceException;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.cmr.util.AgentStatusDataProvider;
import info.novatec.inspectit.cmr.util.LicenseUtil;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.schlichtherle.license.LicenseContentException;

/**
 * Thesting the {@link RegistrationService} of CMR.
 * 
 * @author Ivan Senic
 * 
 */
public class RegistrationServiceTest extends AbstractTestNGLogSupport {

	/**
	 * Service to test.
	 */
	private RegistrationService registrationService;

	/**
	 * Mocked {@link LicenseUtil}.
	 */
	@Mock
	private LicenseUtil licenseUtil;

	/**
	 * Mocked {@link PlatformIdentDaoImpl}.
	 */
	@Mock
	private PlatformIdentDaoImpl platformIdentDao;

	/**
	 * Mocked {@link MethodIdentDaoImpl}.
	 */
	@Mock
	private MethodIdentDaoImpl methodIdentDao;

	/**
	 * Mocked {@link MethodSensorTypeIdentDaoImpl}.
	 */
	@Mock
	private MethodSensorTypeIdentDaoImpl methodSensorTypeIdentDao;

	/**
	 * Mocked {@link PlatformSensorTypeIdentDaoImpl}.
	 */
	@Mock
	private PlatformSensorTypeIdentDaoImpl platformSensorTypeIdentDao;

	@Mock
	private AgentStatusDataProvider agentStatusDataProvider;

	/**
	 * Initializes mocks. Has to run before each test so that mocks are clear.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);

		registrationService = new RegistrationService();
		registrationService.licenseUtil = licenseUtil;
		registrationService.platformIdentDao = platformIdentDao;
		registrationService.methodIdentDao = methodIdentDao;
		registrationService.methodSensorTypeIdentDao = methodSensorTypeIdentDao;
		registrationService.platformSensorTypeIdentDao = platformSensorTypeIdentDao;
		registrationService.agentStatusDataProvider = agentStatusDataProvider;
		registrationService.log = LogFactory.getLog(RegistrationService.class);
	}

	/**
	 * Test that no registration will be done if the {@link LicenseUtil} does not validate license.
	 * 
	 * @throws LicenseContentException
	 *             If {@link LicenseContentException} occurs.
	 * @throws RemoteException
	 *             If remote exception occurs.
	 * @throws ServiceException
	 *             If {@link ServiceException} occurs.
	 */
	@Test(expectedExceptions = { LicenseException.class })
	@SuppressWarnings("unchecked")
	public void noRegistrationAllowedByLicenseUtil() throws LicenseContentException, RemoteException, ServiceException {
		doThrow(LicenseContentException.class).when(licenseUtil).validateLicense(anyList(), anyString());
		try {
			registrationService.registerPlatformIdent(new ArrayList<String>(), "agentName", "version");
		} catch (LicenseException e) {
			verifyZeroInteractions(platformIdentDao);
			throw e;
		}
	}

	/**
	 * Tests that an exception will be thrown if the database returns two or more platform idents
	 * after findByExample search.
	 * 
	 * @throws LicenseContentException
	 *             If {@link LicenseContentException} occurs.
	 * @throws RemoteException
	 *             If remote exception occurs.
	 * @throws ServiceException
	 */
	@Test(expectedExceptions = { ServiceException.class })
	public void noRegistrationTwoAgents() throws LicenseContentException, RemoteException, ServiceException {
		List<String> definedIps = new ArrayList<String>();
		definedIps.add("ip");
		String agentName = "agentName";
		String version = "version";

		doNothing().when(licenseUtil).validateLicense(definedIps, agentName);

		List<PlatformIdent> dbResponseList = new ArrayList<PlatformIdent>();
		dbResponseList.add(new PlatformIdent());
		dbResponseList.add(new PlatformIdent());
		when(platformIdentDao.findByExample((PlatformIdent) anyObject())).thenReturn(dbResponseList);

		registrationService.registerPlatformIdent(definedIps, agentName, version);
	}

	/**
	 * Test that registration will be done properly if the {@link LicenseUtil} validates license.
	 * 
	 * @throws LicenseContentException
	 *             If {@link LicenseContentException} occurs.
	 * @throws RemoteException
	 *             If remote exception occurs.
	 * @throws ServiceException
	 *             If {@link ServiceException} occurs.
	 */
	@Test
	public void registerNewPlatformIdent() throws LicenseContentException, RemoteException, ServiceException {
		final long platformId = 10;
		List<String> definedIps = new ArrayList<String>();
		definedIps.add("ip");
		String agentName = "agentName";
		String version = "version";

		doNothing().when(licenseUtil).validateLicense(definedIps, agentName);
		when(platformIdentDao.findByExample((PlatformIdent) anyObject())).thenReturn(Collections.<PlatformIdent> emptyList());
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				PlatformIdent platformIdent = (PlatformIdent) invocation.getArguments()[0];
				platformIdent.setId(Long.valueOf(platformId));
				return null;
			}
		}).when(platformIdentDao).saveOrUpdate((PlatformIdent) anyObject());

		long registeredId = registrationService.registerPlatformIdent(definedIps, agentName, version);
		assertThat(registeredId, is(equalTo(platformId)));

		ArgumentCaptor<PlatformIdent> argument = ArgumentCaptor.forClass(PlatformIdent.class);
		verify(platformIdentDao, times(1)).saveOrUpdate(argument.capture());

		assertThat(argument.getValue().getDefinedIPs(), is(equalTo(definedIps)));
		assertThat(argument.getValue().getAgentName(), is(equalTo(agentName)));
		assertThat(argument.getValue().getVersion(), is(equalTo(version)));
		assertThat(argument.getValue().getTimeStamp(), is(notNullValue()));

		verify(agentStatusDataProvider, times(1)).registerConnected(platformId);
	}

	/**
	 * Tests that the version and timestamp will be updated if the agent is already registered.
	 * 
	 * @throws LicenseContentException
	 *             If {@link LicenseContentException} occurs.
	 * @throws RemoteException
	 *             If remote exception occurs.
	 * @throws ServiceException
	 *             If {@link ServiceException} occurs.
	 */
	@Test
	public void registerExistingPlatformIdent() throws LicenseContentException, RemoteException, ServiceException {
		long platformId = 10;
		List<String> definedIps = new ArrayList<String>();
		definedIps.add("ip");
		String agentName = "agentName";
		String version = "version";
		Timestamp timestamp = new Timestamp(1);

		PlatformIdent platformIdent = new PlatformIdent();
		platformIdent.setId(Long.valueOf(platformId));
		platformIdent.setAgentName(agentName);
		platformIdent.setDefinedIPs(definedIps);
		platformIdent.setVersion("versionOld");
		platformIdent.setTimeStamp(timestamp);
		List<PlatformIdent> findByExampleList = new ArrayList<PlatformIdent>();
		findByExampleList.add(platformIdent);

		doNothing().when(licenseUtil).validateLicense(definedIps, agentName);
		when(platformIdentDao.findByExample((PlatformIdent) anyObject())).thenReturn(findByExampleList);

		long registeredId = registrationService.registerPlatformIdent(definedIps, agentName, version);
		assertThat(registeredId, is(equalTo(platformId)));

		ArgumentCaptor<PlatformIdent> argument = ArgumentCaptor.forClass(PlatformIdent.class);
		verify(platformIdentDao, times(1)).saveOrUpdate(argument.capture());

		assertThat(argument.getValue().getDefinedIPs(), is(equalTo(definedIps)));
		assertThat(argument.getValue().getAgentName(), is(equalTo(agentName)));
		assertThat(argument.getValue().getVersion(), is(equalTo(version)));
		assertThat(argument.getValue().getTimeStamp(), is(notNullValue()));
		assertThat(argument.getValue().getTimeStamp(), is(not(timestamp)));

		verify(agentStatusDataProvider, times(1)).registerConnected(platformId);
	}

	/**
	 * Test that registration will be done properlly if the {@link LicenseUtil} validates license
	 * and IP based registration is off.
	 * 
	 * @throws LicenseContentException
	 *             If {@link LicenseContentException} occurs.
	 * @throws RemoteException
	 * @throws ServiceException
	 *             If {@link ServiceException} occurs.
	 */
	@Test
	public void registerNewPlatformIdentNoIpBased() throws LicenseContentException, LicenseException, RemoteException, ServiceException {
		final long platformId = 10;
		List<String> definedIps = new ArrayList<String>();
		definedIps.add("ip");
		String agentName = "agentName";
		String version = "version";

		registrationService.ipBasedAgentRegistration = false;
		doNothing().when(licenseUtil).validateLicense(definedIps, agentName);
		when(platformIdentDao.findByExample((PlatformIdent) anyObject())).thenReturn(Collections.<PlatformIdent> emptyList());
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				PlatformIdent platformIdent = (PlatformIdent) invocation.getArguments()[0];
				platformIdent.setId(Long.valueOf(platformId));
				return null;
			}
		}).when(platformIdentDao).saveOrUpdate((PlatformIdent) anyObject());

		long registeredId = registrationService.registerPlatformIdent(definedIps, agentName, version);
		assertThat(registeredId, equalTo(platformId));

		ArgumentCaptor<PlatformIdent> argument = ArgumentCaptor.forClass(PlatformIdent.class);
		verify(platformIdentDao, times(1)).saveOrUpdate(argument.capture());

		assertThat(argument.getValue().getDefinedIPs(), equalTo(definedIps));
		assertThat(argument.getValue().getAgentName(), equalTo(agentName));
		assertThat(argument.getValue().getVersion(), equalTo(version));
		assertThat(argument.getValue().getTimeStamp(), is(notNullValue()));

		verify(agentStatusDataProvider, times(1)).registerConnected(platformId);
	}

	/**
	 * Tests that the version and timestamp will be updated if the agent is already registered and
	 * IP registration is off.
	 * 
	 * @throws LicenseContentException
	 *             If {@link LicenseContentException} occurs.
	 * @throws RemoteException
	 *             If remote exception occurs.
	 * @throws ServiceException
	 *             If {@link ServiceException} occurs.
	 */
	@Test
	public void registerExistingPlatformIdentNoIpBased() throws LicenseContentException, RemoteException, ServiceException {
		long platformId = 10;
		List<String> definedIps = new ArrayList<String>();
		definedIps.add("ip");
		String agentName = "agentName";
		String version = "version";
		Timestamp timestamp = new Timestamp(1);

		PlatformIdent platformIdent = new PlatformIdent();
		platformIdent.setId(Long.valueOf(platformId));
		platformIdent.setAgentName(agentName);
		platformIdent.setDefinedIPs(Collections.<String> emptyList());
		platformIdent.setVersion("versionOld");
		platformIdent.setTimeStamp(timestamp);
		List<PlatformIdent> findByExampleList = new ArrayList<PlatformIdent>();
		findByExampleList.add(platformIdent);

		registrationService.ipBasedAgentRegistration = false;
		doNothing().when(licenseUtil).validateLicense(definedIps, agentName);
		when(platformIdentDao.findByExample((PlatformIdent) anyObject())).thenReturn(findByExampleList);

		long registeredId = registrationService.registerPlatformIdent(definedIps, agentName, version);
		assertThat(registeredId, equalTo(platformId));

		ArgumentCaptor<PlatformIdent> argument = ArgumentCaptor.forClass(PlatformIdent.class);
		verify(platformIdentDao, times(1)).saveOrUpdate(argument.capture());

		assertThat(argument.getValue().getDefinedIPs(), equalTo(definedIps));
		assertThat(argument.getValue().getAgentName(), equalTo(agentName));
		assertThat(argument.getValue().getVersion(), equalTo(version));
		assertThat(argument.getValue().getTimeStamp(), is(notNullValue()));
		assertThat(argument.getValue().getTimeStamp(), not(equalTo(timestamp)));

		verify(agentStatusDataProvider, times(1)).registerConnected(platformId);
	}

	/**
	 * Tests registration of the new {@link MethodIdent}.
	 * 
	 * @throws RemoteException
	 *             If {@link RemoteException} occurs.
	 */
	@Test
	public void registerNewMethodIdent() throws RemoteException {
		final long methodId = 20;
		long platformId = 1;
		String packageName = "package";
		String className = "class";
		String methodName = "method";
		List<String> parameterTypes = new ArrayList<String>();
		parameterTypes.add("parameter");
		String returnType = "returnType";
		int modifiers = 2;

		PlatformIdent platformIdent = new PlatformIdent();
		when(platformIdentDao.load(platformId)).thenReturn(platformIdent);
		when(methodIdentDao.findForPlatformIdent(eq(platformIdent), (MethodIdent) anyObject())).thenReturn(Collections.<MethodIdent> emptyList());
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				MethodIdent methodIdent = (MethodIdent) invocation.getArguments()[0];
				methodIdent.setId(Long.valueOf(methodId));
				return null;
			}
		}).when(methodIdentDao).saveOrUpdate((MethodIdent) anyObject());

		long registeredId = registrationService.registerMethodIdent(platformId, packageName, className, methodName, parameterTypes, returnType, modifiers);
		assertThat(registeredId, equalTo(methodId));

		ArgumentCaptor<MethodIdent> argument = ArgumentCaptor.forClass(MethodIdent.class);
		verify(methodIdentDao, times(1)).saveOrUpdate(argument.capture());

		assertThat(argument.getValue().getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(argument.getValue().getPackageName(), is(equalTo(packageName)));
		assertThat(argument.getValue().getClassName(), is(equalTo(className)));
		assertThat(argument.getValue().getMethodName(), is(equalTo(methodName)));
		assertThat(argument.getValue().getParameters(), is(equalTo(parameterTypes)));
		assertThat(argument.getValue().getReturnType(), is(equalTo(returnType)));
		assertThat(argument.getValue().getModifiers(), is(equalTo(modifiers)));
		assertThat(argument.getValue(), is(notNullValue()));
	}

	/**
	 * Tests registration of the existing {@link MethodIdent}.
	 * 
	 * @throws RemoteException
	 *             If {@link RemoteException} occurs.
	 */
	@Test
	public void registerExistnigMethodIdent() throws RemoteException {
		final long methodId = 20;
		long platformId = 1;
		String packageName = "package";
		String className = "class";
		String methodName = "method";
		List<String> parameterTypes = new ArrayList<String>();
		parameterTypes.add("parameter");
		String returnType = "returnType";
		int modifiers = 2;
		Timestamp timestamp = new Timestamp(1);

		MethodIdent methodIdent = new MethodIdent();
		methodIdent.setId(Long.valueOf(methodId));
		methodIdent.setPackageName(packageName);
		methodIdent.setClassName(className);
		methodIdent.setMethodName(methodName);
		methodIdent.setParameters(parameterTypes);
		methodIdent.setReturnType(returnType);
		methodIdent.setModifiers(modifiers);
		methodIdent.setTimeStamp(timestamp);

		List<MethodIdent> findByExampleList = new ArrayList<MethodIdent>();
		findByExampleList.add(methodIdent);

		PlatformIdent platformIdent = new PlatformIdent();
		when(platformIdentDao.load(platformId)).thenReturn(platformIdent);
		when(methodIdentDao.findForPlatformIdent(eq(platformIdent), (MethodIdent) anyObject())).thenReturn(findByExampleList);

		long registeredId = registrationService.registerMethodIdent(platformId, packageName, className, methodName, parameterTypes, returnType, modifiers);
		assertThat(registeredId, equalTo(methodId));

		ArgumentCaptor<MethodIdent> argument = ArgumentCaptor.forClass(MethodIdent.class);
		verify(methodIdentDao, times(1)).saveOrUpdate(argument.capture());

		assertThat(argument.getValue().getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(argument.getValue().getPackageName(), is(equalTo(packageName)));
		assertThat(argument.getValue().getClassName(), is(equalTo(className)));
		assertThat(argument.getValue().getMethodName(), is(equalTo(methodName)));
		assertThat(argument.getValue().getParameters(), is(equalTo(parameterTypes)));
		assertThat(argument.getValue().getReturnType(), is(equalTo(returnType)));
		assertThat(argument.getValue().getModifiers(), is(equalTo(modifiers)));
		assertThat(argument.getValue(), is(notNullValue()));
		assertThat(argument.getValue().getTimeStamp(), is(not(timestamp)));
	}

	/**
	 * Test the registration of the method sensor type.
	 * 
	 * @throws RemoteException
	 *             If {@link RemoteException} occurs.
	 */
	@Test
	public void registerMethodSensorType() throws RemoteException {
		final long methodSensorId = 30;
		long platformId = 1;
		String fqcName = "class";

		PlatformIdent platformIdent = new PlatformIdent();
		when(platformIdentDao.load(platformId)).thenReturn(platformIdent);
		when(methodSensorTypeIdentDao.findByExample((MethodSensorTypeIdent) anyObject())).thenReturn(Collections.<MethodSensorTypeIdent> emptyList());
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				MethodSensorTypeIdent methodSensorIdent = (MethodSensorTypeIdent) invocation.getArguments()[0];
				methodSensorIdent.setId(Long.valueOf(methodSensorId));
				return null;
			}
		}).when(methodSensorTypeIdentDao).saveOrUpdate((MethodSensorTypeIdent) anyObject());

		long registeredId = registrationService.registerMethodSensorTypeIdent(platformId, fqcName);
		assertThat(registeredId, is(equalTo(methodSensorId)));

		ArgumentCaptor<MethodSensorTypeIdent> methodSensorArgument = ArgumentCaptor.forClass(MethodSensorTypeIdent.class);
		verify(methodSensorTypeIdentDao, times(1)).saveOrUpdate(methodSensorArgument.capture());
		assertThat(methodSensorArgument.getValue().getFullyQualifiedClassName(), is(equalTo(fqcName)));

		verify(platformIdentDao, times(1)).saveOrUpdate(platformIdent);
		assertThat(methodSensorArgument.getValue(), is(equalTo(platformIdent.getSensorTypeIdents().toArray()[0])));
	}

	/**
	 * Test the registration of the platform sensor type.
	 * 
	 * @throws RemoteException
	 *             If {@link RemoteException} occurs.
	 */
	@Test
	public void registerPlatformSensorType() throws RemoteException {
		final long platformSensorId = 20;
		long platformId = 1;
		String fqcName = "class";

		PlatformIdent platformIdent = new PlatformIdent();
		when(platformIdentDao.load(platformId)).thenReturn(platformIdent);
		when(platformSensorTypeIdentDao.findByExample((PlatformSensorTypeIdent) anyObject())).thenReturn(Collections.<PlatformSensorTypeIdent> emptyList());
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				PlatformSensorTypeIdent platformSensorTypeIdent = (PlatformSensorTypeIdent) invocation.getArguments()[0];
				platformSensorTypeIdent.setId(Long.valueOf(platformSensorId));
				return null;
			}
		}).when(platformSensorTypeIdentDao).saveOrUpdate((PlatformSensorTypeIdent) anyObject());

		long registeredId = registrationService.registerPlatformSensorTypeIdent(platformId, fqcName);
		assertThat(registeredId, is(equalTo(platformSensorId)));

		ArgumentCaptor<PlatformSensorTypeIdent> platformSensorArgument = ArgumentCaptor.forClass(PlatformSensorTypeIdent.class);
		verify(platformSensorTypeIdentDao, times(1)).saveOrUpdate(platformSensorArgument.capture());
		assertThat(platformSensorArgument.getValue().getFullyQualifiedClassName(), is(equalTo(fqcName)));

		verify(platformIdentDao, times(1)).saveOrUpdate(platformIdent);
		assertThat(platformSensorArgument.getValue(), is(equalTo(platformIdent.getSensorTypeIdents().toArray()[0])));
	}

	/**
	 * Test the adding of the method sensor type to method ident.
	 * 
	 * @throws RemoteException
	 *             If {@link RemoteException} occurs.
	 */
	@Test
	public void registerSensorTypeWithMethod() throws RemoteException {
		long methodId = 20;
		long methodSensorId = 50;

		MethodIdent methodIdent = new MethodIdent();
		MethodSensorTypeIdent methodSensorTypeIdent = new MethodSensorTypeIdent();

		when(methodIdentDao.load(methodId)).thenReturn(methodIdent);
		when(methodSensorTypeIdentDao.load(methodSensorId)).thenReturn(methodSensorTypeIdent);

		registrationService.addSensorTypeToMethod(methodSensorId, methodId);

		verify(methodIdentDao, times(1)).saveOrUpdate(methodIdent);
		verify(methodSensorTypeIdentDao, times(1)).saveOrUpdate(methodSensorTypeIdent);
		assertThat(methodSensorTypeIdent, is(equalTo(methodIdent.getMethodSensorTypeIdents().toArray()[0])));
	}

}
