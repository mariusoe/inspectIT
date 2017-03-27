package rocks.inspectit.server.processor.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
public class StatisticsCmrProcessor extends AbstractCmrDataProcessor implements Runnable {

	private static final String STATISTICS_LOG = "./statistics.log";

	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService scheduledExecutorService;

	private Map<Long, Map<Class<?>, AtomicLong>> dataCounter = new HashMap<>();

	private long totalDataCount = 0L;

	@Log
	private Logger log;

	private BufferedWriter writer;

	private long intervalMs = 5000;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		Map<Class<?>, AtomicLong> agentMap = dataCounter.get(defaultData.getPlatformIdent());
		if (agentMap == null) {
			agentMap = new HashMap<>();
			dataCounter.put(defaultData.getPlatformIdent(), agentMap);
		}

		AtomicLong counter = agentMap.get(defaultData.getClass());
		if (counter == null) {
			counter = new AtomicLong(0L);
			agentMap.put(defaultData.getClass(), counter);
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
		scheduledExecutorService.scheduleAtFixedRate(this, intervalMs, intervalMs, TimeUnit.MILLISECONDS);

		try {
			this.writer = new BufferedWriter(new FileWriter(new File(STATISTICS_LOG), true));

			writer.write("#####################################");

		} catch (IOException e) {
			log.error("Cannot open file writer", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		writeLine(new Date() + " * * * *  * * * * * * * * * * *  * * *");

		long oldTotalDataCount = totalDataCount;

		double agentRate, rate;
		long count, agentCount;

		for (Entry<Long, Map<Class<?>, AtomicLong>> entry : dataCounter.entrySet()) {
			writeLine("> agent '" + entry.getKey());

			agentCount = 0L;

			for (Entry<Class<?>, AtomicLong> innerEntry : entry.getValue().entrySet()) {
				count = innerEntry.getValue().getAndSet(0L);

				totalDataCount += count;
				agentCount += count;
				rate = ((double) count / intervalMs) * 1000D;

				writeLine(">> '" + innerEntry.getKey().getSimpleName() + "'\t\t#:" + count + " rate:" + rate + " e/s");
			}

			agentRate = ((double) agentCount / intervalMs) * 1000D;
			writeLine(">> == total:" + agentCount + "\trate:" + agentRate + "e/s");
		}

		double cmrRate = ((double) (totalDataCount - oldTotalDataCount) / intervalMs) * 1000D;

		writeLine("CMR received " + (totalDataCount - oldTotalDataCount) + " elements in the last " + intervalMs + "ms => " + cmrRate + " e/s");
		writeLine("Total received: " + totalDataCount);
	}

	private void writeLine(String line) {
		try {
			writer.write(line);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			log.error("Error while writing to file.", e);
		}
	}
}
