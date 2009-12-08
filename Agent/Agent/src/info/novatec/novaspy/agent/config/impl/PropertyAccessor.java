package info.novatec.novaspy.agent.config.impl;

import info.novatec.novaspy.agent.config.IPropertyAccessor;
import info.novatec.novaspy.agent.config.PropertyAccessException;
import info.novatec.novaspy.communication.data.ParameterContentData;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to programatically build the path to access a specific
 * method parameter or a field of a class.
 * 
 * @author Patrice Bouillet
 * @author Stefan Siegl
 * 
 */
public class PropertyAccessor implements IPropertyAccessor {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(PropertyAccessor.class.getName());

	/**
	 * An array containing the names of all methods that might be called by the
	 * PropertyAccessor. Names should not include the brackets.
	 */
	private static final String[] ALLOWED_METHODS = new String[] { "size", "length" };

	/**
	 * {@inheritDoc}
	 */
	public String getPropertyContent(PropertyPathStart propertyPathStart, Object clazz, Object[] parameters) throws PropertyAccessException {
		if (null == propertyPathStart) {
			throw new PropertyAccessException("Property path start cannot be null!");
		}

		if (propertyPathStart.isClassOfExecutedMethod()) {
			if (null == clazz) {
				throw new PropertyAccessException("Class reference cannot be null!");
			}

			return getPropertyContent(propertyPathStart.getPathToContinue(), clazz);
		} else {
			if (null == parameters) {
				throw new PropertyAccessException("Parameter array reference cannot be null!");
			}

			if (propertyPathStart.getSignaturePosition() >= parameters.length) {
				throw new PropertyAccessException("Signature position out of range!");
			}

			return getPropertyContent(propertyPathStart.getPathToContinue(), parameters[propertyPathStart.getSignaturePosition()]);
		}
	}

	/**
	 * Checks whether or not the method may be called within the parameter
	 * storage algorithm.
	 * 
	 * @param method
	 *            The method name to check for.
	 * @return <code>true</code> if the method is accepted.
	 */
	private boolean isAcceptedMethod(String method) {
		for (int i = 0; i < ALLOWED_METHODS.length; i++) {
			String allowed = ALLOWED_METHODS[i];
			if (allowed.equals(method)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Inner static recursive method to go along the given path.
	 * 
	 * @see PropertyPath
	 * 
	 * @param propertyPath
	 *            The path to follow.
	 * @param object
	 *            The object to analyze.
	 * @return The {@link String} representation of the field or parameter
	 *         followed by the path.
	 * @throws PropertyAccessException
	 *             This exception is thrown whenever something unexpectedly
	 *             happens while accessing a property.
	 */
	private String getPropertyContent(PropertyPath propertyPath, Object object) throws PropertyAccessException {
		if (null == object) {
			return "null";
		}

		if (null == propertyPath) {
			// end of the path to follow, return the String representation of
			// the object
			return object.toString();
		}

		Class c;
		if (object instanceof Class) {
			// This check is needed when a static class is passed to this
			// method.
			c = (Class) object;
		} else {
			c = object.getClass();
		}

		// We need to differ between calls of methods and the navigation of
		// properties of an object. This differentiation is integrated to
		// force the user to add () to the method to be called, thus the
		// user is aware what he is doing and no unwanted method calls are
		// performed.
		if (propertyPath.isMethodCall()) {

			// strip the "()" from the path to find the method
			String methodName = propertyPath.getName().substring(0, propertyPath.getName().length() - 2);

			// check if this method may be called
			if (!isAcceptedMethod(methodName)) {
				throw new PropertyAccessException("Method " + methodName + " MAY not be called!");
			}

			// special handling for the length method of Array objects
			// Array objects do not inherit from the static Array class, thus
			// trying to retrieve the method by reflection is not possible
			if (methodName.equals("length")) {
				if (object.getClass().isArray()) { // ensure that we are really
					// dealing with an array
					return getPropertyContent(propertyPath.getPathToContinue(), new Integer(Array.getLength(object)));
				} else {
					LOGGER.severe("Trying to access the lenght() method for a non array type");
					throw new PropertyAccessException("Trying to access the lenght() method for a non array type");
				}
			}

			do {
				// we are iterating using getDeclaredMethods as this call will
				// also provide the default access and protected methods which
				// the
				// call to getMethods() will not
				Method[] methods = c.getDeclaredMethods();
				for (int i = 0; i < methods.length; i++) {
					Method method = methods[i];
					if (methodName.equals(method.getName())) {

						// We are only calling methods that do not take an
						// argument
						if (method.getParameterTypes().length != 0) {
							LOGGER.info("Skipping matching method " + method.getName() + " as it is not a no argument method");
							continue;
						}

						try {
							Object result = method.invoke(object, null);
							return getPropertyContent(propertyPath.getPathToContinue(), result);
						} catch (IllegalArgumentException e) {
							LOGGER.severe(e.getMessage());
							throw new PropertyAccessException("Illegal Argument Exception!", e);
						} catch (IllegalAccessException e) {
							LOGGER.severe(e.getMessage());
							throw new PropertyAccessException("IllegalAccessException!", e);
						} catch (InvocationTargetException e) {
							LOGGER.severe(e.getMessage());
							throw new PropertyAccessException("InvocationTargetException!", e);
						}

					}
				}

				c = c.getSuperclass();
			} while (c != Object.class);

		} else { // We are dealing with a property navigation and not an method
			// call
			do {
				Field[] fields = c.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];
					if (propertyPath.getName().equals(field.getName())) {
						try {
							field.setAccessible(true);
							Object fieldObject = field.get(object);
							return getPropertyContent(propertyPath.getPathToContinue(), fieldObject);
						} catch (SecurityException e) {
							LOGGER.severe(e.getMessage());
							throw new PropertyAccessException("Security Exception was thrown while accessing a field!", e);
						} catch (IllegalArgumentException e) {
							LOGGER.severe(e.getMessage());
							throw new PropertyAccessException("Illegal Argument Exception!", e);
						} catch (IllegalAccessException e) {
							LOGGER.severe(e.getMessage());
							throw new PropertyAccessException("Illegal Access Exception!", e);
						}
					}
				}

				c = c.getSuperclass();
			} while (c != Object.class);
		}

		throw new PropertyAccessException("Property or method " + propertyPath.getName() + " can not be found!");
	}

	/**
	 * {@inheritDoc}
	 */
	public List getParameterContentData(List propertyAccessorList, Object clazz, Object[] parameters) {
		List parameterContentData = new ArrayList();
		for (Iterator iterator = propertyAccessorList.iterator(); iterator.hasNext();) {
			PropertyPathStart start = (PropertyPathStart) iterator.next();
			try {
				String content = this.getPropertyContent(start, clazz, parameters);
				ParameterContentData paramContentData = new ParameterContentData();
				paramContentData.setContent("'" + content);
				paramContentData.setMethodParameter(!start.isClassOfExecutedMethod());
				paramContentData.setName(start.getName());
				paramContentData.setSignaturePosition(start.getSignaturePosition());
				parameterContentData.add(paramContentData);
			} catch (PropertyAccessException e) {
				if (LOGGER.isLoggable(Level.SEVERE)) {
					LOGGER.severe("Cannot access the property: " + start + " . Will be removed from the list to prevent further errors! (" + e.getMessage() + ")");
				}
				iterator.remove();
			}
		}

		return parameterContentData;
	}

	/**
	 * Every path can have another follower path. These classes are used to
	 * describe the way to find a specific property in an object.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public static class PropertyPath {

		/**
		 * The name of this path.
		 */
		private String name;

		/**
		 * The path to continue.
		 */
		private PropertyPath pathToContinue;

		public PropertyPath() {
		}

		public PropertyPath(String name) {
			this.name = name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setPathToContinue(PropertyPath pathToContinue) {
			this.pathToContinue = pathToContinue;
		}

		public PropertyPath getPathToContinue() {
			return pathToContinue;
		}

		public boolean isMethodCall() {
			return name.endsWith("()");
		}

		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			if (null != pathToContinue) {
				return name + "-->" + pathToContinue.toString();
			} else {
				return name;
			}
		}

	}

	/**
	 * The start definition of a property accessor.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public static class PropertyPathStart extends PropertyPath {

		/**
		 * Defines if we are starting in the class where the method etc. is
		 * executed currently.
		 */
		private boolean classOfExecutedMethod = false;

		/**
		 * The position of the parameter in the signature if the
		 * <code>classOfExecutedMethod</code> value is set to <code>false</code>
		 * .
		 */
		private int signaturePosition = -1;

		public void setClassOfExecutedMethod(boolean classOfExecutedMethod) {
			this.classOfExecutedMethod = classOfExecutedMethod;
		}

		public boolean isClassOfExecutedMethod() {
			return classOfExecutedMethod;
		}

		public void setSignaturePosition(int signaturePosition) {
			this.signaturePosition = signaturePosition;
		}

		public int getSignaturePosition() {
			return signaturePosition;
		}

		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			if (null != getPathToContinue()) {
				return "[" + getName() + "] " + getPathToContinue().toString();
			} else {
				return "[" + getName() + "]";
			}
		}

	}

}
