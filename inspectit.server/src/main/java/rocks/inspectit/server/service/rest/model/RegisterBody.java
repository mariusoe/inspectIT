package rocks.inspectit.server.service.rest.model;

import java.util.List;

/**
 * @author Marius Oehler
 *
 */
public class RegisterBody {

	private List<String> ips;

	private String name;

	private String version;


	/**
	 * Gets {@link #ips}.
	 * 
	 * @return {@link #ips}
	 */
	public List<String> getIps() {
		return this.ips;
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
	 * Gets {@link #version}.
	 *
	 * @return {@link #version}
	 */
	public String getVersion() {
		return this.version;
	}

}
