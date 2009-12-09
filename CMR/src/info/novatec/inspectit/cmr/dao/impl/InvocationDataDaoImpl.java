package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.InvocationDataDao;
import info.novatec.inspectit.cmr.util.Configuration;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.NonUniqueResultException;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

/**
 * Default implementation of the {@link InvocationDataDao} interface.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InvocationDataDaoImpl extends HibernateDaoSupport implements InvocationDataDao {

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
		invocCriteria.add(Restrictions.eq("platformIdent", platformId));
		invocCriteria.add(Restrictions.eq("methodIdent", methodId));
		invocCriteria.add(Restrictions.isNull("parentSequence"));

		// TODO Bug anyone?
		// http://opensource.atlassian.com/projects/hibernate/browse/HHH-817
		ProjectionList proList = Projections.projectionList();
		proList.add(new CustomPropertyAliasProjection("id", "id"));
		proList.add(new CustomPropertyAliasProjection("platformIdent", "platformIdent"));
		proList.add(new CustomPropertyAliasProjection("methodIdent", "methodIdent"));
		proList.add(new CustomPropertyAliasProjection("sensorTypeIdent", "sensorTypeIdent"));
		proList.add(new CustomPropertyAliasProjection("timeStamp", "timeStamp"));
		proList.add(new CustomPropertyAliasProjection("position", "position"));
		proList.add(new CustomPropertyAliasProjection("duration", "duration"));
		proList.add(new CustomPropertyAliasProjection("childCount", "childCount"));
		invocCriteria.setProjection(proList);
		invocCriteria.addOrder(Order.desc("timeStamp"));
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
		invocCriteria.add(Restrictions.eq("platformIdent", platformId));
		invocCriteria.add(Restrictions.isNull("parentSequence"));

		// TODO Bug anyone?
		// http://opensource.atlassian.com/projects/hibernate/browse/HHH-817
		ProjectionList proList = Projections.projectionList();
		proList.add(new CustomPropertyAliasProjection("id", "id"));
		proList.add(new CustomPropertyAliasProjection("platformIdent", "platformIdent"));
		proList.add(new CustomPropertyAliasProjection("methodIdent", "methodIdent"));
		proList.add(new CustomPropertyAliasProjection("sensorTypeIdent", "sensorTypeIdent"));
		proList.add(new CustomPropertyAliasProjection("timeStamp", "timeStamp"));
		proList.add(new CustomPropertyAliasProjection("position", "position"));
		proList.add(new CustomPropertyAliasProjection("duration", "duration"));
		proList.add(new CustomPropertyAliasProjection("childCount", "childCount"));
		invocCriteria.setProjection(proList);
		invocCriteria.addOrder(Order.desc("timeStamp"));
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

			// filter all files which match the template
			final StringBuilder compareString = new StringBuilder();
			compareString.append(template.getPlatformIdent());
			compareString.append("_");
			compareString.append(template.getTimeStamp().getTime());
			compareString.append(".inv");

			File[] files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.equals(compareString.toString());
				}
			});

			if (files.length != 1) {
				throw new RuntimeException("Invocation could not be found!");
			}

			try {
				// create a RemoteStreamServer (note the finally block which
				// only releases the RMI resources if the method fails before
				// returning.)
				RemoteInputStreamServer istream = null;
				try {
					istream = new SimpleRemoteInputStream(new BufferedInputStream(new FileInputStream(files[0])));
					// export the final stream for returning to the client
					RemoteInputStream result = istream.export();
					// after all the hard work, discard the local reference (we
					// are passing responsibility to the client)
					istream = null;
					return result;
				} finally {
					// we will only close the stream here if the server fails
					// before
					// returning an exported stream
					if (istream != null) {
						istream.close();
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException("File not found exception!");
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("IOException");
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
	 *             If this list does not contain a unique result, this exception
	 *             is thrown.
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
	 * @param configuration
	 *            the configuration to set
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

}
