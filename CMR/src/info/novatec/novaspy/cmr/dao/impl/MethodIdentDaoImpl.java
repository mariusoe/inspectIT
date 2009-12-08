package info.novatec.novaspy.cmr.dao.impl;

import info.novatec.novaspy.cmr.dao.MethodIdentDao;
import info.novatec.novaspy.cmr.model.MethodIdent;
import info.novatec.novaspy.cmr.model.PlatformIdent;

import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The default implementation of the {@link MethodIdentDao} interface by using
 * the {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the
 * {@link HibernateDaoSupport} class.
 * 
 * @author Patrice Bouillet
 * 
 */
public class MethodIdentDaoImpl extends HibernateDaoSupport implements MethodIdentDao {

	/**
	 * {@inheritDoc}
	 */
	public void delete(MethodIdent methodIdent) {
		getHibernateTemplate().delete(methodIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(List<MethodIdent> methodIdents) {
		getHibernateTemplate().deleteAll(methodIdents);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<MethodIdent> findAll() {
		return getHibernateTemplate().loadAll(MethodIdent.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<MethodIdent> findByExample(MethodIdent methodIdent) {
		return getHibernateTemplate().findByExample(methodIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodIdent load(Long id) {
		return (MethodIdent) getHibernateTemplate().get(MethodIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(MethodIdent methodIdent) {
		getHibernateTemplate().saveOrUpdate(methodIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<MethodIdent> findForPlatformIdent(PlatformIdent platformIdent, MethodIdent methodIdentExample) {
		DetachedCriteria methodCriteria = DetachedCriteria.forClass(MethodIdent.class);
		methodCriteria.add(Restrictions.eq("packageName", methodIdentExample.getPackageName()));
		methodCriteria.add(Restrictions.eq("className", methodIdentExample.getClassName()));
		methodCriteria.add(Restrictions.eq("methodName", methodIdentExample.getMethodName()));
		methodCriteria.add(Restrictions.eq("parameters", methodIdentExample.getParameters()));
		methodCriteria.add(Restrictions.eq("returnType", methodIdentExample.getReturnType()));

		methodCriteria.setFetchMode("platformIdent", FetchMode.JOIN);
		DetachedCriteria platformCriteria = methodCriteria.createCriteria("platformIdent");
		platformCriteria.add(Restrictions.eq("id", platformIdent.getId()));

		return getHibernateTemplate().findByCriteria(methodCriteria);
	}
}
