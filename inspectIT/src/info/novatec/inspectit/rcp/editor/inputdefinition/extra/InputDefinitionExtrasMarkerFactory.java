package info.novatec.inspectit.rcp.editor.inputdefinition.extra;

import com.google.common.base.Objects;

/**
 * Factory for {@link InputDefinitionExtraMarker}s.
 * 
 * @author Ivan Senic
 * 
 */
public final class InputDefinitionExtrasMarkerFactory {

	/**
	 * Private constructor.
	 */
	private InputDefinitionExtrasMarkerFactory() {
	}

	/**
	 * Marker for {@link CombinedMetricsInputDefinitionExtra}.
	 */
	public static final InputDefinitionExtraMarker<CombinedMetricsInputDefinitionExtra> COMBINED_METRICS_EXTRAS_MARKER = new InputDefinitionExtraMarker<CombinedMetricsInputDefinitionExtra>() {
		@Override
		public Class<CombinedMetricsInputDefinitionExtra> getInputDefinitionExtraClass() {
			return CombinedMetricsInputDefinitionExtra.class;
		}

	};

	/**
	 * Marker for {@link NavigationSteppingInputDefinitionExtra}.
	 */
	public static final InputDefinitionExtraMarker<NavigationSteppingInputDefinitionExtra> NAVIGATION_STEPPING_EXTRAS_MARKER = new InputDefinitionExtraMarker<NavigationSteppingInputDefinitionExtra>() {
		@Override
		public Class<NavigationSteppingInputDefinitionExtra> getInputDefinitionExtraClass() {
			return NavigationSteppingInputDefinitionExtra.class;
		}

	};

	/**
	 * Abstract class for input definition extras marker.
	 * 
	 * @author Ivan Senic
	 * 
	 * @param <E>
	 *            Type of input definition extra.
	 */
	public abstract static class InputDefinitionExtraMarker<E extends IInputDefinitionExtra> {

		/**
		 * @return Returns the class type of the input definition extra.
		 */
		public abstract Class<E> getInputDefinitionExtraClass();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(getInputDefinitionExtraClass());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (object == null) {
				return false;
			}
			if (getClass() != object.getClass()) {
				return false;
			}
			InputDefinitionExtraMarker<?> that = (InputDefinitionExtraMarker<?>) object;
			return Objects.equal(this.getInputDefinitionExtraClass(), that.getInputDefinitionExtraClass());

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return Objects.toStringHelper(this)
					.add("inputDefintionExtraClass", getInputDefinitionExtraClass())
					.toString();
		}

	}

}
