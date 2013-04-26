package info.novatec.inspectit.cmr.property;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class PropertyManagerTest {

	private PropertyManager propertyManager;

	@BeforeMethod
	public void init() {
		propertyManager = new PropertyManager();
	}

	/**
	 * Tests that the loading of default configuration can be executed with no exceptions.
	 */
	@Test
	public void loadDefaultConfiguration() throws JAXBException, IOException {
		propertyManager.loadDefaultConfiguration();
	}
}
