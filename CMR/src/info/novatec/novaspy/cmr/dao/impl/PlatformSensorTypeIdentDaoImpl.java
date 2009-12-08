package info.novatec.novaspy.cmr.dao.impl;

import info.novatec.novaspy.cmr.dao.PlatformSensorTypeIdentDao;
import info.novatec.novaspy.cmr.model.PlatformSensorTypeIdent;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * The default implementation of the {@link PlatformSensorTypeIdentDao}
 * interface by using the {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the
 * {@link HibernateDaoSupport} class.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PlatformSensorTypeIdentDaoImpl extends HibernateDaoSupport implements PlatformSensorTypeIdentDao {

	/**
	 * {@inheritDoc}
	 */
	public void delete(PlatformSensorTypeIdent platformSensorTypeIdent) {
		getHibernateTemplate().delete(platformSensorTypeIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(List<PlatformSensorTypeIdent> platformSensorTypeIdents) {
		getHibernateTemplate().deleteAll(platformSensorTypeIdents);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<PlatformSensorTypeIdent> findAll() {
		return getHibernateTemplate().loadAll(PlatformSensorTypeIdent.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public PlatformSensorTypeIdent load(Long id) {
		return (PlatformSensorTypeIdent) getHibernateTemplate().get(PlatformSensorTypeIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(PlatformSensorTypeIdent platformSensorTypeIdent) {
		getHibernateTemplate().saveOrUpdate(platformSensorTypeIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<PlatformSensorTypeIdent> findByExample(PlatformSensorTypeIdent platformSensorTypeIdent) {
		return getHibernateTemplate().findByExample(platformSensorTypeIdent);
	}

}
