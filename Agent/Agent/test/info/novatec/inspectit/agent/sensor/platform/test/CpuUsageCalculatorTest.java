package info.novatec.inspectit.agent.sensor.platform.test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import info.novatec.inspectit.agent.sensor.platform.provider.OperatingSystemInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.sun.SunOperatingSystemInfoProvider;
import info.novatec.inspectit.agent.test.AbstractLogSupport;

import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.logging.Level;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sun.management.OperatingSystemMXBean;

public class CpuUsageCalculatorTest extends AbstractLogSupport {

	@Mock
	private RuntimeMXBean runtimeBean;

	@Mock
	private OperatingSystemMXBean osBean;

	private OperatingSystemInfoProvider wrapper;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		wrapper = new SunOperatingSystemInfoProvider();

		Field field = wrapper.getClass().getDeclaredField("runtimeBean");
		field.setAccessible(true);
		field.set(wrapper, runtimeBean);

		field = wrapper.getClass().getDeclaredField("osBean");
		field.setAccessible(true);
		field.set(wrapper, osBean);
	}

	@Test
	public void calculateCpuUsage() {
		int availableProc = 1;

		// process cpu time is provided as nanoseconds
		long processCpuTime1 = 200L * 1000 * 1000; // ns representation of 200ms
		long processCpuTime2 = 500L * 1000 * 1000; // ns representation of 500ms

		// uptime is provided in milliseconds
		long uptime1 = 500L; // 500ms
		long uptime2 = 1100L; // 1100ms

		when(runtimeBean.getUptime()).thenReturn(uptime1).thenReturn(uptime2);
		when(osBean.getAvailableProcessors()).thenReturn(availableProc);
		when(osBean.getProcessCpuTime()).thenReturn(processCpuTime1).thenReturn(processCpuTime2);

		float cpuUsage1 = wrapper.retrieveCpuUsage();
		assertEquals(cpuUsage1, 0.0f, 0.01f);

		float cpuUsage2 = wrapper.retrieveCpuUsage();
		// CPU usage can only be deduced after the second call
		long process = (processCpuTime2 - processCpuTime1);
		long upAsNano = ((uptime2 - uptime1) * 1000 * 1000);
		float expectedUsage = (float) process / upAsNano * 100;
		assertEquals(cpuUsage2, expectedUsage, 0.01f);
	}

	@Override
	protected Level getLogLevel() {
		return Level.FINEST;
	}

}