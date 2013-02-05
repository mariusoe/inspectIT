package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.cache.impl.BufferElement;
import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.storage.CmrStorageManager;
import info.novatec.inspectit.cmr.util.CacheIdGenerator;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.communication.data.VmArgumentData;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.recording.RecordingState;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
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
 * @author Stefan Siegl
 */
@Repository
public class DefaultDataDaoImpl extends HibernateDaoSupport implements DefaultDataDao {

	/** The logger of this class. */
	@Logger
	Log log;

	/**
	 * The buffer to put invocation sequences in.
	 */
	@Autowired
	private IBuffer<MethodSensorData> buffer;

	/**
	 * The indexing tree for direct object indexing.
	 */
	@Autowired
	private IBufferTreeComponent<DefaultData> indexingTree;

	/**
	 * Id generator for the objects that are going to the cache.
	 */
	@Autowired
	private CacheIdGenerator cacheIdGenerator;

	/**
	 * Storage manager.
	 */
	@Autowired
	private CmrStorageManager storageManager;

	/**
	 * Timer data aggregator.
	 */
	@Autowired
	private TimerDataAggregator timerDataAggregator;

	/**
	 * Denotes if the {@link TimerData} objects have to be saved to database.
	 */
	@Value(value = "${cmr.saveTimerDataToDatabase}")
	private boolean saveTimerDataToDatabase;

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
	public DefaultDataDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public void save(DefaultData defaultData) {
		getHibernateTemplate().save(defaultData);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveAll(List<? extends DefaultData> defaultDataCollection) {
		StatelessSession session = getHibernateTemplate().getSessionFactory().openStatelessSession();
		Transaction tx = session.beginTransaction();
		boolean isRecording = storageManager.getRecordingState() == RecordingState.ON;
		for (DefaultData element : defaultDataCollection) {
			cacheIdGenerator.assignObjectAnId(element);
			if (element instanceof InvocationSequenceData) {
				InvocationSequenceData invoc = (InvocationSequenceData) element;
				extractDataFromInvocation(session, invoc, invoc);
				buffer.put(new BufferElement<MethodSensorData>(invoc));
			} else if (element instanceof SqlStatementData) {
				// create the exclusive times for SQLs, because we know that exclusive time equals
				// the total duration
				SqlStatementData sqlStatementData = (SqlStatementData) element;
				sqlStatementData.setExclusiveCount(1L);
				sqlStatementData.setExclusiveDuration(sqlStatementData.getDuration());
				sqlStatementData.calculateExclusiveMax(sqlStatementData.getDuration());
				sqlStatementData.calculateExclusiveMin(sqlStatementData.getDuration());
				buffer.put(new BufferElement<MethodSensorData>(sqlStatementData));
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
				if (saveTimerDataToDatabase) {
					timerDataAggregator.processTimerData(timerData);
				}
				buffer.put(new BufferElement<MethodSensorData>(timerData));
			} else {
				session.insert(element);
			}

			if (isRecording) {
				storageManager.record(element);
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
	private void extractDataFromInvocation(StatelessSession session, InvocationSequenceData invData, InvocationSequenceData topInvocationParent) {
		double exclusiveDurationDelta = 0d;
		Collection<InvocationSequenceData> childRemoveCollection = null;
		Set<Long> identityHashCodeSet = new HashSet<Long>();
		for (InvocationSequenceData child : (List<InvocationSequenceData>) invData.getNestedSequences()) {
			boolean durationAddedToTheExclusive = false;
			cacheIdGenerator.assignObjectAnId(child);
			if (null != child.getTimerData()) {
				// this object is now saved in the next recursion step when the exclusive duration
				// is set
				// thus just calculate the exclusive duration
				exclusiveDurationDelta += child.getTimerData().getDuration();
				durationAddedToTheExclusive = true;
			}
			if (null != child.getSqlStatementData()) {
				topInvocationParent.setNestedSqlStatements(Boolean.TRUE);
				if (null == child.getTimerData()) {
					// I don't know if the situation that both timer and sql are set in one
					// invocation, but just to be sure I only include the time of the sql, if i did
					// not already included the time of the timer before
					exclusiveDurationDelta += child.getSqlStatementData().getDuration();
					durationAddedToTheExclusive = true;
				}

				// for SQLs we know immediately that exclusive duration is as a duration
				child.getSqlStatementData().setExclusiveCount(1L);
				child.getSqlStatementData().setExclusiveDuration(child.getSqlStatementData().getDuration());
				child.getSqlStatementData().calculateExclusiveMax(child.getSqlStatementData().getDuration());
				child.getSqlStatementData().calculateExclusiveMin(child.getSqlStatementData().getDuration());

				cacheIdGenerator.assignObjectAnId(child.getSqlStatementData());
				child.getSqlStatementData().addInvocationParentId(topInvocationParent.getId());
				try {
					indexingTree.put(child.getSqlStatementData());
				} catch (IndexingException e) {
					// indexing exception should not happen
					log.error(e.getMessage(), e);
				}
			}
			if (null != child.getExceptionSensorDataObjects()) {
				topInvocationParent.setNestedExceptions(Boolean.TRUE);
				for (Object data : child.getExceptionSensorDataObjects()) {
					ExceptionSensorData exceptionData = (ExceptionSensorData) data;
					if (exceptionData.getExceptionEvent() == ExceptionEvent.CREATED) {
						connectErrorMessagesInExceptionData(exceptionData);
						if (identityHashCodeSet.add(exceptionData.getThrowableIdentityHashCode())) {
							if (null == childRemoveCollection) {
								childRemoveCollection = new ArrayList<InvocationSequenceData>();
							}
							ExceptionSensorData dataToIndex = manageExceptionConstructorDelegation(invData, exceptionData, childRemoveCollection);
							if (null != dataToIndex) {
								cacheIdGenerator.assignObjectAnId(dataToIndex);
								dataToIndex.addInvocationParentId(topInvocationParent.getId());
								try {
									indexingTree.put(dataToIndex);
								} catch (IndexingException e) {
									// indexing exception should not happen
									log.error(e.getMessage(), e);
								}
							}
						}
					}
				}
			}
			extractDataFromInvocation(session, child, topInvocationParent);
			if (!durationAddedToTheExclusive) {
				exclusiveDurationDelta += computeNestedDuration(child);
			}
		}

		if (null != invData.getTimerData()) {
			TimerData data = invData.getTimerData();
			double exclusiveTime = invData.getTimerData().getDuration() - exclusiveDurationDelta;
			data.setExclusiveCount(1L);
			data.setExclusiveDuration(exclusiveTime);
			data.calculateExclusiveMax(exclusiveTime);
			data.calculateExclusiveMin(exclusiveTime);

			// Ensure not to save HttpTimerData!
			if (saveTimerDataToDatabase && data.getClass().equals(TimerData.class)) {
				timerDataAggregator.processTimerData(data);

			}
			cacheIdGenerator.assignObjectAnId(data);
			data.addInvocationParentId(topInvocationParent.getId());
			try {
				indexingTree.put(data);
			} catch (IndexingException e) {
				// indexing exception should not happen
				log.error(e.getMessage(), e);
			}
		}

		if (null != childRemoveCollection && !childRemoveCollection.isEmpty()) {
			int oldChildrenListSize = invData.getNestedSequences().size();
			invData.getNestedSequences().removeAll(childRemoveCollection);
			int newChildrenListSize = invData.getNestedSequences().size();

			InvocationSequenceData alterChildCountInvocation = invData;
			int alterSize = oldChildrenListSize - newChildrenListSize;
			while (null != alterChildCountInvocation) {
				alterChildCountInvocation.setChildCount(alterChildCountInvocation.getChildCount() - alterSize);
				alterChildCountInvocation = alterChildCountInvocation.getParentSequence();
			}
		}
	}

	/**
	 * Computes the duration of the nested invocation elements.
	 * 
	 * @param data
	 *            The data objects which is inspected for its nested elements.
	 * @return The duration of all nested sequences (with their nested sequences as well).
	 */
	private double computeNestedDuration(InvocationSequenceData data) {
		if (data.getNestedSequences().isEmpty()) {
			return 0;
		}

		double nestedDuration = 0d;
		boolean added = false;
		for (InvocationSequenceData nestedData : (List<InvocationSequenceData>) data.getNestedSequences()) {
			if (null != nestedData.getTimerData()) {
				nestedDuration = nestedDuration + nestedData.getTimerData().getDuration();
				added = true;
			} else if (null != nestedData.getSqlStatementData() && 1 == nestedData.getSqlStatementData().getCount()) {
				nestedDuration = nestedDuration + nestedData.getSqlStatementData().getDuration();
				added = true;
			}

			if (!added && !nestedData.getNestedSequences().isEmpty()) {
				// nothing was added, but there could be child elements with
				// time measurements
				nestedDuration = nestedDuration + computeNestedDuration(nestedData);
			}
			added = false;
		}

		return nestedDuration;
	}

	/**
	 * Deals with the constructor delegation in the invocation sequence data. This method has side
	 * effects.
	 * 
	 * @param parent
	 *            Invocation parent where the exception data is found.
	 * @param firstExceptionData
	 *            Exception that needs to be handled.
	 * @param childremoveCollection
	 *            Collection where invocation children for removal will be added.
	 * @return Exception data to be indexed.
	 */
	private ExceptionSensorData manageExceptionConstructorDelegation(InvocationSequenceData parent, ExceptionSensorData firstExceptionData, Collection<InvocationSequenceData> childremoveCollection) {
		InvocationSequenceData lastInvocationChild = null;
		ExceptionSensorData lastExceptionData = null;
		long identityHashCode = firstExceptionData.getThrowableIdentityHashCode();
		long sensorTypeIdent = -1;
		int lastExceptionDataChildIndex = -1;

		for (int i = 0, j = parent.getNestedSequences().size(); i < j; i++) {
			InvocationSequenceData invData = (InvocationSequenceData) parent.getNestedSequences().get(i);
			if (null != invData.getExceptionSensorDataObjects()) {
				for (ExceptionSensorData exData : (List<ExceptionSensorData>) invData.getExceptionSensorDataObjects()) {
					if (exData.getThrowableIdentityHashCode() == identityHashCode) {
						if (null != lastInvocationChild) {
							lastInvocationChild.setExceptionSensorDataObjects(null);
						}
						lastInvocationChild = invData;
						lastExceptionData = exData;
						lastExceptionDataChildIndex = i;
						sensorTypeIdent = invData.getSensorTypeIdent();
					}
				}
			}
		}

		lastExceptionDataChildIndex--;
		while (lastExceptionDataChildIndex >= 0) {
			InvocationSequenceData invData = (InvocationSequenceData) parent.getNestedSequences().get(lastExceptionDataChildIndex);
			if (invData.getSensorTypeIdent() != sensorTypeIdent || null != invData.getTimerData() || null != invData.getSqlStatementData() || null != invData.getExceptionSensorDataObjects()) {
				break;
			}
			childremoveCollection.add(invData);
			lastExceptionDataChildIndex--;
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List findByExampleSinceIdIgnoreMethodId(DefaultData template) {
		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.gt("id", template.getId()));
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));

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
		DetachedCriteria subQuery = DetachedCriteria.forClass(template.getClass());
		subQuery.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		subQuery.setProjection(Projections.projectionList().add(Projections.max("id")));

		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			subQuery.add(Restrictions.eq("methodIdent", methodSensorData.getMethodIdent()));
			defaultDataCriteria.setFetchMode("parameterContentData", FetchMode.JOIN);
		}
		defaultDataCriteria.add(Property.forName("id").eq(subQuery));
		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		List<DefaultData> resultList = getHibernateTemplate().findByCriteria(defaultDataCriteria);
		if (CollectionUtils.isNotEmpty(resultList)) {
			return resultList.get(0);
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(Long platformId) {
		// because H2 does not support cascading on delete we need to clear all connected data
		Query query = getSession().createQuery("delete from VmArgumentData where systemInformationId in (select id from SystemInformationData where platformIdent = :platformIdent)");
		query.setLong("platformIdent", platformId);
		query.executeUpdate();

		// then delete all default data
		query = getSession().createQuery("delete from DefaultData where platformIdent = :platformIdent");
		query.setLong("platformIdent", platformId);
		query.executeUpdate();
	}

}