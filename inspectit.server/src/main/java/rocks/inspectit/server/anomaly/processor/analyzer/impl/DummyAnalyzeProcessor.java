package rocks.inspectit.server.anomaly.processor.analyzer.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.server.anomaly.data.AnalyzableData;
import rocks.inspectit.server.anomaly.processor.analyzer.AbstractAnalyzeProcessor;
import rocks.inspectit.shared.cs.ci.anomaly.configuration.processor.analyze.DummyAnalyzeProcessorConfiguration;

/**
 * @author Marius Oehler
 *
 */
@XmlRootElement
public class DummyAnalyzeProcessor extends AbstractAnalyzeProcessor<DummyAnalyzeProcessorConfiguration> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void analyze(AnalyzableData<?> analyzable) {
		// TODO Auto-generated method stub
	}

}
