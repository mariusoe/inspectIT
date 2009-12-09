package info.novatec.inspectit.rcp.model.combinedmetrics;

import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.ICombinedMetricsDataAccessService;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.model.Composite;
import info.novatec.inspectit.rcp.model.DeferredComposite;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IElementCollector;

/**
 * @author Patrice Bouillet
 * 
 */
public class DeferredVSACategoryComposite extends DeferredComposite {

	/**
	 * The data access service to retrieve needed data.
	 */
	private ICombinedMetricsDataAccessService dataAccessService;

	/**
	 * The repository definition.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * The template object.
	 */
	private TimerData template;

	private PlatformIdent platformIdent;

	private MethodSensorTypeIdent methodSensorType;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		try {
			Composite browserComposite = (Composite) object;

			monitor.beginTask("Loading of Combined Metrics Categories ...", IProgressMonitor.UNKNOWN);

			template = new TimerData();
			template.setPlatformIdent(platformIdent.getId());
			template.setSensorTypeIdent(methodSensorType.getId());
			// not needed currently
			// template.setMethodIdent(0L);
			template.setId(-1L);

			dataAccessService = repositoryDefinition.getCombinedMetricsDataAccessService();
			List<ParameterContentData> parameterContentDataList = dataAccessService.getWorkflows(template);

			List<String> workflowList = new ArrayList<String>(parameterContentDataList.size());
			for (ParameterContentData parameterContentData : parameterContentDataList) {
				String workflowName = parameterContentData.getContent();
				if (!workflowList.contains(workflowName)) {
					workflowList.add(workflowName);
				}

				monitor.worked(1);

				if (monitor.isCanceled()) {
					break;
				}
			}

			// create Workflow submenus
			Collections.sort(workflowList);
			for (String workflow : workflowList) {
				DeferredVSAWorkflowComposite workflowComposite = new DeferredVSAWorkflowComposite();
				workflowComposite.setRepositoryDefinition(repositoryDefinition);
				workflowComposite.setInputDefinition(getInputDefinition());
				workflowComposite.setName(workflow.substring(1, workflow.length()));
				workflowComposite.setPlatformIdent(platformIdent);
				workflowComposite.setWorkflowName(workflow.substring(1, workflow.length()));
				workflowComposite.setTemplate(template);
				workflowComposite.setMethodSensorType(methodSensorType);

				collector.add(workflowComposite, monitor);
				browserComposite.addChild(workflowComposite);

				if (monitor.isCanceled()) {
					break;
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * @param repositoryDefinition
	 *            the repositoryDefinition to set
	 */
	public void setRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		this.repositoryDefinition = repositoryDefinition;
	}

	/**
	 * @param methodSensorTypeIdent
	 */
	public void setMethodSensorType(MethodSensorTypeIdent methodSensorTypeIdent) {
		this.methodSensorType = methodSensorTypeIdent;
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
	public ImageDescriptor getImageDescriptor() {
		return InspectIT.getDefault().getImageDescriptor(InspectITConstants.VSA_LOGO);
	}

}
