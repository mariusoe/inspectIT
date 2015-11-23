package info.novatec.inspectit.cmr.processor.influxdb;

import info.novatec.inspectit.cmr.influxdb.InfluxDbService;
import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.spring.logger.Log;

import java.util.concurrent.TimeUnit;

import org.hibernate.StatelessSession;
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
	 * Instance of the {@link InfluxDbService}.
	 */
	@Autowired
	InfluxDbService influxDb;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, StatelessSession session) {
		MemoryInformationData data = (MemoryInformationData) defaultData;

		Point point = Point.measurement("memory_information").time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS).tag("platform_ident", String.valueOf(data.getPlatformIdent()))
				.field("total_comitted_heap_size", data.getTotalComittedHeapMemorySize()).field("total_comitted_non_heap_size", data.getTotalComittedNonHeapMemorySize())
				.field("total_comitted_virtual_size", data.getTotalComittedVirtualMemSize()).field("total_free_physical_space", data.getTotalFreePhysMemory())
				.field("total_free_swap_space", data.getTotalFreeSwapSpace()).field("total_used_heap_size", data.getTotalUsedHeapMemorySize())
				.field("total_used_non_heap_size", data.getTotalUsedNonHeapMemorySize()).build();

		influxDb.write(point);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof MemoryInformationData;
	}

}
