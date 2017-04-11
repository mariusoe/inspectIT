package rocks.inspectit.server.util.lookup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.lang.reflect.Field;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link CountryLookupUtil}.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class CountryLookupUtilTest extends TestBase {

	@InjectMocks
	CountryLookupUtil countryLookupUtil;

	@Mock
	Logger log;

	/**
	 * Tests the {@link CountryLookupUtil#postConstruct()} method.
	 *
	 */
	public static class PostConstruct extends CountryLookupUtilTest {

		@SuppressWarnings("unchecked")
		private Map<Integer, Network[]> getNetworkMap() throws Exception {
			Field field = CountryLookupUtil.class.getDeclaredField("networks");
			field.setAccessible(true);
			return (Map<Integer, Network[]>) field.get(countryLookupUtil);
		}

		@Test
		public void successful() throws Exception {
			countryLookupUtil.postConstruct();

			assertThat(getNetworkMap().entrySet(), is(not(empty())));
		}
	}

	/**
	 * Tests the {@link CountryLookupUtil#lookup(String)} method.
	 *
	 */
	public static class Lookup extends CountryLookupUtilTest {

		@Test
		public void successful() {
			countryLookupUtil.postConstruct();

			Network network = countryLookupUtil.lookup("2.16.6.1");

			assertThat(network.getCountry().getIsoCode(), is("DE"));
		}

		@Test
		public void successfulReservedNetwork() {
			countryLookupUtil.postConstruct();

			Network network = countryLookupUtil.lookup("10.0.0.1");

			assertThat(network.getCountry().getName(), is("Local Network"));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void invalidIpAddress() {
			countryLookupUtil.postConstruct();

			countryLookupUtil.lookup("300.0.0.0");
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void nullIpAddress() {
			countryLookupUtil.postConstruct();

			countryLookupUtil.lookup(null);
		}
	}

}
