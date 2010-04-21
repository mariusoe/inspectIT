package info.novatec.inspectit.cmr.dao.ci.impl;

import info.novatec.inspectit.cmr.dao.ci.SensorTypeDataDao;
import info.novatec.inspectit.communication.data.ci.SensorTypeData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author Matthias Huber
 *
 */
public class SensorTypeDataDaoImpl extends HibernateDaoSupport implements SensorTypeDataDao {

	/**
	 * @see SensorTypeDataDao#addSensorType(SensorTypeData)
	 */
	public long addSensorType(SensorTypeData sensorTypeData) {
		getHibernateTemplate().save(sensorTypeData);
		
		return sensorTypeData.getId();
	}

	/**
	 * @see SensorTypeDataDao#deleteSensorType(long)
	 */
	public void deleteSensorType(long sensorTypeId) throws EntityNotFoundException {
		HibernateTemplate hibernateTemplate = getHibernateTemplate();
		Object sensorType = hibernateTemplate.get(SensorTypeData.class, sensorTypeId);

		if (null != sensorType) {
			hibernateTemplate.delete(sensorType);
		} else {
			throw new EntityNotFoundException("Sensor Type could not be found!");
		}
	}

	/**
	 * @see SensorTypeDataDao#updateSensorTpe(SensorTypeData)
	 */
	public void updateSensorType(SensorTypeData sensorTypeData) {
		getHibernateTemplate().update(sensorTypeData);
	}

}
