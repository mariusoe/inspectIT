package info.novatec.inspectit.cmr.storage.util;

import net.sf.gilead.configuration.ConfigurationHelper;
import net.sf.gilead.core.PersistentBeanManager;
import net.sf.gilead.core.hibernate.HibernateUtil;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Clones the persistant objects by removing the Hibernate persistant collections.
 *
 * @author Ivan Senic
 *
 */
@Component
public class PersistentObjectCloner {

	/**
	 * Persistant bean manager that does the cloning.
	 */
	private PersistentBeanManager persistentBeanManager;

	/**
	 * Default constructor. Needs hibernate {@link SessionFactory} for proper initialization.
	 *
	 * @param sessionFactory
	 *            {@link SessionFactory}.
	 */
	@Autowired
	public PersistentObjectCloner(SessionFactory sessionFactory) {
		persistentBeanManager = ConfigurationHelper.initBeanManagerForCloneOnly(new HibernateUtil(sessionFactory));
	}

	/**
	 * Clones the the given object and removes the persistent Hibernate collections.
	 *
	 * @param object
	 *            Object to clone.
	 * @param <E>
	 *            Type of the object.
	 * @return Clone.
	 */
	@SuppressWarnings("unchecked")
	public <E> E clone(E object) {
		return (E) persistentBeanManager.clone(object);
	}

}
