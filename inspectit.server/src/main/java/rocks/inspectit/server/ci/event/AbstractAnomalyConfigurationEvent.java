package rocks.inspectit.server.ci.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import rocks.inspectit.shared.cs.ci.anomaly.configuration.AnomalyDetectionGroupConfiguration;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractAnomalyConfigurationEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 6347481185320884807L;

	/**
	 * @param source
	 */
	public AbstractAnomalyConfigurationEvent(Object source) {
		super(source);
	}

	public static class AnomalyDetectionGroupConfigurationCreatedEvent extends AbstractAnomalyConfigurationEvent {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = -8093058876052864848L;

		private final AnomalyDetectionGroupConfiguration groupConfiguration;

		/**
		 * @param source
		 */
		public AnomalyDetectionGroupConfigurationCreatedEvent(Object source, AnomalyDetectionGroupConfiguration groupConfiguration) {
			super(source);
			this.groupConfiguration = groupConfiguration;
		}

		/**
		 * Gets {@link #groupConfiguration}.
		 *
		 * @return {@link #groupConfiguration}
		 */
		public AnomalyDetectionGroupConfiguration getGroupConfiguration() {
			return this.groupConfiguration;
		}
	}

	public static class AnomalyDetectionGroupConfigurationsLoadedEvent extends AbstractAnomalyConfigurationEvent {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = -8093058876052864848L;

		private final List<AnomalyDetectionGroupConfiguration> groupConfigurations;

		/**
		 * @param source
		 */
		public AnomalyDetectionGroupConfigurationsLoadedEvent(Object source, List<AnomalyDetectionGroupConfiguration> groupConfigurations) {
			super(source);
			this.groupConfigurations = groupConfigurations;
		}

		/**
		 * Gets {@link #groupConfigurations}.
		 *
		 * @return {@link #groupConfigurations}
		 */
		public List<AnomalyDetectionGroupConfiguration> getGroupConfigurations() {
			return this.groupConfigurations;
		}
	}
}
