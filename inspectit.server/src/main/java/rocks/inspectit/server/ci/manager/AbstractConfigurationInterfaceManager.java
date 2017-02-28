package rocks.inspectit.server.ci.manager;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.ci.ConfigurationInterfacePathResolver;
import rocks.inspectit.shared.cs.jaxb.JAXBTransformator;

/**
 * @author Marius Oehler
 *
 */
public abstract class AbstractConfigurationInterfaceManager {

	/**
	 * {@link JAXBTransformator}.
	 */
	protected final JAXBTransformator transformator = new JAXBTransformator();

	/**
	 * Path resolver.
	 */
	@Autowired
	protected ConfigurationInterfacePathResolver pathResolver;

	/**
	 * Returns given path relative to schema part.
	 *
	 * @param path
	 *            path to relativize
	 * @return path relative to schema part
	 * @see #getSchemaPath()
	 */
	protected Path getRelativeToSchemaPath(Path path) {
		return path.relativize(pathResolver.getSchemaPath());
	}

	/**
	 * If path is a file that ends with the <i>.xml</i> extension.
	 *
	 * @param path
	 *            Path to the file.
	 * @return If path is a file that ends with the <i>.xml</i> extension.
	 */
	protected boolean isXmlFile(Path path) {
		return !Files.isDirectory(path) && path.toString().endsWith(".xml");
	}
}
