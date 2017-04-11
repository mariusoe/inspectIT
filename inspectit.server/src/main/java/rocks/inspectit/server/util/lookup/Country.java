package rocks.inspectit.server.util.lookup;

/**
 * Represents a country.
 *
 * @author Marius Oehler
 *
 */
public class Country {

	/**
	 * The country's continent.
	 */
	private Continent continent;

	/**
	 * The country name.
	 */
	private String name;

	/**
	 * The country's iso code.
	 */
	private String isoCode;

	/**
	 * No-arg constructors.
	 */
	public Country() {
	}

	/**
	 * Constructor.
	 *
	 * @param continent
	 *            the continent
	 * @param countryName
	 *            the country name
	 * @param isoCode
	 *            the iso code
	 */
	public Country(Continent continent, String countryName, String isoCode) {
		this.continent = continent;
		this.name = countryName;
		this.isoCode = isoCode;
	}

	/**
	 * Gets {@link #continent}.
	 *
	 * @return {@link #continent}
	 */
	public Continent getContinent() {
		return this.continent;
	}

	/**
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets {@link #isoCode}.
	 *
	 * @return {@link #isoCode}
	 */
	public String getIsoCode() {
		return this.isoCode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return name + " [code=" + isoCode + ", continent=" + continent.toString() + "]";
	}
}
