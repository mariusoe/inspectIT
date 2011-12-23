package info.novatec.inspectit.cmr.cache.impl;

import static org.mockito.Mockito.mock;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;

import org.apache.commons.logging.Log;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Testing the calculations inside of {@link BufferProperties} class.
 * 
 * @author Ivan Senic
 * 
 */
public class BufferPropertiesTest extends AbstractTestNGLogSupport {

	/**
	 * Buffer properties to test.
	 */
	private BufferProperties bufferProperties;

	/**
	 * Initialization of the buffer properties.
	 * 
	 * @throws Exception
	 *             if an exception is thrown while executing the post construct method in the buffer
	 *             properties.
	 */
	@BeforeClass
	public void init() throws Exception {
		bufferProperties = new BufferProperties();
		bufferProperties.log = mock(Log.class);
		bufferProperties.bytesMaintenancePercentage = 0.2f;
		bufferProperties.evictionFragmentSizePercentage = 0.1f;
		bufferProperties.evictionOccupancyPercentage = 0.8f;
		bufferProperties.indexingTreeCleaningThreads = 1;
		bufferProperties.indexingWaitTime = 500l;
		bufferProperties.maxObjectExpansionRate = 0.05f;
		bufferProperties.maxObjectExpansionRateActiveTillBufferSize = 10;
		bufferProperties.maxOldSpaceOccupancy = 0.9f;
		bufferProperties.maxOldSpaceOccupancyActiveFromOldGenSize = 1024 * 1024 * 100;
		bufferProperties.minObjectExpansionRate = 0.02f;
		bufferProperties.minObjectExpansionRateActiveFromBufferSize = 100;
		bufferProperties.minOldSpaceOccupancy = 0.2f;
		bufferProperties.minOldSpaceOccupancyActiveTillOldGenSize = 1024 * 1024 * 200;
		bufferProperties.postConstruct();
	}

	/**
	 * Parameterized test to assure that no matter how big buffer size is, expansion rate will be
	 * between min and max.
	 * 
	 * @param bufferSize
	 *            Buffer size.
	 */
	@Test(dataProvider = "Buffer-Size-Provider")
	public void parametrizedExpansionRateTest(long bufferSize) {
		float expansionRate = bufferProperties.getObjectSecurityExpansionRate(bufferSize);
		Assert.assertTrue(expansionRate <= bufferProperties.getMaxObjectExpansionRate() && expansionRate >= bufferProperties.getMinObjectExpansionRate(), "Expansion rate is: " + expansionRate);
		if (bufferSize > bufferProperties.getMaxObjectExpansionRateActiveTillBufferSize() && bufferSize < bufferProperties.getMinObjectExpansionRateActiveFromBufferSize()) {
			Assert.assertTrue(expansionRate < bufferProperties.getMaxObjectExpansionRate() && expansionRate > bufferProperties.getMinObjectExpansionRate(), "Expansion rate is: " + expansionRate);
		}
	}

	/**
	 * Single expansion rate test.
	 */
	@Test
	public void singleExpansionRateTest() {
		long bufferSize = bufferProperties.getMaxObjectExpansionRateActiveTillBufferSize()
				+ (bufferProperties.getMinObjectExpansionRateActiveFromBufferSize() - bufferProperties.getMaxObjectExpansionRateActiveTillBufferSize()) / 2;
		float expansionRate = bufferProperties.getObjectSecurityExpansionRate(bufferSize);
		float expectedRate = bufferProperties.getMinObjectExpansionRate() + (bufferProperties.getMaxObjectExpansionRate() - bufferProperties.getMinObjectExpansionRate()) / 2;
		Assert.assertEquals(expansionRate, expectedRate);
	}

	/**
	 * Parameters generation for {@link #parametrizedExpansionRateTest(long)}.
	 * 
	 * @return Buffer size.
	 */
	@DataProvider(name = "Buffer-Size-Provider")
	public Object[][] bufferSizeParameterProvider() {
		int size = 100;
		Object[][] parameters = new Object[size][1];
		for (int i = 0; i < size; i++) {
			parameters[i][0] = getRandomLong(2000000000L);
		}
		return parameters;
	}

	/**
	 * Returns random positive long number smaller than given max value.
	 * 
	 * @param max
	 *            Max value.
	 * @return Long.
	 */
	private long getRandomLong(long max) {
		long value = (long) (Math.random() * max);
		return value - value % 10 + 10;
	}

}
