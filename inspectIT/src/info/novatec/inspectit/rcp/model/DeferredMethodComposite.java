package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
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
				targetSensorType.setImage(sensorTypeEnum.getImage());

				if (sensorTypeEnum.isOpenable()) {
					InputDefinition inputDefinition = new InputDefinition();
					inputDefinition.setRepositoryDefinition(repositoryDefinition);
					inputDefinition.setId(sensorTypeEnum);

					EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
					editorPropertiesData.setPartName(sensorTypeEnum.getDisplayName());
					editorPropertiesData.setPartTooltip(sensorTypeEnum.getDisplayName());
					editorPropertiesData.setImage(sensorTypeEnum.getImage());
					editorPropertiesData.setHeaderText(method.getPlatformIdent().getAgentName());
					MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(method.getId());
					editorPropertiesData.setHeaderDescription(TextFormatter.getMethodString(methodIdent));
					inputDefinition.setEditorPropertiesData(editorPropertiesData);

					IdDefinition idDefinition = new IdDefinition();
					idDefinition.setPlatformId(method.getPlatformIdent().getId());
					idDefinition.setSensorTypeId(methodSensorTypeIdent.getId());
					idDefinition.setMethodId(method.getId());

					inputDefinition.setIdDefinition(idDefinition);
					targetSensorType.setInputDefinition(inputDefinition);
				}

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
		return ModifiersImageFactory.getImage(method.getModifiers());
	}

}
