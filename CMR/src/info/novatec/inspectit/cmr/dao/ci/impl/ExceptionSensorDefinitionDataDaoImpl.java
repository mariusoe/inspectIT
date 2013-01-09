package info.novatec.inspectit.cmr.dao.ci.impl;

import info.novatec.inspectit.cmr.dao.ci.ExceptionSensorDefinitionDataDao;
import info.novatec.inspectit.communication.data.ci.ExceptionSensorDefinitionData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import org.hibernate.SessionFactory;
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
public class ExceptionSensorDefinitionDataDaoImpl extends HibernateDaoSupport implements ExceptionSensorDefinitionDataDao {

	/**
	 * This constructor is used to set the {@link SessionFactory} that is needed by
	 * {@link HibernateDaoSupport}. In a future version it may be useful to go away from the
	 * {@link HibernateDaoSupport} and directly use the {@link SessionFactory}. This is described
	 * here:
	 * http://blog.springsource.com/2007/06/26/so-should-you-still-use-springs-hibernatetemplate
	 * -andor-jpatemplate
	 * 
	 * @param sessionFactory
	 *            the session factory.
	 */
	@Autowired
	public ExceptionSensorDefinitionDataDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public long addExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData) {
		getHibernateTemplate().save(exceptionSensorDefinitionData);

		return exceptionSensorDefinitionData.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteExceptionSensorDefinition(long exceptionSensorDefinitionId) throws EntityNotFoundException {
		HibernateTemplate hibernateTemplate = getHibernateTemplate();
		Object exceptionSensorDefinition = hibernateTemplate.get(ExceptionSensorDefinitionData.class, exceptionSensorDefinitionId);

		if (null != exceptionSensorDefinition) {
			hibernateTemplate.delete(exceptionSensorDefinition);
		} else {
			throw new EntityNotFoundException("Exception Sensor Definition could be found!");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData) {
		getHibernateTemplate().update(exceptionSensorDefinitionData);
	}

}
