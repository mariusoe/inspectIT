package info.novatec.inspectit.storage.serializer.schema;

import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests if the schemas for serialization are up-to-date. Any change of the domain classes, needs to
 * be reflected in the schema. Thus, this test should prove that all schemas are correct.
 *
 * @author Ivan Senic
 *
 */
public class SchemaTest {

	/**
	 * Schema manager.
	 */
	private ClassSchemaManager schemaManager;

	/**
	 * Initializes the {@link ClassSchemaManager}.
	 *
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@BeforeClass
	public void init() throws IOException {
		schemaManager = SchemaManagerTestProvider.getClassSchemaManagerForTests();
	}

	/**
	 * Tests schemas for correctness.
	 *
	 * @param className
	 *            Name of the class to test.
	 * @param schema
	 *            {@link ClassSchema} for given class.
	 *
	 * @throws ClassNotFoundException
	 *             If class loading fails.
	 */
	@Test(dataProvider = "schemaProvider")
	public void checkClassFieldsWithSchema(String className, ClassSchema schema) throws ClassNotFoundException {
		// assert that schema gotten by the .getSchema is same
		Assert.assertEquals(schemaManager.getSchema(className), schema);

		Class<?> clazz = Class.forName(className);
		Set<Integer> markerSet = new HashSet<Integer>();
		while (!clazz.equals(Object.class)) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
					Integer marker = schema.getFieldMarker(field.getName());
					// assert that the field will be in schema and that it has different marker
					// than other fields
					Assert.assertNotNull(marker, "Field " + field.getName() + " of class " + className + " is not available in class schema.");
					Assert.assertTrue(markerSet.add(marker), "Same marker exists for two different fields in class " + className + ". Duplicated number for field " + field.getName());
				}
			}
			clazz = clazz.getSuperclass();
		}
	}

	/**
	 * @return Schemas to test.
	 */
	@DataProvider(name = "schemaProvider")
	public Object[][] getSchemas() {
		Map<String, ClassSchema> schemasMap = schemaManager.getSchemaMap();
		Object[][] data = new Object[schemasMap.size() - 1][2];
		int i = 0;
		for (Map.Entry<String, ClassSchema> entry : schemasMap.entrySet()) {
			if (entry.getKey().equals(InvocationSequenceData.class.getName())) {
				// do not include invocations in this test
				continue;
			}
			data[i][0] = entry.getKey();
			data[i][1] = entry.getValue();
			i++;
		}
		return data;
	}

	/**
	 * Additional test for the invocation because the parent field is ommited during serialization,
	 * thus we can not test it as other classes.
	 */
	@Test
	public void checkInvocationSequenceDataSchema() {
		ClassSchema schema = schemaManager.getSchema(InvocationSequenceData.class.getName());
		Class<?> clazz = InvocationSequenceData.class;
		Set<Integer> markerSet = new HashSet<Integer>();
		while (!clazz.equals(Object.class)) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (!field.getName().equals("parentSequence") && !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
					Integer marker = schema.getFieldMarker(field.getName());
					// assert that the field will be in schema and that it has different marker
					// than other fields
					Assert.assertNotNull(marker, "Field " + field.getName() + " of class " + clazz.getName() + " is not available in class schema.");
					Assert.assertTrue(markerSet.add(marker), "Same marker exists for two different fields in class " + schema + ". Duplicated number for field " + field.getName());
				}
			}
			clazz = clazz.getSuperclass();
		}
	}

}
