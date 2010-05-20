package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.model.Component;
import info.novatec.inspectit.rcp.model.Composite;
import info.novatec.inspectit.rcp.model.DeferredComposite;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;

import java.io.File;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

public class DeleteStorageDataHandler extends AbstractStorageHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Display display = HandlerUtil.getActiveShell(event).getDisplay();

		ITreeSelection selection = (ITreeSelection) HandlerUtil.getActiveMenuSelection(event);
		TreePath path = (TreePath) selection.getPaths()[0];
		Composite composite = (Composite) path.getFirstSegment();
		final StorageRepositoryDefinition definition = findStorageRepositoryDefinition(composite);

		if (null != definition) {
			deleteDirectory(new File(definition.getPath()));

			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					InspectIT.getDefault().getRepositoryManager().updateStorageRepository();
				}
			});
		} else {
			InspectIT.getDefault().createErrorDialog("Cannot remove storage entry, no repository definition found!", null, -1);
		}

		return null;
	}

	/**
	 * Tries to find the storage repository definition in the hierarchy of
	 * composites.
	 * 
	 * @param composite
	 *            the root composite to search in.
	 * @return the storage repository definition.
	 */
	private StorageRepositoryDefinition findStorageRepositoryDefinition(Composite composite) {
		if (composite instanceof DeferredComposite) {
			DeferredComposite deferredComposite = (DeferredComposite) composite;
			RepositoryDefinition definition = deferredComposite.getRepositoryDefinition();
			if (definition instanceof StorageRepositoryDefinition) {
				return (StorageRepositoryDefinition) definition;
			}
		}

		if (composite.hasChildren()) {
			List<Component> components = composite.getChildren();
			for (Component component : components) {
				if (component instanceof Composite) {
					return findStorageRepositoryDefinition((Composite) component);
				}
			}
		}

		return null;
	}

}
