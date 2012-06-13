package info.novatec.inspectit.cmr.service;

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
import org.testng.Assert;
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
	 */
	@Test(expectedExceptions = { LicenseException.class })
	@SuppressWarnings("unchecked")
	public void noRegistrationAllowedByLicenseUtil() throws LicenseContentException, RemoteException {
		doThrow(LicenseContentException.class).when(licenseUtil).validateLicense(anyList(), anyString());
		try {
			registrationService.registerPlatformIdent(new ArrayList<String>(), "agentName", "version");
		} catch (LicenseException e) {
			verifyZeroInteractions(platformIdentDao);
			throw e;
		}
	}

	/**
	 * Test that registration will be done properlly if the {@link LicenseUtil} validates license.
	 * 
	 * @throws LicenseContentException
	 *             If {@link LicenseContentException} occurs.
	 * @throws RemoteException
	 *             If remote exception occurs.
	 */
	@Test
	public void registerNewPlatformIdent() throws LicenseContentException, RemoteException {
		final long platformId = 10;
		final List<String> definedIps = new ArrayList<String>();
		definedIps.add("ip");
		final String agentName = "agentName";
		final String version = "version";

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
		Assert.assertEquals(registeredId, platformId);

		ArgumentCaptor<PlatformIdent> argument = ArgumentCaptor.forClass(PlatformIdent.class);
		verify(platformIdentDao, times(1)).saveOrUpdate(argument.capture());

		Assert.assertEquals(definedIps, argument.getValue().getDefinedIPs());
		Assert.assertEquals(agentName, argument.getValue().getAgentName());
		Assert.assertEquals(version, argument.getValue().getVersion());
		Assert.assertNotNull(argument.getValue().getTimeStamp());

		verify(agentStatusDataProvider, times(1)).registerConnected(platformId);
	}

	/**
	 * Tests that the version and timestamp will be updated if the agent is already registered.
	 * 
	 * @throws LicenseContentException
	 *             If {@link LicenseContentException} occurs.
	 * @throws RemoteException
	 *             If remote exception occurs.
	 */
	@Test
	public void registerExistingPlatformIdent() throws LicenseContentException, RemoteException {
		final long platformId = 10;
		final List<String> definedIps = new ArrayList<String>();
		definedIps.add("ip");
		final String agentName = "agentName";
		final String version = "version";
		final Timestamp timestamp = new Timestamp(1);

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
		Assert.assertEquals(registeredId, platformId);

		ArgumentCaptor<PlatformIdent> argument = ArgumentCaptor.forClass(PlatformIdent.class);
		verify(platformIdentDao, times(1)).saveOrUpdate(argument.capture());

		Assert.assertEquals(argument.getValue().getDefinedIPs(), definedIps);
		Assert.assertEquals(argument.getValue().getAgentName(), agentName);
		Assert.assertEquals(argument.getValue().getVersion(), version);
		Assert.assertNotNull(argument.getValue().getTimeStamp());
		Assert.assertNotSame(argument.getValue().getTimeStamp(), timestamp);

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
		Assert.assertEquals(registeredId, methodId);

		ArgumentCaptor<MethodIdent> argument = ArgumentCaptor.forClass(MethodIdent.class);
		verify(methodIdentDao, times(1)).saveOrUpdate(argument.capture());

		Assert.assertEquals(argument.getValue().getPlatformIdent(), platformIdent);
		Assert.assertEquals(argument.getValue().getPackageName(), packageName);
		Assert.assertEquals(argument.getValue().getClassName(), className);
		Assert.assertEquals(argument.getValue().getMethodName(), methodName);
		Assert.assertEquals(argument.getValue().getParameters(), parameterTypes);
		Assert.assertEquals(argument.getValue().getReturnType(), returnType);
		Assert.assertEquals(argument.getValue().getModifiers(), modifiers);
		Assert.assertNotNull(argument.getValue().getTimeStamp());
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
		final Timestamp timestamp = new Timestamp(1);

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
		Assert.assertEquals(registeredId, methodId);

		ArgumentCaptor<MethodIdent> argument = ArgumentCaptor.forClass(MethodIdent.class);
		verify(methodIdentDao, times(1)).saveOrUpdate(argument.capture());

		Assert.assertEquals(argument.getValue().getPlatformIdent(), platformIdent);
		Assert.assertEquals(argument.getValue().getPackageName(), packageName);
		Assert.assertEquals(argument.getValue().getClassName(), className);
		Assert.assertEquals(argument.getValue().getMethodName(), methodName);
		Assert.assertEquals(argument.getValue().getParameters(), parameterTypes);
		Assert.assertEquals(argument.getValue().getReturnType(), returnType);
		Assert.assertEquals(argument.getValue().getModifiers(), modifiers);
		Assert.assertNotNull(argument.getValue().getTimeStamp());
		Assert.assertNotSame(argument.getValue().getTimeStamp(), timestamp);
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
		Assert.assertEquals(registeredId, methodSensorId);

		ArgumentCaptor<MethodSensorTypeIdent> methodSensorArgument = ArgumentCaptor.forClass(MethodSensorTypeIdent.class);
		verify(methodSensorTypeIdentDao, times(1)).saveOrUpdate(methodSensorArgument.capture());
		Assert.assertEquals(methodSensorArgument.getValue().getFullyQualifiedClassName(), fqcName);

		verify(platformIdentDao, times(1)).saveOrUpdate(platformIdent);
		Assert.assertEquals(platformIdent.getSensorTypeIdents().toArray()[0], methodSensorArgument.getValue());
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
		Assert.assertEquals(registeredId, platformSensorId);

		ArgumentCaptor<PlatformSensorTypeIdent> platformSensorArgument = ArgumentCaptor.forClass(PlatformSensorTypeIdent.class);
		verify(platformSensorTypeIdentDao, times(1)).saveOrUpdate(platformSensorArgument.capture());
		Assert.assertEquals(platformSensorArgument.getValue().getFullyQualifiedClassName(), fqcName);

		verify(platformIdentDao, times(1)).saveOrUpdate(platformIdent);
		Assert.assertEquals(platformIdent.getSensorTypeIdents().toArray()[0], platformSensorArgument.getValue());
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
		Assert.assertEquals(methodIdent.getMethodSensorTypeIdents().toArray()[0], methodSensorTypeIdent);
	}

}
