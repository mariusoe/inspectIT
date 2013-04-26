package info.novatec.inspectit.cmr.property.validation.validators.impl;

import info.novatec.inspectit.cmr.property.SingleProperty;
import info.novatec.inspectit.cmr.property.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.validation.ValidationError;
import info.novatec.inspectit.cmr.property.validation.validators.ISinglePropertyValidator;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

/**
 * Is FQN validator.
 * 
 * @author Ivan Senic
 * 
 */
@XmlRootElement(name = "isFullyQualifiedClassName")
public class FullyQualifiedClassNameValidator implements ISinglePropertyValidator<String> {

	/**
	 * {@inheritDoc}
	 */
	public void validate(SingleProperty<? extends String> property, PropertyValidation propertyValidation) {
		if (!prove(property.getValue())) {
			ValidationError validationError = new ValidationError("Value of property '" + property.getLogicalName() + "' must be a fully qualified class name");
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
	private boolean prove(String value) {
		return StringUtils.isNotEmpty(value) && value.matches("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*");
	}
}
