package info.novatec.inspectit.cmr.cache.indexing.restriction.impl;

import info.novatec.inspectit.cmr.cache.indexing.restriction.IIndexQueryRestriction;
import info.novatec.inspectit.cmr.cache.indexing.restriction.IIndexQueryRestrictionProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * This restriction processor caches the methods of each class that needs to be invoke. It also
 * marks in the cache all method that do not exist for specific class and an attempt to find them
 * was made.
 * 
 * @author Ivan Senic
 * 
 */
public class CachingIndexQueryRestrictionProcessor implements IIndexQueryRestrictionProcessor {

	/**
	 * Map for caching methods.
	 */
	private Map<Integer, Method> cacheMap;

	/**
	 * Marker method.
	 */
	private Method markerMethod;
	
	/**
	 * Logger for restriction processor.
	 */
	private static final Logger LOGGER = Logger.getLogger(CachingIndexQueryRestrictionProcessor.class);

	/**
	 * Default constructor. Sets {@link #markerMethod} to refer to {@link Object#toString()}.
	 */
	public CachingIndexQueryRestrictionProcessor() {
		super();
		cacheMap = new ConcurrentHashMap<Integer, Method>();
		try {
			// setting marker method to point to Object.toString()
			markerMethod = Object.class.getMethod("toString", new Class[0]);
		} catch (SecurityException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean areAllRestrictionsFulfilled(Object object, List<IIndexQueryRestriction> restrictions) {
		for (IIndexQueryRestriction indexingRestriction : restrictions) {
			int cacheKey = getMethodCacheKey(object.getClass(), indexingRestriction.getQualifiedMethodName());
			Method method = cacheMap.get(cacheKey);
			if (method == null) { // method has not yet in cache
				try {
					Method methodFromClass = object.getClass().getMethod(indexingRestriction.getQualifiedMethodName(), new Class<?>[0]);
					Object fieldValue = methodFromClass.invoke(object, new Object[0]);
					if (!indexingRestriction.isFulfilled(fieldValue)) {
						return false;
					}
				} catch (SecurityException e) {
					LOGGER.error(e.getMessage(), e);
				} catch (NoSuchMethodException e) {
					// not found, put marker method at this place in map
					cacheMap.put(cacheKey, markerMethod);
				} catch (IllegalArgumentException e) {
					LOGGER.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					LOGGER.error(e.getMessage(), e);
				} catch (InvocationTargetException e) {
					LOGGER.error(e.getMessage(), e);
				}
			} else {
				if (markerMethod.equals(method)) { // such method does not exists for this class
					break;
				} else { // method found try to invoke
					try {
						Object fieldValue = method.invoke(object, new Object[0]);
						if (!indexingRestriction.isFulfilled(fieldValue)) {
							return false;
						}
					} catch (IllegalArgumentException e) {
						LOGGER.error(e.getMessage(), e);
					} catch (IllegalAccessException e) {
						LOGGER.error(e.getMessage(), e);
					} catch (InvocationTargetException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			}
		}
		return true;
	}

	/**
	 * Returns key for the hash map based on the supplied class and method name.
	 * 
	 * @param clazz
	 *            class
	 * @param methodName
	 *            method name
	 * @return int key
	 */
	private int getMethodCacheKey(Class<?> clazz, String methodName) {
		final int prime = 31;
		int result = 0;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		return result;
	}

}