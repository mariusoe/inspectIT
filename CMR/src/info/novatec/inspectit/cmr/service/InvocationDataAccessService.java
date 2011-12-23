package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.InvocationDataDao;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.cmr.spring.logger.Logger;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Patrice Bouillet
 * 
 */
@Service
public class InvocationDataAccessService implements IInvocationDataAccessService {

	/** The logger of this class. */
	@Logger
	Log log;

	/**
	 * The invocation DAO.
	 */
	@Autowired
	private InvocationDataDao invocationDataDao;

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit) {
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, limit);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit) {
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, methodId, limit);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Date fromDate, Date toDate) {
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, limit, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate) {
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, methodId, limit, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Collection<Long> invocationIdCollection, int limit) {
		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, invocationIdCollection, limit);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template) {
		InvocationSequenceData result = invocationDataDao.getInvocationSequenceDetail(template);
		return result;
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("|-Invocation Data Access Service active...");
		}
	}

}