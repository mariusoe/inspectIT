package info.novatec.inspectit.storage.serializer.util;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Serialization utility set.
 * 
 * @author Ivan Senic
 * 
 */
public final class SerializationUtil {

	/**
	 * Private constructor.
	 */
	private SerializationUtil() {
	}

	/**
	 * {@link PlatformIdent} object that is loaded from the database is not in a valid state to be
	 * serialized because of the Hibernate lazy fetching. Thus, this method resolves all the
	 * problematic issues, and alters the provided object in the way that we can serialize it. This
	 * method does not change any data.
	 * 
	 * @param originalPlatformIdent
	 *            Platform ident to be made serializable.
	 */
	public static void makePlatformIdentSerializable(PlatformIdent originalPlatformIdent) {
		// copy defined ips
		List<String> definedIPs = new ArrayList<String>();
		for (String ip : originalPlatformIdent.getDefinedIPs()) {
			definedIPs.add(ip);
		}
		originalPlatformIdent.setDefinedIPs(definedIPs);

		// solve sensor type idents
		Set<SensorTypeIdent> sensorTypeIdents = new HashSet<SensorTypeIdent>();
		for (SensorTypeIdent sensorTypeIdent : originalPlatformIdent.getSensorTypeIdents()) {
			// platform idents set
			Set<PlatformIdent> platformIdentSet = new HashSet<PlatformIdent>();
			platformIdentSet.add(originalPlatformIdent);
			sensorTypeIdent.setPlatformIdents(platformIdentSet);

			// clear many-to-many, to be set later
			if (sensorTypeIdent instanceof MethodSensorTypeIdent) {
				((MethodSensorTypeIdent) sensorTypeIdent).setMethodIdents(new HashSet<MethodIdent>());
			}
			sensorTypeIdents.add(sensorTypeIdent);
		}
		originalPlatformIdent.setSensorTypeIdents(sensorTypeIdents);

		// solve method idents
		Set<MethodIdent> methodIdents = new HashSet<MethodIdent>();
		for (MethodIdent methodIdent : originalPlatformIdent.getMethodIdents()) {
			// connect to right platform ident
			((MethodIdent) methodIdent).setPlatformIdent(originalPlatformIdent);
			// copy parameters
			List<String> parameters = new ArrayList<String>();
			for (String parameter : methodIdent.getParameters()) {
				parameters.add(parameter);
			}
			methodIdent.setParameters(parameters);
			// solve many to many with sensor type idents
			Set<MethodSensorTypeIdent> methodSensorTypeIdents = new HashSet<MethodSensorTypeIdent>();
			for (MethodSensorTypeIdent methodSensorTypeIdent : methodIdent.getMethodSensorTypeIdents()) {
				// try to find it in platformIdent
				for (SensorTypeIdent sensorTypeIdent : originalPlatformIdent.getSensorTypeIdents()) {
					if (methodSensorTypeIdent.equals(sensorTypeIdent)) {
						((MethodSensorTypeIdent) sensorTypeIdent).getMethodIdents().add(methodIdent);
						methodSensorTypeIdents.add((MethodSensorTypeIdent) sensorTypeIdent);
						break;
					}
				}
			}
			methodIdent.setMethodSensorTypeIdents(methodSensorTypeIdents);
			methodIdents.add(methodIdent);
		}
		originalPlatformIdent.setMethodIdents(methodIdents);
	}
}
