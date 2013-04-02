package info.novatec.inspectit.storage.serializer.schema;

import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;

import java.io.IOException;

import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * A test utility class that provides instances of {@link ClassSchemaManager} for testing purposes.
 * 
 * @author Ivan Senic
 * 
 */
public final class SchemaManagerTestProvider {

	/**
	 * Private constructor.
	 */
	private SchemaManagerTestProvider() {
	}

	/**
	 * Returns properly instantiated {@link ClassSchemaManager} that can be used in tests.
	 * 
	 * @return Returns properly instantiated {@link ClassSchemaManager} that can be used in tests.
	 * @throws IOException
	 *             If {@link IOException} occcurs.
	 */
	public static ClassSchemaManager getClassSchemaManagerForTests() throws IOException {
		ClassSchemaManager schemaManager = new ClassSchemaManager();
		schemaManager.log = LogFactory.getLog(ClassSchemaManager.class);
		schemaManager.setSchemaListFile(new ClassPathResource(ClassSchemaManager.SCHEMA_DIR + "/" + ClassSchemaManager.SCHEMA_LIST_FILE, schemaManager.getClass().getClassLoader()));
		schemaManager.loadSchemasFromLocations();
		return schemaManager;
	}
}
