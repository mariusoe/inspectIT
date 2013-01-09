package info.novatec.inspectit.cmr.dao.ci.impl;

import info.novatec.inspectit.cmr.dao.ci.EnvironmentDataDao;
import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Matthias Huber
 * 
 */
@Repository
public class EnvironmentDataDaoImpl extends HibernateDaoSupport implements EnvironmentDataDao {

	/**
	 * This constructor is used to set the {@link SessionFactory} that is needed by
	 * {@link HibernateDaoSupport}. In a future version it may be useful to go away from the
	 * {@link HibernateDaoSupport} and directly use the {@link SessionFactory}. This is described
	 * here:
	 * http://blog.springsource.com/2007/06/26/so-should-you-still-use-springs-hibernatetemplate
	 * -andor-jpatemplate
	 * 
	 * @param sessionFactory
	 *            the hibernate session factory.
	 */
	@Autowired
	public EnvironmentDataDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public long addEnvironment(EnvironmentData environmentData) {
		getHibernateTemplate().save(environmentData);

		return environmentData.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteEnvironment(long environmentId) throws EntityNotFoundException {
		HibernateTemplate hibernateTemplate = getHibernateTemplate();
		Object environment = hibernateTemplate.get(EnvironmentData.class, environmentId);

		if (null != environment) {
			hibernateTemplate.delete(environment);
		} else {
			throw new EntityNotFoundException("Environment could not be found!");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<EnvironmentData> getEnvironments() {
		DetachedCriteria environmentDataCriteria = DetachedCriteria.forClass(EnvironmentData.class);
		environmentDataCriteria.setFetchMode("profiles", FetchMode.JOIN);
		environmentDataCriteria.setFetchMode("sensorTypes", FetchMode.JOIN);
		environmentDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		List<EnvironmentData> result = getHibernateTemplate().findByCriteria(environmentDataCriteria);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateEnvironmentSettings(EnvironmentData environmentData) {
		getHibernateTemplate().update(environmentData);
	}

}
