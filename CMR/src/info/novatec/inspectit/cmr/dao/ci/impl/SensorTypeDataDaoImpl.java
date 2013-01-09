package info.novatec.inspectit.cmr.dao.ci.impl;

import info.novatec.inspectit.cmr.dao.ci.SensorTypeDataDao;
import info.novatec.inspectit.communication.data.ci.SensorTypeData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Matthias Huber
 */
@Repository
public class SensorTypeDataDaoImpl extends HibernateDaoSupport implements SensorTypeDataDao {

	/**
	 * This constructor is used to set the {@link SessionFactory} that is needed by
	 * {@link HibernateDaoSupport}. In a future version it may be useful to go away from the
	 * {@link HibernateDaoSupport} and directly use the {@link SessionFactory}. This is described
	 * here:
	 * http://blog.springsource.com/2007/06/26/so-should-you-still-use-springs-hibernatetemplate
	 * -andor-jpatemplate
	 * 
	 * @param sessionFactory
	 *            the Hibernate Session Factory.
	 */
	@Autowired
	public SensorTypeDataDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public long addSensorType(SensorTypeData sensorTypeData) {
		getHibernateTemplate().save(sensorTypeData);

		return sensorTypeData.getId();
	}

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
	 */
	public void updateSensorType(SensorTypeData sensorTypeData) {
		getHibernateTemplate().update(sensorTypeData);
	}

}
