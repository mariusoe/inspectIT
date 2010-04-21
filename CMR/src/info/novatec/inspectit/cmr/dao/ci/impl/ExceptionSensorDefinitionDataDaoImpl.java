package info.novatec.inspectit.cmr.dao.ci.impl;

import info.novatec.inspectit.cmr.dao.ci.ExceptionSensorDefinitionDataDao;
import info.novatec.inspectit.communication.data.ci.ExceptionSensorDefinitionData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author Matthias Huber
 *
 */
public class ExceptionSensorDefinitionDataDaoImpl extends HibernateDaoSupport implements ExceptionSensorDefinitionDataDao {

	/**
	 * @see ExceptionSensorDefinitionDataDao#addExceptionSensorDefinition(ExceptionSensorDefinitionData)
	 */
	public long addExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData) {
		getHibernateTemplate().save(exceptionSensorDefinitionData);
		
		return exceptionSensorDefinitionData.getId();
	}

	/**
	 * @see ExceptionSensorDefinitionDataDao#deleteExceptionSensorDefinition(long)
	 */
	public void deleteExceptionSensorDefinition(long exceptionSensorDefinitionId) throws EntityNotFoundException {
		HibernateTemplate hibernateTemplate = getHibernateTemplate();
		Object exceptionSensorDefinition = hibernateTemplate.get(ExceptionSensorDefinitionData.class,
				exceptionSensorDefinitionId);

		if (null != exceptionSensorDefinition) {
			hibernateTemplate.delete(exceptionSensorDefinition);
		} else {
			throw new EntityNotFoundException("Exception Sensor Definition could be found!");
		}
	}

	/**
	 * @see ExceptionSensorDefinitionDataDao#updateExceptionSensorDefinition(ExceptionSensorDefinitionData)
	 */
	public void updateExceptionSensorDefinition(ExceptionSensorDefinitionData exceptionSensorDefinitionData) {
		getHibernateTemplate().update(exceptionSensorDefinitionData);
	}

}
