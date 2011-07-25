package info.novatec.inspectit.agent.sensor.platform.provider.factory;

import info.novatec.inspectit.agent.sensor.platform.provider.PlatformSensorInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.def.DefaultPlatformSensorInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.sun.SunPlatformSensorInfoProvider;
import info.novatec.inspectit.util.UnderlyingSystemInfo;
import info.novatec.inspectit.util.UnderlyingSystemInfo.JvmProvider;

/**
 * This class decides which {@link PlatformSensorInfoProvider} will be used.
 * 
 * @author Ivan Senic
 * 
 */
public final class PlatformSensorInfoProviderFactory {

	/**
	 * {@link PlatformSensorInfoProvider} when the agent is running on the Sun JVM.
	 */
	private static volatile SunPlatformSensorInfoProvider sunPlatformSensorInfoProvider;

	/**
	 * {@link PlatformSensorInfoProvider} when the agent is nopt running on the Sun JVM.
	 */
	private static volatile DefaultPlatformSensorInfoProvider defaultPlatformSensorInfoProvider;

	/**
	 * Private constructor.
	 */
	private PlatformSensorInfoProviderFactory() {
	}

	/**
	 * Returns the correct {@link PlatformSensorInfoProvider} based on the JVM vendor.
	 * 
	 * @return {@link PlatformSensorInfoProvider}
	 * @see UnderlyingSystemInfo
	 */
	public static PlatformSensorInfoProvider getPlatformSensorInfoProvider() {
		if (UnderlyingSystemInfo.JVM_PROVIDER == JvmProvider.SUN) {
			if (null == sunPlatformSensorInfoProvider) {
				createSunPlatformSensorInfoProvider();
			}
			return sunPlatformSensorInfoProvider;
		} else {
			if (null == defaultPlatformSensorInfoProvider) {
				createDefaultPlatformSensorInfoProvider();
			}
			return defaultPlatformSensorInfoProvider;
		}
	}

	/**
	 * Creates the {@link SunPlatformSensorInfoProvider}.
	 */
	private static synchronized void createSunPlatformSensorInfoProvider() {
		if (null == sunPlatformSensorInfoProvider) {
			sunPlatformSensorInfoProvider = new SunPlatformSensorInfoProvider();
		}
	}

	/**
	 * Creates the {@link DefaultPlatformSensorInfoProvider}.
	 */
	private static synchronized void createDefaultPlatformSensorInfoProvider() {
		if (null == defaultPlatformSensorInfoProvider) {
			defaultPlatformSensorInfoProvider = new DefaultPlatformSensorInfoProvider();
		}
	}
}
