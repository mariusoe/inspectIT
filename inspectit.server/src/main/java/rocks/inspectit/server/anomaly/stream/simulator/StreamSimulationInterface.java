/**
 *
 */
package rocks.inspectit.server.anomaly.stream.simulator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.stream.AnomalyStreamSystem;
import rocks.inspectit.server.util.CacheIdGenerator;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
@Component
public class StreamSimulationInterface implements InitializingBean, Runnable {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * {@link CacheIdGenerator}.
	 */
	@Autowired
	AnomalyStreamSystem streamSystem;

	/**
	 * Port.
	 */
	private final int port = 5000;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		new Thread(this).start();

		if (log.isInfoEnabled()) {
			log.info("|-StreamSimulationInterface is active on port {}...", port);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		ServerSocket sSocket;
		try {
			sSocket = new ServerSocket(port);

			while (true) {
				try {
					if (log.isInfoEnabled()) {
						log.info("|-Waiting for StreamSimulationClient...", port);
					}
					Socket socket = sSocket.accept();

					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

					while (true) {
						Object object = ois.readObject();

						streamSystem.getStream().process((InvocationSequenceData) object);
					}
				} catch (Exception e) {
					if (log.isInfoEnabled()) {
						log.info("|-Connection closed", port);
					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
