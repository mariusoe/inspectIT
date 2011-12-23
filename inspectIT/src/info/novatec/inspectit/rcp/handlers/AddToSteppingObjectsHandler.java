package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.rcp.editor.composite.AbstractCompositeSubView;
import info.novatec.inspectit.rcp.editor.composite.TabbedCompositeSubView;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.editor.tree.SteppingTreeSubView;
import info.novatec.inspectit.rcp.util.OccurrenceFinderFactory;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for adding the new stepping object to the list of the stepping objects in the stepping
 * tree sub view.
 * 
 * @author Ivan Senic
 * 
 */
public class AddToSteppingObjectsHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		if (activeEditor instanceof AbstractRootEditor) {
			AbstractRootEditor rootEditor = (AbstractRootEditor) activeEditor;
			SteppingTreeSubView steppingTreeSubView = findSteppingSubView(rootEditor.getSubView());
			if (steppingTreeSubView != null) {
				Iterator<?> iterator = ((StructuredSelection) HandlerUtil.getCurrentSelection(event)).iterator();
				while (iterator.hasNext()) {
					Object element = iterator.next();
					if (element instanceof InvocationSequenceData) {
						InvocationSequenceData invData = (InvocationSequenceData) element;
						if (invData.getSqlStatementData() != null) {
							steppingTreeSubView.addObjectToSteppingControl(createTemplate(invData.getSqlStatementData()));
							steppingTreeSubView.addObjectToSteppingControl(createTemplate(getTimerDataFromSql(invData.getSqlStatementData())));
							// HttpTimerData elements should not be added to the list of locate-able
							// elements.
						} else if (invData.getTimerData() != null && !invData.getTimerData().getClass().equals(HttpTimerData.class)) {
							steppingTreeSubView.addObjectToSteppingControl(createTemplate(invData.getTimerData()));
						} else if (invData.getExceptionSensorDataObjects() != null && invData.getExceptionSensorDataObjects().isEmpty()) {
							steppingTreeSubView.addObjectToSteppingControl(createTemplate(invData.getExceptionSensorDataObjects().get(0)));
						}

					} else {
						steppingTreeSubView.addObjectToSteppingControl(createTemplate(element));
					}

				}

				// switch this to tree tab
				ISubView sashSubView = rootEditor.getSubView();
				if (sashSubView instanceof AbstractCompositeSubView) {
					for (ISubView subView : ((AbstractCompositeSubView) sashSubView).getSubViews()) {
						if (subView instanceof TabbedCompositeSubView) {
							CTabFolder tabFolder = (CTabFolder) subView.getControl();
							for (CTabItem tabItem : tabFolder.getItems()) {
								if (tabItem.getControl().equals(steppingTreeSubView.getControl())) {
									tabFolder.setSelection(tabItem);
									break;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Creates a steppable template from a {@link Object}.
	 * 
	 * @param invocationAwareData
	 *            {@link Object}
	 * @return Templates to be used as steppable object.
	 */
	private Object createTemplate(Object invocationAwareData) {
		if (invocationAwareData instanceof SqlStatementData) {
			SqlStatementData template = OccurrenceFinderFactory.getEmptyTemplate((SqlStatementData) invocationAwareData);
			template.setSql(((SqlStatementData) invocationAwareData).getSql());
			template.setMethodIdent(((SqlStatementData) invocationAwareData).getMethodIdent());
			return template;
		} else if (invocationAwareData instanceof TimerData) {
			TimerData template = OccurrenceFinderFactory.getEmptyTemplate((TimerData) invocationAwareData);
			template.setMethodIdent(((TimerData) invocationAwareData).getMethodIdent());
			return template;
		} else if (invocationAwareData instanceof ExceptionSensorData) {
			ExceptionSensorData template = OccurrenceFinderFactory.getEmptyTemplate((ExceptionSensorData) invocationAwareData);
			template.setExceptionEvent(ExceptionEvent.CREATED);
			template.setThrowableType(((ExceptionSensorData) invocationAwareData).getThrowableType());
			return template;
		}
		return null;
	}

	/**
	 * Tries to find the {@link SteppingTreeSubView} in the current editor's sub view.
	 * 
	 * @param subView
	 *            editor main sub view
	 * @return {@link SteppingTreeSubView} instance if found, otherwise null.
	 */
	private SteppingTreeSubView findSteppingSubView(ISubView subView) {
		if (subView instanceof SteppingTreeSubView) {
			return (SteppingTreeSubView) subView;
		} else if (subView instanceof AbstractCompositeSubView) {
			AbstractCompositeSubView compositeSubView = (AbstractCompositeSubView) subView;
			for (ISubView viewInCompositeSubView : compositeSubView.getSubViews()) {
				SteppingTreeSubView foundView = findSteppingSubView(viewInCompositeSubView);
				if (null != foundView) {
					return foundView;
				}
			}
		}
		return null;
	}

	/**
	 * Creates new {@link TimerData} instance with method indent copied from given SQL.
	 * 
	 * @param sqlStatementData
	 *            SQL to provide the method ident.
	 * @return Timer data.
	 */
	private TimerData getTimerDataFromSql(SqlStatementData sqlStatementData) {
		TimerData timerData = new TimerData();
		timerData.setMethodIdent(sqlStatementData.getMethodIdent());
		return timerData;
	}

}
