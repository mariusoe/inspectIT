package info.novatec.inspectit.rcp.editor.table;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.AbstractSubView;
import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.FormRootEditor;
import info.novatec.inspectit.rcp.editor.table.input.TableInputController;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Sub-view which is used to create a table.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TableSubView extends AbstractSubView {

	/**
	 * The referenced input controller.
	 */
	private final TableInputController tableInputController;

	/**
	 * The created table viewer.
	 */
	private TableViewer tableViewer;

	/**
	 * Defines if a job is currently already executing.
	 */
	private volatile boolean jobInSchedule = false;

	/**
	 * Default constructor which needs a tree input controller to create all the
	 * content etc.
	 * 
	 * @param tableInputController
	 *            The table input controller.
	 */
	public TableSubView(TableInputController tableInputController) {
		Assert.isNotNull(tableInputController);

		this.tableInputController = tableInputController;
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		tableInputController.setInputDefinition(getRootEditor().getInputDefinition());

		Table table = toolkit.createTable(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer = new TableViewer(table);
		tableInputController.createColumns(tableViewer);
		tableViewer.setUseHashlookup(true);
		tableViewer.setContentProvider(tableInputController.getContentProvider());
		tableViewer.setLabelProvider(tableInputController.getLabelProvider());
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				tableInputController.doubleClick(event);
			}
		});
		tableViewer.setComparator(tableInputController.getComparator());
		if (null != tableViewer.getComparator()) {
			TableColumn[] tableColumns = tableViewer.getTable().getColumns();
			for (TableColumn tableColumn : tableColumns) {
				tableColumn.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						tableViewer.refresh();
					}
				});
			}
		}

		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		getRootEditor().getSite().registerContextMenu(FormRootEditor.ID + ".tablesubview", menuManager, tableViewer);
		Control control = tableViewer.getControl();
		Menu menu = menuManager.createContextMenu(control);
		control.setMenu(menu);

		Object input = tableInputController.getTableInput();
		tableViewer.setInput(input);
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		if (!jobInSchedule) {
			jobInSchedule = true;
			Job job = new Job("Retrieving Data from the CMR") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						tableInputController.doRefresh(monitor);
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								Object input = tableInputController.getTableInput();
								tableViewer.setInput(input);
								if (tableViewer.getTable().isVisible()) {
									tableViewer.refresh();
								}
							}
						});
					} catch (Throwable throwable) {
						throwable.printStackTrace();
						return Status.CANCEL_STATUS;
					} finally {
						jobInSchedule = false;
					}

					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDataInput(List<? extends DefaultData> data) {
		if (tableInputController.canOpenInput(data)) {
			tableViewer.setInput(data);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return tableViewer.getControl();
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelectionProvider getSelectionProvider() {
		return tableViewer;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PreferenceId> getPreferenceIds() {
		return tableInputController.getPreferenceIds();
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		if (PreferenceId.ITEMCOUNT.equals(preferenceEvent.getPreferenceId())) {
			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();
			int limit = (Integer) preferenceMap.get(PreferenceId.ItemCount.COUNT_SELECTION_ID);
			tableInputController.setLimit(limit);
			this.doRefresh();
		}
		
		tableInputController.preferenceEventFired(preferenceEvent);
		switch (preferenceEvent.getPreferenceId()) {
		case CLEAR_BUFFER:
			if (tableInputController.getPreferenceIds().contains(PreferenceId.CLEAR_BUFFER)) {
				tableViewer.refresh();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Returns the table input controller.
	 * 
	 * @return The table input controller.
	 */
	public TableInputController getTableInputController() {
		return tableInputController;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		tableInputController.dispose();
	}

}
