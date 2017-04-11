package rocks.inspectit.server.influx.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.RandomUtils;
import org.influxdb.dto.Point.Builder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.server.util.lookup.Continent;
import rocks.inspectit.server.util.lookup.Country;
import rocks.inspectit.server.util.lookup.CountryLookupUtil;
import rocks.inspectit.server.util.lookup.Network;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class BusinessTransactionPointBuilderTest extends AbstractPointBuilderTest {

	@InjectMocks
	BusinessTransactionPointBuilder builder;

	@Mock
	ICachedDataService cachedDataService;

	@Mock
	PlatformIdent platformIdent;

	@Mock
	BusinessTransactionData businessTransactionData;

	@Mock
	ApplicationData applicationData;

	@Mock
	HttpTimerData httpTimerData;

	@Mock
	HttpInfo httpInfo;

	@Mock
	InvocationSequenceData data;

	@Mock
	CountryLookupUtil countryLookupUtil;

	public class CreateBuilder extends BusinessTransactionPointBuilderTest {

		static final long PLATFORM_ID = 1L;
		static final int APP_ID = 2;
		static final int BT_ID = 3;
		static final String AGENT_NAME = "Agent";
		static final String APP_NAME = "App";
		static final String BT_NAME = "Bt";

		@BeforeMethod
		public void setup() {
			when(platformIdent.getAgentName()).thenReturn(AGENT_NAME);
			when(applicationData.getName()).thenReturn(APP_NAME);
			when(businessTransactionData.getName()).thenReturn(BT_NAME);
		}

		@Test
		public void happyPath() throws Exception {
			Network network = new Network(0, 0, new Country(new Continent("Europe", "EU"), "Germany", "DE"));
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);
			when(cachedDataService.getBusinessTransactionForId(APP_ID, BT_ID)).thenReturn(businessTransactionData);
			when(businessTransactionData.getApplication()).thenReturn(applicationData);
			when(countryLookupUtil.lookup("0.0.0.0")).thenReturn(network);

			double duration = RandomUtils.nextDouble();
			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getApplicationId()).thenReturn(APP_ID);
			when(data.getBusinessTransactionId()).thenReturn(BT_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getDuration()).thenReturn(duration);
			when(data.getId()).thenReturn(Long.MAX_VALUE);
			when(data.getTimerData()).thenReturn(httpTimerData);
			when(httpTimerData.getHttpInfo()).thenReturn(httpInfo);
			when(httpInfo.getRemoteAddress()).thenReturn("0.0.0.0");

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.BusinessTransaction.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_NAME, String.valueOf(AGENT_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.BusinessTransaction.TAG_APPLICATION_NAME, String.valueOf(APP_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME, String.valueOf(BT_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.BusinessTransaction.TAG_COUNTRY_CODE, "DE"));
			assertThat(getFields(pointBuilder), hasEntry(Series.BusinessTransaction.FIELD_DURATION, (Object) duration));
			assertThat(getFields(pointBuilder), hasEntry(Series.BusinessTransaction.FIELD_TRACE_ID, (Object) Long.MAX_VALUE));
		}

		@Test
		public void noPlatform() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(null);
			when(cachedDataService.getBusinessTransactionForId(APP_ID, BT_ID)).thenReturn(businessTransactionData);
			when(businessTransactionData.getApplication()).thenReturn(applicationData);

			double duration = RandomUtils.nextDouble();
			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getApplicationId()).thenReturn(APP_ID);
			when(data.getBusinessTransactionId()).thenReturn(BT_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getDuration()).thenReturn(duration);

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.BusinessTransaction.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), not(hasKey(Series.TAG_AGENT_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.BusinessTransaction.TAG_APPLICATION_NAME, String.valueOf(APP_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME, String.valueOf(BT_NAME)));
			assertThat(getFields(pointBuilder), hasEntry(Series.BusinessTransaction.FIELD_DURATION, (Object) duration));
		}

		@Test
		public void noBusinessContext() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);
			when(cachedDataService.getBusinessTransactionForId(APP_ID, BT_ID)).thenReturn(null);

			double duration = RandomUtils.nextDouble();
			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getApplicationId()).thenReturn(APP_ID);
			when(data.getBusinessTransactionId()).thenReturn(BT_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getDuration()).thenReturn(duration);

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.BusinessTransaction.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_NAME, String.valueOf(AGENT_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.BusinessTransaction.TAG_APPLICATION_NAME, String.valueOf(ApplicationDefinition.UNKNOWN_APP)));
			assertThat(getTags(pointBuilder), hasEntry(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME, String.valueOf(BusinessTransactionDefinition.UNKNOWN_BUSINESS_TX)));
			assertThat(getFields(pointBuilder), hasEntry(Series.BusinessTransaction.FIELD_DURATION, (Object) duration));
		}

		@Test
		public void noCountryCode() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);
			when(cachedDataService.getBusinessTransactionForId(APP_ID, BT_ID)).thenReturn(businessTransactionData);
			when(businessTransactionData.getApplication()).thenReturn(applicationData);

			double duration = RandomUtils.nextDouble();
			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getApplicationId()).thenReturn(APP_ID);
			when(data.getBusinessTransactionId()).thenReturn(BT_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getDuration()).thenReturn(duration);
			when(data.getId()).thenReturn(Long.MAX_VALUE);
			when(data.getTimerData()).thenReturn(httpTimerData);
			when(httpTimerData.getHttpInfo()).thenReturn(httpInfo);
			when(httpInfo.getRemoteAddress()).thenReturn(null);

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.BusinessTransaction.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_NAME, String.valueOf(AGENT_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.BusinessTransaction.TAG_APPLICATION_NAME, String.valueOf(APP_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME, String.valueOf(BT_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.BusinessTransaction.TAG_COUNTRY_CODE, String.valueOf(BusinessTransactionPointBuilder.COUNTRY_CODE_NOT_AVAILABLE)));
			assertThat(getFields(pointBuilder), hasEntry(Series.BusinessTransaction.FIELD_DURATION, (Object) duration));
			assertThat(getFields(pointBuilder), hasEntry(Series.BusinessTransaction.FIELD_TRACE_ID, (Object) Long.MAX_VALUE));
		}

	}

}
