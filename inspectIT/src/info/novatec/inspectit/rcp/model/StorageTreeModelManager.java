package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.service.storage.StoragePlatformIdent;
import info.novatec.inspectit.rcp.view.server.ServerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The manager is used to create a storage tree model currently used by the
 * {@link ServerView}.
 * 
 * @author Patrice Bouillet
 */
public class StorageTreeModelManager extends TreeModelManager {

	/**
	 * Every tree model manager needs a reference to a
	 * {@link RepositoryDefinition} which reflects a CMR.
	 * 
	 * @param repositoryDefinition
	 *            The definition of the repository / CMR.
	 */
	public StorageTreeModelManager(RepositoryDefinition repositoryDefinition) {
		super(repositoryDefinition);
	}

	/**
	 * Returns the root elements of this model.
	 * 
	 * @return The root elements.
	 */
	@SuppressWarnings("unchecked")
	public Object[] getRootElements() {
		List<Component> agents = new ArrayList<Component>();

		for (StoragePlatformIdent platformIdent : (List<StoragePlatformIdent>) repositoryDefinition.getGlobalDataAccessService().getConnectedAgents()) {
			// Important is that we need different repository definitions for
			// each entry instead of just one for the whole group!
			StorageRepositoryDefinition definition = new StorageRepositoryDefinition(platformIdent.getPath());

			Composite agentTree = new Composite();
			agentTree.setName(platformIdent.getFolderName() + " " + platformIdent.getAgentName() + " [v. " + platformIdent.getVersion() + "]");
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Name: ");
			stringBuilder.append(platformIdent.getAgentName());
			stringBuilder.append("\n");
			stringBuilder.append("[v. ");
			stringBuilder.append(platformIdent.getVersion());
			stringBuilder.append("]");
			stringBuilder.append("\n");
			Collections.sort(platformIdent.getDefinedIPs());
			for (String ip : (List<String>) platformIdent.getDefinedIPs()) {
				stringBuilder.append(ip);
				if (platformIdent.getDefinedIPs().indexOf(ip) != platformIdent.getDefinedIPs().size() - 1) {
					stringBuilder.append("\n");
				}
			}
			agentTree.setTooltip(stringBuilder.toString());
			agentTree.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_AGENT));

			// Add all sub-trees to this Agent
			agentTree.addChild(getInstrumentedMethodsTree(platformIdent, definition));
			agentTree.addChild(getInvocationSequenceTree(platformIdent, definition));

			// add it to the list of the agents
			agents.add(agentTree);
		}

		return agents.toArray();
	}

}
