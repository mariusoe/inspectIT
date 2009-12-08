package info.novatec.novaspy.cmr.service;

import info.novatec.novaspy.cmr.dao.InvocationDataDao;
import info.novatec.novaspy.cmr.util.Converter;
import info.novatec.novaspy.communication.data.InvocationSequenceData;

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
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> GlobalDataAccessService.getInvocationSequenceOverview()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, limit);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get invocation sequence overview duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- GlobalDataAccessService.getInvocationSequenceOverview()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> GlobalDataAccessService.getInvocationSequenceOverview()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		List<InvocationSequenceData> result = invocationDataDao.getInvocationSequenceOverview(platformId, methodId, limit);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get invocation sequence overview duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- GlobalDataAccessService.getInvocationSequenceOverview()");
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getInvocationSequenceDetail(InvocationSequenceData template) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("--> GlobalDataAccessService.getInvocationSequenceDetail()");
		}

		long time = 0;
		if (LOGGER.isDebugEnabled()) {
			time = System.nanoTime();
		}

		Object result = invocationDataDao.getInvocationSequenceDetail(template);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get invocation sequence detail duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<-- GlobalDataAccessService.getInvocationSequenceDetail()");
		}

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