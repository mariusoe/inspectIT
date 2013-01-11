package info.novatec.inspectit.cmr.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.cmr.util.AgentStatusDataProvider;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.TimerData;

import java.lang.ref.SoftReference;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.LogFactory;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the agent storage service.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class AgentStorageServiceTest extends AbstractTestNGLogSupport {

	/**
	 * Service to be tested.
	 */
	private AgentStorageService agentStorageService;

	/**
	 * {@link AgentStatusDataProvider}.
	 */
	@Mock
	private AgentStatusDataProvider agentStatusDataProvider;

	/**
	 * Initializes the mocks.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Proves that the data will be dropped after the timeout if there is no place in the queue and
	 * amount of dropped data be remembered.
	 * 
	 * @throws RemoteException
	 *             If {@link RemoteException} occurs.
	 */
	@Test
	public void dropDataAfterTimeout() throws RemoteException {
		agentStorageService = new AgentStorageService(new ArrayBlockingQueue<SoftReference<List<? extends DefaultData>>>(1));
		agentStorageService.platformIdentDateSaver = agentStatusDataProvider;
		agentStorageService.log = LogFactory.getLog(AgentStorageService.class);

		List<DefaultData> dataList = new ArrayList<DefaultData>();
		TimerData timerData = new TimerData();
		timerData.setPlatformIdent(1L);
		dataList.add(timerData);

		agentStorageService.addDataObjects(dataList);
		agentStorageService.addDataObjects(dataList);

		assertThat(agentStorageService.getDroppedDataCount(), is(equalTo(dataList.size())));
		Mockito.verify(agentStatusDataProvider, Mockito.times(2)).registerDataSent(1L);
	}

	/**
	 * Provides that data will be processed if there is place in the queue.
	 * 
	 * @throws RemoteException
	 *             If {@link RemoteException} occurs.
	 */
	@Test
	public void acceptData() throws RemoteException {
		agentStorageService = new AgentStorageService(new ArrayBlockingQueue<SoftReference<List<? extends DefaultData>>>(1));
		agentStorageService.platformIdentDateSaver = agentStatusDataProvider;
		agentStorageService.log = LogFactory.getLog(AgentStorageService.class);

		List<DefaultData> dataList = new ArrayList<DefaultData>();
		TimerData timerData = new TimerData();
		timerData.setPlatformIdent(1L);
		dataList.add(timerData);

		agentStorageService.addDataObjects(dataList);

		assertThat(agentStorageService.getDroppedDataCount(), is(equalTo(0)));
		Mockito.verify(agentStatusDataProvider, Mockito.times(1)).registerDataSent(1L);
	}
}
