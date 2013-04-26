package info.novatec.inspectit.cmr.property.validation.validators.impl;

import info.novatec.inspectit.cmr.property.SingleProperty;
import info.novatec.inspectit.cmr.property.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.validation.ValidationError;
import info.novatec.inspectit.cmr.property.validation.validators.ISinglePropertyValidator;

import java.util.Collection;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Is not empty validator. Works on strings, collections and maps.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 */
@XmlRootElement(name = "isNotEmpty")
public class NotEmptyValidator<T extends Object> implements ISinglePropertyValidator<T> {

	/**
	 * {@inheritDoc}
	 */
	public void validate(SingleProperty<? extends T> property, PropertyValidation propertyValidation) {
		if (!prove(property.getValue())) {
			ValidationError validationError = new ValidationError("Value of property '" + property.getLogicalName() + "' must not be empty String/Collection/Map");
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
		if (value instanceof String) {
			return StringUtils.isNotEmpty((String) value);
		} else if (value instanceof Collection) {
			return CollectionUtils.isNotEmpty((Collection<?>) value);
		} else if (value instanceof Map) {
			return MapUtils.isNotEmpty((Map<?, ?>) value);
		} else {
			throw new RuntimeException("The isNotEmpty Validator not used with String, Collection or Map object. Passed object is " + value);
		}
	}
}
