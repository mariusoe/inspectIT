package info.novatec.inspectit.cmr.processor.influxdb;

import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.cmr.tsdb.InfluxDBService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.spring.logger.Log;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Marius Oehler
 *
 */
public class MemoryMetricProcessor extends AbstractCmrDataProcessor {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * Instance of the {@link InfluxDBService}.
	 */
	@Autowired
	InfluxDBService influxDb;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof MemoryInformationData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		MemoryInformationData data = (MemoryInformationData) defaultData;

		Point point = Point.measurement("memory_information").time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS).tag("platform_ident", String.valueOf(data.getPlatformIdent()))
				.addField("total_comitted_heap_size", data.getTotalComittedHeapMemorySize()).addField("total_comitted_non_heap_size", data.getTotalComittedNonHeapMemorySize())
				.addField("total_comitted_virtual_size", data.getTotalComittedVirtualMemSize()).addField("total_free_physical_space", data.getTotalFreePhysMemory())
				.addField("total_free_swap_space", data.getTotalFreeSwapSpace()).addField("total_used_heap_size", data.getTotalUsedHeapMemorySize())
				.addField("total_used_non_heap_size", data.getTotalUsedNonHeapMemorySize()).build();

		influxDb.insert(point);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isMonitoringProcessor() {
		return true;
	}
}
