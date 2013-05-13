package info.novatec.inspectit.cmr.property.configuration.validator;

import info.novatec.inspectit.cmr.property.configuration.GroupedProperty;
import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.configuration.validation.ValidationError;

import java.util.ArrayList;
import java.util.Collections;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract validator for all comparing validators.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractComparingValidator<T> implements IGroupedProperyValidator, ISinglePropertyValidator<T> {

	/**
	 * Name of the property to be tested. Can be <code>null</code> in case of
	 * {@link ISinglePropertyValidator} validator.
	 */
	@XmlAttribute(name = "property", required = false)
	private String property;

	/**
	 * What to compare the property to. It can be logical name of the property in case of group
	 * validation or literal in case of single validation.
	 */
	@XmlAttribute(name = "than", required = true)
	private String than;

	/**
	 * Compares property against another property. If comparing proves to be wrong the implementing
	 * class is responsible of adding the correct {@link ValidationError} to the
	 * {@link PropertyValidation}.
	 * 
	 * @param property
	 *            {@link SingleProperty}
	 * @param against
	 *            {@link SingleProperty} to compare against.
	 * @param propertyValidation
	 *            {@link PropertyValidation}
	 */
	protected abstract void compare(SingleProperty<? extends T> property, SingleProperty<? extends T> against, PropertyValidation propertyValidation);

	/**
	 * Compares property against value. If comparing proves to be wrong the implementing class is
	 * responsible of adding the correct {@link ValidationError} to the {@link PropertyValidation}.
	 * 
	 * @param property
	 *            {@link SingleProperty}
	 * @param against
	 *            Value to compare against.
	 * @param propertyValidation
	 *            {@link PropertyValidation}
	 */
	protected abstract void compare(SingleProperty<? extends T> property, T against, PropertyValidation propertyValidation);


	/**
	 * {@inheritDoc}
	 */
	public void validate(SingleProperty<? extends T> property, PropertyValidation propertyValidation) {
		T against = property.parseLiteral(than);

		if (null == against) {
			ValidationError validationError = new ValidationError(Collections.<SingleProperty<?>> singletonList(property), "Validation of  property " + property.getName()
					+ " failed because literal (" + than + ") to compare against can not be parsed.");
			propertyValidation.addValidationError(validationError);
			return;
		}

		compare(property, against, propertyValidation);
	}

	/**
	 * {@inheritDoc}
	 */
	public void validateForValue(SingleProperty<? extends T> property, PropertyValidation propertyValidation, T value) {
		compare(property, value, propertyValidation);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void validate(GroupedProperty groupProperty, PropertyValidation propertyValidation) {
		if (StringUtils.isEmpty(property)) {
			ValidationError validationError = new ValidationError(new ArrayList<SingleProperty<?>>(groupProperty.getSingleProperties()), "Validation of grouped property " + groupProperty.getName()
					+ " failed because property logical name is not set.");
			propertyValidation.addValidationError(validationError);
			return;
		}

		if (StringUtils.isEmpty(than)) {
			ValidationError validationError = new ValidationError(new ArrayList<SingleProperty<?>>(groupProperty.getSingleProperties()), "Validation of grouped property " + groupProperty.getName()
					+ " failed because logical name of the property to compare against is not set.");
			propertyValidation.addValidationError(validationError);
			return;
		}

		SingleProperty<?> compare = groupProperty.forLogicalname(property);
		SingleProperty<?> against = groupProperty.forLogicalname(than);

		if (null == compare) {
			ValidationError validationError = new ValidationError(new ArrayList<SingleProperty<?>>(groupProperty.getSingleProperties()), "Validation of grouped property " + groupProperty.getName()
					+ " failed because property with logical name '" + property
					+ "' does not exist.");
			propertyValidation.addValidationError(validationError);
			return;
		}

		if (null == against) {
			ValidationError validationError = new ValidationError(new ArrayList<SingleProperty<?>>(groupProperty.getSingleProperties()), "Validation of grouped property " + groupProperty.getName()
					+ " failed because property with logical name '" + than
					+ "' does not exist.");
			propertyValidation.addValidationError(validationError);
			return;
		}

		try {
			compare((SingleProperty<T>) compare, (SingleProperty<T>) against, propertyValidation);
		} catch (Exception e) {
			ValidationError validationError = new ValidationError(new ArrayList<SingleProperty<?>>(groupProperty.getSingleProperties()), "Validation of grouped property " + groupProperty.getName()
					+ " failed because exception occurred during validation. Exception message: " + e.getMessage());
			propertyValidation.addValidationError(validationError);
		}
	}

	/**
	 * Gets {@link #property}.
	 *   
	 * @return {@link #property}  
	 */
	public String getProperty() {
		return property;
	}

	/**  
	 * Sets {@link #property}.  
	 *   
	 * @param property  
	 *            New value for {@link #property}  
	 */
	public void setProperty(String property) {
		this.property = property;
	}

	/**
	 * Gets {@link #than}.
	 *   
	 * @return {@link #than}  
	 */
	public String getThan() {
		return than;
	}

	/**  
	 * Sets {@link #than}.  
	 *   
	 * @param than  
	 *            New value for {@link #than}  
	 */
	public void setThan(String than) {
		this.than = than;
	}

}