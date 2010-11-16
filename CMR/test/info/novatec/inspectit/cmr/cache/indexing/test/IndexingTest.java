package info.novatec.inspectit.cmr.cache.indexing.test;

import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.cache.indexing.IndexQuery;
import info.novatec.inspectit.cmr.cache.indexing.impl.Branch;
import info.novatec.inspectit.cmr.cache.indexing.impl.IndexingException;
import info.novatec.inspectit.cmr.cache.indexing.impl.LeafingBranch;
import info.novatec.inspectit.cmr.test.AbstractLogSupport;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for testing functionality of indexing branches. The test case includes inserting
 * {@value #MAX_ELEMENTS} into the tree structure with 4 levels, where 2 levels have normal brunches
 * (see {@link Branch}) and 2 levels have leafing branches (see {@link LeafingBranch}.
 * 
 * @author Ivan Senic
 * 
 */

@ContextConfiguration(locations = { "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-model.xml" })
public class IndexingTest  extends AbstractLogSupport {

	/**
	 * Branch to test.
	 */
	@Autowired
	private ITreeComponent<MethodSensorData> rootBranch;

	/**
	 * All elements are also saved in this list separately.
	 */
	private List<MethodSensorData> elements;

	/**
	 * ID generator.
	 */
	private static long nextId = 1;

	/**
	 * Number of elements to be created at the begining of the test.
	 */
	private static final int MAX_ELEMENTS = 100000;
	
	/**
	 * Init. Put {@value #MAX_ELEMENTS} elements in tree.
	 */
	@Test
	public void initElements() {
		elements = new ArrayList<MethodSensorData>();
		for (int i = 0; i < MAX_ELEMENTS; i++) {
			MethodSensorData element = getRandomInstance();
			elements.add(element);
			try {
				rootBranch.put(element);
			} catch (IndexingException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Test tree with empty query. All elements should be returned.
	 */
	@Test(dependsOnMethods={"initElements"})
	public void emptyQueryTest() {
		List<MethodSensorData> results = rootBranch.query(new IndexQuery());
		Assert.assertEquals(results.size(), MAX_ELEMENTS);
	}

	/**
	 * Test retrieval of one element, with supplied template.
	 */
	@Test(dependsOnMethods={"initElements"})
	public void oneElementTest() {
		for (MethodSensorData element : elements) {
			Assert.assertEquals(rootBranch.get(element), element);
		}
	}

	/**
	 * Test tree with query that holds only platform ident.
	 */
	@Test(dependsOnMethods={"initElements"})
	public void platformIdentTest() {
		IndexQuery query = new IndexQuery();
		long platformIdent = getRandomLong(50);
		query.setPlatformIdent(platformIdent);

		List<MethodSensorData> results = rootBranch.query(query);
		for (DefaultData result : results) {
			Assert.assertEquals(result.getPlatformIdent(), platformIdent);
		}

		int i = 0;
		int j = 0;
		for (MethodSensorData element : elements) {
			if (element.getPlatformIdent() == platformIdent) {
				i++;
			}
			if (element.isQueryComplied(query)) {
				j++;
			}
		}
		Assert.assertEquals(results.size(), i);
		Assert.assertEquals(results.size(), j);
	}

	/**
	 * Test tree with query that holds only method ident.
	 */
	@Test(dependsOnMethods={"initElements"})
	public void methodIdentTest() {
		IndexQuery query = new IndexQuery();
		long methodIdent = getRandomLong(200);
		query.setMethodIdent(methodIdent);

		List<MethodSensorData> results = rootBranch.query(query);
		for (MethodSensorData result : results) {
			Assert.assertEquals(result.getMethodIdent(), methodIdent);
		}

		int i = 0;
		int j = 0;
		for (MethodSensorData element : elements) {
			if (element.getMethodIdent() == methodIdent) {
				i++;
			}
			if (element.isQueryComplied(query)) {
				j++;
			}
		}
		Assert.assertEquals(results.size(), i);
		Assert.assertEquals(results.size(), j);
	}

	/**
	 * Test tree with query that holds only object type.
	 */
	@Test(dependsOnMethods={"initElements"})
	public void objectTypeTest() {
		IndexQuery query = new IndexQuery();
		query.setObjectClass(InvocationSequenceData.class);

		List<MethodSensorData> results = rootBranch.query(query);
		for (DefaultData result : results) {
			Assert.assertEquals(result.getClass(), InvocationSequenceData.class);
		}
		int i = 0;
		int j = 0;
		for (MethodSensorData element : elements) {
			if (element.getClass().equals(InvocationSequenceData.class)) {
				i++;
			}
			if (element.isQueryComplied(query)) {
				j++;
			}
		}
		Assert.assertEquals(results.size(), i);
		Assert.assertEquals(results.size(), j);
	}

	/**
	 * Test tree with query that holds only time interval.
	 */
	@Test(dependsOnMethods={"initElements"})
	public void timestampTest() {
		Timestamp minusHour = new Timestamp(new Date().getTime() + 20 * 60 * 1000);
		Timestamp plusHour = new Timestamp(new Date().getTime() + 25 * 60 * 1000);
		IndexQuery query = new IndexQuery();
		query.setFromDate(minusHour);
		query.setToDate(plusHour);

		List<MethodSensorData> results = rootBranch.query(query);
		// Assert.assertEquals(results.size(), maxElements);
		for (DefaultData result : results) {
			Assert.assertEquals(minusHour.compareTo(result.getTimeStamp()) <= 0, true);
			Assert.assertEquals(plusHour.compareTo(result.getTimeStamp()) >= 0, true);
		}
	}

	/**
	 * Test tree with query that holds platform ident, sensor type ident, method ident, time
	 * interval and object type.
	 */
	@Test(dependsOnMethods={"initElements"})
	public void joinedQueryTest() {
		long platformIdent = getRandomLong(50);
		long sensorTypeIdent = getRandomLong(100);
		long methodIdent = getRandomLong(200);
		Timestamp minusHour = new Timestamp(new Date().getTime() + 25 * 60 * 1000);
		Timestamp plusHour = new Timestamp(new Date().getTime() + 30 * 60 * 1000);

		IndexQuery query = new IndexQuery();
		query.setFromDate(minusHour);
		query.setToDate(plusHour);
		query.setObjectClass(InvocationSequenceData.class);
		query.setMethodIdent(methodIdent);
		query.setSensorTypeIdent(sensorTypeIdent);
		query.setPlatformIdent(platformIdent);

		List<MethodSensorData> results = rootBranch.query(query);
		for (MethodSensorData result : results) {
			Assert.assertEquals(minusHour.before(result.getTimeStamp()), true);
			Assert.assertEquals(plusHour.after(result.getTimeStamp()), true);
			Assert.assertEquals(result.getClass(), InvocationSequenceData.class);
			Assert.assertEquals(result.getMethodIdent(), methodIdent);
			Assert.assertEquals(result.getPlatformIdent(), platformIdent);
			Assert.assertEquals(result.getSensorTypeIdent(), sensorTypeIdent);
		}

		int i = 0;
		for (MethodSensorData element : elements) {
			if (element.isQueryComplied(query)) {
				i++;
			}
		}
		Assert.assertEquals(results.size(), i);

	}
	/**
	 * Test tree with query that holds platform ident and method ident.
	 */
	@Test(dependsOnMethods={"initElements"})
	public void differentLevelsTest() {
		long platformIdent = getRandomLong(50);
		long methodIdent = getRandomLong(200);

		IndexQuery query = new IndexQuery();
		query.setMethodIdent(methodIdent);
		query.setPlatformIdent(platformIdent);

		List<MethodSensorData> results = rootBranch.query(query);
		for (MethodSensorData result : results) {
			Assert.assertEquals(result.getMethodIdent(), methodIdent);
			Assert.assertEquals(result.getPlatformIdent(), platformIdent);
		}

		int i = 0;
		for (MethodSensorData element : elements) {
			if (element.isQueryComplied(query)) {
				i++;
			}
		}
		Assert.assertEquals(results.size(), i);

	}

	/**
	 * Test tree with query that holds platform ident, sensor type ident, method ident, time
	 * interval and object type.
	 */
	@Test(dependsOnMethods={"initElements"})
	public void differentLevelsTest2() {
		Timestamp minusHour = new Timestamp(new Date().getTime() + 25 * 60 * 1000);
		Timestamp plusHour = new Timestamp(new Date().getTime() + 30 * 60 * 1000);

		IndexQuery query = new IndexQuery();
		query.setFromDate(minusHour);
		query.setToDate(plusHour);
		query.setObjectClass(InvocationSequenceData.class);

		List<MethodSensorData> results = rootBranch.query(query);
		for (DefaultData result : results) {
			Assert.assertEquals(minusHour.before(result.getTimeStamp()), true);
			Assert.assertEquals(plusHour.after(result.getTimeStamp()), true);
			Assert.assertEquals(result.getClass(), InvocationSequenceData.class);
		}

		int i = 0;
		for (MethodSensorData element : elements) {
			if (element.isQueryComplied(query)) {
				i++;
			}
		}
		Assert.assertEquals(results.size(), i);
	}

	/**
	 * Test a removal of one element from the indexing tree.
	 */
	@Test(dependsOnMethods={"initElements", "emptyQueryTest"})
	public void removeElementFromIndexing() {
		MethodSensorData newElement = getRandomInstance();
		try {
			rootBranch.put(newElement);
		} catch (IndexingException e) {

		}
		Assert.assertEquals(newElement, rootBranch.getAndRemove(newElement));
		Assert.assertNull(rootBranch.getAndRemove(newElement));
	}

	/**
	 * Clear all test.
	 */
	@Test(dependsOnMethods={"initElements", "emptyQueryTest"})
	public void clearBranchTest() {
		rootBranch.clearAll();
		Assert.assertEquals(0, rootBranch.getNumberOfElements());
		initElements();
	}

	/**
	 * Returns random instance of {@link MethodSensorData} with already set id, platform ident,
	 * method ident, sensor type ident and timestamp.
	 * 
	 * @return
	 */
	private static MethodSensorData getRandomInstance() {
		MethodSensorData instance = null;
		if (nextId % 3 == 0) {
			instance = new TimerData();
		} else if (nextId % 3 == 1) {
			instance = new InvocationSequenceData();
		} else {
			instance = new SqlStatementData();
		}
		instance.setId(nextId++);
		instance.setPlatformIdent(getRandomLong(50));
		instance.setSensorTypeIdent(getRandomLong(100));
		instance.setMethodIdent(getRandomLong(200));
		instance.setTimeStamp(getRandomTimestamp(60 * 60 * 1000));
		return instance;
	}

	/**
	 * Returns random long that is a dividend of 10. Long is greater that 0 and equal or less then
	 * passed maximum
	 * 
	 * @param max
	 * @return
	 */
	private static long getRandomLong(long max) {
		long value = (long) (Math.random() * max);
		return value - value % 10 + 10;
	}

	/**
	 * Returns a {@link Timestamp} instance. Date is between now() and now() + maxOffset value.
	 * 
	 * @param maxOffset
	 * @return
	 */
	private static Timestamp getRandomTimestamp(long maxOffset) {
		long value = (long) (Math.random() * maxOffset);
		value -= value % 10;
		return new Timestamp(new Date().getTime() + value);
	}
}
