package rocks.inspectit.server.processor.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertNull;

import org.mockito.InjectMocks;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link PrivacyCmrProcessor} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class PrivacyCmrProcessorTest extends TestBase {

	@InjectMocks
	PrivacyCmrProcessor processor;

	/**
	 * Test the
	 * {@link PrivacyCmrProcessorTest#process(rocks.inspectit.shared.all.communication.DefaultData, javax.persistence.EntityManager)}
	 * method.
	 *
	 */
	public static class Process extends PrivacyCmrProcessorTest {

		HttpInfo httpInfo;

		HttpTimerData timerData;

		InvocationSequenceData sequenceData;

		@BeforeMethod
		public void setup() {
			httpInfo = new HttpInfo();
			timerData = new HttpTimerData();
			timerData.setHttpInfo(httpInfo);
			sequenceData = new InvocationSequenceData();
			sequenceData.setTimerData(timerData);
		}

		@Test
		public void removeIpAddress() {
			processor.storeIpAddress = false;
			httpInfo.setRemoteAddress("1.2.3.4");

			processor.process(sequenceData, null);

			assertNull(httpInfo.getRemoteAddress());
		}

		@Test
		public void removeIpAddressNoHttpInfo() {
			processor.storeIpAddress = false;
			timerData.setHttpInfo(null);

			processor.process(sequenceData, null);
		}

		@Test
		public void removeIpAddressNoTimerData() {
			processor.storeIpAddress = false;
			sequenceData.setTimerData(null);

			processor.process(sequenceData, null);
		}

		@Test
		public void doNothing() {
			processor.storeIpAddress = true;
			httpInfo.setRemoteAddress("1.2.3.4");

			processor.process(sequenceData, null);

			assertThat(httpInfo.getRemoteAddress(), is(equalTo("1.2.3.4")));
		}
	}
}
