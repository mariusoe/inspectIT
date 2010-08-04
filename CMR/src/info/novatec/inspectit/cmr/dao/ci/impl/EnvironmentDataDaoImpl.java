package info.novatec.inspectit.cmr.dao.ci.impl;

import info.novatec.inspectit.cmr.dao.ci.EnvironmentDataDao;
import info.novatec.inspectit.communication.data.ci.EnvironmentData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author Matthias Huber
 * 
 */
public class EnvironmentDataDaoImpl extends HibernateDaoSupport implements EnvironmentDataDao {

	/**
	 * @see EnvironmentDataDao#addEnvironment(EnvironmentData)
	 */
	public long addEnvironment(EnvironmentData environmentData) {
		getHibernateTemplate().save(environmentData);

		return environmentData.getId();
	}

	/**
	 * @see EnvironmentDataDao#deleteEnvironment(long)
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
	 * @see EnvironmentDataDao#getEnvironments()
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
	 * @see EnvironmentDataDao#updateEnvironmentSettings(EnvironmentData)
	 */
	public void updateEnvironmentSettings(EnvironmentData environmentData) {
		getHibernateTemplate().update(environmentData);
	}

}
