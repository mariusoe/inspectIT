package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.progress.IElementCollector;

import com.google.common.base.Objects;

/**
 * This composite holds Agents of one CMR as the deferred children.
 * 
 * @author Ivan Senic
 * 
 */
public class DeferredAgentsComposite extends DeferredComposite implements ICmrRepositoryProvider {

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Default constructor.
	 * 
	 * @param cmrRepositoryDefinition
	 *            Repository.
	 */
	public DeferredAgentsComposite(CmrRepositoryDefinition cmrRepositoryDefinition) {
		setRepositoryDefinition(cmrRepositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		monitor.beginTask("Loading agents..", IProgressMonitor.UNKNOWN);
		try {
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
				List<? extends PlatformIdent> agents = cmrRepositoryDefinition.getGlobalDataAccessService().getConnectedAgents();
				Map<Long, AgentStatusData> agentStatusDataMap = cmrRepositoryDefinition.getGlobalDataAccessService().getAgentStatusDataMap();
				if (null != agents) {
					for (PlatformIdent platformIdent : agents) {
						AgentStatusData agentStatusData = agentStatusDataMap.get(platformIdent.getId());
						Component agentLeaf = new AgentLeaf(platformIdent, agentStatusData);
						collector.add(agentLeaf, monitor);
						((Composite) object).addChild(agentLeaf);

						if (monitor.isCanceled()) {
							break;
						}
					}
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			this.cmrRepositoryDefinition = (CmrRepositoryDefinition) repositoryDefinition;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryDefinition getRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return cmrRepositoryDefinition.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), cmrRepositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		if (!super.equals(object)) {
			return false;
		}
		DeferredAgentsComposite that = (DeferredAgentsComposite) object;
		return Objects.equal(this.cmrRepositoryDefinition, that.cmrRepositoryDefinition);
	}

}
