package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

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
public class DeferredMethodComposite extends DeferredComposite {

	/**
	 * This method is needed to load all the sensor types for this method.
	 */
	private MethodIdent method;

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
			Composite composite = (Composite) object;
			Set<MethodSensorTypeIdent> methodSensorTypeIdents = method.getMethodSensorTypeIdents();
			monitor.beginTask("Loading of Sensor Type Elements...", methodSensorTypeIdents.size());

			for (MethodSensorTypeIdent methodSensorTypeIdent : methodSensorTypeIdents) {
				Component targetSensorType = new Leaf();
				String fqn = methodSensorTypeIdent.getFullyQualifiedClassName();
				SensorTypeEnum sensorTypeEnum = SensorTypeEnum.get(fqn);
				targetSensorType.setName(sensorTypeEnum.getDisplayName());
				targetSensorType.setImageDescriptor(sensorTypeEnum.getImageDescriptor());

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(repositoryDefinition);
				inputDefinition.setId(sensorTypeEnum);
				inputDefinition.setPartName(sensorTypeEnum.getDisplayName());
				inputDefinition.setPartTooltip(sensorTypeEnum.getDisplayName());
				inputDefinition.setImageDescriptor(sensorTypeEnum.getImageDescriptor());
				inputDefinition.setHeaderText(method.getPlatformIdent().getAgentName());
				MethodIdent methodIdent = repositoryDefinition.getGlobalDataAccessService().getMethodIdentForId(method.getId());
				inputDefinition.setHeaderDescription(TextFormatter.getMethodString(methodIdent));

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(method.getPlatformIdent().getId());
				idDefinition.setSensorTypeId(methodSensorTypeIdent.getId());
				idDefinition.setMethodId(method.getId());

				inputDefinition.setIdDefinition(idDefinition);
				targetSensorType.setInputDefinition(inputDefinition);

				collector.add(targetSensorType, monitor);
				composite.addChild(targetSensorType);

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
	 * @param method
	 *            the method to set
	 */
	public void setMethod(MethodIdent method) {
		this.method = method;
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
		return ModifiersImageFactory.getImageDescriptor(method.getModifiers());
	}

}
