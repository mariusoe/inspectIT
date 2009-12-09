package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.List;

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
public class DeferredClassComposite extends DeferredComposite {

	/**
	 * All the methods which are displayed in the tree.
	 */
	private List<MethodIdent> methods = new ArrayList<MethodIdent>();

	/**
	 * The format string of the output.
	 */
	private static final String METHOD_FORMAT = "%s(%s)";

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
			Composite classComposite = (Composite) object;
			monitor.beginTask("Loading of Method Elements...", methods.size());

			for (MethodIdent method : methods) {
				DeferredMethodComposite composite = new DeferredMethodComposite();
				composite.setRepositoryDefinition(repositoryDefinition);

				if (null != method.getParameters()) {
					String parameters = method.getParameters().toString();
					parameters = parameters.substring(1, parameters.length() - 1);

					composite.setName(String.format(METHOD_FORMAT, method.getMethodName(), parameters));
				} else {
					composite.setName(String.format(METHOD_FORMAT, method.getMethodName(), ""));
				}
				composite.setMethod(method);

				collector.add(composite, monitor);
				classComposite.addChild(composite);
				monitor.worked(1);

				if (monitor.isCanceled()) {
					break;
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Adds a method to be displayed later in this sub-tree.
	 * 
	 * @param methodIdent
	 *            The method to be displayed.
	 */
	public void addMethodToDisplay(MethodIdent methodIdent) {
		methods.add(methodIdent);
	}

	/**
	 * @param repositoryDefinition
	 *            the repositoryDefinition to set
	 */
	public void setRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		this.repositoryDefinition = repositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_CLASS);
	}

}
