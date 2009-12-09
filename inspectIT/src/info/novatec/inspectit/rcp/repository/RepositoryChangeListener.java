package info.novatec.inspectit.rcp.repository;

import java.util.EventListener;

public interface RepositoryChangeListener extends EventListener {
	
	void repositoryAdded(RepositoryDefinition repositoryDefinition);
	
	void repositoryRemoved(RepositoryDefinition repositoryDefinition);

	void updateRepository(RepositoryDefinition repositoryDefinition);

}
