package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.cache.impl.BufferElement;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.cache.indexing.impl.IndexingException;
import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.util.CacheIdGenerator;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
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
 * @author Ivan Senic
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
	private IBuffer<MethodSensorData> buffer;

	/**
	 * The indexing tree for direct object indexing.
	 */
	private ITreeComponent<MethodSensorData> indexingTree;

	/**
	 * Id generator for the objects that are going to the cache.
	 */
	private CacheIdGenerator cacheIdGenerator;

	/**
	 * Logger for default data DAO.
	 */
	private static final Logger LOGGER = Logger.getLogger(DefaultDataDaoImpl.class);
	
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
			cacheIdGenerator.assignObjectAnId(element);
			if (element instanceof InvocationSequenceData) {
				InvocationSequenceData invoc = (InvocationSequenceData) element;
				if (configuration.isEnhancedInvocationStorageMode()) {
					extractDataFromInvocation(session, invoc, invoc);
					buffer.put(new BufferElement<MethodSensorData>(invoc));
					// commented out because we don't save anything anymore to the database!
					// saveStrippedInvocationInDatabase(invoc, session, true);
				} else {
					saveInvocationInDatabase(invoc, session, 0);
				}
			} else if (element instanceof SqlStatementData) {
				// saveSqlStatementData(session, (SqlStatementData) element);
				// session.insert(element);
				buffer.put(new BufferElement<MethodSensorData>((SqlStatementData) element));
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
				// saveExceptionSensorData(session, exData);
				connectErrorMessagesInExceptionData(exData);
				buffer.put(new BufferElement<MethodSensorData>(exData));
			} else if (element instanceof TimerData) {
				TimerData timerData = (TimerData) element;
				saveTimerData(session, timerData);
				buffer.put(new BufferElement<MethodSensorData>(timerData));
			} else {
				session.insert(element);
			}
		}
		tx.commit();
		session.close();
	}

	/**
	 * Extract data from the invocation in the way that timer data is saved to the Db, while SQL
	 * statements and Exceptions are indexed into the root branch.
	 * 
	 * @param session
	 *            Session needed for DB persistence.
	 * @param invData
	 *            Invocation data to be extracted.
	 * @param topInvocationParent
	 *            Top invocation object.
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void extractDataFromInvocation(StatelessSession session, InvocationSequenceData invData, InvocationSequenceData topInvocationParent) {
		double exclusiveDurationDelta = 0d;
		Set<Long> identityHashCodeSet = new HashSet<Long>();
		for (InvocationSequenceData child : (List<InvocationSequenceData>) invData.getNestedSequences()) {
			cacheIdGenerator.assignObjectAnId(child);
			if (null != child.getTimerData()) {
				// this object is now saved in the next recursion step when the exclusive duration
				// is set
				// thus just calculate the exclusive duration
				exclusiveDurationDelta += child.getTimerData().getDuration();
			}
			if (null != child.getSqlStatementData()) {
				if (null == child.getTimerData()) {
					// I don't know if the situation that both timer and sql are set in one
					// invocation, but just to be sure I only include the time of the sql, if i did
					// not already included the time of the timer before
					exclusiveDurationDelta += child.getSqlStatementData().getDuration();
				}

				// for SQLs we know immediately that exclusive duration is as a duration
				child.getSqlStatementData().setExclusiveCount(1L);
				child.getSqlStatementData().setExclusiveDuration(child.getSqlStatementData().getDuration());
				child.getSqlStatementData().setExclusiveMax(child.getSqlStatementData().getDuration());
				child.getSqlStatementData().setExclusiveMin(child.getSqlStatementData().getDuration());

				cacheIdGenerator.assignObjectAnId(child.getSqlStatementData());
				child.getSqlStatementData().addInvocationParentId(topInvocationParent.getId());
				try {
					indexingTree.put(child.getSqlStatementData());
				} catch (IndexingException e) {
					// indexing exception should not happen
					LOGGER.error(e.getMessage(), e);
				}
			}
			if (null != child.getExceptionSensorDataObjects()) {
				for (Object data : child.getExceptionSensorDataObjects()) {
					ExceptionSensorData exceptionData = (ExceptionSensorData) data;
					if (exceptionData.getExceptionEvent() == ExceptionEventEnum.CREATED) {
						connectErrorMessagesInExceptionData(exceptionData);
						if (identityHashCodeSet.add(exceptionData.getThrowableIdentityHashCode())) {
							ExceptionSensorData dataToIndex = manageExceptionConstructorDelegation(invData, exceptionData);
							cacheIdGenerator.assignObjectAnId(dataToIndex);
							dataToIndex.addInvocationParentId(topInvocationParent.getId());
							try {
								indexingTree.put(dataToIndex);
							} catch (IndexingException e) {
								// indexing exception should not happen
								LOGGER.error(e.getMessage(), e);
							}
						}
					}
				}
			}
			extractDataFromInvocation(session, child, topInvocationParent);
		}
		if (null != invData.getTimerData()) {
			double exclusiveTime = invData.getTimerData().getDuration() - exclusiveDurationDelta;
			invData.getTimerData().setExclusiveCount(1L);
			invData.getTimerData().setExclusiveDuration(exclusiveTime);
			invData.getTimerData().setExclusiveMin(exclusiveTime);
			invData.getTimerData().setExclusiveMax(exclusiveTime);

			saveTimerData(session, invData.getTimerData());
			cacheIdGenerator.assignObjectAnId(invData.getTimerData());
			invData.getTimerData().addInvocationParentId(topInvocationParent.getId());
			try {
				indexingTree.put(invData.getTimerData());
			} catch (IndexingException e) {
				// indexing exception should not happen
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Deals with the constructor delegation in the invocation sequence data.
	 * 
	 * @param parent
	 *            Invocation parent where the exception data is found.
	 * @param firstExceptionData
	 *            Exception that needs to be handled.
	 * @return Exception data that will be indexed.
	 */
	@SuppressWarnings("unchecked")
	private ExceptionSensorData manageExceptionConstructorDelegation(InvocationSequenceData parent, ExceptionSensorData firstExceptionData) {
		InvocationSequenceData lastInvocationChild = null;
		ExceptionSensorData lastExceptionData = null;
		long identityHashCode = firstExceptionData.getThrowableIdentityHashCode();

		for (InvocationSequenceData invData : (List<InvocationSequenceData>) parent.getNestedSequences()) {
			if (null != invData.getExceptionSensorDataObjects()) {
				for (ExceptionSensorData exData : (List<ExceptionSensorData>) invData.getExceptionSensorDataObjects()) {
					if (exData.getThrowableIdentityHashCode() == identityHashCode) {
						if (null != lastInvocationChild) {
							lastInvocationChild.setExceptionSensorDataObjects(null);
						}
						lastInvocationChild = invData;
						lastExceptionData = exData;
					}
				}
			}
		}

		if (null != lastExceptionData) {
			return lastExceptionData;
		} else {
			return firstExceptionData;
		}
	}

	/**
	 * Connects exception message between linked exception data.
	 * 
	 * @param exceptionSensorData
	 *            Parent exception data, thus the one that has exception event CREATED.
	 */
	private void connectErrorMessagesInExceptionData(ExceptionSensorData exceptionSensorData) {
		ExceptionSensorData child = exceptionSensorData.getChild();
		if (null != child) {
			child.setErrorMessage(exceptionSensorData.getErrorMessage());
			connectErrorMessagesInExceptionData(child);
		}
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
			if (!data.getErrorMessage().equals(child.getErrorMessage())) {
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
	public void setBuffer(IBuffer<MethodSensorData> buffer) {
		this.buffer = buffer;
	}

	/**
	 * 
	 * @param indexingTree
	 *            indexing tree to see
	 */
	public void setIndexingTree(ITreeComponent<MethodSensorData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * 
	 * @param cacheIdGenerator
	 *            cache id generator
	 */
	public void setCacheIdGenerator(CacheIdGenerator cacheIdGenerator) {
		this.cacheIdGenerator = cacheIdGenerator;
	}

}
