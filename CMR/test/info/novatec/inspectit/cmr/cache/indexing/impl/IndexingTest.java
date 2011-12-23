package info.novatec.inspectit.cmr.cache.indexing.impl;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.cache.indexing.AbstractIndexer.ChildBranchType;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.cache.indexing.restriction.IIndexQueryRestrictionProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for testing functionality of {@link ITreeComponent}.
 * 
 * @author Ivan Senic
 * 
 */
public class IndexingTest {

	/**
	 * Index query to use.
	 */
	private IndexQuery indexQuery;

	/**
	 * The mocked processor.
	 */
	private IIndexQueryRestrictionProcessor processor;

	/**
	 * Initializes the mocks
	 */
	@SuppressWarnings("unchecked")
	@BeforeClass
	public void init() {
		processor = mock(IIndexQueryRestrictionProcessor.class);
		when(processor.areAllRestrictionsFulfilled(anyObject(), anyList())).thenReturn(true);
	}

	/**
	 * Initializes the index query.
	 */
	@BeforeMethod
	public void initMethod() {
		indexQuery = new IndexQuery();
		indexQuery.restrictionProcessor = processor;
	}

	/**
	 * Test tree with empty query. All elements should be returned.
	 * 
	 * @throws IndexingException
	 *             If {@link IndexingException} occurs.
	 */
	@Test
	public void emptyQueryTest() throws IndexingException {
		ITreeComponent<DefaultData> rootBranch = new Branch<DefaultData>(new ObjectTypeIndexer<DefaultData>());

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getId()).thenReturn(1L);
		when(defaultData.isQueryComplied(indexQuery)).thenReturn(true);
		rootBranch.put(defaultData);

		SqlStatementData defaultData2 = mock(SqlStatementData.class);
		when(defaultData2.getId()).thenReturn(2L);
		when(defaultData2.isQueryComplied(indexQuery)).thenReturn(true);
		rootBranch.put(defaultData2);

		List<DefaultData> results = rootBranch.query(indexQuery);
		Assert.assertEquals(2, results.size());
	}

	/**
	 * Test put and retrieval of one element.
	 * 
	 * @throws IndexingException
	 *             If {@link IndexingException} occurs.
	 */
	@Test
	public void putAndGetElement() throws IndexingException {
		ITreeComponent<DefaultData> rootBranch = new Branch<DefaultData>(new ObjectTypeIndexer<DefaultData>());

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getId()).thenReturn(1L);
		rootBranch.put(defaultData);

		Assert.assertEquals(defaultData, rootBranch.get(defaultData));
	}

	/**
	 * Test tree with query that holds only platform ident.
	 * 
	 * @throws IndexingException
	 *             If {@link IndexingException} occurs.
	 */
	@Test
	public void queryBranchWithPlatformIdent() throws IndexingException {
		ITreeComponent<DefaultData> rootBranch = new Branch<DefaultData>(new PlatformIdentIndexer<DefaultData>());

		DefaultData defaultData1 = mock(DefaultData.class);
		when(defaultData1.getId()).thenReturn(1L);
		when(defaultData1.getPlatformIdent()).thenReturn(10L);
		when(defaultData1.isQueryComplied(indexQuery)).thenReturn(true);
		rootBranch.put(defaultData1);

		DefaultData defaultData2 = mock(DefaultData.class);
		when(defaultData2.getId()).thenReturn(2L);
		when(defaultData2.getPlatformIdent()).thenReturn(20L);
		when(defaultData2.isQueryComplied(indexQuery)).thenReturn(true);
		rootBranch.put(defaultData2);

		indexQuery.setPlatformIdent(10L);

		List<DefaultData> results = rootBranch.query(indexQuery);
		Assert.assertEquals(1, results.size());
		for (DefaultData result : results) {
			Assert.assertEquals(result.getPlatformIdent(), 10L);
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
		ITreeComponent<MethodSensorData> rootBranch = new Branch<MethodSensorData>(new MethodIdentIndexer<MethodSensorData>());

		MethodSensorData defaultData1 = mock(MethodSensorData.class);
		when(defaultData1.getId()).thenReturn(1L);
		when(defaultData1.getMethodIdent()).thenReturn(10L);
		when(defaultData1.isQueryComplied(indexQuery)).thenReturn(true);
		rootBranch.put(defaultData1);

		MethodSensorData defaultData2 = mock(MethodSensorData.class);
		when(defaultData2.getId()).thenReturn(2L);
		when(defaultData2.getMethodIdent()).thenReturn(20L);
		when(defaultData2.isQueryComplied(indexQuery)).thenReturn(true);
		rootBranch.put(defaultData2);

		indexQuery.setMethodIdent(10L);

		List<MethodSensorData> results = rootBranch.query(indexQuery);
		Assert.assertEquals(1, results.size());
		for (MethodSensorData result : results) {
			Assert.assertEquals(result.getMethodIdent(), 10L);
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
		ITreeComponent<DefaultData> rootBranch = new Branch<DefaultData>(new ObjectTypeIndexer<DefaultData>());

		TimerData defaultData1 = mock(TimerData.class);
		when(defaultData1.getId()).thenReturn(1L);
		when(defaultData1.isQueryComplied(indexQuery)).thenReturn(true);
		rootBranch.put(defaultData1);

		SqlStatementData defaultData2 = mock(SqlStatementData.class);
		when(defaultData2.getId()).thenReturn(2L);
		when(defaultData2.isQueryComplied(indexQuery)).thenReturn(true);
		rootBranch.put(defaultData2);

		List<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(defaultData1.getClass());
		indexQuery.setObjectClasses(searchedClasses);

		List<DefaultData> results = rootBranch.query(indexQuery);
		Assert.assertEquals(1, results.size());
		for (DefaultData result : results) {
			Assert.assertEquals(result.getClass(), defaultData1.getClass());
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

		ITreeComponent<DefaultData> rootBranch = new Branch<DefaultData>(new TimestampIndexer<DefaultData>());

		DefaultData defaultData1 = mock(DefaultData.class);
		when(defaultData1.getId()).thenReturn(1L);
		when(defaultData1.getTimeStamp()).thenReturn(new Timestamp(new Date().getTime()));
		when(defaultData1.isQueryComplied(indexQuery)).thenReturn(true);
		rootBranch.put(defaultData1);

		DefaultData defaultData2 = mock(DefaultData.class);
		when(defaultData2.getId()).thenReturn(2L);
		when(defaultData2.getTimeStamp()).thenReturn(plusHour);
		when(defaultData2.isQueryComplied(indexQuery)).thenReturn(true);
		rootBranch.put(defaultData2);

		indexQuery.setFromDate(minusHour);
		indexQuery.setToDate(plusHour);

		List<DefaultData> results = rootBranch.query(indexQuery);
		Assert.assertEquals(1, results.size());
		for (DefaultData result : results) {
			Assert.assertEquals(minusHour.compareTo(result.getTimeStamp()) <= 0, true);
			Assert.assertEquals(plusHour.compareTo(result.getTimeStamp()) >= 0, true);
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
		ITreeComponent<DefaultData> rootBranch = new Branch<DefaultData>(new PlatformIdentIndexer<DefaultData>(ChildBranchType.NORMAL_BRANCH, new ObjectTypeIndexer<DefaultData>(
				ChildBranchType.NORMAL_BRANCH, new SensorTypeIdentIndexer<DefaultData>())));

		TimerData defaultData1 = mock(TimerData.class);
		when(defaultData1.getId()).thenReturn(1L);
		when(defaultData1.getPlatformIdent()).thenReturn(10L);
		when(defaultData1.getSensorTypeIdent()).thenReturn(10L);
		when(defaultData1.isQueryComplied(indexQuery)).thenReturn(true);
		rootBranch.put(defaultData1);

		SqlStatementData defaultData2 = mock(SqlStatementData.class);
		when(defaultData2.getId()).thenReturn(2L);
		when(defaultData2.getPlatformIdent()).thenReturn(10L);
		when(defaultData2.getSensorTypeIdent()).thenReturn(20L);
		when(defaultData2.isQueryComplied(indexQuery)).thenReturn(true);
		rootBranch.put(defaultData2);

		indexQuery.setPlatformIdent(10L);

		List<DefaultData> results = rootBranch.query(indexQuery);
		Assert.assertEquals(2, results.size());
		for (DefaultData result : results) {
			Assert.assertEquals(result.getPlatformIdent(), 10L);
		}

		indexQuery.setPlatformIdent(10L);
		indexQuery.setSensorTypeIdent(10L);

		results = rootBranch.query(indexQuery);
		Assert.assertEquals(1, results.size());
		for (DefaultData result : results) {
			Assert.assertEquals(result.getPlatformIdent(), 10L);
			Assert.assertEquals(result.getSensorTypeIdent(), 10L);
		}

	}

	/**
	 * Test a removal of one element from the indexing tree.
	 * 
	 * @throws IndexingException
	 *             If {@link IndexingException} occurs.
	 */
	@Test
	public void removeElement() throws IndexingException {
		ITreeComponent<DefaultData> rootBranch = new Branch<DefaultData>(new ObjectTypeIndexer<DefaultData>());

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getId()).thenReturn(1L);
		rootBranch.put(defaultData);
		rootBranch.getAndRemove(defaultData);

		Assert.assertEquals(null, rootBranch.get(defaultData));
	}

	/**
	 * Clear all test.
	 * 
	 * @throws IndexingException
	 *             If {@link IndexingException} occurs.
	 */
	@Test
	public void clearBranch() throws IndexingException {
		ITreeComponent<DefaultData> rootBranch = new Branch<DefaultData>(new ObjectTypeIndexer<DefaultData>());

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getId()).thenReturn(1L);
		rootBranch.put(defaultData);

		rootBranch.clearAll();
		Assert.assertEquals(0, rootBranch.getNumberOfElements());
	}

}