package info.novatec.inspectit.cmr.dao.ci.impl;

import info.novatec.inspectit.cmr.dao.ci.PlatformSensorDefinitionDataDao;
import info.novatec.inspectit.communication.data.ci.PlatformSensorDefinitionData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author Matthias Huber
 *
 */
public class PlatformSensorDefinitionDataDaoImpl extends HibernateDaoSupport implements PlatformSensorDefinitionDataDao {

	/**
	 * @see PlatformSensorDefinitionDataDao#addPlatformSensorDefinition(PlatformSensorDefinitionData)
	 */
	public long addPlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData) {
		getHibernateTemplate().save(platformSensorDefinitionData);

		return platformSensorDefinitionData.getId();
	}

	/**
	 * @see PlatformSensorDefinitionDataDao#deletePlatformSensorDefinition(long)
	 */
	public void deletePlatformSensorDefinition(long platformSensorDefinitionId) throws EntityNotFoundException {
		HibernateTemplate hibernateTemplate = getHibernateTemplate();
		Object platformSensorDefinition = hibernateTemplate.get(PlatformSensorDefinitionData.class,
				platformSensorDefinitionId);

		if (null != platformSensorDefinition) {
			hibernateTemplate.delete(platformSensorDefinition);
		} else {
			throw new EntityNotFoundException("Platform Sensor Definition could not be found!");
		}
	}

	/**
	 * @see PlatformSensorDefinitionDataDao#updatePlatformSensorDefinition(PlatformSensorDefinitionData)
	 */
	public void updatePlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData) {
		getHibernateTemplate().update(platformSensorDefinitionData);
	}

}
