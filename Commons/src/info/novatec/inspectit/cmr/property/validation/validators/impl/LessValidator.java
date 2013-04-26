package info.novatec.inspectit.cmr.property.validation.validators.impl;

import info.novatec.inspectit.cmr.property.SingleProperty;
import info.novatec.inspectit.cmr.property.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.validation.ValidationError;
import info.novatec.inspectit.cmr.property.validation.validators.AbstractComparingValidator;

import javax.xml.bind.annotation.XmlRootElement;

import org.hamcrest.Matchers;

/**
 * Is less than validator.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 *            Type of values to compare.
 */
@XmlRootElement(name = "isLess")
public class LessValidator<T extends Number> extends AbstractComparingValidator<T> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compare(SingleProperty<? extends T> property, SingleProperty<? extends T> against, PropertyValidation propertyValidation) {
		if (!matches(property.getValue(), against.getValue())) {
			ValidationError validationError = new ValidationError("Value of property '" + property.getLogicalName() + "' must be less than value of property '" + against.getLogicalName() + "'");
			propertyValidation.addValidationError(validationError);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compare(SingleProperty<? extends T> property, T against, PropertyValidation propertyValidation) {
		if (!matches(property.getValue(), against)) {
			ValidationError validationError = new ValidationError("Value of property '" + property.getLogicalName() + "' must be less than " + against);
			propertyValidation.addValidationError(validationError);
		}
	}

	/**
	 * Executes compare.
	 * 
	 * @param value
	 *            Value.
	 * @param against
	 *            Value to compare against.
	 * @return Returns true if match was valid.
	 */
	private boolean matches(T value, T against) {
		return Matchers.lessThan(against.doubleValue()).matches(value.doubleValue());
	}

}
