package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.ExceptionTypeInputDefinitionExtra;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Handler that opens the only concrete exception type view.
 * 
 * @author Ivan Senic
 * 
 */
public class NavigateToExceptionTypeHandler extends AbstractHandler implements IHandler {

	/**
	 * Parameter that defines if view should be single or grouped.
	 */
	private static final String VIEW_PARAM_ID = "info.novatec.inspectit.rcp.commands.navigateToExceptionType.ViewType";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String viewType = event.getParameter(VIEW_PARAM_ID);

		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelectionChecked(event);
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		RepositoryDefinition repositoryDefinition = rootEditor.getInputDefinition().getRepositoryDefinition();

		Object selectedObject = selection.getFirstElement();
		ExceptionSensorData dataToNavigateTo = null;
		if (selectedObject instanceof ExceptionSensorData) {
			dataToNavigateTo = (ExceptionSensorData) selectedObject;
		} else if (selectedObject instanceof InvocationSequenceData) {
			List<ExceptionSensorData> exceptions = ((InvocationSequenceData) selectedObject).getExceptionSensorDataObjects();
			if (null != exceptions && !exceptions.isEmpty()) {
				for (ExceptionSensorData exSensorData : exceptions) {
					if (0 != exSensorData.getMethodIdent()) {
						dataToNavigateTo = exSensorData;
						break;
					}
				}
			}
		}

		if (null != dataToNavigateTo) {
			ExceptionSensorData exceptionSensorData = dataToNavigateTo;

			// exit if the object does not carry the methodIdent
			if (null == exceptionSensorData.getThrowableType()) {
				return null;
			}

			InputDefinition inputDefinition = new InputDefinition();
			inputDefinition.setRepositoryDefinition(repositoryDefinition);
			if ("single".equals(viewType)) {
				inputDefinition.setId(SensorTypeEnum.EXCEPTION_SENSOR);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setPartName("Exception Sensor");
				editorPropertiesData.setPartTooltip("Exception Sensor");
				editorPropertiesData.setImage(SensorTypeEnum.EXCEPTION_SENSOR.getImage());
				editorPropertiesData.setHeaderText("Throwable type");
				editorPropertiesData.setHeaderDescription(exceptionSensorData.getThrowableType());
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(exceptionSensorData.getPlatformIdent());
				inputDefinition.setIdDefinition(idDefinition);

				ExceptionTypeInputDefinitionExtra exceptionTypeInputDefinitionExtra = new ExceptionTypeInputDefinitionExtra();
				exceptionTypeInputDefinitionExtra.setThrowableType(exceptionSensorData.getThrowableType());
				inputDefinition.addInputDefinitonExtra(InputDefinitionExtrasMarkerFactory.EXCEPTION_TYPE_EXTRAS_MARKER, exceptionTypeInputDefinitionExtra);
			} else if ("grouped".equals(viewType)) {
				inputDefinition.setId(SensorTypeEnum.EXCEPTION_SENSOR_GROUPED);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setPartName("Exceptions (Grouped)");
				editorPropertiesData.setPartTooltip("Exceptions (Grouped)");
				editorPropertiesData.setImage(SensorTypeEnum.EXCEPTION_SENSOR_GROUPED.getImage());
				editorPropertiesData.setHeaderText("Throwable type");
				editorPropertiesData.setHeaderDescription(exceptionSensorData.getThrowableType());
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(exceptionSensorData.getPlatformIdent());
				inputDefinition.setIdDefinition(idDefinition);

				ExceptionTypeInputDefinitionExtra exceptionTypeInputDefinitionExtra = new ExceptionTypeInputDefinitionExtra();
				exceptionTypeInputDefinitionExtra.setThrowableType(exceptionSensorData.getThrowableType());
				inputDefinition.addInputDefinitonExtra(InputDefinitionExtrasMarkerFactory.EXCEPTION_TYPE_EXTRAS_MARKER, exceptionTypeInputDefinitionExtra);
			}

			// open the view via command
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

			Command command = commandService.getCommand(OpenViewHandler.COMMAND);
			ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
			IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
			context.addVariable(OpenViewHandler.INPUT, inputDefinition);

			try {
				command.executeWithChecks(executionEvent);
			} catch (Exception e) {
				InspectIT.getDefault().createErrorDialog(e.getMessage(), e, -1);
			}
		}
		return null;
	}
}
