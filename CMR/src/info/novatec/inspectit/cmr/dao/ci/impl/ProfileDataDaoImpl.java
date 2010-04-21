package info.novatec.inspectit.cmr.dao.ci.impl;

import info.novatec.inspectit.cmr.dao.ci.ProfileDataDao;
import info.novatec.inspectit.communication.data.ci.ProfileData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author Matthias Huber
 *
 */
public class ProfileDataDaoImpl extends HibernateDaoSupport implements ProfileDataDao {

	/**
	 * @see ProfileDataDao#addProfile(ProfileData)
	 */
	public long addProfile(ProfileData profileData) {
		getHibernateTemplate().save(profileData);
		
		return profileData.getId();
	}

	/**
	 * @see ProfileDataDao#deleteProfile(long)
	 */
	public void deleteProfile(long profileId) throws EntityNotFoundException {
		HibernateTemplate hibernateTemplate = getHibernateTemplate();
		Object profile = hibernateTemplate.get(ProfileData.class, profileId);

		if (null != profile) {
			hibernateTemplate.delete(profile);
		} else {
			throw new EntityNotFoundException("Profile could not be found!");
		}
	}

	/**
	 * @see ProfileDataDao#getProfile(long)
	 */
	public ProfileData getProfile(long profileId) {
		DetachedCriteria profileDataCriteria = DetachedCriteria.forClass(ProfileData.class);
		profileDataCriteria.add(Restrictions.eq("id", profileId));
		profileDataCriteria.setFetchMode("exceptionSensorDefinitions", FetchMode.JOIN);
		profileDataCriteria.setFetchMode("methodSensorDefinitions", FetchMode.JOIN);
		profileDataCriteria.setFetchMode("platformSensorDefinitions", FetchMode.JOIN);
		profileDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		return (ProfileData) DataAccessUtils.uniqueResult(getHibernateTemplate().findByCriteria(profileDataCriteria));
	}

	/**
	 * @see ProfileDataDao#updateProfile(ProfileData)
	 */
	public void updateProfile(ProfileData profileData) {
		getHibernateTemplate().update(profileData);
	}

}