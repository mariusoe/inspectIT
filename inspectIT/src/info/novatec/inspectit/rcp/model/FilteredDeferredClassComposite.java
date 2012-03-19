package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.formatter.TextFormatter;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.progress.IElementCollector;

/**
 * This composite shows only one sensor for each method of the class that has a
 * {@link SensorTypeEnum} type.
 * 
 * @author Ivan Senic
 * 
 */
public class FilteredDeferredClassComposite extends DeferredClassComposite {

	/**
	 * Sensor to show.
	 */
	private SensorTypeEnum sensorTypeEnumToShow;

	/**
	 * @param sensorTypeEnum
	 *            Set the sensor type to show.
	 */
	public FilteredDeferredClassComposite(SensorTypeEnum sensorTypeEnum) {
		this.sensorTypeEnumToShow = sensorTypeEnum;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		try {
			List<MethodIdent> methods = getMethods();
			Composite classComposite = (Composite) object;
			monitor.beginTask("Loading of Method Elements...", methods.size());
			for (MethodIdent method : methods) {
				for (MethodSensorTypeIdent methodSensorTypeIdent : method.getMethodSensorTypeIdents()) {
					String fqn = methodSensorTypeIdent.getFullyQualifiedClassName();
					SensorTypeEnum sensorTypeEnum = SensorTypeEnum.get(fqn);
					if (sensorTypeEnum == sensorTypeEnumToShow) {
						if (sensorTypeEnum.isOpenable()) {
							Component targetSensorType = new Leaf();
							if (null != method.getParameters()) {
								String parameters = method.getParameters().toString();
								parameters = parameters.substring(1, parameters.length() - 1);

								targetSensorType.setName(String.format(METHOD_FORMAT, method.getMethodName(), parameters));
							} else {
								targetSensorType.setName(String.format(METHOD_FORMAT, method.getMethodName(), ""));
							}
							targetSensorType.setImageDescriptor(ModifiersImageFactory.getImageDescriptor(method.getModifiers()));

							InputDefinition inputDefinition = new InputDefinition();
							inputDefinition.setRepositoryDefinition(getRepositoryDefinition());
							inputDefinition.setId(sensorTypeEnum);
							EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
							editorPropertiesData.setPartName(sensorTypeEnum.getDisplayName());
							editorPropertiesData.setPartTooltip(sensorTypeEnum.getDisplayName());
							editorPropertiesData.setImageDescriptor(sensorTypeEnum.getImageDescriptor());
							editorPropertiesData.setHeaderText(method.getPlatformIdent().getAgentName());
							MethodIdent methodIdent = getRepositoryDefinition().getCachedDataService().getMethodIdentForId(method.getId());
							editorPropertiesData.setHeaderDescription(TextFormatter.getMethodString(methodIdent));
							inputDefinition.setEditorPropertiesData(editorPropertiesData);

							IdDefinition idDefinition = new IdDefinition();
							idDefinition.setPlatformId(method.getPlatformIdent().getId());
							idDefinition.setSensorTypeId(methodSensorTypeIdent.getId());
							idDefinition.setMethodId(method.getId());

							inputDefinition.setIdDefinition(idDefinition);
							targetSensorType.setInputDefinition(inputDefinition);

							collector.add(targetSensorType, monitor);
							classComposite.addChild(targetSensorType);
						}
						break;
					}
				}

				monitor.worked(1);
				if (monitor.isCanceled()) {
					break;
				}
			}
		} finally {
			monitor.done();
		}
	}

}
