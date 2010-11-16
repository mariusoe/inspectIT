package info.novatec.inspectit.cmr.cache.indexing.restriction.test;

import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.cmr.cache.indexing.restriction.impl.IndexQueryRestrictionFactory;
import info.novatec.inspectit.cmr.test.AbstractLogSupport;
import info.novatec.inspectit.cmr.util.IndexQueryProvider;
import info.novatec.inspectit.communication.data.TimerData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests the indexing restirction used with index queries.
 * 
 * @author Ivan Senic
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-model.xml" })
public class IndexingRestricionTests extends AbstractLogSupport {

	/**
	 * Timer data.
	 */
	private TimerData timerData;

	/**
	 * Index query provider.
	 */
	@Autowired
	private IndexQueryProvider indexQueryProvider;

	/**
	 * Query.
	 */
	private IIndexQuery query;

	/**
	 * Initialize. Set up the timer data.
	 */
	@BeforeClass
	public void init() {
		timerData = new TimerData();
		timerData.setId(1L);
	}

	/**
	 * Test equal restriction.
	 */
	@Test
	public void testEqualsRestriction() {
		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("id", 1L));
		Assert.assertTrue(timerData.isQueryComplied(query));

		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("id", 0L));
		Assert.assertFalse(timerData.isQueryComplied(query));
	}

	/**
	 * Test not equal restriction.
	 */
	@Test
	public void testNotEqualsRestriction() {
		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.notEqual("id", 1L));
		Assert.assertFalse(timerData.isQueryComplied(query));

		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.notEqual("id", 0L));
		Assert.assertTrue(timerData.isQueryComplied(query));
	}

	/**
	 * Test is null restriction.
	 */
	@Test
	public void testIsNull() {
		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.isNull("id"));
		Assert.assertFalse(timerData.isQueryComplied(query));
	}

	/**
	 * Test is not null restriction.
	 */
	@Test
	public void testIsNotNull() {
		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.isNotNull("id"));
		Assert.assertTrue(timerData.isQueryComplied(query));
	}

	/**
	 * Test greater than restriction.
	 */
	@Test
	public void testGreaterThan() {
		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.greaterThan("id", 1L));
		Assert.assertFalse(timerData.isQueryComplied(query));

		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.greaterThan("id", 0L));
		Assert.assertTrue(timerData.isQueryComplied(query));

		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.greaterThan("id", 2L));
		Assert.assertFalse(timerData.isQueryComplied(query));
	}

	/**
	 * Test greater or equal restriction.
	 */
	@Test
	public void testGreaterEqual() {
		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.greaterEqual("id", 1L));
		Assert.assertTrue(timerData.isQueryComplied(query));

		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.greaterEqual("id", 0L));
		Assert.assertTrue(timerData.isQueryComplied(query));

		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.greaterEqual("id", 2L));
		Assert.assertFalse(timerData.isQueryComplied(query));
	}

	/**
	 * Test less than restriction.
	 */
	@Test
	public void testLessThan() {
		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.lessThan("id", 1L));
		Assert.assertFalse(timerData.isQueryComplied(query));

		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.lessThan("id", 0L));
		Assert.assertFalse(timerData.isQueryComplied(query));

		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.lessThan("id", 2L));
		Assert.assertTrue(timerData.isQueryComplied(query));
	}

	/**
	 * Test less or equal restriction.
	 */
	@Test
	public void testLessEqual() {
		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.lessEqual("id", 1L));
		Assert.assertTrue(timerData.isQueryComplied(query));

		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.lessEqual("id", 0L));
		Assert.assertFalse(timerData.isQueryComplied(query));

		query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.lessEqual("id", 2L));
		Assert.assertTrue(timerData.isQueryComplied(query));
	}

}
