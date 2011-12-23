package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.communication.data.HttpTimerData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the buffer aggregation of <code>HttpTimerData</code> elements.
 * 
 * @author Stefan Siegl
 */
public class BufferHttpTimerDataDaoTest extends AbstractTestNGLogSupport {

	private BufferHttpTimerDataDaoImpl underTest = new BufferHttpTimerDataDaoImpl();

	@Test
	public void aggregationWithInspectITHeaderTwoDifferent() {
		final HttpTimerData data = new HttpTimerData();
		data.setHeaders(new HashMap<String, String>() {
			{
				put("inspectit", "tag1");
			}
		});
		data.setUri("URI");
		data.setRequestMethod("GET");

		final HttpTimerData data2 = new HttpTimerData();
		data2.setHeaders(new HashMap<String, String>() {
			{
				put("inspectit", "tag2");
			}
		});
		data2.setUri("URI");
		data2.setRequestMethod("GET");

		final List<HttpTimerData> input = new ArrayList<HttpTimerData>() {
			{
				add(data);
				add(data2);
			}
		};

		final List<HttpTimerData> output = underTest.aggregate(input, false, false);
		Assert.assertNotNull(output);
		Assert.assertEquals(output.size(), 2);
	}

	@Test
	public void aggregationWithInspectITHeader() {
		final HttpTimerData data = new HttpTimerData();
		data.setHeaders(new HashMap<String, String>() {
			{
				put("inspectit", "tag1");
			}
		});
		data.setUri("URI");
		data.setRequestMethod("GET");
		data.setDuration(100d);
		data.setCpuDuration(10d);
		data.calculateCpuMin(10d);
		data.calculateCpuMax(10d);
		data.setCpuDuration(10d);

		final HttpTimerData data2 = new HttpTimerData();
		data2.setHeaders(new HashMap<String, String>() {
			{
				put("inspectit", "tag1");
			}
		});
		data2.setUri("URI");
		data2.setRequestMethod("GET");
		data2.setDuration(500d);
		data2.calculateCpuMin(20d);
		data2.calculateCpuMax(20d);
		data2.setCpuDuration(20d);

		final List<HttpTimerData> input = new ArrayList<HttpTimerData>() {
			{
				add(data);
				add(data2);
			}
		};

		final List<HttpTimerData> output = underTest.aggregate(input, false, false);
		Assert.assertNotNull(output);
		Assert.assertEquals(output.size(), 1);
		HttpTimerData result = output.get(0);
		Assert.assertEquals(result.getUri(), HttpTimerData.UNDEFINED);
		Assert.assertEquals(result.hasInspectItTaggingHeader(), true);
		Assert.assertEquals(result.getInspectItTaggingHeaderValue(), "tag1");
		Assert.assertEquals(result.getDuration(), 600d);
		Assert.assertEquals(result.getCpuDuration(), 30d);
		Assert.assertNull(result.getAttributes());
		Assert.assertNull(result.getParameters());
	}

	@Test
	public void aggregationURI() {
		final HttpTimerData data = new HttpTimerData();
		data.setUri("URI");
		data.setRequestMethod("GET");
		data.setDuration(100d);
		data.setCpuDuration(10d);
		data.calculateCpuMin(10d);
		data.calculateCpuMax(10d);
		data.setCpuDuration(10d);

		final HttpTimerData data2 = new HttpTimerData();
		data2.setUri("URI");
		data2.setRequestMethod("POST");
		data2.setDuration(500d);
		data2.calculateCpuMin(20d);
		data2.calculateCpuMax(20d);
		data2.setCpuDuration(20d);

		final List<HttpTimerData> input = new ArrayList<HttpTimerData>() {
			{
				add(data);
				add(data2);
			}
		};

		final List<HttpTimerData> output = underTest.aggregate(input, true, false);
		Assert.assertNotNull(output);
		Assert.assertEquals(output.size(), 1);
		HttpTimerData result = output.get(0);
		Assert.assertEquals(result.getUri(), "URI");
		Assert.assertEquals(result.hasInspectItTaggingHeader(), false);
		Assert.assertEquals(result.getDuration(), 600d);
		Assert.assertEquals(result.getCpuDuration(), 30d);
	}

	@Test
	public void aggregationURIRequestMethods() {
		final HttpTimerData data = new HttpTimerData();
		data.setUri("URI");
		data.setRequestMethod("GET");
		data.setDuration(100d);
		data.setCpuDuration(10d);
		data.calculateCpuMin(10d);
		data.calculateCpuMax(10d);
		data.setCpuDuration(10d);

		final HttpTimerData data2 = new HttpTimerData();
		data2.setUri("URI");
		data2.setRequestMethod("POST");
		data2.setDuration(500d);
		data2.calculateCpuMin(20d);
		data2.calculateCpuMax(20d);
		data2.setCpuDuration(20d);

		final List<HttpTimerData> input = new ArrayList<HttpTimerData>() {
			{
				add(data);
				add(data2);
			}
		};

		final List<HttpTimerData> output = underTest.aggregate(input, true, true);
		Assert.assertNotNull(output);
		Assert.assertEquals(output.size(), 2);
	}

}
