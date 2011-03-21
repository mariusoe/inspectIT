package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.progress.IElementCollector;

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
				if (null != agents) {
					for (PlatformIdent platformIdent : agents) {
						Component agentLeaf = new AgentLeaf(platformIdent);
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

}
