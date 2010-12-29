package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.ExceptionSensorDataDao;
import info.novatec.inspectit.communication.ExceptionEventEnum;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Default implementation of the {@link ExceptionSensorDataDao} interface.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ExceptionSensorDataDaoImpl extends HibernateDaoSupport implements ExceptionSensorDataDao {

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit) {
		// checks whether limit is set to show all elements
		if (limit == -1) {
			return getUngroupedExceptionOverview(template);
		}

		DetachedCriteria criteria = DetachedCriteria.forClass(template.getClass());
		criteria.add(Restrictions.gt("this.id", template.getId()));
		criteria.add(Restrictions.eq("this.platformIdent", template.getPlatformIdent()));
		criteria.add(Restrictions.eq("this.exceptionEvent", ExceptionEventEnum.CREATED));
		criteria.addOrder(Order.desc("timeStamp"));
		criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(criteria, -1, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, Date fromDate, Date toDate) {
		// checks whether limit is set to show all elements
		if (limit == -1) {
			return getUngroupedExceptionOverview(template, fromDate, toDate);
		}

		DetachedCriteria criteria = DetachedCriteria.forClass(template.getClass());
		criteria.add(Restrictions.gt("this.id", template.getId()));
		criteria.add(Restrictions.eq("this.platformIdent", template.getPlatformIdent()));
		criteria.add(Restrictions.eq("this.exceptionEvent", ExceptionEventEnum.CREATED));
		criteria.add(Restrictions.between("timeStamp", new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime())));
		criteria.addOrder(Order.desc("timeStamp"));
		criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(criteria, -1, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template) {
		DetachedCriteria criteria = DetachedCriteria.forClass(template.getClass());
		criteria.add(Restrictions.gt("this.id", template.getId()));
		criteria.add(Restrictions.eq("this.platformIdent", template.getPlatformIdent()));
		criteria.add(Restrictions.eq("this.exceptionEvent", ExceptionEventEnum.CREATED));
		criteria.addOrder(Order.desc("timeStamp"));
		criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(criteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		DetachedCriteria criteria = DetachedCriteria.forClass(template.getClass());
		criteria.add(Restrictions.gt("this.id", template.getId()));
		criteria.add(Restrictions.eq("this.platformIdent", template.getPlatformIdent()));
		criteria.add(Restrictions.eq("this.exceptionEvent", ExceptionEventEnum.CREATED));
		criteria.add(Restrictions.between("timeStamp", new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime())));
		criteria.addOrder(Order.desc("timeStamp"));
		criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(criteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getExceptionTree(ExceptionSensorData template) {
		DetachedCriteria criteria = DetachedCriteria.forClass(template.getClass());
		criteria.add(Restrictions.eq("this.platformIdent", template.getPlatformIdent()));
		criteria.add(Restrictions.eq("this.throwableIdentityHashCode", template.getThrowableIdentityHashCode()));
		criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		List<ExceptionSensorData> resultList = getHibernateTemplate().findByCriteria(criteria);

		// the exception sensor data objects are persisted in reverse order in
		// the db, so we need to reverse the result
		Collections.reverse(resultList);

		return resultList;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template) {
		DetachedCriteria criteria = DetachedCriteria.forClass(template.getClass());
		criteria.add(Restrictions.eq("this.platformIdent", template.getPlatformIdent()));
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.property("throwableType"), "throwableType");
		projectionList.add(Projections.property("exceptionEvent"), "exceptionEvent");
		projectionList.add(Projections.property("errorMessage"), "errorMessage");
		projectionList.add(Projections.property("stackTrace"), "stackTrace");
		projectionList.add(Projections.property("cause"), "cause");
		projectionList.add(Projections.count("exceptionEvent"), "throwableIdentityHashCode");
		projectionList.add(Projections.groupProperty("throwableType"));
		projectionList.add(Projections.groupProperty("exceptionEvent"));
		projectionList.add(Projections.groupProperty("errorMessage"));
		projectionList.add(Projections.groupProperty("stackTrace"));
		projectionList.add(Projections.groupProperty("cause"));
		criteria.setProjection(projectionList);
		criteria.addOrder(Order.asc("exceptionEvent"));
		criteria.setResultTransformer(Transformers.aliasToBean(ExceptionSensorData.class));
		return getHibernateTemplate().findByCriteria(criteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getStackTracesForErrorMessage(ExceptionSensorData template) {
		DetachedCriteria criteria = DetachedCriteria.forClass(template.getClass());
		criteria.add(Restrictions.gt("this.id", template.getId()));
		criteria.add(Restrictions.eq("this.platformIdent", template.getPlatformIdent()));
		criteria.add(Restrictions.eq("this.errorMessage", template.getErrorMessage()));
		criteria.add(Restrictions.isNotNull("this.stackTrace"));
		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.distinct(Projections.property("stackTrace")));
		projections.add(Projections.property("stackTrace"), "stackTrace");
		criteria.setProjection(projections);
		criteria.setResultTransformer(Transformers.aliasToBean(ExceptionSensorData.class));
		return getHibernateTemplate().findByCriteria(criteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		DetachedCriteria criteria = DetachedCriteria.forClass(template.getClass());
		criteria.add(Restrictions.eq("this.platformIdent", template.getPlatformIdent()));
		criteria.add(Restrictions.between("timeStamp", new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime())));
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.property("throwableType"), "throwableType");
		projectionList.add(Projections.property("exceptionEvent"), "exceptionEvent");
		projectionList.add(Projections.property("errorMessage"), "errorMessage");
		projectionList.add(Projections.property("stackTrace"), "stackTrace");
		projectionList.add(Projections.property("cause"), "cause");
		projectionList.add(Projections.count("exceptionEvent"), "throwableIdentityHashCode");
		projectionList.add(Projections.groupProperty("throwableType"));
		projectionList.add(Projections.groupProperty("exceptionEvent"));
		projectionList.add(Projections.groupProperty("errorMessage"));
		projectionList.add(Projections.groupProperty("stackTrace"));
		projectionList.add(Projections.groupProperty("cause"));
		criteria.setProjection(projectionList);
		criteria.addOrder(Order.asc("exceptionEvent"));
		criteria.setResultTransformer(Transformers.aliasToBean(ExceptionSensorData.class));
		return getHibernateTemplate().findByCriteria(criteria);
	}

}
