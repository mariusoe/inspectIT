package rocks.inspectit.server.util.lookup;

/**
 * Represents a continent.
 *
 * @author Marius Oehler
 *
 */
public class Continent {

	/**
	 * The continent's iso code.
	 */
	private String isoCode;

	/**
	 * The continent name.
	 */
	private String name;

	/**
	 * No-arg constructors.
	 */
	public Continent() {
	}

	/**
	 * Constructor.
	 *
	 * @param isoCode
	 *            the iso code
	 * @param name
	 *            the name
	 */
	public Continent(String isoCode, String name) {
		super();
		this.isoCode = isoCode;
		this.name = name;
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
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return name + " [" + isoCode + "]";
	}
}
