/**
 *
 */
package rocks.inspectit.server.anomaly.stream.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rocks.inspectit.server.anomaly.stream.object.StreamObject;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public final class StreamUtils {

	public static Map<String, List<StreamObject<InvocationSequenceData>>> mapByBusinessTransaction(Collection<StreamObject<InvocationSequenceData>> list) {
		Map<String, List<StreamObject<InvocationSequenceData>>> targetMap = new HashMap<>();

		for (StreamObject<InvocationSequenceData> streamObject : list) {
			String buisnessTransaction = streamObject.getContext().getBusinessTransaction();

			if (!targetMap.containsKey(buisnessTransaction)) {
				targetMap.put(buisnessTransaction, new ArrayList<StreamObject<InvocationSequenceData>>());
			}

			targetMap.get(buisnessTransaction).add(streamObject);
		}

		return targetMap;
	}

}
