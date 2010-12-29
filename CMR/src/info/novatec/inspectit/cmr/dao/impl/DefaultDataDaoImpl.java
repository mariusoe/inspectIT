package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.cache.impl.BufferElement;
import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.util.Configuration;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEventEnum;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.communication.data.VmArgumentData;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * The default implementation of the {@link DefaultDataDao} interface by using the
 * {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the {@link HibernateDaoSupport}
 * class.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public class DefaultDataDaoImpl extends HibernateDaoSupport implements DefaultDataDao {

	/**
	 * The configuration bean.
	 */
	private Configuration configuration;

	/**
	 * The buffer to put invocation sequences in.
	 */
	private IBuffer<InvocationSequenceData> buffer;

	/**
	 * {@inheritDoc}
	 */
	public void save(DefaultData defaultData) {
		getHibernateTemplate().save(defaultData);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void saveAll(final List<DefaultData> defaultDataCollection) {
		StatelessSession session = getHibernateTemplate().getSessionFactory().openStatelessSession();
		Transaction tx = session.beginTransaction();
		for (DefaultData element : defaultDataCollection) {
			// TODO ET: constructor delegation filtering must be done here instead of in the
			// InvocationHook
			if (element instanceof InvocationSequenceData) {
				InvocationSequenceData invoc = (InvocationSequenceData) element;
				if (configuration.isEnhancedInvocationStorageMode()) {
					buffer.put(new BufferElement<InvocationSequenceData>(invoc));
					// commented out because we don't save anything anymore to the database!
					// saveStrippedInvocationInDatabase(invoc, session, true);
				} else {
					saveInvocationInDatabase(invoc, session, 0);
				}
			} else if (element instanceof SqlStatementData) {
				// saveSqlStatementData(session, (SqlStatementData) element);
				session.insert(element);
			} else if (element instanceof SystemInformationData) {
				SystemInformationData info = (SystemInformationData) element;
				long systemInformationId = ((Long) session.insert(info)).longValue();
				Set<VmArgumentData> vmSet = info.getVmSet();
				for (VmArgumentData argumentData : vmSet) {
					argumentData.setSystemInformationId(systemInformationId);
					session.insert(argumentData);
				}
			} else if (element instanceof ExceptionSensorData) {
				ExceptionSensorData exData = (ExceptionSensorData) element;
				saveExceptionSensorData(session, exData);
			} else if (element instanceof TimerData) {
				TimerData timerData = (TimerData) element;
				saveTimerData(session, timerData);
			} else {
				session.insert(element);
			}
		}
		tx.commit();
		session.close();
	}

	/**
	 * Saves the timer data objects into the database.
	 * 
	 * @param session
	 *            The current session.
	 * @param timerData
	 *            The timer data object.
	 */
	@SuppressWarnings("unchecked")
	private void saveTimerData(StatelessSession session, TimerData timerData) {
		Long id = (Long) session.insert(timerData);

		Set<ParameterContentData> parameterContentData = timerData.getParameterContentData();
		if (null != parameterContentData && !parameterContentData.isEmpty()) {
			for (ParameterContentData content : parameterContentData) {
				content.setMethodSensorId(id.longValue());
				session.insert(content);
			}
		}
	}

	/**
	 * Saves the {@link ExceptionSensorData} object into the database.
	 * 
	 * @param session
	 *            The session used for db storage.
	 * @param data
	 *            The {@link ExceptionSensorData} object to persist.
	 */
	private void saveExceptionSensorData(StatelessSession session, ExceptionSensorData data) {
		ExceptionSensorData child = data.getChild();
		if (null != child) {
			// we store in each object the error message from the root data object that has the
			// CREATED event
			if (data.getErrorMessage() != child.getErrorMessage()) {
				child.setErrorMessage(data.getErrorMessage());
			}
			// first save the lowermost child
			saveExceptionSensorData(session, child);
		}
		session.insert(data);
	}

	/**
	 * @param session
	 * @param element
	 */
	@SuppressWarnings("unused")
	private SqlStatementData saveSqlStatementData(StatelessSession session, SqlStatementData sql) {
		Query sqlQuery = session.createQuery("from SqlStatementData data left join fetch data.parameterContentData where data.sql=:sql");
		sqlQuery.setString("sql", sql.getSql());
		SqlStatementData sqlDataInDb = (SqlStatementData) sqlQuery.uniqueResult();
		if (null != sqlDataInDb) {
			sqlDataInDb.addDuration(sql.getDuration());
			sqlDataInDb.setCount(sqlDataInDb.getCount() + sql.getCount());
			sqlDataInDb.setAverage(sqlDataInDb.getDuration() / sqlDataInDb.getCount());
			if (sql.getMax() > sqlDataInDb.getMax()) {
				sqlDataInDb.setMax(sql.getMax());
			}
			if (sql.getMin() < sqlDataInDb.getMin()) {
				sqlDataInDb.setMin(sql.getMin());
			}
			session.update(sqlDataInDb);
			return sqlDataInDb;
		} else {
			session.insert(sql);
			return sql;
		}
	}

	/**
	 * Save the invocation to the db and maybe as a file.
	 * 
	 * @param invoc
	 *            The invocation.
	 * @param session
	 *            The session used for db storage.
	 * @param position
	 *            The position.
	 */
	@SuppressWarnings("unchecked")
	private void saveStrippedInvocationInDatabase(InvocationSequenceData invoc, StatelessSession session, boolean saveInDb) {
		if (null != invoc.getTimerData()) {
			session.insert(invoc.getTimerData());
		}

		if (null != invoc.getSqlStatementData()) {
			session.insert(invoc.getSqlStatementData());
		}

		if (null != invoc.getExceptionSensorDataObjects() && !invoc.getExceptionSensorDataObjects().isEmpty()) {
			for (Object object : invoc.getExceptionSensorDataObjects()) {
				ExceptionSensorData exceptionSensorData = (ExceptionSensorData) object;
				if (exceptionSensorData.getExceptionEvent().equals(ExceptionEventEnum.CREATED)) {
					saveExceptionSensorData(session, exceptionSensorData);
				}
			}
		}

		List<InvocationSequenceData> nestedInvocs = invoc.getNestedSequences();
		for (int i = 0; i < nestedInvocs.size(); i++) {
			saveStrippedInvocationInDatabase(nestedInvocs.get(i), session, false);
		}

		if (saveInDb) {
			// just store the root node
			invoc.setNestedSequences(Collections.EMPTY_LIST);
			invoc.setPosition(0);
			session.insert(invoc);
		}
	}

	/**
	 * Save the invocation to the db and maybe as a file.
	 * 
	 * @param invoc
	 *            The invocation.
	 * @param session
	 *            The session used for db storage.
	 * @param position
	 *            The position.
	 */
	@SuppressWarnings("unchecked")
	private void saveInvocationInDatabase(InvocationSequenceData invoc, StatelessSession session, int position) {
		if (null != invoc.getTimerData()) {
			session.insert(invoc.getTimerData());
		}

		if (null != invoc.getSqlStatementData()) {
			session.insert(invoc.getSqlStatementData());
		}

		// store everything.
		invoc.setPosition(position);
		Long id = (Long) session.insert(invoc);

		if (null != invoc.getParameterContentData()) {
			Set<ParameterContentData> contents = invoc.getParameterContentData();
			for (ParameterContentData content : contents) {
				content.setMethodSensorId(id.longValue());
				session.insert(content);
			}
		}

		List<InvocationSequenceData> nestedInvocs = invoc.getNestedSequences();
		for (int i = 0; i < nestedInvocs.size(); i++) {
			saveInvocationInDatabase(nestedInvocs.get(i), session, i);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<DefaultData> findByExampleWithLastInterval(DefaultData template, long timeInterval) {
		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		defaultDataCriteria.add(Restrictions.eq("sensorTypeIdent", template.getSensorTypeIdent()));
		defaultDataCriteria.add(Restrictions.gt("timeStamp", new Timestamp(System.currentTimeMillis() - timeInterval)));

		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			defaultDataCriteria.add(Restrictions.eq("methodIdent", methodSensorData.getMethodIdent()));
			defaultDataCriteria.setFetchMode("parameterContentData", FetchMode.JOIN);
		}

		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(defaultDataCriteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<DefaultData> findByExampleSinceId(DefaultData template) {
		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.gt("id", template.getId()));
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		defaultDataCriteria.add(Restrictions.eq("sensorTypeIdent", template.getSensorTypeIdent()));

		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			defaultDataCriteria.add(Restrictions.eq("methodIdent", methodSensorData.getMethodIdent()));
			defaultDataCriteria.setFetchMode("parameterContentData", FetchMode.JOIN);
		}

		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(defaultDataCriteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List findByExampleSinceIdIgnoreMethodId(DefaultData template) {
		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.gt("id", template.getId()));
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		// defaultDataCriteria.add(Restrictions.eq("sensorTypeIdent",
		// template.getSensorTypeIdent()));

		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(defaultDataCriteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<DefaultData> findByExampleFromToDate(DefaultData template, Date fromDate, Date toDate) {
		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		defaultDataCriteria.add(Restrictions.eq("sensorTypeIdent", template.getSensorTypeIdent()));
		defaultDataCriteria.add(Restrictions.between("timeStamp", new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime())));

		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			defaultDataCriteria.add(Restrictions.eq("methodIdent", methodSensorData.getMethodIdent()));
			defaultDataCriteria.setFetchMode("parameterContentData", FetchMode.JOIN);
		}

		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(defaultDataCriteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public DefaultData findByExampleLastData(DefaultData template) {
		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		// defaultDataCriteria.add(Restrictions.eq("sensorTypeIdent",
		// template.getSensorTypeIdent()));
		defaultDataCriteria.setProjection(Projections.projectionList().add(Projections.max("id")));

		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			defaultDataCriteria.add(Restrictions.eq("methodIdent", methodSensorData.getMethodIdent()));
			defaultDataCriteria.setFetchMode("parameterContentData", FetchMode.JOIN);
		}

		List<Long> resultList = getHibernateTemplate().findByCriteria(defaultDataCriteria, -1, 1);
		if ((null != resultList) && !resultList.isEmpty() && (null != resultList.get(0))) {
			long id = resultList.get(0);
			DefaultData defaultData = (DefaultData) getHibernateTemplate().get(template.getClass(), id);

			defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
			return defaultData;
		} else {
			return null;
		}
	}

	/**
	 * @param configuration
	 *            the configuration to set
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * 
	 * @param buffer
	 *            buffer to set
	 */
	public void setBuffer(IBuffer<InvocationSequenceData> buffer) {
		this.buffer = buffer;
	}

}
