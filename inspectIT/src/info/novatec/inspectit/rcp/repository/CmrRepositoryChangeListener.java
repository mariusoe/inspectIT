package info.novatec.inspectit.rcp.repository;

import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Extended {@link RepositoryChangeListener} only for events on the {@link CmrRepositoryDefinition}
 * s.
 * 
 * @author Ivan Senic
 * 
 */
public interface CmrRepositoryChangeListener extends RepositoryChangeListener {

	/**
	 * If the online status of the repository has been changed.
	 * 
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param oldStatus Old status.
	 * @param newStatus New status.
	 */
	void repositoryOnlineStatusUpdated(CmrRepositoryDefinition repositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus);

}
