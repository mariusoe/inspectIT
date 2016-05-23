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
import java.nio.charset.Charset;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.io.Files;

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
	 * Folder where all grafana files are located.
	 */
	private static final String DEFAULT_GRAFANA_DIRECTORY = "grafana";

	/**
	 * Folder where all dashboards are located.
	 */
	private static final String DEFAULT_DASHBOARD_DIRECTORY = DEFAULT_GRAFANA_DIRECTORY + "/dashboards";

	/**
	 * Folder where all datasources are located.
	 */
	private static final String DEFAULT_DATASOURCE_DIRECTORY = DEFAULT_GRAFANA_DIRECTORY + "/datasources";

	/**
	 * API URL to the Grafana dashboard API.
	 */
	private static final String GRAFANA_DASHBOARD_API = "/api/dashboards/db";

	/**
	 * API URL to the Grafana datasource API.
	 */
	private static final String GRAFANA_DATASOURCE_API = "/api/datasources";

	/**
	 * Grafana username.
	 */
	@Value("${anomaly.grafana.username}")
	private String username;

	/**
	 * Grafana password.
	 */
	@Value("${anomaly.grafana.password}")
	private String password;

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
		inject();
	}

	/**
	 * Is called when the roomUri is changed.
	 */
	@PropertyUpdate(properties = { "anomaly.grafana.apiKey", "anomaly.grafana.serverUrl" })
	private void onPropertiesUpdate() {
		inject();
	}

	/**
	 * Returns a Basic authentication string.
	 *
	 * @return base64 encode credentials
	 */
	private String getHttpAuthentication() {
		String authenticationString = username + ":" + password;
		return new String(Base64.encodeBase64(authenticationString.getBytes()));
	}

	/**
	 * Injection of the dashboards to the specified Grafana server.
	 */
	private void inject() {
		if (!isEnabled) {
			if (log.isDebugEnabled()) {
				log.debug("|-Injection of Grafana dashboards is disabled");
			}
			return;
		}

		// injecting datasources
		if (log.isInfoEnabled()) {
			log.info("|-Injecting Grafana datasources to Grafana on server {}...", serverUrl);
		}

		try {
			File datasourceDirectory = ResourcesPathResolver.getResourceFile(DEFAULT_DATASOURCE_DIRECTORY);

			for (File datasourceFile : datasourceDirectory.listFiles()) {
				sendDatasource(datasourceFile);
			}
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("||-Couldn't load the predefined Grafana datasources.", e);
			}
		}

		// injecting dashboards
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
	 * Reads the Gafana datasource form the specified file and sends it to Grafana.
	 *
	 * @param datasourceFile
	 *            the file containing the datasource Json object
	 */
	private void sendDatasource(File datasourceFile) {
		String datasourceJson;
		try {
			datasourceJson = Files.toString(datasourceFile, Charset.forName("UTF-8"));
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("||-Couldn't load the Grafana datasource from file '" + datasourceFile.getName() + "'.", e);
			}
			return;
		}

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(serverUrl + GRAFANA_DATASOURCE_API).openConnection();
			connection.setDoOutput(true);

			// query is your body
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Length", Integer.toString(datasourceJson.length()));
			connection.addRequestProperty("Authorization", "Basic " + getHttpAuthentication());

			connection.getOutputStream().write(datasourceJson.getBytes("UTF8"));

			int responseCode = connection.getResponseCode();

			switch (responseCode) {
			case 200:
				if (log.isInfoEnabled()) {
					log.info("||-Grafana datasource was injected into Grafana.");
				}
				break;
			case 400:
				if (log.isErrorEnabled()) {
					log.error("||-Grafana datasource couldn't be injected. The datasource Json is not valid.");
				}
				break;
			case 401:
				if (log.isErrorEnabled()) {
					log.error("||-Authorization against Grafana failed.");
				}
				break;
			case 500:
				if (log.isInfoEnabled()) {
					log.info("||-Grafana datasource from file {} already exists.", datasourceFile.getName());
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

	/**
	 * Reads the Grafana dashboard of the specified file and sends it to the server.
	 *
	 * @param dashboardFile
	 *            the file containing the dashboard Json object
	 */
	private void sendDashboard(File dashboardFile) {
		String dashboardJson;
		try {
			dashboardJson = Files.toString(dashboardFile, Charset.forName("UTF-8"));
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("||-Couldn't load the Grafana dashboard from file '" + dashboardFile.getName() + "'.", e);
			}
			return;
		}

		// wrap dashboard
		String wrappedDashboard = "{\"dashboard\": " + dashboardJson + ",\"overwrite\": false}";

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(serverUrl + GRAFANA_DASHBOARD_API).openConnection();
			connection.setDoOutput(true);

			// query is your body
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Length", Integer.toString(wrappedDashboard.length()));
			connection.addRequestProperty("Authorization", "Basic " + getHttpAuthentication());

			connection.getOutputStream().write(wrappedDashboard.getBytes("UTF8"));

			int responseCode = connection.getResponseCode();

			switch (responseCode) {
			case 200:
				if (log.isInfoEnabled()) {
					log.info("||-Grafana dashboard was injected into Grafana.");
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
