package info.novatec.inspectit.cmr.property.validation.validators.impl;

import info.novatec.inspectit.cmr.property.SingleProperty;
import info.novatec.inspectit.cmr.property.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.validation.ValidationError;
import info.novatec.inspectit.cmr.property.validation.validators.ISinglePropertyValidator;

import javax.xml.bind.annotation.XmlRootElement;

import org.hamcrest.Matchers;

/**
 * Is negative validator.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 */
@XmlRootElement(name = "isNegative")
public class NegativeValidator<T extends Number> implements ISinglePropertyValidator<T> {

	/**
	 * {@inheritDoc}
	 */
	public void validate(SingleProperty<? extends T> property, PropertyValidation propertyValidation) {
		if (!prove(property.getValue())) {
			ValidationError validationError = new ValidationError("Value of property '" + property.getLogicalName() + "' must be negative");
			propertyValidation.addValidationError(validationError);
		}
	}

	/**
	 * Executes prove.
	 * 
	 * @param value
	 *            Value.
	 * @return Returns true if prove was valid.
	 */
	private boolean prove(T value) {
		return Matchers.lessThan(0d).matches(value.doubleValue());
	}
}
