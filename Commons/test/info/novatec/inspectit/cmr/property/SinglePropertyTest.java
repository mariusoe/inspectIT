package info.novatec.inspectit.cmr.property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import info.novatec.inspectit.cmr.property.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.validation.ValidationError;
import info.novatec.inspectit.cmr.property.validation.validators.ISinglePropertyValidator;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class SinglePropertyTest {

	@Mock
	ISinglePropertyValidator<Object> validator1;

	@Mock
	ISinglePropertyValidator<Object> validator2;

	@BeforeMethod
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void singlePropertyValidation() {
		SingleProperty<Object> singleProperty = new SingleProperty<Object>() {

			@Override
			protected Object getDefaultValue() {
				return null;
			}

			@Override
			protected void setDefaultValue(Object defaultValue) {
			}

			@Override
			protected Object getUsedValue() {
				return null;
			}

			@Override
			protected void setUsedValue(Object usedValue) {
			}

			@Override
			public Object parseLiteral(String literal) {
				return null;
			}
		};

		final String validationMsg = "My validation error message";

		doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				PropertyValidation propertyValidation = (PropertyValidation) args[1];
				propertyValidation.addValidationError(new ValidationError(validationMsg));
				return null;
			}
		}).when(validator1).validate(eq(singleProperty), Mockito.<PropertyValidation> anyObject());

		singleProperty.addValidator(validator1);
		singleProperty.addValidator(validator2);
		PropertyValidation propertyValidation = singleProperty.validate();

		ArgumentCaptor<PropertyValidation> captor1 = ArgumentCaptor.forClass(PropertyValidation.class);
		verify(validator1, times(1)).validate(eq(singleProperty), captor1.capture());
		assertThat(captor1.getValue().getProperty(), is(equalTo((AbstractProperty) singleProperty)));

		ArgumentCaptor<PropertyValidation> captor2 = ArgumentCaptor.forClass(PropertyValidation.class);
		verify(validator2, times(1)).validate(eq(singleProperty), captor2.capture());
		assertThat(captor2.getValue().getProperty(), is(equalTo((AbstractProperty) singleProperty)));

		assertThat(propertyValidation.hasErrors(), is(true));
		assertThat(propertyValidation.getErrorCount(), is(1));
		assertThat(propertyValidation.getErrors(), hasSize(1));
		assertThat(propertyValidation.getErrors().iterator().next().getMessage(), is(equalTo(validationMsg)));
	}
}
