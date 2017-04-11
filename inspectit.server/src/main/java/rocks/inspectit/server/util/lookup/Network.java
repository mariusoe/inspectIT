package rocks.inspectit.server.util.lookup;

/**
 * Representation of a network.
 *
 * @author Marius Oehler
 *
 */
public class Network {

	/**
	 * The network address.
	 */
	private final int networkAddress;

	/**
	 * The network subnet mask.
	 */
	private final int subnetMask;

	/**
	 * The country related to this network.
	 */
	private final Country country;

	/**
	 * Constructor.
	 *
	 * @param networkAddress
	 *            the network address
	 * @param subnetMask
	 *            the subnet mask
	 * @param country
	 *            the country of the network
	 */
	public Network(int networkAddress, int subnetMask, Country country) {
		this.networkAddress = networkAddress;
		this.subnetMask = subnetMask;
		this.country = country;
	}

	/**
	 * Gets {@link #networkAddress}.
	 *
	 * @return {@link #networkAddress}
	 */
	public int getNetworkAddress() {
		return this.networkAddress;
	}

	/**
	 * Gets {@link #subnetMask}.
	 *
	 * @return {@link #subnetMask}
	 */
	public int getSubnetMask() {
		return this.subnetMask;
	}

	/**
	 * Gets {@link #country}.
	 *
	 * @return {@link #country}
	 */
	public Country getCountry() {
		return this.country;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Network [address=" + networkAddress + ", country=" + country + "]";
	}
}
