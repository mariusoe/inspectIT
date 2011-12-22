package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Handler for navigation from the aggregated timer data to the plotting.
 * 
 * @author Ivan Senic
 * 
 */
public class NavigateToPlotting extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object selectedObject = selection.getFirstElement();
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		RepositoryDefinition repositoryDefinition = rootEditor.getInputDefinition().getRepositoryDefinition();
		InputDefinition inputDefinition = null;
		TimerData timerData;

		if (selectedObject instanceof TimerData) {
			timerData = (TimerData) selectedObject;
		} else if (selectedObject instanceof InvocationSequenceData) {
			InvocationSequenceData invoc = (InvocationSequenceData) selectedObject;
			if (invoc.getTimerData() != null) {
				timerData = invoc.getTimerData();
			} else {
				return null;
			}
		} else {
			return null;
		}

		MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(timerData.getMethodIdent());
		inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(repositoryDefinition);
		inputDefinition.setId(SensorTypeEnum.TIMER);
		inputDefinition.setPartName(SensorTypeEnum.TIMER.getDisplayName());
		inputDefinition.setPartTooltip(SensorTypeEnum.TIMER.getDisplayName());
		inputDefinition.setImageDescriptor(SensorTypeEnum.TIMER.getImageDescriptor());
		inputDefinition.setHeaderText(methodIdent.getPlatformIdent().getAgentName());
		inputDefinition.setHeaderDescription(TextFormatter.getMethodString(methodIdent));

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(timerData.getPlatformIdent());
		idDefinition.setSensorTypeId(timerData.getSensorTypeIdent());
		idDefinition.setMethodId(timerData.getMethodIdent());

		inputDefinition.setIdDefinition(idDefinition);

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

		return null;
	}

}
