package info.novatec.inspectit.communication.data.test;

import info.novatec.inspectit.communication.data.InvocationAwareData;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests that functionality in the {@link InvocationAwareData} is correct.
 * 
 * @author Ivan Senic
 * 
 */
public class InvocationAwareDataTest {

	/**
	 * Tests the simple aggregation when objects are in different invocations.
	 */
	@Test
	public void testSimpleDifferentParentsAggregation() {
		InvocationAwareData i1 = getInvocationAwareData();
		i1.addInvocationParentId(Long.valueOf(1));

		InvocationAwareData i2 = getInvocationAwareData();
		i2.addInvocationParentId(Long.valueOf(2));

		i1.aggregateInvocationAwareData(i2);

		Assert.assertEquals(i1.getObjectsInInvocationsCount(), 2);
		Assert.assertEquals(i1.getInvocationParentsIdSet().size(), 2);
		Assert.assertTrue(i1.getInvocationParentsIdSet().contains(1L));
		Assert.assertTrue(i1.getInvocationParentsIdSet().contains(2L));
	}

	/**
	 * Tests the simple aggregation when objects are in same invocations.
	 */
	@Test
	public void testSimpleSameParentsAggregation() {
		InvocationAwareData i1 = getInvocationAwareData();
		i1.addInvocationParentId(Long.valueOf(1));

		InvocationAwareData i2 = getInvocationAwareData();
		i2.addInvocationParentId(Long.valueOf(1));

		i1.aggregateInvocationAwareData(i2);

		Assert.assertEquals(i1.getObjectsInInvocationsCount(), 2);
		Assert.assertEquals(i1.getInvocationParentsIdSet().size(), 1);
		Assert.assertTrue(i1.getInvocationParentsIdSet().contains(1L));
	}

	/**
	 * Tests the complicated aggregation when objects are in different invocations.
	 */
	@Test
	public void testComplicatedDifferentParentsAggregation() {
		InvocationAwareData i1 = getInvocationAwareData();
		i1.addInvocationParentId(Long.valueOf(1));

		InvocationAwareData i2 = getInvocationAwareData();
		i2.addInvocationParentId(Long.valueOf(2));

		i1.aggregateInvocationAwareData(i2);

		InvocationAwareData i3 = getInvocationAwareData();
		i3.addInvocationParentId(Long.valueOf(3));

		InvocationAwareData i4 = getInvocationAwareData();
		i4.addInvocationParentId(Long.valueOf(4));

		i3.aggregateInvocationAwareData(i4);

		i1.aggregateInvocationAwareData(i3);

		Assert.assertEquals(i1.getObjectsInInvocationsCount(), 4);
		Assert.assertEquals(i1.getInvocationParentsIdSet().size(), 4);
		Assert.assertTrue(i1.getInvocationParentsIdSet().contains(1L));
		Assert.assertTrue(i1.getInvocationParentsIdSet().contains(2L));
		Assert.assertTrue(i1.getInvocationParentsIdSet().contains(3L));
		Assert.assertTrue(i1.getInvocationParentsIdSet().contains(4L));
	}
	
	/**
	 * Tests the complicated aggregation when objects are in same invocations.
	 */
	@Test
	public void testComplicatedSameParentsAggregation() {
		InvocationAwareData i1 = getInvocationAwareData();
		i1.addInvocationParentId(Long.valueOf(1));

		InvocationAwareData i2 = getInvocationAwareData();
		i2.addInvocationParentId(Long.valueOf(2));

		i1.aggregateInvocationAwareData(i2);

		InvocationAwareData i3 = getInvocationAwareData();
		i3.addInvocationParentId(Long.valueOf(1));

		InvocationAwareData i4 = getInvocationAwareData();
		i4.addInvocationParentId(Long.valueOf(2));

		i3.aggregateInvocationAwareData(i4);

		i1.aggregateInvocationAwareData(i3);

		Assert.assertEquals(i1.getObjectsInInvocationsCount(), 4);
		Assert.assertEquals(i1.getInvocationParentsIdSet().size(), 2);
		Assert.assertTrue(i1.getInvocationParentsIdSet().contains(1L));
		Assert.assertTrue(i1.getInvocationParentsIdSet().contains(2L));
	}

	/**
	 * Gets the instance of the abstract class {@link InvocationAwareData}.
	 * 
	 * @return Gets the instance of the abstract class {@link InvocationAwareData}.
	 */
	private InvocationAwareData getInvocationAwareData() {
		return new InvocationAwareData() {

			/**
			 * Generated UID.
			 */
			private static final long serialVersionUID = 8228055838391889943L;

			@Override
			public double getInvocationAffiliationPercentage() {
				return 0;
			}
		};
	}

}
