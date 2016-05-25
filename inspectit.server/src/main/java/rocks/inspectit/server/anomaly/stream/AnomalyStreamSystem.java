/**
 *
 */
package rocks.inspectit.server.anomaly.stream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.stream.comp.IResultProcessor;
import rocks.inspectit.server.anomaly.stream.comp.IStreamProcessor;
import rocks.inspectit.server.anomaly.stream.comp.impl.BaselineStreamProcessor;
import rocks.inspectit.server.anomaly.stream.comp.impl.ResultProcessor;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
public class AnomalyStreamSystem implements InitializingBean {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * {@link ExecutorService} for sending keep alive messages.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	ScheduledExecutorService executorService;

	/**
	 *
	 */
	private IStreamProcessor<InvocationSequenceData> streamProcessor;

	/**
	 *
	 */
	private IResultProcessor<InvocationSequenceData> resultProcessor;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		resultProcessor = new ResultProcessor();
		streamProcessor = new BaselineStreamProcessor(10000, resultProcessor, executorService);

		if (log.isInfoEnabled()) {
			log.info("|-AnomalyStreamSystem initialized...");
		}
	}

	public IStreamProcessor<InvocationSequenceData> getStream() {
		return streamProcessor;
	}
}
