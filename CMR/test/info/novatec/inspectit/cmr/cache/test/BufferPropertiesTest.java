package info.novatec.inspectit.cmr.cache.test;

import info.novatec.inspectit.cmr.cache.impl.BufferProperties;
import info.novatec.inspectit.cmr.test.AbstractLogSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Testing the calculations inside of {@link BufferProperties} class.
 * 
 * @author Ivan Senic
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-model.xml" })
public class BufferPropertiesTest extends AbstractLogSupport {

	/**
	 * Buffer properties to test.
	 */
	@Autowired
	private BufferProperties bufferProperties;

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
		int size = 1000;
		Object[][] parameters = new Object[size][1];
		for (int i = 0; i < size; i++) {
			parameters[i][0] = getRandomLong(2000000000L);
		}
		return parameters;
	}

	/**
	 * Returns random positive long number smaller than given max value.
	 * 
	 * @param max Max value.
	 * @return Long.
	 */
	private long getRandomLong(long max) {
		long value = (long) (Math.random() * max);
		return value - value % 10 + 10;
	}
}
