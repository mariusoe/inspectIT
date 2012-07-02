package info.novatec.inspectit.rcp.model.combinedmetrics;

import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.model.Composite;
import info.novatec.inspectit.rcp.model.DeferredComposite;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.progress.IElementCollector;

/**
 * This class only initializes the sub-tree if it is requested. Furthermore, the creation of the
 * objects is done piece after piece, so that an immediate visualization can be seen (important for
 * sub-trees which are very large).
 * 
 * @author Eduard Tudenhoefner
 * @author Patrice Bouillet
 * 
 */
public class DeferredCombinedMetricsComposite extends DeferredComposite {

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

			monitor.beginTask("Loading of Combined Metrics Categories ...", IProgressMonitor.UNKNOWN);

			// create appropriate menus
			Set<SensorTypeIdent> sensorTypeIdents = platformIdent.getSensorTypeIdents();
			for (SensorTypeIdent sensorTypeIdent : sensorTypeIdents) {
				SensorTypeEnum sensorType = SensorTypeEnum.get(sensorTypeIdent.getFullyQualifiedClassName());
				switch (sensorType) {
				case MARVIN_WORKFLOW:
					MethodSensorTypeIdent methodSensorTypeIdent = (MethodSensorTypeIdent) sensorTypeIdent;
					// create VSA submenu
					DeferredVSACategoryComposite vsaComposite = new DeferredVSACategoryComposite();
					vsaComposite.setRepositoryDefinition(repositoryDefinition);
					vsaComposite.setInputDefinition(getInputDefinition());
					vsaComposite.setName("VSA Marvin");
					vsaComposite.setMethodSensorType(methodSensorTypeIdent);
					vsaComposite.setPlatformIdent(platformIdent);

					collector.add(vsaComposite, monitor);
					browserComposite.addChild(vsaComposite);

					break;
				default:
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

}
