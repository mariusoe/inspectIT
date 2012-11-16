package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.dao.PlatformIdentDao;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.cmr.util.AgentStatusDataProvider;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.spring.logger.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Patrice Bouillet
 * 
 */
@Service
public class GlobalDataAccessService implements IGlobalDataAccessService {

	/** The logger of this class. */
	@Logger
	Log log;

	/**
	 * The platform ident DAO.
	 */
	@Autowired
	private PlatformIdentDao platformIdentDao;

	/**
	 * The default data DAO.
	 */
	@Autowired
	private DefaultDataDao defaultDataDao;

	/**
	 * {@link AgentStatusDataProvider}.
	 */
	@Autowired
	private AgentStatusDataProvider agentStatusProvider;

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public Map<PlatformIdent, AgentStatusData> getConnectedAgents() {
		List<PlatformIdent> agents = platformIdentDao.findAllInitialized();
		Map<Long, AgentStatusData> agentStatusMap = agentStatusProvider.getAgentStatusDataMap();

		Map<PlatformIdent, AgentStatusData> resultMap = new HashMap<PlatformIdent, AgentStatusData>();
		for (PlatformIdent platformIdent : agents) {
			resultMap.put(platformIdent, agentStatusMap.get(platformIdent.getId()));
		}
		return resultMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public Map<Long, AgentStatusData> getAgentStatusDataMap() {
		return agentStatusProvider.getAgentStatusDataMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<DefaultData> getLastDataObjects(DefaultData template, long timeInterval) {
		List<DefaultData> result = defaultDataDao.findByExampleWithLastInterval(template, timeInterval);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public DefaultData getLastDataObject(DefaultData template) {
		DefaultData result = defaultDataDao.findByExampleLastData(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<DefaultData> getDataObjectsFromToDate(DefaultData template, Date fromDate, Date toDate) {
		if (fromDate.after(toDate)) {
			return Collections.emptyList();
		}

		List<DefaultData> result = defaultDataDao.findByExampleFromToDate(template, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<DefaultData> getDataObjectsSinceId(DefaultData template) {
		List<DefaultData> result = defaultDataDao.findByExampleSinceId(template);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<DefaultData> getDataObjectsSinceIdIgnoreMethodId(DefaultData template) {
		List<DefaultData> result = defaultDataDao.findByExampleSinceIdIgnoreMethodId(template);
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
			log.info("|-Global Data Access Service active...");
		}
	}

}
