package rocks.inspectit.server.processor.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.util.lookup.Country;
import rocks.inspectit.server.util.lookup.CountryLookupUtil;
import rocks.inspectit.server.util.lookup.Network;
import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link LookupCmrProcessor} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class LookupCmrProcessorTest extends TestBase {

	@InjectMocks
	LookupCmrProcessor processor;

	@Mock
	CountryLookupUtil countryLookupUtil;

	/**
	 * Test the
	 * {@link LookupCmrProcessor#process(rocks.inspectit.shared.all.communication.DefaultData, javax.persistence.EntityManager)}
	 * method.
	 *
	 */
	public static class Process extends LookupCmrProcessorTest {

		Network network;

		HttpInfo httpInfo;

		HttpTimerData timerData;

		InvocationSequenceData sequenceData;

		@BeforeMethod
		public void setup() {
			network = new Network(0, 0, new Country(null, "", "XY"));
			httpInfo = new HttpInfo();
			timerData = new HttpTimerData();
			sequenceData = new InvocationSequenceData();
			sequenceData.setTimerData(timerData);
		}

		@Test
		public void successful() {
			httpInfo.setRemoteAddress("1.2.3.4");
			timerData.setHttpInfo(httpInfo);
			when(countryLookupUtil.lookup("1.2.3.4")).thenReturn(network);

			processor.process(sequenceData, null);

			assertThat(httpInfo.getCountryCode(), is(equalTo("XY")));
			verify(countryLookupUtil).lookup("1.2.3.4");
			verifyNoMoreInteractions(countryLookupUtil);
		}

		@Test
		public void noRemoteAddress() {
			timerData.setHttpInfo(httpInfo);

			processor.process(sequenceData, null);

			assertNull(httpInfo.getCountryCode());
			verifyZeroInteractions(countryLookupUtil);
		}

		@Test
		public void noHttpInfo() {
			when(countryLookupUtil.lookup("1.2.3.4")).thenReturn(network);

			processor.process(sequenceData, null);

			assertNull(httpInfo.getCountryCode());
			verifyZeroInteractions(countryLookupUtil);
		}

		@Test
		public void noTimerData() {
			sequenceData.setTimerData(null);

			processor.process(sequenceData, null);

			assertNull((sequenceData.getTimerData()));
			verifyZeroInteractions(countryLookupUtil);
		}
	}
}
