package rocks.inspectit.server.processor.impl;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
public class StatisticsCmrProcessor extends AbstractCmrDataProcessor implements Runnable {

	private static final long INTERVAL_MS = 5000;

	@Autowired
	private InfluxDBDao influx;

	@Log
	private Logger log;

	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService scheduledExecutorService;

	private Map<Long, Map<Class<?>, AtomicLong>> dataCounter = new ConcurrentHashMap<>();

	private long totalDataCount = 0L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		Map<Class<?>, AtomicLong> agentMap = dataCounter.get(defaultData.getPlatformIdent());
		if (agentMap == null) {
			synchronized (dataCounter) {
				agentMap = dataCounter.get(defaultData.getPlatformIdent());
				if (agentMap == null) {
					agentMap = new ConcurrentHashMap<>();
					dataCounter.put(defaultData.getPlatformIdent(), agentMap);
				}
			}
		}

		AtomicLong counter = agentMap.get(defaultData.getClass());
		if (counter == null) {
			synchronized (dataCounter) {
				counter = agentMap.get(defaultData.getClass());
				if (counter == null) {
					counter = new AtomicLong(0L);
					agentMap.put(defaultData.getClass(), counter);
				}
			}
		}

		counter.incrementAndGet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return true;
	}

	@PostConstruct
	private void postConstruct() {
		scheduledExecutorService.scheduleAtFixedRate(this, INTERVAL_MS, INTERVAL_MS, TimeUnit.MILLISECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		long oldTotalDataCount = totalDataCount;

		long count;
		long time = System.currentTimeMillis();

		for (Entry<Long, Map<Class<?>, AtomicLong>> agentEntry : dataCounter.entrySet()) {
			for (Entry<Class<?>, AtomicLong> dataEntry : agentEntry.getValue().entrySet()) {
				count = dataEntry.getValue().getAndSet(0L);
				String className = dataEntry.getKey().getSimpleName();

				totalDataCount += count;

				Builder builder = Point.measurement("cmr_statistics").time(time, TimeUnit.MILLISECONDS);
				builder.tag("agent", String.valueOf(agentEntry.getKey()));
				builder.tag("dataClass", className);
				builder.addField("count", count);
				influx.insert(builder.build());
			}
		}

		long totalDelta = totalDataCount - oldTotalDataCount;

		com.sun.management.OperatingSystemMXBean systemBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

		Builder builder = Point.measurement("cmr_statistics").time(time, TimeUnit.MILLISECONDS);
		builder.addField("total_count", totalDelta);
		builder.addField("total_count_absolute", totalDataCount);
		builder.addField("system_cpu", systemBean.getSystemCpuLoad());
		builder.addField("process_cpu", systemBean.getProcessCpuLoad());
		builder.addField("commited_memory", systemBean.getCommittedVirtualMemorySize());

		influx.insert(builder.build());
	}
}
