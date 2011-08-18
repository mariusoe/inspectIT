package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEventEnum;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.editor.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.util.OccurrenceFinderFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * Handler for navigating from table view that contains {@link IInvocationAwareData}, to the
 * invocation sequence view.
 * 
 * @author Ivan Senic
 * 
 */
public class NavigateToInvocationsHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		RepositoryDefinition repositoryDefinition = rootEditor.getInputDefinition().getRepositoryDefinition();
		InputDefinition inputDefinition = null;

		List<InvocationAwareData> invocationAwareDataList = new ArrayList<InvocationAwareData>();
		String textualDesc = null;
		int selectionSize = selection.size();
		int invocationsCount = 0;
		long platformIdent = getPlatformIdent(selection.getFirstElement());

		if (selectionSize > 1) {
			textualDesc = "multiple selected objects";
		}
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			Object selectedObject = iterator.next();
			if (selectedObject instanceof InvocationAwareData) {
				InvocationAwareData invocationAwareData = (InvocationAwareData) selectedObject;
				if (1 == selectionSize) {
					textualDesc = TextFormatter.getInvocationAwareDataTextualRepresentation(invocationAwareData, repositoryDefinition);
				}
				if (null != invocationAwareData.getInvocationParentsIdSet()) {
					invocationAwareDataList.add(invocationAwareData);
					invocationsCount += invocationAwareData.getInvocationParentsIdSet().size();
				}
			}
		}

		if (invocationsCount > 0) {
			inputDefinition = new InputDefinition();
			inputDefinition.setRepositoryDefinition(repositoryDefinition);
			inputDefinition.setId(SensorTypeEnum.NAVIGATION_INVOCATION);
			inputDefinition.setPartName("Invocation Sequences");
			inputDefinition.setPartTooltip("Invocation Sequences (that contain " + textualDesc + ")");
			inputDefinition.setImageDescriptor(SensorTypeEnum.INVOCATION_SEQUENCE.getImageDescriptor());
			inputDefinition.setHeaderText("Invocation Sequences");
			if (invocationsCount > 1) {
				inputDefinition.setHeaderDescription("Show All  (that contain " + textualDesc + ")");
			} else if (invocationsCount == 1) {
				inputDefinition.setHeaderDescription("Show One  (that contains " + textualDesc + ")");
			}
			inputDefinition.addAdditionalOption("invocationAwareDataList", invocationAwareDataList);
			inputDefinition.addAdditionalOption("steppingObjects", getTemplates(invocationAwareDataList));
			IdDefinition idDefinition = new IdDefinition();
			idDefinition.setPlatformId(platformIdent);
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
		}

		return null;
	}

	/**
	 * Creates a steppable template from a list of {@link InvocationAwareData}.
	 * 
	 * @param invocationAwareDataList
	 *            {@link InvocationAwareData} list.
	 * @return Templates to be used as steppable objects.
	 */
	private List<Object> getTemplates(List<InvocationAwareData> invocationAwareDataList) {
		List<Object> steppableTemplates = new ArrayList<Object>();
		for (InvocationAwareData invocationAwareData : invocationAwareDataList) {
			if (invocationAwareData instanceof SqlStatementData) {
				SqlStatementData template = OccurrenceFinderFactory.getEmptyTemplate((SqlStatementData) invocationAwareData);
				template.setSql(((SqlStatementData) invocationAwareData).getSql());
				template.setMethodIdent(((SqlStatementData) invocationAwareData).getMethodIdent());
				steppableTemplates.add(template);
			} else if (invocationAwareData instanceof TimerData) {
				TimerData template = OccurrenceFinderFactory.getEmptyTemplate((TimerData) invocationAwareData);
				template.setMethodIdent(((TimerData) invocationAwareData).getMethodIdent());
				steppableTemplates.add(template);
			} else if (invocationAwareData instanceof ExceptionSensorData) {
				ExceptionSensorData template = OccurrenceFinderFactory.getEmptyTemplate((ExceptionSensorData) invocationAwareData);
				template.setExceptionEvent(ExceptionEventEnum.CREATED);
				template.setThrowableType(((ExceptionSensorData) invocationAwareData).getThrowableType());
				steppableTemplates.add(template);
			}
		}
		return steppableTemplates;
	}

	/**
	 * Returns the platform id for the object.
	 * 
	 * @param firstElement
	 *            Object.
	 * @return If object is instance of {@link DefaultData} method returns its platform id,
	 *         otherwise 0.
	 */
	private long getPlatformIdent(Object firstElement) {
		if (firstElement instanceof DefaultData) {
			return ((DefaultData) firstElement).getPlatformIdent();
		}
		return 0;
	}

}
