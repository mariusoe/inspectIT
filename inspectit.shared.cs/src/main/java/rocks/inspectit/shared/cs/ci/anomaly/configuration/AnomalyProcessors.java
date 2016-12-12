package rocks.inspectit.shared.cs.ci.anomaly.configuration;

/**
 * @author Marius Oehler
 *
 */
public enum AnomalyProcessors {

	ANALYZER_DUMMY("rocks.inspectit.server.anomaly.processor.analyzer.impl.DummyAnalyzeProcessor"),

	BASELINE_DUMMY("rocks.inspectit.server.anomaly.processor.baseline.impl.DummyBaselineProcessor"),

	CLASSIFIER_DUMMY("rocks.inspectit.server.anomaly.processor.classifier.impl.DummyClassifyProcessor");

	private String fqn;

	AnomalyProcessors(String fqn) {
		this.fqn = fqn;
	}

	/**
	 * Gets {@link #fqn}.
	 *
	 * @return {@link #fqn}
	 */
	public String getFqn() {
		return this.fqn;
	}

}
