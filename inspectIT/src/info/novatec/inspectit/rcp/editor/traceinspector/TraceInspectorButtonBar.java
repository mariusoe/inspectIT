package info.novatec.inspectit.rcp.editor.traceinspector;

import info.novatec.inspectit.rcp.editor.traceinspector.input.TraceInspectorController;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ColumnLayout;

public class TraceInspectorButtonBar extends Composite {
    private Button smallerButton;
    private Button biggerButton;
    private TraceInspectorController traceInspectorController = null;

    public TraceInspectorButtonBar(Composite parent, int style, TraceInspectorController controller) {
	super(parent, style);

	this.traceInspectorController = controller;
	ColumnLayout cl = new ColumnLayout();
        this.setLayout(cl);
	
	smallerButton = new Button(this, SWT.PUSH);
        smallerButton.setText("-");
        smallerButton.setSize(50,50);
	smallerButton.addSelectionListener(new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
		traceInspectorController.decreaseResolution(10.0);
	    }
	});

	
	biggerButton = new Button(this, SWT.PUSH);
        biggerButton.setText("+");
        biggerButton.setSize(50,50);
        biggerButton.addSelectionListener(new SelectionAdapter() {

	    @Override
	    public void widgetSelected(SelectionEvent e) {
		traceInspectorController.increaseResolution(10.0);
	    }

	});
    }

}
