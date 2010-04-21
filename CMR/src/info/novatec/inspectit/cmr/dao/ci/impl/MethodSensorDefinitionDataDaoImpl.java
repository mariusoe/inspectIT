package info.novatec.inspectit.cmr.dao.ci.impl;

import info.novatec.inspectit.cmr.dao.ci.MethodSensorDefinitionDataDao;
import info.novatec.inspectit.communication.data.ci.MethodSensorDefinitionData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author Matthias Huber
 *
 */
public class MethodSensorDefinitionDataDaoImpl extends HibernateDaoSupport implements MethodSensorDefinitionDataDao {

	/**
	 * @see MethodSensorDefinitionDataDao#addMethodSensorDefinition(MethodSensorDefinitionData)
	 */
	public long addMethodSensorDefinition(MethodSensorDefinitionData methodSensorDefinitionData) {
		getHibernateTemplate().save(methodSensorDefinitionData);
		
		return methodSensorDefinitionData.getId();
	}

	/**
	 * @see MethodSensorDefinitionDataDao#deleteMethodSensorDefinition(long)
	 */
	public void deleteMethodSensorDefinition(long methodSensorDefinitionId) throws EntityNotFoundException {
		HibernateTemplate hibernateTemplate = getHibernateTemplate();
		Object methodSensorDefinition = hibernateTemplate.get(MethodSensorDefinitionData.class,
				methodSensorDefinitionId);

		if (null != methodSensorDefinition) {
			hibernateTemplate.delete(methodSensorDefinition);
		} else {
			throw new EntityNotFoundException("Method Sensor Definition could not be found!");
		}
	}

	/**
	 * @see MethodSensorDefinitionDataDao#updateMethodSensorDefinition(MethodSensorDefinitionData)
	 */
	public void updateMethodSensorDefinition(MethodSensorDefinitionData methodSensorDefinitionData) {
		getHibernateTemplate().update(methodSensorDefinitionData);
	}

}
