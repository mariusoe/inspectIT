/**
 *
 */
package rocks.inspectit.server.anomaly.stream.simulator;

import java.io.ObjectOutputStream;
import java.net.Socket;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class SimulatorClient {

	/**
	 * @param args
	 * @throws Exception
	 * 			@throws
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Start SimulatorClient");

		Socket socket = new Socket("127.0.0.1", 5000);

		ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());

		while (true) {
			InvocationSequenceData data = new InvocationSequenceData();
			data.setDuration(50D + Math.random() * 20);
			data.setChildCount(1);

			os.writeObject(data);

			Thread.sleep(250);
		}
	}

}
