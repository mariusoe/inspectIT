package info.novatec.inspectit.cmr.util;

import info.novatec.inspectit.util.IHibernateUtil;

import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
import org.springframework.stereotype.Component;

/**
 * Our own Hibernate utility class.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class HibernateUtil implements IHibernateUtil {

	/**
	 * {@inheritDoc}
	 */
	public boolean isInitialized(Object proxy) {
		return Hibernate.isInitialized(proxy);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPersistentCollection(Class<?> collectionClass) {
		return PersistentCollection.class.isAssignableFrom(collectionClass);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPersistentMap(Class<?> collectionClass) {
		return PersistentMap.class.isAssignableFrom(collectionClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPersistentList(Class<?> collectionClass) {
		return PersistentList.class.isAssignableFrom(collectionClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPersistentSet(Class<?> collectionClass) {
		return PersistentSet.class.isAssignableFrom(collectionClass);
	}

}
