package info.novatec.inspectit.rcp.editor.graph.plot.aggregation;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.MemoryInformationData;

import java.util.List;

/**
 * The MemoryDataAggregator provides a method for data aggregation.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class MemoryDataAggregator implements IDataAggregator {

	/**
	 * {@inheritDoc}
	 */
	public DefaultData aggregateData(List<? extends DefaultData> defaultData, int fromIndex, int toIndex) {
		// do nothing when list is empty
		if (!defaultData.isEmpty()) {
			long platformIdent = defaultData.get(0).getPlatformIdent();
			long sensorTypeIdent = defaultData.get(0).getSensorTypeIdent();

			long minFreePhysMemory = Long.MAX_VALUE;
			long maxFreePhysMemory = 0;
			long totalFreePhysMemory = 0;

			long minFreeSwapSpace = Long.MAX_VALUE;
			long maxFreeSwapSpace = 0;
			long totalFreeSwapSpace = 0;

			long minComittedVirtualMemSize = Long.MAX_VALUE;
			long maxComittedVirtualMemSize = 0;
			long totalComittedVirtualMemSize = 0;

			long minUsedHeapMemorySize = Long.MAX_VALUE;
			long maxUsedHeapMemorySize = 0;
			long totalUsedHeapMemorySize = 0;

			long minComittedHeapMemorySize = Long.MAX_VALUE;
			long maxComittedHeapMemorySize = 0;
			long totalComittedHeapMemorySize = 0;

			long minUsedNonHeapMemorySize = Long.MAX_VALUE;
			long maxUsedNonHeapMemorySize = 0;
			long totalUsedNonHeapMemorySize = 0;

			long minComittedNonHeapMemorySize = Long.MAX_VALUE;
			long maxComittedNonHeapMemorySize = 0;
			long totalComittedNonHeapMemorySize = 0;

			int totalCount = 0;

			for (int i = fromIndex; i <= toIndex; i++) {
				MemoryInformationData dataObject = (MemoryInformationData) defaultData.get(i);
				// aggregate the values
				totalCount += dataObject.getCount();

				minComittedHeapMemorySize = Math.min(dataObject.getMinComittedHeapMemorySize(), minComittedHeapMemorySize);
				minComittedNonHeapMemorySize = Math.min(dataObject.getMinComittedNonHeapMemorySize(), minComittedNonHeapMemorySize);
				minComittedVirtualMemSize = Math.min(dataObject.getMinComittedVirtualMemSize(), minComittedVirtualMemSize);
				minFreePhysMemory = Math.min(dataObject.getMinFreePhysMemory(), minFreePhysMemory);
				minFreeSwapSpace = Math.min(dataObject.getMinFreeSwapSpace(), minFreeSwapSpace);
				minUsedHeapMemorySize = Math.min(dataObject.getMinUsedHeapMemorySize(), minUsedHeapMemorySize);
				minUsedNonHeapMemorySize = Math.min(dataObject.getMinUsedNonHeapMemorySize(), minUsedNonHeapMemorySize);

				maxComittedHeapMemorySize = Math.max(dataObject.getMaxComittedHeapMemorySize(), maxComittedHeapMemorySize);
				maxComittedNonHeapMemorySize = Math.max(dataObject.getMaxComittedNonHeapMemorySize(), maxComittedNonHeapMemorySize);
				maxComittedVirtualMemSize = Math.max(dataObject.getMaxComittedVirtualMemSize(), maxComittedVirtualMemSize);
				maxFreePhysMemory = Math.max(dataObject.getMaxFreePhysMemory(), maxFreePhysMemory);
				maxFreeSwapSpace = Math.max(dataObject.getMaxFreeSwapSpace(), maxFreeSwapSpace);
				maxUsedHeapMemorySize = Math.max(dataObject.getMaxUsedHeapMemorySize(), maxUsedHeapMemorySize);
				maxUsedNonHeapMemorySize = Math.max(dataObject.getMaxUsedNonHeapMemorySize(), maxUsedNonHeapMemorySize);

				totalComittedHeapMemorySize += dataObject.getTotalComittedHeapMemorySize();
				totalComittedNonHeapMemorySize += dataObject.getTotalComittedNonHeapMemorySize();
				totalComittedVirtualMemSize += dataObject.getTotalComittedVirtualMemSize();
				totalFreePhysMemory += dataObject.getTotalFreePhysMemory();
				totalFreeSwapSpace += dataObject.getTotalFreeSwapSpace();
				totalUsedHeapMemorySize += dataObject.getTotalUsedHeapMemorySize();
				totalUsedNonHeapMemorySize += dataObject.getTotalUsedNonHeapMemorySize();
			}

			MemoryInformationData data = new MemoryInformationData(null, platformIdent, sensorTypeIdent);

			data.setMinComittedHeapMemorySize(minComittedHeapMemorySize);
			data.setMinComittedNonHeapMemorySize(minComittedNonHeapMemorySize);
			data.setMinComittedVirtualMemSize(minComittedVirtualMemSize);
			data.setMinFreePhysMemory(minFreePhysMemory);
			data.setMinFreeSwapSpace(minFreeSwapSpace);
			data.setMinUsedHeapMemorySize(minUsedHeapMemorySize);
			data.setMinUsedNonHeapMemorySize(minUsedNonHeapMemorySize);

			data.setMaxComittedHeapMemorySize(maxComittedHeapMemorySize);
			data.setMaxComittedNonHeapMemorySize(maxComittedNonHeapMemorySize);
			data.setMaxComittedVirtualMemSize(maxComittedVirtualMemSize);
			data.setMaxFreePhysMemory(maxFreePhysMemory);
			data.setMaxFreeSwapSpace(maxFreeSwapSpace);
			data.setMaxUsedHeapMemorySize(maxUsedHeapMemorySize);
			data.setMaxUsedNonHeapMemorySize(maxUsedNonHeapMemorySize);

			data.setTotalComittedHeapMemorySize(totalComittedHeapMemorySize);
			data.setTotalComittedNonHeapMemorySize(totalComittedNonHeapMemorySize);
			data.setTotalComittedVirtualMemSize(totalComittedVirtualMemSize);
			data.setTotalFreePhysMemory(totalFreePhysMemory);
			data.setTotalFreeSwapSpace(totalFreeSwapSpace);
			data.setTotalUsedHeapMemorySize(totalUsedHeapMemorySize);
			data.setTotalUsedNonHeapMemorySize(totalUsedNonHeapMemorySize);

			data.setCount(totalCount);

			return data;
		}

		return null;
	}

}
