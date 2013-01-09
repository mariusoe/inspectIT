package info.novatec.inspectit.cmr.dao.ci.impl;

import info.novatec.inspectit.cmr.dao.ci.PlatformSensorDefinitionDataDao;
import info.novatec.inspectit.communication.data.ci.PlatformSensorDefinitionData;
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
public class PlatformSensorDefinitionDataDaoImpl extends HibernateDaoSupport implements PlatformSensorDefinitionDataDao {

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
	public PlatformSensorDefinitionDataDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public long addPlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData) {
		getHibernateTemplate().save(platformSensorDefinitionData);

		return platformSensorDefinitionData.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public void deletePlatformSensorDefinition(long platformSensorDefinitionId) throws EntityNotFoundException {
		HibernateTemplate hibernateTemplate = getHibernateTemplate();
		Object platformSensorDefinition = hibernateTemplate.get(PlatformSensorDefinitionData.class, platformSensorDefinitionId);

		if (null != platformSensorDefinition) {
			hibernateTemplate.delete(platformSensorDefinition);
		} else {
			throw new EntityNotFoundException("Platform Sensor Definition could not be found!");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void updatePlatformSensorDefinition(PlatformSensorDefinitionData platformSensorDefinitionData) {
		getHibernateTemplate().update(platformSensorDefinitionData);
	}

}
