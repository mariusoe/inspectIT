/**
 *
 */
package rocks.inspectit.server.anomaly.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

/**
 * @author Marius Oehler
 *
 */
public class StatisticUtilsTest {

	@Test
	public void meanTest() {
		double[] input = new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		double expected = 5D;

		double result = StatisticUtils.mean(input);

		assertThat(result, is(equalTo(expected)));
	}

	@Test
	public void meanLengthTest() {
		double[] input = new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		int length = 6;
		double expected = 3.5D;

		double result = StatisticUtils.mean(input, length);

		assertThat(result, is(equalTo(expected)));
	}
}
