package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.TimerData;

import java.lang.ref.SoftReference;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the agent storage service.
 * 
 * @author Ivan Senic
 * 
 */
public class AgentStorageServiceTest extends AbstractTestNGLogSupport {

	/**
	 * Service to be tested.
	 */
	private AgentStorageService agentStorageService;

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
		agentStorageService.log = LogFactory.getLog(AgentStorageService.class);

		List<DefaultData> dataList = new ArrayList<DefaultData>();
		dataList.add(new TimerData());

		agentStorageService.addDataObjects(dataList);
		agentStorageService.addDataObjects(dataList);

		Assert.assertEquals(dataList.size(), agentStorageService.getDroppedDataCount());
	}

	/**
	 * provies that data will be processed if there is place in the queue.
	 * 
	 * @throws RemoteException
	 *             If {@link RemoteException} occurs.
	 */
	@Test
	public void acceptData() throws RemoteException {
		agentStorageService = new AgentStorageService(new ArrayBlockingQueue<SoftReference<List<? extends DefaultData>>>(1));
		agentStorageService.log = LogFactory.getLog(AgentStorageService.class);

		List<DefaultData> dataList = new ArrayList<DefaultData>();
		dataList.add(new TimerData());

		agentStorageService.addDataObjects(dataList);

		Assert.assertEquals(0, agentStorageService.getDroppedDataCount());
	}
}
