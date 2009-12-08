package info.novatec.novaspy.cmr.dao.impl;

import info.novatec.novaspy.cmr.dao.DefaultDataDao;
import info.novatec.novaspy.cmr.dao.InvocationDataDao;
import info.novatec.novaspy.cmr.util.Configuration;
import info.novatec.novaspy.cmr.util.Converter;
import info.novatec.novaspy.communication.DefaultData;
import info.novatec.novaspy.communication.MethodSensorData;
import info.novatec.novaspy.communication.data.ExceptionSensorData;
import info.novatec.novaspy.communication.data.InvocationSequenceData;
import info.novatec.novaspy.communication.data.ParameterContentData;
import info.novatec.novaspy.communication.data.SqlStatementData;
import info.novatec.novaspy.communication.data.SystemInformationData;
import info.novatec.novaspy.communication.data.TimerData;
import info.novatec.novaspy.communication.data.VmArgumentData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * The default implementation of the {@link DefaultDataDao} interface by using
 * the {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the
 * {@link HibernateDaoSupport} class.
 * 
 * @author Patrice Bouillet
 * 
 */
public class DefaultDataDaoImpl extends HibernateDaoSupport implements DefaultDataDao {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(DefaultDataDaoImpl.class);

	/**
	 * The xstream used to write JSON files.
	 */
	private XStream xstream = new XStream(new JettisonMappedXmlDriver());

	/**
	 * The configuration bean.
	 */
	private Configuration configuration;

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
			if (element instanceof InvocationSequenceData) {
				InvocationSequenceData invoc = (InvocationSequenceData) element;
				if (configuration.isEnhancedInvocationStorageMode()) {
					saveInvocationAsJson(invoc);
					saveStrippedInvocationInDatabase(invoc, session, true);
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
	 * Saves the passed invocation as a compressed JSON file.
	 * 
	 * @param invocation
	 *            The invocation object.
	 */
	private void saveInvocationAsJson(InvocationSequenceData invocation) {
		OutputStream fos = null;
		OutputStream bos = null;
		OutputStream output = null;

		try {
			// create path
			StringBuilder path = new StringBuilder();
			path.append(InvocationDataDao.INVOCATION_STORAGE_DIRECTORY);
			path.append(invocation.getPlatformIdent());
			path.append("_");
			path.append(invocation.getTimeStamp().getTime());
			path.append(".inv");

			// create file
			File file = new File(path.toString());

			// create streams
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			output = new GZIPOutputStream(bos);

			long time = 0;
			if (LOGGER.isDebugEnabled()) {
				time = System.nanoTime();
			}

			// store into json file
			xstream.toXML(invocation, output);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Invocation Conversion: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != output) {
					output.close();
				}
				if (null != bos) {
					bos.close();
				}
				if (null != fos) {
					fos.close();
				}
			} catch (IOException e) {
				// ignore the exception
			}
		}
	}

	/**
	 * Saves the {@link ExceptionSensorData} object into the databse.
	 * 
	 * @param session
	 *            The session used for db storage.
	 * @param data
	 *            The {@link ExceptionSensorData} object to persist.
	 */
	private void saveExceptionSensorData(StatelessSession session, ExceptionSensorData data) {
		if (null != data.getChild()) {
			saveExceptionSensorData(session, data.getChild());
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
}
