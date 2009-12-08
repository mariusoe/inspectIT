package info.novatec.novaspy.rcp.model;

import info.novatec.novaspy.cmr.model.MethodIdent;
import info.novatec.novaspy.cmr.model.PlatformIdent;
import info.novatec.novaspy.rcp.NovaSpy;
import info.novatec.novaspy.rcp.NovaSpyConstants;
import info.novatec.novaspy.rcp.repository.RepositoryDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
public class DeferredInstrumentationBrowserComposite extends DeferredComposite {

	/**
	 * The platform ident is used to create the package elements in the
	 * sub-tree.
	 */
	private PlatformIdent platformIdent;

	/**
	 * The repository definition.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		try {
			Composite browserComposite = (Composite) object;
			Set<MethodIdent> methodIdents = platformIdent.getMethodIdents();
			monitor.beginTask("Loading of Package Elements...", IProgressMonitor.UNKNOWN);
			Map<String, DeferredPackageComposite> packageNames = new HashMap<String, DeferredPackageComposite>(methodIdents.size());

			for (MethodIdent methodIdent : methodIdents) {
				String packageName = methodIdent.getPackageName().trim();
				// check if the given package was already added.
				if (!packageNames.containsKey(packageName)) {
					DeferredPackageComposite composite = new DeferredPackageComposite();
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

				if (monitor.isCanceled()) {
					break;
				}
			}
		} finally {
			monitor.done();
		}
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
		return NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_INSTRUMENTATION_BROWSER);
	}

}
