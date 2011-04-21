package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.InvocationDataDao;
import info.novatec.inspectit.cmr.util.aop.Log;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Patrice Bouillet
 * 
 */
public class InvocationDataAccessService implements IInvocationDataAccessService, InitializingBean {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(InvocationDataAccessService.class);

	/**
	 * The invocation DAO.
	 */
	private InvocationDataDao invocationDataDao;

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit) {
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, limit);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit) {
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, methodId, limit);
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Date fromDate, Date toDate) {
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, limit, fromDate, toDate);
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Log
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate) {
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, methodId, limit, fromDate, toDate);
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Log
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Collection invocationIdCollection, int limit) {
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, invocationIdCollection, limit);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Log
	public InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template) {
		InvocationSequenceData result = invocationDataDao.getInvocationSequenceDetail(template);
		return result;
	}

	/**
	 * @param invocationDataDao
	 *            the invocationDataDao to set
	 */
	public void setInvocationDataDao(InvocationDataDao invocationDataDao) {
		this.invocationDataDao = invocationDataDao;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("|-Invocation Data Access Service active...");
		}
	}

}