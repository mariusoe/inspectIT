package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.ExceptionSensorDataDao;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
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
	public List<ExceptionSensorData> getExceptionTreeOverview(ExceptionSensorData template, int limit) {
		// TODO ET: refactor this method

		// checks whether limit is set to show all elements
		if (limit == -1) {
			return getExceptionTreeOverview(template);
		}

		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.gt("id", template.getId()));
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		List<ExceptionSensorData> resultList = new ArrayList<ExceptionSensorData>();
		List<ExceptionSensorData> exceptionSensorDataList = getHibernateTemplate().findByCriteria(defaultDataCriteria);

		// the exception sensor data objects are persisted in reverse order in
		// the db, so we need to reverse the result
		Collections.reverse(exceptionSensorDataList);

		int counter = 0;
		while ((resultList.size() < limit) && (counter < exceptionSensorDataList.size())) {
			ExceptionSensorData data = exceptionSensorDataList.get(counter);

			if (counter == 0) {
				resultList.add(exceptionSensorDataList.get(counter));
			} else if ((counter - 1) > 0) {
				if (data.getThrowableIdentityHashCode() != exceptionSensorDataList.get(counter - 1).getThrowableIdentityHashCode()) {
					resultList.add(data);
				}
			}
			counter++;
		}

		return resultList;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getExceptionTreeOverview(ExceptionSensorData template) {
		// TODO ET: refactor this method
		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.gt("id", template.getId()));
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		List<ExceptionSensorData> resultList = new ArrayList<ExceptionSensorData>();
		List<ExceptionSensorData> exceptionSensorDataList = getHibernateTemplate().findByCriteria(defaultDataCriteria);

		// the exception sensor data objects are persisted in reverse order in
		// the db, so we need to reverse the result
		Collections.reverse(exceptionSensorDataList);

		int counter = 0;
		while (counter < exceptionSensorDataList.size()) {
			ExceptionSensorData data = exceptionSensorDataList.get(counter);

			if (counter == 0) {
				resultList.add(exceptionSensorDataList.get(counter));
			} else if ((counter - 1) > 0) {
				if (data.getThrowableIdentityHashCode() != exceptionSensorDataList.get(counter - 1).getThrowableIdentityHashCode()) {
					resultList.add(data);
				}
			}
			counter++;
		}

		return resultList;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ExceptionSensorData> getExceptionTreeDetails(ExceptionSensorData template) {
		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		defaultDataCriteria.add(Restrictions.eq("throwableIdentityHashCode", template.getThrowableIdentityHashCode()));
		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		List<ExceptionSensorData> resultList = getHibernateTemplate().findByCriteria(defaultDataCriteria);

		// the exception sensor data objects are persisted in reverse order in
		// the db, so we need to reverse the result
		Collections.reverse(resultList);

		return resultList;
	}

}
