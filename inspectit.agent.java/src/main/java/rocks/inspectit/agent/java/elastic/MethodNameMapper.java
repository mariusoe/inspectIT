package rocks.inspectit.agent.java.elastic;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for mapping method ids to its actual name.
 *
 * @author Marius Oehler
 *
 */
public final class MethodNameMapper {
	/**
	 * This is just a dirty implementation. for the prototype. This should basically not done this
	 * way but actually using the registration flow or something similar.
	 */

	private static Map<Long, String> methodMapping = new HashMap<Long, String>();

	public static void put(long methodId, String methodName) {
		if (!methodMapping.containsKey(methodId)) {
			methodMapping.put(methodId, methodName);
		}
	}

	public static boolean exists(long methodId) {
		return methodMapping.containsKey(methodId);
	}

	public static String resolve(long methodId) {
		return methodMapping.get(methodId);
	}

	private MethodNameMapper() {
	}
}
