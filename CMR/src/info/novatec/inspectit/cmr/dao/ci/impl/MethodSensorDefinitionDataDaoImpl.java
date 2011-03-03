package info.novatec.inspectit.cmr.dao.ci.impl;

import info.novatec.inspectit.cmr.dao.ci.MethodSensorDefinitionDataDao;
import info.novatec.inspectit.communication.data.ci.MethodSensorDefinitionData;
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
public class MethodSensorDefinitionDataDaoImpl extends HibernateDaoSupport implements MethodSensorDefinitionDataDao {

	/**
	 * This constructor is used to set the {@link SessionFactory} that is needed by
	 * {@link HibernateDaoSupport}. In a future version it may be useful to go away from the
	 * {@link HibernateDaoSupport} and directly use the {@link SessionFactory}. This is described
	 * here:
	 * http://blog.springsource.com/2007/06/26/so-should-you-still-use-springs-hibernatetemplate
	 * -andor-jpatemplate
	 * 
	 * @param sessionFactory
	 */
	@Autowired
	public MethodSensorDefinitionDataDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
	
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
