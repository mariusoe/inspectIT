package info.novatec.inspectit.cmr.storage;

import info.novatec.inspectit.cmr.dao.impl.PlatformIdentDaoImpl;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.storage.StorageFileExtensions;
import info.novatec.inspectit.storage.StorageWriter;
import info.novatec.inspectit.storage.util.PersistentObjectCloner;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * {@link StorageWriter} implementation for the CMR.
 *
 * @author Ivan Senic
 *
 */
@Component("cmrStorageWriter")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class CmrStorageWriter extends StorageWriter {

	/**
	 * Set of involved Agents, used after recording to store proper Agent information.
	 */
	private Set<Long> involvedAgentsSet = new HashSet<Long>();

	/**
	 * Platform ident dao.
	 */
	@Autowired
	private PlatformIdentDaoImpl platformIdentDao;

	/**
	 * Cloner for transforming the agent data.
	 */
	@Autowired
	private PersistentObjectCloner persistentObjectCloner;

	/**
	 * Logger for buffer.
	 */
	private static final Logger LOGGER = Logger.getLogger(CmrStorageWriter.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(DefaultData defaultData) {
		super.write(defaultData);
		involvedAgentsSet.add(defaultData.getPlatformIdent());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IOException
	 */
	protected void writeAgentData() throws IOException {
		List<PlatformIdent> involvedPlatformIdents = platformIdentDao.findAllInitialized(involvedAgentsSet);
		for (PlatformIdent agent : involvedPlatformIdents) {
			agent = persistentObjectCloner.clone(agent);
			super.writeNonDefaultDataObject(agent, agent.getId() + StorageFileExtensions.AGENT_FILE_EXT);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void finalizeWrite() {
		try {
			writeAgentData();
		} catch (IOException e) {
			LOGGER.error("Exception trying to write agent data to disk.", e);
		}
		super.finalizeWrite();
	}

	/**
	 * @param platformIdentDao
	 *            the platformIdentDao to set
	 */
	public void setPlatformIdentDao(PlatformIdentDaoImpl platformIdentDao) {
		this.platformIdentDao = platformIdentDao;
	}

}
