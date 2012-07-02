package info.novatec.inspectit.rcp.model.combinedmetrics;

import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.ICombinedMetricsDataAccessService;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.CombinedMetricsInputDefinitionExtra;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import info.novatec.inspectit.rcp.model.Component;
import info.novatec.inspectit.rcp.model.Composite;
import info.novatec.inspectit.rcp.model.DeferredComposite;
import info.novatec.inspectit.rcp.model.Leaf;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.progress.IElementCollector;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class DeferredVSAWorkflowComposite extends DeferredComposite {

	private PlatformIdent platformIdent;

	/**
	 * The repository definition.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * The name of the workflow.
	 */
	private String workflowName = "";

	private ICombinedMetricsDataAccessService dataAccessService;

	private TimerData template;

	private MethodSensorTypeIdent methodSensorType;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		try {
			Composite workflowComposite = (Composite) object;
			monitor.beginTask("Loading of Activity Elements...", IProgressMonitor.UNKNOWN);

			dataAccessService = repositoryDefinition.getCombinedMetricsDataAccessService();
			List<ParameterContentData> parameterContentDataList = dataAccessService.getActivities(template, workflowName);

			List<String> activityList = new ArrayList<String>(parameterContentDataList.size());
			for (ParameterContentData parameterContentData : parameterContentDataList) {
				String activityName = parameterContentData.getContent();

				if (!activityList.contains(activityName)) {
					activityList.add(activityName);
				}

				monitor.worked(1);

				if (monitor.isCanceled()) {
					break;
				}
			}

			Collections.sort(activityList);
			for (String activity : activityList) {
				Component targetActivity = new Leaf();
				targetActivity.setName(activity.substring(1, activity.length()));
				targetActivity.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ACTIVITY));

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(repositoryDefinition);
				inputDefinition.setId(SensorTypeEnum.MARVIN_WORKFLOW);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setPartName("VSA Marvin (" + workflowName + " :: " + activity + ")");
				editorPropertiesData.setPartTooltip("VSA Marvin (" + workflowName + " :: " + activity + ")");
				editorPropertiesData.setImage(SensorTypeEnum.MARVIN_WORKFLOW.getImage());
				editorPropertiesData.setHeaderText("VSA Marvin (" + workflowName + " :: " + activity + ")");
				editorPropertiesData.setHeaderDescription(activity);
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				CombinedMetricsInputDefinitionExtra combinedMetricsExtra = new CombinedMetricsInputDefinitionExtra();
				combinedMetricsExtra.setWorkflow(workflowName);
				combinedMetricsExtra.setActivity(activity);
				inputDefinition.addInputDefinitonExtra(InputDefinitionExtrasMarkerFactory.COMBINED_METRICS_EXTRAS_MARKER, combinedMetricsExtra);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(methodSensorType.getId());
				inputDefinition.setIdDefinition(idDefinition);
				targetActivity.setInputDefinition(inputDefinition);

				collector.add(targetActivity, monitor);
				workflowComposite.addChild(targetActivity);

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
	 * 
	 * @param platformIdent
	 */
	public void setPlatformIdent(PlatformIdent platformIdent) {
		this.platformIdent = platformIdent;
	}

	/**
	 * @param workflowName
	 *            the workflowName to set
	 */
	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	/**
	 * @param template
	 */
	public void setTemplate(TimerData template) {
		this.template = template;
	}

	/**
	 * @param methodSensorTypeIdent
	 */
	public void setMethodSensorType(MethodSensorTypeIdent methodSensorTypeIdent) {
		this.methodSensorType = methodSensorTypeIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage() {
		return InspectIT.getDefault().getImage(InspectITImages.IMG_ACTIVITY);
	}

}
