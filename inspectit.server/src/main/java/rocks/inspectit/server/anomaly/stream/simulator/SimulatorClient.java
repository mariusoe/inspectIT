/**
 *
 */
package rocks.inspectit.server.anomaly.stream.simulator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Marius Oehler
 *
 */
public class SimulatorClient extends Thread {

	double errorRate = 0.01D;

	/**
	 * @param args @throws Exception @throws
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("- Start SimulatorClient -");

		SimulatorClient client = new SimulatorClient();
		client.start();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("> current error-rate: " + client.errorRate);
			System.out.print("> enter new error-rate: ");

			try {
				client.errorRate = Double.parseDouble(br.readLine());
			} catch (NumberFormatException nfe) {
				System.err.println("Invalid Format!");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			Socket socket = new Socket("127.0.0.1", 5000);

			ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());

			while (true) {
				InvocationSequenceData data = new InvocationSequenceData();
				data.setDuration(50D + Math.random() * 20);
				data.setChildCount(1);

				if (Math.random() < errorRate) {
					data.setDuration(100 + Math.random() * 100);
				}

				os.writeObject(data);

				Thread.sleep(250);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
