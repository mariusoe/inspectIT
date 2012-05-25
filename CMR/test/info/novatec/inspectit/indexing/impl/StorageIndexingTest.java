package info.novatec.inspectit.indexing.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.indexer.impl.MethodIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.ObjectTypeIndexer;
import info.novatec.inspectit.indexing.indexer.impl.PlatformIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.SensorTypeIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.TimestampIndexer;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.ArrayBasedStorageLeaf;
import info.novatec.inspectit.indexing.storage.impl.LeafWithNoDescriptors;
import info.novatec.inspectit.indexing.storage.impl.StorageBranch;
import info.novatec.inspectit.indexing.storage.impl.StorageBranchIndexer;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for checking the {@link IStorageTreeComponent}s.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageIndexingTest {

	/**
	 * {@link StorageIndexQuery}.
	 */
	private StorageIndexQuery storageIndexQuery;

	/**
	 * Initializes the index query.
	 */
	@BeforeMethod
	public void initMethod() {
		storageIndexQuery = new StorageIndexQuery();
	}

	/**
	 * Test tree with empty query. All elements should be returned.
	 * 
	 * @throws IndexingException
	 *             If {@link IndexingException} occurs.
	 */
	@Test
	public void emptyQueryTest() throws IndexingException {
		IStorageTreeComponent<DefaultData> rootBranch = new StorageBranch<DefaultData>(new StorageBranchIndexer<DefaultData>(new ObjectTypeIndexer<DefaultData>()));

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getId()).thenReturn(1L);
		rootBranch.put(defaultData);

		SqlStatementData defaultData2 = mock(SqlStatementData.class);
		when(defaultData2.getId()).thenReturn(2L);
		rootBranch.put(defaultData2);

		List<IStorageDescriptor> results = rootBranch.query(storageIndexQuery);
		Assert.assertEquals(2, results.size());
	}

	/**
	 * Test that putting one element will return the same {@link IStorageDescriptor} as when get is
	 * executed..
	 * 
	 * @throws IndexingException
	 *             If {@link IndexingException} occurs.
	 */
	@Test
	public void putAndGetElement() throws IndexingException {
		IStorageTreeComponent<DefaultData> rootBranch = new StorageBranch<DefaultData>(new StorageBranchIndexer<DefaultData>(new ObjectTypeIndexer<DefaultData>()));

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getId()).thenReturn(1L);
		IStorageDescriptor storageDescriptor = rootBranch.put(defaultData);

		Assert.assertEquals(0, storageDescriptor.compareTo(rootBranch.get(defaultData)));
	}

	/**
	 * Test tree with query that holds only platform ident.
	 * 
	 * @throws IndexingException
	 *             If {@link IndexingException} occurs.
	 */
	@Test
	public void queryBranchWithPlatformIdent() throws IndexingException {
		IStorageTreeComponent<DefaultData> rootBranch = new StorageBranch<DefaultData>(new StorageBranchIndexer<DefaultData>(new PlatformIdentIndexer<DefaultData>()));

		DefaultData defaultData1 = mock(DefaultData.class);
		when(defaultData1.getId()).thenReturn(1L);
		when(defaultData1.getPlatformIdent()).thenReturn(10L);
		IStorageDescriptor storageDescriptor1 = rootBranch.put(defaultData1);
		storageDescriptor1.setSize(100L);

		DefaultData defaultData2 = mock(DefaultData.class);
		when(defaultData2.getId()).thenReturn(2L);
		when(defaultData2.getPlatformIdent()).thenReturn(20L);
		IStorageDescriptor storageDescriptor2 = rootBranch.put(defaultData2);
		storageDescriptor2.setSize(200L);

		storageIndexQuery.setPlatformIdent(10L);

		List<IStorageDescriptor> results = rootBranch.query(storageIndexQuery);
		Assert.assertEquals(1, results.size());
		for (IStorageDescriptor result : results) {
			Assert.assertEquals(result.getSize(), 100L);
		}
	}

	/**
	 * Test tree with query that holds only method ident.
	 * 
	 * @throws IndexingException
	 *             If {@link IndexingException} occurs.
	 */
	@Test
	public void queryBranchWithMethodIdent() throws IndexingException {
		IStorageTreeComponent<MethodSensorData> rootBranch = new StorageBranch<MethodSensorData>(new StorageBranchIndexer<MethodSensorData>(new MethodIdentIndexer<MethodSensorData>()));

		MethodSensorData defaultData1 = mock(MethodSensorData.class);
		when(defaultData1.getId()).thenReturn(1L);
		when(defaultData1.getMethodIdent()).thenReturn(10L);
		IStorageDescriptor storageDescriptor1 = rootBranch.put(defaultData1);
		storageDescriptor1.setSize(100L);

		MethodSensorData defaultData2 = mock(MethodSensorData.class);
		when(defaultData2.getId()).thenReturn(2L);
		when(defaultData2.getMethodIdent()).thenReturn(20L);
		IStorageDescriptor storageDescriptor2 = rootBranch.put(defaultData2);
		storageDescriptor2.setSize(200L);

		storageIndexQuery.setMethodIdent(10L);

		List<IStorageDescriptor> results = rootBranch.query(storageIndexQuery);
		Assert.assertEquals(1, results.size());
		for (IStorageDescriptor result : results) {
			Assert.assertEquals(result.getSize(), 100L);
		}
	}

	/**
	 * Test tree with query that holds only object type.
	 * 
	 * @throws IndexingException
	 *             If {@link IndexingException} occurs.
	 */
	@Test
	public void queryBranchWithObjectType() throws IndexingException {
		IStorageTreeComponent<DefaultData> rootBranch = new StorageBranch<DefaultData>(new StorageBranchIndexer<DefaultData>(new ObjectTypeIndexer<DefaultData>()));

		TimerData defaultData1 = mock(TimerData.class);
		when(defaultData1.getId()).thenReturn(1L);
		IStorageDescriptor storageDescriptor1 = rootBranch.put(defaultData1);
		storageDescriptor1.setSize(100L);

		SqlStatementData defaultData2 = mock(SqlStatementData.class);
		when(defaultData2.getId()).thenReturn(2L);
		IStorageDescriptor storageDescriptor2 = rootBranch.put(defaultData2);
		storageDescriptor2.setSize(200L);

		List<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(defaultData1.getClass());
		storageIndexQuery.setObjectClasses(searchedClasses);

		List<IStorageDescriptor> results = rootBranch.query(storageIndexQuery);
		Assert.assertEquals(1, results.size());
		for (IStorageDescriptor result : results) {
			Assert.assertEquals(result.getSize(), 100L);
		}
	}

	/**
	 * Test tree with query that holds only time interval.
	 * 
	 * @throws IndexingException
	 *             If {@link IndexingException} occurs.
	 */
	@Test
	public void queryBranchWithTimestampInterval() throws IndexingException {
		Timestamp minusHour = new Timestamp(new Date().getTime() + 20 * 60 * 1000);
		Timestamp plusHour = new Timestamp(new Date().getTime() + 25 * 60 * 1000);

		IStorageTreeComponent<DefaultData> rootBranch = new StorageBranch<DefaultData>(new StorageBranchIndexer<DefaultData>(new TimestampIndexer<DefaultData>()));

		DefaultData defaultData1 = mock(DefaultData.class);
		when(defaultData1.getId()).thenReturn(1L);
		when(defaultData1.getTimeStamp()).thenReturn(new Timestamp(new Date().getTime()));
		IStorageDescriptor storageDescriptor1 = rootBranch.put(defaultData1);
		storageDescriptor1.setSize(100L);

		DefaultData defaultData2 = mock(DefaultData.class);
		when(defaultData2.getId()).thenReturn(2L);
		when(defaultData2.getTimeStamp()).thenReturn(plusHour);
		IStorageDescriptor storageDescriptor2 = rootBranch.put(defaultData2);
		storageDescriptor2.setSize(200L);

		storageIndexQuery.setFromDate(minusHour);
		storageIndexQuery.setToDate(plusHour);

		List<IStorageDescriptor> results = rootBranch.query(storageIndexQuery);
		Assert.assertEquals(1, results.size());
		for (IStorageDescriptor result : results) {
			Assert.assertEquals(result.getSize(), 200L);
		}
	}

	/**
	 * Test tree with query that holds platform ident and sensor ident in different levels.
	 * 
	 * @throws IndexingException
	 *             If {@link IndexingException} occurs.
	 */
	@Test
	public void queryDifferentLevels() throws IndexingException {
		StorageBranchIndexer<DefaultData> sensorTypeIndexer = new StorageBranchIndexer<DefaultData>(new SensorTypeIdentIndexer<DefaultData>());
		StorageBranchIndexer<DefaultData> objectTypeIndexer = new StorageBranchIndexer<DefaultData>(new ObjectTypeIndexer<DefaultData>(), sensorTypeIndexer);
		StorageBranchIndexer<DefaultData> platformTypeIndexer = new StorageBranchIndexer<DefaultData>(new PlatformIdentIndexer<DefaultData>(), objectTypeIndexer);
		IStorageTreeComponent<DefaultData> rootBranch = new StorageBranch<DefaultData>(platformTypeIndexer);

		TimerData defaultData1 = mock(TimerData.class);
		when(defaultData1.getId()).thenReturn(1L);
		when(defaultData1.getPlatformIdent()).thenReturn(10L);
		when(defaultData1.getSensorTypeIdent()).thenReturn(10L);
		IStorageDescriptor storageDescriptor1 = rootBranch.put(defaultData1);
		storageDescriptor1.setSize(100L);

		SqlStatementData defaultData2 = mock(SqlStatementData.class);
		when(defaultData2.getId()).thenReturn(2L);
		when(defaultData2.getPlatformIdent()).thenReturn(10L);
		when(defaultData2.getSensorTypeIdent()).thenReturn(20L);
		IStorageDescriptor storageDescriptor2 = rootBranch.put(defaultData2);
		storageDescriptor2.setSize(200L);

		storageIndexQuery.setPlatformIdent(10L);

		List<IStorageDescriptor> results = rootBranch.query(storageIndexQuery);
		Assert.assertEquals(2, results.size());
		long totalSize = 0;
		for (IStorageDescriptor result : results) {
			totalSize += result.getSize();
		}
		Assert.assertEquals(totalSize, 300L);

		storageIndexQuery.setPlatformIdent(10L);
		storageIndexQuery.setSensorTypeIdent(10L);

		results = rootBranch.query(storageIndexQuery);
		Assert.assertEquals(1, results.size());
		for (IStorageDescriptor result : results) {
			Assert.assertEquals(result.getSize(), 100L);
		}

	}

	/**
	 * Tests that the indexing the same element in the {@link ArrayBasedStorageLeaf} will thrown a
	 * exception.
	 * 
	 * @throws IndexingException
	 *             {@link IndexingException}
	 */
	@Test(expectedExceptions = { IndexingException.class })
	public void indexSameElement() throws IndexingException {
		ArrayBasedStorageLeaf<DefaultData> arrayBasedStorageLeaf = new ArrayBasedStorageLeaf<DefaultData>();

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getId()).thenReturn(1L);
		arrayBasedStorageLeaf.put(defaultData);
		arrayBasedStorageLeaf.put(defaultData);
	}

	/**
	 * Test the removing of the element in the {@link ArrayBasedStorageLeaf}.
	 * 
	 * @throws IndexingException
	 *             {@link IndexingException}
	 */
	@Test
	public void removeElement() throws IndexingException {
		ArrayBasedStorageLeaf<DefaultData> arrayBasedStorageLeaf = new ArrayBasedStorageLeaf<DefaultData>();

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getId()).thenReturn(1L);
		arrayBasedStorageLeaf.put(defaultData);

		Assert.assertNotNull(arrayBasedStorageLeaf.getAndRemove(defaultData));
		Assert.assertNull(arrayBasedStorageLeaf.get(defaultData));
		Assert.assertTrue(arrayBasedStorageLeaf.query(storageIndexQuery).isEmpty());
	}

	/**
	 * Test the removing of the element in the {@link ArrayBasedStorageLeaf} when the leaf is full
	 * of elements.
	 * 
	 * @throws IndexingException
	 *             {@link IndexingException}
	 */
	@Test
	public void removeElementFromFullLeaf() throws IndexingException {
		ArrayBasedStorageLeaf<DefaultData> arrayBasedStorageLeaf = new ArrayBasedStorageLeaf<DefaultData>();

		DefaultData defaultData = mock(DefaultData.class);
		long i = 1L;
		int entries = 100;
		while (i <= entries) {
			when(defaultData.getId()).thenReturn(i);
			IStorageDescriptor storageDescriptor = arrayBasedStorageLeaf.put(defaultData);
			storageDescriptor.setSize(i);
			i++;
		}
		long removeId = 50L;
		when(defaultData.getId()).thenReturn(removeId);

		Assert.assertNotNull(arrayBasedStorageLeaf.getAndRemove(defaultData));
		Assert.assertNull(arrayBasedStorageLeaf.get(defaultData));
		List<IStorageDescriptor> results = arrayBasedStorageLeaf.query(storageIndexQuery);
		Assert.assertEquals(results.size(), entries - 1);
		for (IStorageDescriptor storageDescriptor : results) {
			Assert.assertTrue(storageDescriptor.getSize() != removeId);
		}
	}

	/**
	 * Tests that the total returned size of the leaf with no descriptors will be the same as the
	 * amount given.
	 * 
	 * @throws IndexingException
	 *             {@link IndexingException}
	 */
	@Test
	public void totalSizeOfBoundedDescriptor() throws IndexingException {
		LeafWithNoDescriptors<DefaultData> leafWithNoDescriptors = new LeafWithNoDescriptors<DefaultData>();

		DefaultData defaultData = mock(DefaultData.class);
		long i = 1L;
		int entries = 100;
		long totalSize = 0L;
		while (i <= entries) {
			when(defaultData.getId()).thenReturn(i);
			IStorageDescriptor storageDescriptor = leafWithNoDescriptors.put(defaultData);
			storageDescriptor.setSize(i);
			totalSize += i;
			i++;
		}

		List<IStorageDescriptor> results = leafWithNoDescriptors.query(storageIndexQuery);
		long totalReturnedSize = 0;
		for (IStorageDescriptor storageDescriptor : results) {
			totalReturnedSize += storageDescriptor.getSize();
		}
		Assert.assertEquals(totalReturnedSize, totalSize);
	}

}
