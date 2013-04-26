package info.novatec.inspectit.cmr.property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.cmr.property.configuration.Configuration;
import info.novatec.inspectit.cmr.property.impl.LongProperty;
import info.novatec.inspectit.cmr.property.impl.StringProperty;
import info.novatec.inspectit.cmr.property.section.PropertySection;
import info.novatec.inspectit.cmr.property.validation.validators.impl.LessValidator;
import info.novatec.inspectit.cmr.property.validation.validators.impl.NotEmptyValidator;
import info.novatec.inspectit.cmr.property.validation.validators.impl.PositiveValidator;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class XmlTransformationTest {

	private File f = new File("test.xml");;

	@Test
	public void marshalTest() throws JAXBException {
		Configuration configuration = new Configuration();

		PropertySection section = new PropertySection("MySection");
		configuration.addSection(section);

		SingleProperty<String> property1 = new StringProperty("title", "Define title", "properties.title", "Lorem ipsum", true, false);
		property1.addValidator(new NotEmptyValidator<String>());
		section.addProperty(property1);

		SingleProperty<Long> property2 = new LongProperty("speed", "Define speed", "properties.speed", 10L, true, true);
		property2.addValidator(new PositiveValidator<Long>());
		section.addProperty(property2);

		GroupedProperty groupedProperty = new GroupedProperty("myGroup", "Lets show how can you group properties");
		SingleProperty<Long> property3 = new LongProperty("Max rotation", "Define max rotation", "properties.rotation.max", 90L, false, false);
		groupedProperty.addSingleProperty(property3);
		SingleProperty<Long> property4 = new LongProperty("Min rotation", "Define min rotation", "properties.rotation.min", 0L, false, false);
		groupedProperty.addSingleProperty(property4);
		LessValidator<Long> lessValidator = new LessValidator<Long>();
		lessValidator.setProperty("properties.rotation.min");
		lessValidator.setThan("properties.rotation.max");
		groupedProperty.addValidator(lessValidator);

		section.addProperty(groupedProperty);

		JAXBContext context = JAXBContext.newInstance(Configuration.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshaller.marshal(configuration, System.out);
		marshaller.marshal(configuration, f);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(Configuration.class)));
		assertThat((Configuration) object, is(equalTo(configuration)));
	}

	@BeforeTest
	@AfterTest
	public void deleteFile() {
		if (f.exists()) {
			f.delete();
		}
	}
}
