package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
public class DeferredBrowserComposite extends DeferredComposite {

	/**
	 * The platform ident is used to create the package elements in the sub-tree.
	 */
	private PlatformIdent platformIdent;

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
			Composite browserComposite = (Composite) object;
			Set<MethodIdent> methodIdents = platformIdent.getMethodIdents();
			monitor.beginTask("Loading of Package Elements...", IProgressMonitor.UNKNOWN);
			Map<String, DeferredPackageComposite> packageNames = new HashMap<String, DeferredPackageComposite>(methodIdents.size());

			for (MethodIdent methodIdent : methodIdents) {
				if (select(methodIdent)) {
					String packageName = methodIdent.getPackageName();
					if (packageName == null) {
						packageName = "";
					} else {
						packageName = packageName.trim();
					}
					// check if the given package was already added.
					if (!packageNames.containsKey(packageName)) {
						DeferredPackageComposite composite = getNewChild();
						composite.setRepositoryDefinition(repositoryDefinition);
						if (packageName.equals("")) {
							composite.setName("(default)");
						} else {
							composite.setName(packageName);
						}

						collector.add(composite, monitor);
						browserComposite.addChild(composite);
						packageNames.put(packageName, composite);
					}

					DeferredPackageComposite composite = packageNames.get(packageName);
					composite.addClassToDisplay(methodIdent);
				}

				if (monitor.isCanceled()) {
					break;
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * @return Returns the right implementation of the {@link DeferredPackageComposite} to use for
	 *         the child.
	 */
	protected DeferredPackageComposite getNewChild() {
		return new DeferredPackageComposite();
	}

	/**
	 * Should this method ident pass the selection process.
	 * 
	 * @param methodIdent
	 *            {@link MethodIdent}.
	 * @return Should this method ident pass the selection process.
	 */
	protected boolean select(MethodIdent methodIdent) {
		return true;
	}

	/**
	 * The platform ident used to retrieve these packages.
	 * 
	 * @param platformIdent
	 *            the platformIdent to set
	 */
	public void setPlatformIdent(PlatformIdent platformIdent) {
		this.platformIdent = platformIdent;
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
		return InspectIT.getDefault().getImage(InspectITImages.IMG_INSTRUMENTATION_BROWSER);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), platformIdent, repositoryDefinition);
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
		DeferredBrowserComposite that = (DeferredBrowserComposite) object;
		return Objects.equal(this.platformIdent, that.platformIdent)
				&& Objects.equal(this.repositoryDefinition, that.repositoryDefinition);
	}

}
