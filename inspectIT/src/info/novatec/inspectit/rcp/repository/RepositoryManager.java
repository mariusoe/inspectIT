package info.novatec.inspectit.rcp.repository;

import info.novatec.inspectit.rcp.preferences.PreferencesUtils;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.util.ListenerList;
import info.novatec.inspectit.rcp.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * The repository manager stores all the repository definitions.
 * 
 * @author Patrice Bouillet
 * 
 */
public class RepositoryManager {

	/**
	 * Update online repository status job repetition time in milliseconds.
	 */
	public static final long UPDATE_JOB_REPETITION = 60000;

	/**
	 * The list containing the available {@link RepositoryDefinition} objects.
	 */
	private List<RepositoryDefinition> repositoryDefinitions = new ArrayList<RepositoryDefinition>();

	/**
	 * The list of listeners to be notified.
	 */
	private ListenerList<RepositoryChangeListener> repositoryChangeListeners = new ListenerList<RepositoryChangeListener>();

	/**
	 * Map of jobs.
	 */
	private Map<RepositoryDefinition, UpdateRepositoryJob> repositoryUpdateJobMap = new ConcurrentHashMap<RepositoryDefinition, UpdateRepositoryJob>();

	/**
	 * Starts the repository manager (e.g. loads all the saved data).
	 */
	public void startup() {
		List<CmrRepositoryDefinition> savedCmrs = PreferencesUtils.getCmrRepositoryDefinitions();
		repositoryDefinitions.addAll(savedCmrs);
		for (RepositoryDefinition repositoryDefinition : repositoryDefinitions) {
			if (repositoryDefinition instanceof CmrRepositoryDefinition) {
				UpdateRepositoryJob updateRepositoryJob = new UpdateRepositoryJob((CmrRepositoryDefinition) repositoryDefinition, true);
				updateRepositoryJob.schedule();
				repositoryUpdateJobMap.put(repositoryDefinition, updateRepositoryJob);
			}
		}

	}

	/**
	 * Adds a repository definition handled by this manager.
	 * 
	 * @param repositoryDefinition
	 *            The definition to add.
	 */
	public void addRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		if (!repositoryDefinitions.contains(repositoryDefinition)) {
			repositoryDefinitions.add(repositoryDefinition);

			savePreference();

			for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
				repositoryChangeListener.repositoryAdded(repositoryDefinition);
				if (repositoryDefinition instanceof CmrRepositoryDefinition && repositoryChangeListener instanceof CmrRepositoryChangeListener) {
					((CmrRepositoryDefinition) repositoryDefinition).addCmrRepositoryChangeListener((CmrRepositoryChangeListener) repositoryChangeListener);
				}
			}

			if (repositoryDefinition instanceof CmrRepositoryDefinition) {
				UpdateRepositoryJob updateRepositoryJob = new UpdateRepositoryJob((CmrRepositoryDefinition) repositoryDefinition, true);
				updateRepositoryJob.schedule();
				repositoryUpdateJobMap.put(repositoryDefinition, updateRepositoryJob);
			}
		}
	}

	/**
	 * Removes a repository definition and notifies all registered listeners.
	 * 
	 * @param repositoryDefinition
	 *            The definition to remove.
	 */
	public void removeRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		repositoryDefinitions.remove(repositoryDefinition);

		savePreference();

		for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
			repositoryChangeListener.repositoryRemoved(repositoryDefinition);
		}

		UpdateRepositoryJob updateRepositoryJob = repositoryUpdateJobMap.remove(repositoryDefinition);
		if (null != updateRepositoryJob) {
			updateRepositoryJob.cancel();
		}
	}

	/**
	 * Notifies all listeners that a certain repository definition has been updated.
	 * 
	 * @param repositoryDefinition
	 *            The definition which was updated.
	 */
	public void updateRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			// first re-schedule the already existing job
			UpdateRepositoryJob existingUpdateRepositoryJob = repositoryUpdateJobMap.get(repositoryDefinition);
			if (null != existingUpdateRepositoryJob) {
				if (existingUpdateRepositoryJob.cancel()) {
					existingUpdateRepositoryJob.schedule(UPDATE_JOB_REPETITION);
				}

			}
			// then execute a short term job for updating and signal to also refresh the repository
			final CmrRepositoryDefinition cmrRepositoryDefinition = (CmrRepositoryDefinition) repositoryDefinition;
			UpdateRepositoryJob updateRepositoryJob = new UpdateRepositoryJob(cmrRepositoryDefinition, false) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					OnlineStatus oldStatus = cmrRepositoryDefinition.getOnlineStatus();
					IStatus status = super.run(monitor);
					OnlineStatus newStatus = cmrRepositoryDefinition.getOnlineStatus();
					// only if status did not change, signal to refresh the view
					if (ObjectUtils.equals(newStatus, oldStatus)) {
						for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
							repositoryChangeListener.updateRepository(cmrRepositoryDefinition);
						}
					}
					return status;
				}
			};
			updateRepositoryJob.schedule();
		} else if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			updateStorageRepository();
		}
	}

	/**
	 * Notifies all listeners that the storage repository has been updated.
	 */
	public void updateStorageRepository() {
		for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
			repositoryChangeListener.updateStorageRepository();
		}
	}

	/**
	 * Forces the CMR Online update check. If the {@link CmrRepositoryDefinition} to check is not on
	 * the current list of repositories, this method will create a small job to check online status,
	 * but this job won't be rescheduled.
	 * 
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	public void forceCmrRepositoryOnlineStatusUpdate(final CmrRepositoryDefinition repositoryDefinition) {
		UpdateRepositoryJob updateRepositoryJob = repositoryUpdateJobMap.get(repositoryDefinition);
		if (null != updateRepositoryJob) {
			if (updateRepositoryJob.cancel()) {
				updateRepositoryJob.schedule();
			}
		}
	}

	/**
	 * Returns all registered repository definitions handled by this manager. The list is
	 * unmodifiable.
	 * 
	 * @return The list of repository definitions.
	 */
	public List<RepositoryDefinition> getRepositoryDefinitions() {
		return Collections.unmodifiableList(repositoryDefinitions);
	}

	/**
	 * Adds a listener which notifies on certain events.
	 * 
	 * @param repositoryChangeListener
	 *            The listener to add.
	 */
	public void addRepositoryChangeListener(RepositoryChangeListener repositoryChangeListener) {
		repositoryChangeListeners.add(repositoryChangeListener);
		if (repositoryChangeListener instanceof CmrRepositoryChangeListener) {
			for (RepositoryDefinition repositoryDefinition : repositoryDefinitions) {
				if (repositoryDefinition instanceof CmrRepositoryDefinition) {
					((CmrRepositoryDefinition) repositoryDefinition).addCmrRepositoryChangeListener((CmrRepositoryChangeListener) repositoryChangeListener);
				}
			}
		}
	}

	/**
	 * Removes the listener.
	 * 
	 * @param repositoryChangeListener
	 *            The listener to remove.
	 */
	public void removeRepositoryChangeListener(RepositoryChangeListener repositoryChangeListener) {
		repositoryChangeListeners.remove(repositoryChangeListener);
		if (repositoryChangeListener instanceof CmrRepositoryChangeListener) {
			for (RepositoryDefinition repositoryDefinition : repositoryDefinitions) {
				if (repositoryDefinition instanceof CmrRepositoryDefinition) {
					((CmrRepositoryDefinition) repositoryDefinition).removeCmrRepositoryChangeListener((CmrRepositoryChangeListener) repositoryChangeListener);
				}
			}
		}
	}

	/**
	 * Save the preferences to the backend store.
	 */
	private void savePreference() {
		List<CmrRepositoryDefinition> toSave = new ArrayList<CmrRepositoryDefinition>();
		for (RepositoryDefinition repositoryDefinition : repositoryDefinitions) {
			if (repositoryDefinition instanceof CmrRepositoryDefinition) {
				toSave.add((CmrRepositoryDefinition) repositoryDefinition);
			}
		}
		PreferencesUtils.saveCmrRepositoryDefinitions(toSave, false);
	}

	/**
	 * Update online status of all repositories job.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class UpdateRepositoryJob extends Job {

		/**
		 * CMR to update.
		 */
		private CmrRepositoryDefinition cmrRepositoryDefinition;

		/**
		 * Should job be rescheduled after its execution.
		 */
		private boolean rescheduleJob;

		/**
		 * Default constructor.
		 * 
		 * @param cmrRepositoryDefinition
		 *            {@link CmrRepositoryDefinition} to update.
		 * @param rescheduleJob
		 *            If job should be rescheduled after execution.
		 */
		public UpdateRepositoryJob(CmrRepositoryDefinition cmrRepositoryDefinition, boolean rescheduleJob) {
			super("Updating online status of CMR repository " + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort());
			this.cmrRepositoryDefinition = cmrRepositoryDefinition;
			this.rescheduleJob = rescheduleJob;
			this.setUser(false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				cmrRepositoryDefinition.refreshOnlineStatus();
				return Status.OK_STATUS;
			} finally {
				if (rescheduleJob) {
					this.schedule(UPDATE_JOB_REPETITION);
				}
			}
		}
	}

}
