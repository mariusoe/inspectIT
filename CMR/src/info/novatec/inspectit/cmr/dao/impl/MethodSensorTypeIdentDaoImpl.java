package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.MethodSensorTypeIdentDao;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * The default implementation of the {@link MethodSensorTypeIdentDao} interface
 * by using the {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the
 * {@link HibernateDaoSupport} class.
 * 
 * @author Patrice Bouillet
 * 
 */
public class MethodSensorTypeIdentDaoImpl extends HibernateDaoSupport implements MethodSensorTypeIdentDao {

	/**
	 * {@inheritDoc}
	 */
	public void delete(MethodSensorTypeIdent methodSensorTypeIdent) {
		getHibernateTemplate().delete(methodSensorTypeIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(List<MethodSensorTypeIdent> methodSensorTypeIdents) {
		getHibernateTemplate().deleteAll(methodSensorTypeIdents);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<MethodSensorTypeIdent> findAll() {
		return getHibernateTemplate().loadAll(MethodSensorTypeIdent.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodSensorTypeIdent load(Long id) {
		return (MethodSensorTypeIdent) getHibernateTemplate().get(MethodSensorTypeIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(MethodSensorTypeIdent methodSensorTypeIdent) {
		getHibernateTemplate().saveOrUpdate(methodSensorTypeIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<MethodSensorTypeIdent> findByExample(MethodSensorTypeIdent methodSensorTypeIdent) {
		return getHibernateTemplate().findByExample(methodSensorTypeIdent);
	}

}
