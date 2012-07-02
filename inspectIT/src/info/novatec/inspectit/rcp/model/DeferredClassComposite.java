package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.progress.IElementCollector;

import com.google.common.base.Objects;

/**
 * This class only initializes the sub-tree if it is requested. Furthermore, the creation of the
 * objects is done piece after piece, so that an immediate visualization can be seen (important for
 * sub-trees which are very large).
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
	protected static final String METHOD_FORMAT = "%s(%s)";

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
	public Image getImage() {
		return InspectIT.getDefault().getImage(InspectITImages.IMG_CLASS);
	}

	/**
	 * @return the methods
	 */
	protected List<MethodIdent> getMethods() {
		return methods;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), methods, repositoryDefinition);
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
		DeferredClassComposite that = (DeferredClassComposite) object;
		return Objects.equal(this.methods, that.methods)
				&& Objects.equal(this.repositoryDefinition, that.repositoryDefinition);
	}

}
