package info.novatec.inspectit.rcp.editor.exception;

import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.exception.input.ExceptionMessagesTreeInputController;
import info.novatec.inspectit.rcp.editor.exception.input.ExceptionOverviewInputController.ExtendedExceptionSensorData;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class DeferredStackTraces implements IDeferredWorkbenchAdapter {

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		ExtendedExceptionSensorData exceptionSensorData = (ExtendedExceptionSensorData) object;

		if (null != exceptionSensorData.getExceptionEventString()) {
			// DB is only queried when the exception eventType is set.
			// This check is basically to prevent that DB gets queried when we
			// have a data object just containing the stack trace. Having such
			// an object means that there are no more children to retrieve.

			InputDefinition inputDefinition = exceptionSensorData.getInputDefinition();

			ExceptionSensorData template = new ExceptionSensorData();
			template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
			template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
			template.setMethodIdent(inputDefinition.getIdDefinition().getMethodId());
			IExceptionDataAccessService dataAccessService = inputDefinition.getRepositoryDefinition().getExceptionDataAccessService();

			template.setErrorMessage(exceptionSensorData.getErrorMessage());
			List<ExceptionSensorData> stackTraces = (List<ExceptionSensorData>) dataAccessService.getStackTracesForErrorMessage(template);

			List<ExtendedExceptionSensorData> result = convertObjects(stackTraces);
			monitor.beginTask("Loading of Exception Sensor Data Objects...", result.size());

			collector.add(result.toArray(), monitor);
			monitor.done();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ISchedulingRule getRule(Object object) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isContainer() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getChildren(Object o) {
		return new Object[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel(Object o) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParent(Object o) {
		return o;
	}

	/**
	 * Converts a list of {@link ExceptionSensorData} objects to a list of
	 * {@link ExtendedExceptionSensorData} objects in order to be shown in the
	 * {@link ExceptionMessagesTreeInputController}.
	 * 
	 * @param dataList
	 *            The list containing {@link ExceptionSensorData} objects to be
	 *            converted.
	 * @return A list of {@link ExtendedExceptionSensorData} objects in order to
	 *         be shown in the {@link ExceptionMessagesTreeInputController}.
	 */
	private List<ExtendedExceptionSensorData> convertObjects(List<ExceptionSensorData> dataList) {
		List<ExtendedExceptionSensorData> convertedObjects = null;
		if (null != dataList) {
			convertedObjects = new ArrayList<ExtendedExceptionSensorData>();

			for (ExceptionSensorData data : dataList) {
				ExtendedExceptionSensorData obj = new ExtendedExceptionSensorData();
				obj.setStackTrace(data.getStackTrace());
				obj.setCreatedCounter(-1);
				obj.setRethrownCounter(-1);
				obj.setHandledCounter(-1);
				convertedObjects.add(obj);
			}
		}
		return convertedObjects;
	}

}
