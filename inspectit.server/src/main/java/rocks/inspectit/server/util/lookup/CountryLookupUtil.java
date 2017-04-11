package rocks.inspectit.server.util.lookup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.stereotype.Component;

import com.google.common.net.InetAddresses;

import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.util.ResourcesPathResolver;

/**
 * Utility class for looking up the country code of an IP adress.
 *
 * @author Marius Oehler
 *
 */
@Component
public class CountryLookupUtil {

	/**
	 * Logger of this file.
	 */
	@Log
	private Logger log;

	/**
	 * The resource directory.
	 */
	private static final String RESOURCE_DIRECTORY = "resources";

	/**
	 * The IP lookup file.
	 */
	private static final String IP_MAPPING_CSV = "GeoLite2-IPv4.csv";

	/**
	 * The country mapping file.
	 */
	private static final String COUNTRY_MAPPING_CSV = "GeoLite2-Country-en.csv";

	/**
	 * Map containing all loaded {@link Continent}s.
	 */
	private final Map<String, Continent> continentMap = new HashMap<>();

	/**
	 * Map containing all loaded {@link Country}.
	 */
	private final Map<String, Country> countryMap = new HashMap<>();

	/**
	 * Map containing all loaded {@link Network}s. The networks are stored in arrays sorted on the 8
	 * most significant bits of the IP address (first block).
	 */
	private final Map<Integer, Network[]> networks = new HashMap<>();

	/**
	 * This map is only used during the initialization and holding loaded {@link Network}s.
	 */
	private Map<Integer, List<Network>> initializationNetworkMap = new HashMap<>();

	/**
	 * Gets the file of the given filename.
	 *
	 * @param filename
	 *            the filename
	 * @return {@link File} representing the given filename
	 */
	private File getFile(String filename) {
		try {
			File resourceDirectory = ResourcesPathResolver.getResourceFile(RESOURCE_DIRECTORY);
			return ResourcesPathResolver.getResourceFile(filename, resourceDirectory);
		} catch (IOException exception) {
			throw new BeanInitializationException("Error while trying to open '" + filename + "'.", exception);
		}
	}

	/**
	 * Initials this class. Here, the lookup array is loaded.
	 */
	@PostConstruct
	public void postConstruct() {
		if (log.isInfoEnabled()) {
			log.info("|-Loading network and country mapping for IP lookup..");
		}

		loadCountriesAndContinents();
		loadNetworks();
		loadReservedNetworks();
		buildNetworkArrayMap();

		if (log.isInfoEnabled()) {
			log.info("||-Network and country mappings have been loaded.");
		}
	}

	/**
	 * Adds reserved networks (private networks).
	 */
	private void loadReservedNetworks() {
		addCountry("0", "--", "Local Network", "--", "Local Network");

		addNetwork("10.0.0.0/8", "0");
		addNetwork("100.64.0.0/10", "0");
		addNetwork("127.0.0.0/8", "0");
		addNetwork("172.16.0.0/12", "0");
		addNetwork("192.0.0.0/24", "0");
		addNetwork("192.168.0.0/16", "0");
		addNetwork("198.18.0.0/15", "0");
	}

	/**
	 * Loads countries and continents.
	 */
	private void loadCountriesAndContinents() {
		File countryFile = getFile(COUNTRY_MAPPING_CSV);

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(countryFile));
		} catch (FileNotFoundException exception) {
			throw new BeanInitializationException("Could not read the country lookup file.", exception);
		}

		try {
			// skip header
			reader.readLine();

			String input = null;
			while ((input = reader.readLine()) != null) { // NOPMD
				String[] split = input.split(",");

				if (split.length != 6) {
					continue;
				}

				String continentCode = split[2];
				String continentName = split[3].replace("\"", "");

				String countryId = split[0];
				String countryCode = split[4];
				String countryName = split[5].replace("\"", "");

				addCountry(countryId, countryCode, countryName, continentCode, continentName);
			}

			reader.close();
		} catch (IOException exception) {
			if (log.isErrorEnabled()) {
				log.error("Error while reading country lookup file.", exception);
			}
		}
	}

	/**
	 * Adds a new {@link Country} to the initialization map.
	 *
	 * @param countryId
	 *            the id of the country
	 * @param countryCode
	 *            the ISO code of the country
	 * @param countryName
	 *            the name of the country
	 * @param continentCode
	 *            the ISO code of the country's continent
	 * @param continentName
	 *            the name of the country's continent
	 */
	private void addCountry(String countryId, String countryCode, String countryName, String continentCode, String continentName) {
		Continent continent = continentMap.get(continentCode);
		if (continent == null) {
			continent = new Continent(continentCode, continentName);
			continentMap.put(continentCode, continent);
		}

		Country country = new Country(continent, countryName, countryCode);
		countryMap.put(countryId, country);
	}

	/**
	 * Loads the networks.
	 */
	private void loadNetworks() {
		File countryFile = getFile(IP_MAPPING_CSV);

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(countryFile));
		} catch (FileNotFoundException exception) {
			throw new BeanInitializationException("Could not read the IP addresses lookup file.", exception);
		}

		try {
			// skip header
			reader.readLine();

			String input = null;
			while ((input = reader.readLine()) != null) { // NOPMD
				String[] split = input.split(",");

				String subnet = split[0];
				String countryId = split[1];

				addNetwork(subnet, countryId);
			}

			reader.close();
		} catch (IOException exception) {
			if (log.isErrorEnabled()) {
				log.error("Error while reading IP addresses lookup file.", exception);
			}
		}
	}

	/**
	 * Adds a new {@link Network} to the initialization map.
	 *
	 * @param subnet
	 *            the subnet to add (e.g.: 192.168.0.0/24)
	 * @param countryId
	 *            the country ID of the subnet
	 */
	private void addNetwork(String subnet, String countryId) {
		Network network = createNetwork(subnet, countryId);

		int networkKey = network.getNetworkAddress() >> 24;

		List<Network> networkList = initializationNetworkMap.get(networkKey);
		if (networkList == null) {
			networkList = new ArrayList<>();
			initializationNetworkMap.put(networkKey, networkList);
		}

		networkList.add(network);
	}

	/**
	 * Creates a map containing arrays containing the loaded {@link Network} based on the
	 * {@link #initializationNetworkMap}. The {@link #initializationNetworkMap} is cleaned at the
	 * end.
	 */
	private void buildNetworkArrayMap() {
		for (Entry<Integer, List<Network>> entry : initializationNetworkMap.entrySet()) {
			List<Network> networkList = entry.getValue();
			Network[] networkArray = new Network[networkList.size()];
			networks.put(entry.getKey(), networkList.toArray(networkArray));
		}

		// clearn init map
		initializationNetworkMap.clear();
	}

	/**
	 * Creates a new network.
	 *
	 * @param ipAddress
	 *            the network address including subnet prefix. E.g.: 192.168.0.1/24
	 * @param countryId
	 *            the country code of the network
	 * @return the created {@link Network}
	 */
	private Network createNetwork(String ipAddress, String countryId) {
		String[] split = ipAddress.split("/");

		int networkAddress = InetAddresses.coerceToInteger(InetAddresses.forString(split[0]));
		int subnetSize = Integer.parseInt(split[1]);
		int subnetMask = ((int) Math.pow(2, subnetSize) - 1) << (32 - subnetSize);

		Country country = countryMap.get(countryId);

		return new Network(networkAddress, subnetMask, country);
	}

	/**
	 * Looksup the {@link Network} of the given IP address.
	 *
	 * @param ipAddressString
	 *            the v4 IP address
	 * @return the {@link Network} of the given IP address or <code>null</code> if no network
	 *         exists.
	 */
	public Network lookup(String ipAddressString) {
		if (ipAddressString == null) {
			throw new IllegalArgumentException("IP address mist not be null.");
		}

		int ipAddress = InetAddresses.coerceToInteger(InetAddresses.forString(ipAddressString));

		int networkKey = ipAddress >> 24;

		Network[] networkArray = networks.get(networkKey);
		if (networkArray == null) {
			return null;
		}

		for (Network network : networkArray) {
			int maskedIpAddress = ipAddress & network.getSubnetMask();
			if (maskedIpAddress == network.getNetworkAddress()) {
				return network;
			}
		}

		return null;
	}
}
