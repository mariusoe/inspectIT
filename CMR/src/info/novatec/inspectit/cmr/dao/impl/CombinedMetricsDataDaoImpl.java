package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.CombinedMetricsDataDao;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
@Repository
public class CombinedMetricsDataDaoImpl extends HibernateDaoSupport implements CombinedMetricsDataDao {

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
	public CombinedMetricsDataDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<TimerData> getCombinedMetrics(TimerData template) {
		DetachedCriteria timerDataCriteria = DetachedCriteria.forClass(template.getClass());
		timerDataCriteria.add(Restrictions.gt("id", template.getId()));
		timerDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		timerDataCriteria.add(Restrictions.eq("sensorTypeIdent", template.getSensorTypeIdent()));
		timerDataCriteria.add(Restrictions.eq("methodIdent", template.getMethodIdent()));
		timerDataCriteria.setFetchMode("parameterContentData", FetchMode.JOIN);
		timerDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(timerDataCriteria);
	}

	@SuppressWarnings("unchecked")
	public List<TimerData> getCombinedMetricsIgnoreMethodId(TimerData template) {
		DetachedCriteria timerDataCriteria = DetachedCriteria.forClass(template.getClass());
		timerDataCriteria.add(Restrictions.gt("id", template.getId()));
		timerDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		timerDataCriteria.add(Restrictions.eq("sensorTypeIdent", template.getSensorTypeIdent()));
		timerDataCriteria.setFetchMode("parameterContentData", FetchMode.JOIN);
		timerDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(timerDataCriteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<TimerData> getCombinedMetricsFromToDate(TimerData template, String workflowName, String activityName, Date fromDate, Date toDate) {

		DetachedCriteria findPCDByWorkflow = DetachedCriteria.forClass(ParameterContentData.class);
		findPCDByWorkflow.setProjection(Projections.property("methodSensorId"));
		findPCDByWorkflow.add(Restrictions.eq("name", "Workflow"));
		findPCDByWorkflow.add(Restrictions.eq("content", "'" + workflowName));

		DetachedCriteria findActivityByWorkflow = DetachedCriteria.forClass(ParameterContentData.class);
		findActivityByWorkflow.setProjection(Projections.property("methodSensorId"));
		findActivityByWorkflow.add(Property.forName("methodSensorId").in(findPCDByWorkflow));
		findActivityByWorkflow.add(Restrictions.eq("name", "Activity"));
		findActivityByWorkflow.add(Restrictions.eq("content", activityName));

		DetachedCriteria timerDataCriteria = DetachedCriteria.forClass(template.getClass());
		timerDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		timerDataCriteria.add(Restrictions.eq("sensorTypeIdent", template.getSensorTypeIdent()));
		timerDataCriteria.add(Restrictions.between("timeStamp", new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime())));
		timerDataCriteria.add(Property.forName("id").in(findActivityByWorkflow));
		timerDataCriteria.setFetchMode("parameterContentData", FetchMode.JOIN);
		timerDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		List<TimerData> result = getHibernateTemplate().findByCriteria(timerDataCriteria);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ParameterContentData> getWorkflows(TimerData template) {
		DetachedCriteria subselect = DetachedCriteria.forClass(TimerData.class);
		subselect.setProjection(Projections.property("id"));
		subselect.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		subselect.add(Restrictions.eq("sensorTypeIdent", template.getSensorTypeIdent()));

		DetachedCriteria mainQuery = DetachedCriteria.forClass(ParameterContentData.class);
		mainQuery.add(org.hibernate.criterion.Property.forName("methodSensorId").in(subselect));
		mainQuery.add(Restrictions.eq("name", "Workflow"));

		List<ParameterContentData> result = getHibernateTemplate().findByCriteria(mainQuery);

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ParameterContentData> getActivities(TimerData template, String workflow) {

		DetachedCriteria findPCDByWorkflow = DetachedCriteria.forClass(ParameterContentData.class);
		findPCDByWorkflow.setProjection(Projections.property("methodSensorId"));
		findPCDByWorkflow.add(Restrictions.eq("name", "Workflow"));
		findPCDByWorkflow.add(Restrictions.eq("content", "'" + workflow));

		DetachedCriteria findActivityByWorkflow = DetachedCriteria.forClass(ParameterContentData.class);
		findActivityByWorkflow.add(Property.forName("methodSensorId").in(findPCDByWorkflow));
		findActivityByWorkflow.add(Restrictions.eq("name", "Activity"));

		List<ParameterContentData> result = getHibernateTemplate().findByCriteria(findActivityByWorkflow);

		return result;

	}

}
