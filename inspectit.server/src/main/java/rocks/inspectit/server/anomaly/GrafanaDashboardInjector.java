/**
 *
 */
package rocks.inspectit.server.anomaly;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.testng.reporters.Files;

import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.util.ResourcesPathResolver;

/**
 * Injector for Grafana dashboards.
 *
 * @author Marius Oehler
 *
 */
public final class GrafanaDashboardInjector {

	/**
	 * Logger for the class.
	 */
	@Log
	private Logger log;

	/**
	 * Folder where all dashboards are located.
	 */
	private static final String DEFAULT_DASHBOARD_DIRECTORY = "grafana-dashboards";

	/**
	 * Grafana API key.
	 */
	@Value("${anomaly.grafana.apiKey}")
	private String apiKey;

	/**
	 * Grafana server URL.
	 */
	@Value("${anomaly.grafana.serverUrl}")
	private String serverUrl;

	/**
	 * Whether the injection is enabled.
	 */
	@Value("${anomaly.grafana.injection}")
	private boolean isEnabled;

	/**
	 * Hidden constructor.
	 */
	private GrafanaDashboardInjector() {
	}

	/**
	 * Will be called after setting of the properties.
	 */
	@PostConstruct
	public void afterPropertiesSet() {
		injectDashboards();
	}

	/**
	 * Is called when the roomUri is changed.
	 */
	@PropertyUpdate(properties = { "anomaly.grafana.apiKey", "anomaly.grafana.serverUrl" })
	private void onPropertiesUpdate() {
		injectDashboards();
	}

	/**
	 * Injection of the dashboards to the specified Grafana server.
	 */
	private void injectDashboards() {
		if (!isEnabled) {
			if (log.isDebugEnabled()) {
				log.debug("|-Injection of Grafana dashboards is disabled");
			}
			return;
		}

		if (log.isInfoEnabled()) {
			log.info("|-Injecting Grafana dashboards to Grafana on server {}...", serverUrl);
		}

		try {
			File dashboardDirectory = ResourcesPathResolver.getResourceFile(DEFAULT_DASHBOARD_DIRECTORY);

			for (File dashboardFile : dashboardDirectory.listFiles()) {
				sendDashboard(dashboardFile);
			}
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("||-Couldn't load the predefined Grafana dashboards.", e);
			}
		}
	}

	/**
	 * Reads the Grafana dashboard of the specified file and sends it to the server.
	 *
	 * @param dashboardFile
	 *            the file containing the dashboard JSON object
	 */
	private void sendDashboard(File dashboardFile) {
		String dashboardJson;
		try {
			dashboardJson = Files.readFile(dashboardFile);
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("||-Couldn't load the Grafana dashboard from file '" + dashboardFile.getName() + "'.", e);
			}
			return;
		}

		// wrap dashboard
		String wrappedDashboard = "{\"dashboard\": " + dashboardJson + ",\"overwrite\": false}";

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(serverUrl + "/api/dashboards/db").openConnection();
			connection.setDoOutput(true);

			// query is your body
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Length", Integer.toString(wrappedDashboard.length()));
			connection.addRequestProperty("Authorization", "Bearer " + apiKey);

			connection.getOutputStream().write(wrappedDashboard.getBytes("UTF8"));

			int responseCode = connection.getResponseCode();

			switch (responseCode) {
			case 200:
				if (log.isInfoEnabled()) {
					log.info("||-Grafana dashboards were injected into Grafana.");
				}
				break;
			case 400:
				if (log.isErrorEnabled()) {
					log.error("||-Grafana dashboards couldn't be injected. The dashboard Json is not valid.");
				}
				break;
			case 401:
				if (log.isErrorEnabled()) {
					log.error("||-Authorization against Grafana failed.");
				}
				break;
			case 412:
				if (log.isInfoEnabled()) {
					log.info("||-The dashboard from file {} was not be injected. It already exists.", dashboardFile.getName());
				}
				break;
			default:
				BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(connection.getInputStream())));

				StringBuffer buffer = new StringBuffer();
				String inputLine = "";
				while ((inputLine = reader.readLine()) != null) {
					buffer.append(inputLine);
				}

				if (log.isErrorEnabled()) {
					log.error("||-An unknown error occured: {}", buffer.toString());
				}
				break;
			}

		} catch (MalformedURLException e) {
			if (log.isErrorEnabled()) {
				log.error("||-The Grafana server URL is not valid.", e);
			}
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("||-Couldn't send the dashboard to the Grafana server.", e);
			}
		}
	}
}
