package info.novatec.inspectit.cmr.property.validation.validators;

import info.novatec.inspectit.cmr.property.SingleProperty;
import info.novatec.inspectit.cmr.property.validation.PropertyValidation;

/**
 * Validator interface working on the {@link SingleProperty}.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 *            Type of the value validator can validate on.
 */
public interface ISinglePropertyValidator<T> {

	/**
	 * Performs validation of the property and adds any found error to the
	 * {@link PropertyValidation}.
	 * 
	 * @param property
	 *            {@link SingleProperty} to validate.
	 * @param propertyValidation
	 *            {@link PropertyValidation} to report errors to.
	 */
	void validate(SingleProperty<? extends T> property, PropertyValidation propertyValidation);
}
