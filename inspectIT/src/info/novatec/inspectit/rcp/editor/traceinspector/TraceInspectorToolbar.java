package info.novatec.inspectit.rcp.editor.traceinspector;

import info.novatec.inspectit.rcp.editor.traceinspector.input.TraceInspectorController;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class TraceInspectorToolbar extends ToolBar {
	private TraceInspectorController traceInspectorController = null;

	public TraceInspectorToolbar(Composite parent, int style, TraceInspectorController traceInspectorController) {
		super(parent, style);

		this.traceInspectorController = traceInspectorController;

		ToolItem increaseResolutionItem = new ToolItem(this, SWT.PUSH);
		increaseResolutionItem.setText("+");
		increaseResolutionItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TraceInspectorToolbar.this.traceInspectorController.increaseResolution(10);
			}
		});

		ToolItem decreaseResolutionItem = new ToolItem(this, SWT.PUSH);
		decreaseResolutionItem.setText("-");
		decreaseResolutionItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TraceInspectorToolbar.this.traceInspectorController.decreaseResolution(10);
			}

		});
	}

}
