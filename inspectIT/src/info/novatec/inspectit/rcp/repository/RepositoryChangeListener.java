package info.novatec.inspectit.rcp.repository;

import java.util.EventListener;

/**
 * The repository change listener which passes the following events: <b>added</b>, <b>removed</b>,
 * <b>updated</b>.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface RepositoryChangeListener extends EventListener {

	/**
	 * If a repository has been added.
	 * 
	 * @param repositoryDefinition
	 *            the repository definition.
	 */
	void repositoryAdded(RepositoryDefinition repositoryDefinition);

	/**
	 * If a repository has been removed.
	 * 
	 * @param repositoryDefinition
	 *            the repository definition.
	 */
	void repositoryRemoved(RepositoryDefinition repositoryDefinition);


	/**
	 * If a repository has been updated.
	 * 
	 * @param repositoryDefinition
	 *            the repository definition.
	 */
	void updateRepository(RepositoryDefinition repositoryDefinition);

	/**
	 * If the storage repository has been updated.
	 */
	void updateStorageRepository();

}
