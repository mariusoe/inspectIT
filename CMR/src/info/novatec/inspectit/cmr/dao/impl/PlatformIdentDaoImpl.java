package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.PlatformIdentDao;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.util.PlatformIdentCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The default implementation of the {@link PlatformIdentDao} interface by using the
 * {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the {@link HibernateDaoSupport}
 * class.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PlatformIdentDaoImpl extends HibernateDaoSupport implements PlatformIdentDao {

	/**
	 * {@link PlatformIdent} cache.
	 */
	private PlatformIdentCache platformIdentCache;

	/**
	 * {@inheritDoc}
	 */
	public void delete(PlatformIdent platformIdent) {
		getHibernateTemplate().delete(platformIdent);
		platformIdentCache.remove(platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(List<PlatformIdent> platformIdents) {
		getHibernateTemplate().deleteAll(platformIdents);
		for (PlatformIdent platformIdent : platformIdents) {
			platformIdentCache.remove(platformIdent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<PlatformIdent> findAll() {
		return getHibernateTemplate().loadAll(PlatformIdent.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<PlatformIdent> findByExample(PlatformIdent platformIdent) {
		return getHibernateTemplate().findByExample(platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public PlatformIdent load(Long id) {
		return (PlatformIdent) getHibernateTemplate().get(PlatformIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(PlatformIdent platformIdent) {
		final int maxDefIPsSize = 1024;

		if (null != platformIdent.getDefinedIPs()) {
			int charNum = 0;
			List<Object> newDefinedIPs = new ArrayList<Object>();
			for (Object item : platformIdent.getDefinedIPs()) {
				// if it is too long, we stop adding
				if (charNum + item.toString().length() <= maxDefIPsSize) {
					newDefinedIPs.add(item);
					// we add 1 also for the white space
					charNum += item.toString().length() + 1;
				} else {
					break;
				}
			}

			// change only if we really cut the list
			if (newDefinedIPs.size() != platformIdent.getDefinedIPs().size()) {
				platformIdent.setDefinedIPs(newDefinedIPs);
			}
		}

		getHibernateTemplate().saveOrUpdate(platformIdent);
		platformIdentCache.markDirty(platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void evict(PlatformIdent platformIdent) {
		getHibernateTemplate().evict(platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void evictAll(List<PlatformIdent> platformIdents) {
		for (PlatformIdent platformIdent : platformIdents) {
			this.evict(platformIdent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PlatformIdent> findAllInitialized() {
		List<PlatformIdent> initializedPlatformIdents = new ArrayList<PlatformIdent>();
		List<Long> cleanIdents = new ArrayList<Long>();
		for (PlatformIdent platformIdent : platformIdentCache.getCleanPlatformIdents()) {
			cleanIdents.add(platformIdent.getId());
			initializedPlatformIdents.add(platformIdent);
		}

		if (cleanIdents.size() != platformIdentCache.getSize()) {
			List<PlatformIdent> cleanPlatformIdents = loadIdentsFromDB(cleanIdents);
			initializedPlatformIdents.addAll(cleanPlatformIdents);
		}

		Collections.sort(initializedPlatformIdents, new Comparator<PlatformIdent>() {
			@Override
			public int compare(PlatformIdent o1, PlatformIdent o2) {
				return (int) (o1.getId().longValue() - o2.getId().longValue());
			}
		});
		return initializedPlatformIdents;
	}

	/**
	 * Initialize all platform idents from the database.
	 */
	public void initPlatformIdentCache() {
		Collection<Long> excludeList = Collections.emptyList();
		loadIdentsFromDB(excludeList);
	}

	/**
	 * Loads agents from database, excluding the agents which IDs is supplied in the exclude
	 * collection.
	 * 
	 * @param excludeIdents
	 *            IDs of the agents that should not be loaded.
	 * @return List of {@link PlatformIdent}.
	 */
	@SuppressWarnings("unchecked")
	private List<PlatformIdent> loadIdentsFromDB(Collection<Long> excludeIdents) {
		DetachedCriteria criteria = DetachedCriteria.forClass(PlatformIdent.class);
		if (!excludeIdents.isEmpty()) {
			criteria.add(Restrictions.not(Restrictions.in("id", excludeIdents)));
		}
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.setFetchMode("methodIdents", FetchMode.JOIN);
		criteria.setFetchMode("methodIdents.methodSensorTypeIdents", FetchMode.JOIN);
		criteria.setFetchMode("sensorTypeIdents", FetchMode.JOIN);
		criteria.setFetchMode("sensorTypeIdents.platformIdents", FetchMode.JOIN);

		List<PlatformIdent> platformIdents = getHibernateTemplate().findByCriteria(criteria);
		for (PlatformIdent platformIdent : platformIdents) {
			platformIdentCache.markClean(platformIdent);
		}
		return platformIdents;
	}

	/**
	 * @param platformIdentCache
	 *            the platformIdentCache to set
	 */
	public void setPlatformIdentCache(PlatformIdentCache platformIdentCache) {
		this.platformIdentCache = platformIdentCache;
	}

}
