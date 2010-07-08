package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.InvocationDataDao;
import info.novatec.inspectit.cmr.util.Configuration;
import info.novatec.inspectit.cmr.util.Converter;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.NonUniqueResultException;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import com.healthmarketscience.rmiio.SerializableInputStream;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * Default implementation of the {@link InvocationDataDao} interface.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InvocationDataDaoImpl extends HibernateDaoSupport implements InvocationDataDao {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(DefaultDataDaoImpl.class);

	/**
	 * The xstream used to write JSON files.
	 */
	private XStream xstream = new XStream(new JettisonMappedXmlDriver());

	/**
	 * The directory of the stored invocations.
	 */
	private File dir = new File(INVOCATION_STORAGE_DIRECTORY);

	/**
	 * The configuration bean.
	 */
	private Configuration configuration;

	{
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit) {
		DetachedCriteria invocCriteria = DetachedCriteria.forClass(InvocationSequenceData.class);
		invocCriteria.add(Restrictions.eq("this.platformIdent", platformId));
		invocCriteria.add(Restrictions.eq("this.methodIdent", methodId));
		invocCriteria.add(Restrictions.isNull("this.parentSequence"));

		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("id"), "id");
		proList.add(Projections.property("platformIdent"), "platformIdent");
		proList.add(Projections.property("methodIdent"), "methodIdent");
		proList.add(Projections.property("sensorTypeIdent"), "sensorTypeIdent");
		proList.add(Projections.property("timeStamp"), "timeStamp");
		proList.add(Projections.property("position"), "position");
		proList.add(Projections.property("duration"), "duration");
		proList.add(Projections.property("childCount"), "childCount");
		invocCriteria.setProjection(proList);
		invocCriteria.addOrder(Order.desc("this.timeStamp"));
		invocCriteria.setResultTransformer(Transformers.aliasToBean(InvocationSequenceData.class));

		List<InvocationSequenceData> result = getHibernateTemplate().findByCriteria(invocCriteria, -1, limit);

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<InvocationSequenceData> getInvocationSequenceOverview(final long platformId, int limit) {
		DetachedCriteria invocCriteria = DetachedCriteria.forClass(InvocationSequenceData.class);
		invocCriteria.add(Restrictions.eq("this.platformIdent", platformId));
		invocCriteria.add(Restrictions.isNull("this.parentSequence"));

		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("id"), "id");
		proList.add(Projections.property("platformIdent"), "platformIdent");
		proList.add(Projections.property("methodIdent"), "methodIdent");
		proList.add(Projections.property("sensorTypeIdent"), "sensorTypeIdent");
		proList.add(Projections.property("timeStamp"), "timeStamp");
		proList.add(Projections.property("position"), "position");
		proList.add(Projections.property("duration"), "duration");
		proList.add(Projections.property("childCount"), "childCount");
		invocCriteria.setProjection(proList);
		invocCriteria.addOrder(Order.desc("this.timeStamp"));
		invocCriteria.setResultTransformer(Transformers.aliasToBean(InvocationSequenceData.class));

		List<InvocationSequenceData> result = getHibernateTemplate().findByCriteria(invocCriteria, -1, limit);

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Object getInvocationSequenceDetail(final InvocationSequenceData template) {
		if (configuration.isEnhancedInvocationStorageMode()) {
			// new mode with stored invocations as files, we are returning a
			// stream of the compressed content back to the client.

			String path = getFilenameForInvocation(template);
			File file = new File(path);

			if (!file.exists()) {
				throw new RuntimeException("Invocation " + path + " could not be found!");
			}

			try {
				return new SerializableInputStream(new DirectRemoteInputStream(new BufferedInputStream(new FileInputStream(file))));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException("File not found exception!");
			}
		} else {
			// old mode via database
			DetachedCriteria invocCriteria = DetachedCriteria.forClass(InvocationSequenceData.class);
			invocCriteria.add(Restrictions.idEq(template.getId()));

			invocCriteria.setFetchMode("nestedSequences", FetchMode.JOIN);
			invocCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

			List<InvocationSequenceData> resultList = getHibernateTemplate().findByCriteria(invocCriteria);
			InvocationSequenceData result = uniqueElement(resultList);

			return result;
		}
	}

	/**
	 * Returns a unique element from the list.
	 * 
	 * @param list
	 *            The list to check for a unique element
	 * @return Returns the unique element.
	 * @throws NonUniqueResultException
	 *             If this list does not contain a unique result, this exception is thrown.
	 */
	private static <T> T uniqueElement(List<T> list) throws NonUniqueResultException {
		int size = list.size();
		if (size == 0) {
			throw new NonUniqueResultException(0);
		}
		T first = list.get(0);
		for (int i = 1; i < size; i++) {
			if (list.get(i) != first) {
				throw new NonUniqueResultException(list.size());
			}
		}
		return first;
	}

	/**
	 * Saves the passed invocation as a compressed JSON file.
	 * 
	 * @param invocation
	 *            The invocation object.
	 */
	@Override
	public void saveInvocation(InvocationSequenceData invocation) {
		OutputStream fos = null;
		OutputStream bos = null;
		OutputStream output = null;

		try {
			// create path
			String path = getFilenameForInvocation(invocation);

			// create file
			File file = new File(path);

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
	 * @param invocation
	 * @return
	 */
	private String getFilenameForInvocation(InvocationSequenceData invocation) {
		StringBuilder path = new StringBuilder();
		path.append(InvocationDataDao.INVOCATION_STORAGE_DIRECTORY);
		path.append(invocation.getPlatformIdent());
		path.append("_");
		path.append(invocation.getTimeStamp().getTime());
		path.append("_");
		path.append(Double.valueOf(invocation.getDuration() * 1000).longValue());
		path.append(INVOCATION_FILE_ENDING);
		return path.toString();
	}

	/**
	 * @param configuration
	 *            the configuration to set
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

}
