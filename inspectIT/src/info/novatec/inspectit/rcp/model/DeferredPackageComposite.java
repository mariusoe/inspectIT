package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IElementCollector;

/**
 * This class only initializes the sub-tree if it is requested. Furthermore, the
 * creation of the objects is done piece after piece, so that an immediate
 * visualization can be seen (important for sub-trees which are very large).
 * 
 * @author Patrice Bouillet
 * 
 */
public class DeferredPackageComposite extends DeferredComposite {

	/**
	 * All the classes which are being displayed in the sub-tree.
	 */
	private List<MethodIdent> classes = new ArrayList<MethodIdent>();

	/**
	 * The repository definition.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		try {
			Composite packageComposite = (Composite) object;
			monitor.beginTask("Loading of Class Elements...", IProgressMonitor.UNKNOWN);
			Map<String, DeferredClassComposite> classNames = new HashMap<String, DeferredClassComposite>(classes.size());

			for (MethodIdent clazz : classes) {
				String className = clazz.getClassName();
				if (!classNames.containsKey(className)) {
					DeferredClassComposite composite = new DeferredClassComposite();
					composite.setRepositoryDefinition(repositoryDefinition);
					composite.setName(className);

					collector.add(composite, monitor);
					packageComposite.addChild(composite);
					classNames.put(className, composite);
				}

				DeferredClassComposite composite = classNames.get(className);
				composite.addMethodToDisplay(clazz);

				if (monitor.isCanceled()) {
					break;
				}
			}

		} finally {
			monitor.done();
		}
	}

	/**
	 * Adds a class which will be displayed in this sub-tree.
	 * 
	 * @param methodIdent
	 *            The class to be displayed.
	 */
	public void addClassToDisplay(MethodIdent methodIdent) {
		classes.add(methodIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		this.repositoryDefinition = repositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryDefinition getRepositoryDefinition() {
		return repositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_PACKAGE);
	}

}
