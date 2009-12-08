package info.novatec.novaspy.agent.config;

import info.novatec.novaspy.agent.config.impl.PropertyAccessor.PropertyPath;
import info.novatec.novaspy.agent.config.impl.PropertyAccessor.PropertyPathStart;
import info.novatec.novaspy.communication.data.ParameterContentData;

import java.util.List;

/**
 * This interface defines methods to access the contents of the fields and
 * method parameters of classes.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IPropertyAccessor {

	/**
	 * Returns the content of the property. Either a field of a class will be
	 * accessed or a method parameter.
	 * 
	 * @see PropertyPathStart
	 * @see PropertyPath
	 * 
	 * @param propertyPathStart
	 *            This parameter defines the start of the path.
	 * @param clazz
	 *            The current instance or class object of the executed method.
	 * @param parameters
	 *            The method parameters (can be <code>null</code>).
	 * @return The {@link String} representation of the field or parameter
	 *         followed by the path.
	 * @throws PropertyAccessException
	 *             This exception is thrown whenever something unexpectedly
	 *             happens while accessing a property.
	 */
	String getPropertyContent(PropertyPathStart propertyPathStart, Object clazz, Object[] parameters) throws PropertyAccessException;

	/**
	 * Converts the list of property accessors {@link PropertyPathStart} into a
	 * list of {@link ParameterContentData}.
	 * 
	 * @param propertyAccessorList
	 *            The list of property accessors.
	 * @param clazz
	 *            The class object.
	 * @param parameters
	 *            The parameters.
	 * @return The list of {@link ParameterContentData}.
	 */
	List getParameterContentData(List propertyAccessorList, Object clazz, Object[] parameters);

}