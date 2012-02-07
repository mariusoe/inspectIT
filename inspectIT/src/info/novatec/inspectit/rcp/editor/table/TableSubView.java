package info.novatec.inspectit.rcp.editor.table;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.AbstractSubView;
import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceConstants;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.editor.root.FormRootEditor;
import info.novatec.inspectit.rcp.editor.root.SubViewClassificationController.SubViewClassification;
import info.novatec.inspectit.rcp.editor.table.input.TableInputController;
import info.novatec.inspectit.rcp.handlers.ShowHideColumnsHandler;
import info.novatec.inspectit.rcp.menu.ShowHideMenuManager;

import java.util.ArrayList;
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
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.Form;
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
	 * Default constructor which needs a tree input controller to create all the content etc.
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

		final Table table = toolkit.createTable(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
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

		// add show hide columns support
		MenuManager headerMenuManager = new ShowHideMenuManager(tableViewer, tableInputController.getClass());
		headerMenuManager.setRemoveAllWhenShown(false);

		// normal selection menu manager
		MenuManager selectionMenuManager = new MenuManager();
		selectionMenuManager.setRemoveAllWhenShown(true);
		getRootEditor().getSite().registerContextMenu(FormRootEditor.ID + ".tablesubview", selectionMenuManager, tableViewer);

		final Menu selectionMenu = selectionMenuManager.createContextMenu(table);
		final Menu headerMenu = headerMenuManager.createContextMenu(table);

		table.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Point pt = Display.getDefault().map(null, table, new Point(event.x, event.y));
				Rectangle clientArea = table.getClientArea();
				boolean header = clientArea.y <= pt.y && pt.y < (clientArea.y + table.getHeaderHeight());
				if (header) {
					table.setMenu(headerMenu);
				} else {
					table.setMenu(selectionMenu);
				}
			}
		});

		/**
		 * IMPORTANT: Only the menu set in the setMenu() will be disposed automatically.
		 */
		table.addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!headerMenu.isDisposed()) {
					headerMenu.dispose();
				}
				if (!selectionMenu.isDisposed()) {
					selectionMenu.dispose();
				}
			}
		});

		Object input = tableInputController.getTableInput();
		tableViewer.setInput(input);

		if (getPreferenceIds().contains(PreferenceId.ITEMCOUNT)) {
			updateCountItemsMessage(PreferenceConstants.DEFAULT_ITEM_COUNT);
		}

		ControlAdapter columnResizeListener = new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if (e.widget instanceof TableColumn) {
					TableColumn column = (TableColumn) e.widget;
					if (column.getWidth() > 0) {
						ShowHideColumnsHandler.registerNewColumnWidth(tableInputController.getClass(), column.getText(), column.getWidth());
					}
				}
			}

			@Override
			public void controlMoved(ControlEvent e) {
				ShowHideColumnsHandler.setColumnOrder(tableInputController.getClass(), tableViewer.getTable().getColumnOrder());
			}
		};

		for (TableColumn column : table.getColumns()) {
			Integer rememberedWidth = ShowHideColumnsHandler.getRememberedColumnWidth(tableInputController.getClass(), column.getText());
			boolean isColumnHidden = ShowHideColumnsHandler.isColumnHidden(tableInputController.getClass(), column.getText());

			if (rememberedWidth != null && !isColumnHidden) {
				column.setWidth(rememberedWidth.intValue());
				column.setResizable(true);
			} else if (isColumnHidden) {
				column.setWidth(0);
				column.setResizable(false);
			}

			column.addControlListener(columnResizeListener);
		}

		// update the order of columns if the order was defined for the class, and no new columns
		// were added
		int[] columnOrder = ShowHideColumnsHandler.getColumnOrder(tableInputController.getClass());
		if (null != columnOrder && columnOrder.length == table.getColumns().length) {
			table.setColumnOrder(columnOrder);
		} else if (null != columnOrder) {
			// if the order exists, but length is not same, then update with the default order
			ShowHideColumnsHandler.setColumnOrder(tableInputController.getClass(), table.getColumnOrder());
		}
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
								// refresh should only influence the master sub views
								if (tableInputController.getSubViewClassification() == SubViewClassification.MASTER) {
									Object input = tableInputController.getTableInput();
									tableViewer.setInput(input);
									if (tableViewer.getTable().isVisible()) {
										tableViewer.refresh();
									}
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
			tableViewer.refresh();
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
			updateCountItemsMessage(limit);
		}

		tableInputController.preferenceEventFired(preferenceEvent);
		switch (preferenceEvent.getPreferenceId()) {
		case CLEAR_BUFFER:
		case TIME_RESOLUTION:
			if (tableInputController.getPreferenceIds().contains(PreferenceId.CLEAR_BUFFER) || tableInputController.getPreferenceIds().contains(PreferenceId.TIME_RESOLUTION)) {
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
	 * Return the names of all columns in the table. Not visible columns names will also be
	 * included. The order of the names will be same to the initial table column order, thus not
	 * reflecting the current state of the table if the columns were moved.
	 *
	 * @return List of column names.
	 */
	public List<String> getColumnNames() {
		List<String> names = new ArrayList<String>();
		for (TableColumn column : tableViewer.getTable().getColumns()) {
			names.add(column.getText());
		}
		return names;
	}

	/**
	 *
	 * @return The list of integers representing the column order in the table. Note that only
	 *         columns that are currently visible will be included in the list.
	 * @see Table#getColumnOrder()
	 */
	public List<Integer> getColumnOrder() {
		int[] order = tableViewer.getTable().getColumnOrder();
		List<Integer> orderWithoutHidden = new ArrayList<Integer>();
		for (int index : order) {
			if (tableViewer.getTable().getColumns()[index].getWidth() > 0) {
				orderWithoutHidden.add(index);
			}
		}
		return orderWithoutHidden;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		tableInputController.dispose();
	}

	/**
	 * Updates the message on the editor's form header, based on the number of items displayed
	 * Currently. Message will be displayed only for master views.
	 *
	 * @param limit
	 *            Number of items displayed. -1 for all.
	 */
	private void updateCountItemsMessage(int limit) {
		if (getTableInputController().getSubViewClassification().equals(SubViewClassification.MASTER)) {
			AbstractRootEditor editor = this.getRootEditor();
			if (editor instanceof FormRootEditor) {
				Form form = ((FormRootEditor) editor).getForm();
				StringBuilder message = new StringBuilder(editor.getInputDefinition().getHeaderDescription() + " - ");
				if (limit == -1) {
					message.append("all displayed");
				} else {
					message.append("last " + limit + " displayed");
				}
				form.setMessage(message.toString());
			}
		}
	}

}
