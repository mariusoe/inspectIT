package rocks.inspectit.agent.java.service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.event.AgentMessagesReceivedEvent;
import rocks.inspectit.shared.all.communication.message.ThreadDumpRequestMessage;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
public class ThreadDumpProvider implements ApplicationListener<AgentMessagesReceivedEvent> {

	/**
	 * The logger for this class.
	 */
	@Log
	private Logger log;

	@Autowired
	private IConnection connection;

	/**
	 * Platform manager.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(AgentMessagesReceivedEvent event) {
		if (!event.containsMessageType(ThreadDumpRequestMessage.class)) {
			return;
		}

		String threadDump = createThreadDump();

		try {
			connection.pushThreadDump(platformManager.getPlatformId(), threadDump);
		} catch (ServerUnavailableException e) {
			e.printStackTrace();
		}
	}

	private String createThreadDump() {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), Integer.MAX_VALUE);

		StringBuilder dump = new StringBuilder();

		for (ThreadInfo threadInfo : threadInfos) {
			dump.append('"');
			dump.append(threadInfo.getThreadName());
			dump.append("\"\r\n");
			dump.append("java.lang.Thread.State: ");
			dump.append(threadInfo.getThreadState());

			for (StackTraceElement stackTraceElement : threadInfo.getStackTrace()) {
				dump.append("\r\n     at ");
				dump.append(stackTraceElement.toString());
			}
			dump.append("\r\n\r\n");
		}

		return dump.toString();

	}
}
