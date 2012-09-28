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
public class PersistentObjectCloner implements IObjectCloner {

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
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public <E> E clone(E object) {
		return (E) persistentBeanManager.clone(object);
	}

}
