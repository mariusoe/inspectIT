package info.novatec.inspectit.rcp.model.combinedmetrics;

import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.ICombinedMetricsDataAccessService;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.InputDefinition.IdDefinition;
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
import org.eclipse.jface.resource.ImageDescriptor;
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
				targetActivity.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_ACTIVITY));

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(repositoryDefinition);
				inputDefinition.setId(SensorTypeEnum.MARVIN_WORKFLOW);
				inputDefinition.setPartName("VSA Marvin (" + workflowName + " :: " + activity + ")");
				inputDefinition.setPartTooltip("VSA Marvin (" + workflowName + " :: " + activity + ")");
				inputDefinition.setImageDescriptor(SensorTypeEnum.MARVIN_WORKFLOW.getImageDescriptor());
				inputDefinition.setHeaderText("VSA Marvin (" + workflowName + " :: " + activity + ")");
				inputDefinition.setHeaderDescription(activity);
				inputDefinition.addAdditionalOption("Workflow", workflowName);
				inputDefinition.addAdditionalOption("Activity", activity);

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
	public ImageDescriptor getImageDescriptor() {
		return InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_ACTIVITY);
	}

}
