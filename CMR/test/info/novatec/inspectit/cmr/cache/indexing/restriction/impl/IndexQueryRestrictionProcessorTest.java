package info.novatec.inspectit.cmr.cache.indexing.restriction.impl;

import info.novatec.inspectit.cmr.cache.indexing.restriction.IIndexQueryRestriction;
import info.novatec.inspectit.cmr.cache.indexing.restriction.IIndexQueryRestrictionProcessor;
import info.novatec.inspectit.cmr.cache.indexing.restriction.impl.CachingIndexQueryRestrictionProcessor;
import info.novatec.inspectit.cmr.cache.indexing.restriction.impl.IndexQueryRestrictionFactory;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the indexing restriction used with index queries.
 * 
 * @author Ivan Senic
 * 
 */
public class IndexQueryRestrictionProcessorTest extends AbstractTestNGLogSupport {

	/**
	 * The processor under test.
	 */
	private IIndexQueryRestrictionProcessor processor;

	/**
	 * Timer data.
	 */
	private TimerData timerData;

	/**
	 * Initialize. Set up the timer data.
	 */
	@BeforeClass
	public void init() {
		timerData = new TimerData();
		timerData.setId(1L);
	}

	/**
	 * Initialize processor before executing each test.
	 */
	@BeforeMethod
	public void initTestMethod() {
		processor = new CachingIndexQueryRestrictionProcessor();
	}

	/**
	 * Test equal restriction.
	 */
	@Test
	public void equalsTrueRestriction() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.equal("id", 1L));
		Assert.assertTrue(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test equal restriction.
	 */
	@Test
	public void equalsFalseRestriction() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.equal("id", 0L));
		Assert.assertFalse(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test not equal restriction.
	 */
	@Test
	public void notEqualsFalseRestriction() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.notEqual("id", 1L));
		Assert.assertFalse(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test not equal restriction.
	 */
	@Test
	public void notEqualsTrueRestriction() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.notEqual("id", 0L));
		Assert.assertTrue(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test is null restriction.
	 */
	@Test
	public void isNull() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.isNull("id"));
		Assert.assertFalse(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test is not null restriction.
	 */
	@Test
	public void isNotNull() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.isNotNull("id"));
		Assert.assertTrue(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test greater than restriction.
	 */
	@Test
	public void greaterThanOne() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.greaterThan("id", 1L));
		Assert.assertFalse(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test greater than restriction.
	 */
	@Test
	public void greaterThanZero() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.greaterThan("id", 0L));
		Assert.assertTrue(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test greater than restriction.
	 */
	@Test
	public void greaterThanTwo() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.greaterThan("id", 2L));
		Assert.assertFalse(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test greater or equal restriction.
	 */
	@Test
	public void greaterEqualOne() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.greaterEqual("id", 1L));
		Assert.assertTrue(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test greater or equal restriction.
	 */
	@Test
	public void greaterEqualZero() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.greaterEqual("id", 0L));
		Assert.assertTrue(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test greater or equal restriction.
	 */
	@Test
	public void greaterEqualTwo() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.greaterEqual("id", 2L));
		Assert.assertFalse(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test less than restriction.
	 */
	@Test
	public void lessThanOne() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.lessThan("id", 1L));
		Assert.assertFalse(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test less than restriction.
	 */
	@Test
	public void lessThanZero() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.lessThan("id", 0L));
		Assert.assertFalse(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test less than restriction.
	 */
	@Test
	public void lessThanTwo() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.lessThan("id", 2L));
		Assert.assertTrue(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test less or equal restriction.
	 */
	@Test
	public void lessEqualOne() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.lessEqual("id", 1L));
		Assert.assertTrue(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test less or equal restriction.
	 */
	@Test
	public void lessEqualZero() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.lessEqual("id", 0L));
		Assert.assertFalse(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

	/**
	 * Test less or equal restriction.
	 */
	@Test
	public void lessEqualTwo() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.lessEqual("id", 2L));
		Assert.assertTrue(processor.areAllRestrictionsFulfilled(timerData, restrictions));
	}

}
