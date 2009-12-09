package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.PlatformIdentDao;
import info.novatec.inspectit.cmr.model.PlatformIdent;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The default implementation of the {@link PlatformIdentDao} interface by using
 * the {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the
 * {@link HibernateDaoSupport} class.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PlatformIdentDaoImpl extends HibernateDaoSupport implements PlatformIdentDao {

	/**
	 * {@inheritDoc}
	 */
	public void delete(PlatformIdent platformIdent) {
		getHibernateTemplate().delete(platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(List<PlatformIdent> platformIdents) {
		getHibernateTemplate().deleteAll(platformIdents);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<PlatformIdent> findAll() {
		return getHibernateTemplate().loadAll(PlatformIdent.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<PlatformIdent> findByExample(PlatformIdent platformIdent) {
		return getHibernateTemplate().findByExample(platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public PlatformIdent load(Long id) {
		return (PlatformIdent) getHibernateTemplate().get(PlatformIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(PlatformIdent platformIdent) {
		getHibernateTemplate().saveOrUpdate(platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void evict(PlatformIdent platformIdent) {
		getHibernateTemplate().evict(platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void evictAll(List<PlatformIdent> platformIdents) {
		for (PlatformIdent platformIdent : platformIdents) {
			this.evict(platformIdent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PlatformIdent> findAllInitialized() {
		DetachedCriteria criteria = DetachedCriteria.forClass(PlatformIdent.class);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.setFetchMode("methodIdents", FetchMode.JOIN);
		criteria.setFetchMode("methodIdents.methodSensorTypeIdents", FetchMode.JOIN);
		criteria.setFetchMode("sensorTypeIdents", FetchMode.JOIN);
		criteria.setFetchMode("sensorTypeIdents.platformIdents", FetchMode.JOIN);

		@SuppressWarnings("unchecked")
		List<PlatformIdent> platformIdents = getHibernateTemplate().findByCriteria(criteria);
		return platformIdents;
	}

}
