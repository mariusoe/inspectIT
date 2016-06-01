/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

/**
 * @author Marius Oehler
 *
 */
public class ConfidenceBandTest {

	@Test
	public void getDistanceToBandTest() {
		double input, expected;
		ConfidenceBand band = new ConfidenceBand(3D, 4D, 2D);

		input = 5D;
		expected = 1D;
		assertThat(band.distanceToBand(input), is(equalTo(expected)));

		input = 1D;
		expected = 1D;
		assertThat(band.distanceToBand(input), is(equalTo(expected)));

		input = 0D;
		expected = 2D;
		assertThat(band.distanceToBand(input), is(equalTo(expected)));

		input = 4D;
		expected = 0D;
		assertThat(band.distanceToBand(input), is(equalTo(expected)));
	}

	@Test
	public void getWidth() {
		double expected;
		ConfidenceBand band;

		band = new ConfidenceBand(3D, 4D, 2D);
		expected = 2D;
		assertThat(band.getWidth(), is(equalTo(expected)));

		band = new ConfidenceBand(0D, 2D, -2D);
		expected = 4D;
		assertThat(band.getWidth(), is(equalTo(expected)));
	}

}
