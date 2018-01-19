package rocks.inspectit.agent.java.core.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lmax.disruptor.EventHandler;

import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.agent.java.elastic.ElasticUtil;
import rocks.inspectit.agent.java.elastic.model.ElasticData;
import rocks.inspectit.agent.java.stats.AgentStatisticsLogger;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * {@link EventHandler} that sends the data wrapped in the {@link DefaultDataWrapper} to the CMR.
 *
 * @author Matthias Huber
 * @author Ivan Senic
 *
 */
@Component
public class DefaultDataHandler implements EventHandler<DefaultDataWrapper> {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The connection to the Central Measurement Repository.
	 */
	@Autowired
	private IConnection connection;

	/**
	 * Stats logger for reporting data dropped count.
	 */
	@Autowired
	private AgentStatisticsLogger statsLogger;

	/**
	 * List where data is collected and then passed to the connection.
	 */
	private List<DefaultData> defaultDatas = new ArrayList<DefaultData>(128);

	/**
	 * Defines if there was an exception before while trying to send the data. Used to throttle the
	 * printing of log statements.
	 */
	private boolean sendingExceptionNotice = false;

	@Autowired
	private ElasticUtil elasticUtil;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEvent(DefaultDataWrapper defaultDataWrapper, long sequence, boolean endOfBatch) {
		defaultDatas.add(defaultDataWrapper.getDefaultData());

		if (endOfBatch) {

			// Send Invocation Sequences to Elastic APM
			List<InvocationSequenceData> invocSequences = extractInvocSequences();
			if (CollectionUtils.isNotEmpty(invocSequences)) {
				ElasticData elasticData = elasticUtil.createElasticData();

				for (InvocationSequenceData isData : invocSequences) {
					elasticUtil.add(elasticData, isData);
				}

				elasticUtil.send(elasticData);
			}

			try {
				if (connection.isConnected()) {
					connection.sendDataObjects(defaultDatas);
					sendingExceptionNotice = false;
				} else {
					statsLogger.dataDropped(defaultDatas.size());
				}
			} catch (ServerUnavailableException serverUnavailableException) {
				if (serverUnavailableException.isServerTimeout()) {
					log.warn("Timeout on server when sending actual data. Data might be lost!", serverUnavailableException);
				} else {
					if (!sendingExceptionNotice) {
						sendingExceptionNotice = true;
						log.error("Connection problem appeared, stopping sending actual data!", serverUnavailableException);
					}
				}
			} finally {
				defaultDatas.clear();
			}
		}
	}

	private List<InvocationSequenceData> extractInvocSequences() {
		List<InvocationSequenceData> invocSequences = new ArrayList<InvocationSequenceData>();

		for (Iterator<DefaultData> iterator = defaultDatas.iterator(); iterator.hasNext();) {
			DefaultData data = iterator.next();
			// only requests
			if ((data instanceof InvocationSequenceData) && (((InvocationSequenceData) data).getTimerData() instanceof HttpTimerData)) {
				invocSequences.add((InvocationSequenceData) data);
				iterator.remove();
			}
		}

		return invocSequences;
	}
}