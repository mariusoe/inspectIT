/**
 *
 */
package info.novatec.inspectit.cmr.anomaly.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Date;

import org.testng.annotations.Test;

/**
 * Testing of the functionality of the {@link AnomalyUtils}.
 *
 * @author Marius Oehler
 *
 */
public class AnomalyUtilsTest {

	/**
	 * Tests the calculation of an exponential weighted moving average.
	 */
	@Test
	public void calculateExponentialMovingAverage() {
		double decay = 0.1D;
		double average = 10D;
		double value = 15;
		double expected = 10.5D;

		double movingAverage = AnomalyUtils.calculateExponentialMovingAverage(decay, average, value);

		assertThat(movingAverage, equalTo(expected));
	}

	/**
	 * Tests the conversion of InfluxDB time strings.
	 */
	@Test
	public void parseInfluxTimeString() {
		Object[][] inputData = { { "2016-03-18T08:04:09Z", 1458284649000L }, { "2016-03-18T08:04:09.1Z", 1458284649100L }, { "2016-03-18T08:04:09.01Z", 1458284649010L },
				{ "2016-03-18T08:04:09.001Z", 1458284649001L } };

		for (Object[] data : inputData) {
			String input = (String) data[0];
			long expected = (long) data[1];

			Date parsedDate = AnomalyUtils.parseInfluxTimeString(input);
			assertThat(expected, equalTo(parsedDate.getTime()));
		}
	}

}
