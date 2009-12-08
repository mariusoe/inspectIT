package info.novatec.novaspy.rcp.editor.traceinspector;

import info.novatec.novaspy.communication.DefaultData;
import info.novatec.novaspy.communication.data.InvocationSequenceData;
import info.novatec.novaspy.rcp.NovaSpy;
import info.novatec.novaspy.rcp.NovaSpyConstants;
import info.novatec.novaspy.rcp.editor.AbstractSubView;
import info.novatec.novaspy.rcp.editor.preferences.PreferenceId;
import info.novatec.novaspy.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.novaspy.rcp.editor.traceinspector.input.SWTCanvasAdapter;
import info.novatec.novaspy.rcp.editor.traceinspector.input.TraceInspectorController;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class TraceInspectorSubView extends AbstractSubView {

    private Canvas canvasControl = null;

    private ToolBar toolBar = null;
    private Control control = null;
    private Label detailLabel = null;
    private InvocationSequenceData invocationSequence = null;
    private TraceInspectorController traceInspectorController = null;
    private SWTCanvasAdapter canvasAdapter = null;

    @Override
    public void createPartControl(Composite parent, FormToolkit toolkit) {
	Composite composite = new Composite(parent, SWT.BORDER | SWT.VERTICAL | SWT.FILL);
	GridLayout gridLayout = new GridLayout(2,false);
        composite.setLayout(gridLayout);	

//Toolbar Construction
	
	this.toolBar = new ToolBar(composite, SWT.NONE);
	RowData rowData = new RowData();
	
	GridData toolBarData = new GridData(SWT.FILL, SWT.BEGINNING,false,false);        
	this.toolBar.setLayoutData(toolBarData);
       
	        	
	ToolItem increaseResolutionButton = new ToolItem(this.toolBar, SWT.PUSH|SWT.BORDER);
        
	increaseResolutionButton.setImage(NovaSpy.getDefault().getImage(NovaSpyConstants.IMG_ZOOMIN));
	increaseResolutionButton.addSelectionListener(new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
		TraceInspectorSubView.this.traceInspectorController.increaseResolution(10);
	    }
	});

	ToolItem decreaseResolutionButton = new ToolItem(this.toolBar, SWT.PUSH|SWT.BORDER);
	decreaseResolutionButton.setImage(NovaSpy.getDefault().getImage(NovaSpyConstants.IMG_ZOOMOUT));
	decreaseResolutionButton.addSelectionListener(new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
		TraceInspectorSubView.this.traceInspectorController.decreaseResolution(10);
	    }
	});

	ToolItem zoomToFitButton = new ToolItem(this.toolBar, SWT.PUSH|SWT.BORDER);
	zoomToFitButton.setImage(NovaSpy.getDefault().getImage(NovaSpyConstants.IMG_ZOOMFIT));
	zoomToFitButton.addSelectionListener(new SelectionAdapter(){
	    @Override
	    public void widgetSelected(SelectionEvent e) {
	       TraceInspectorSubView.this.traceInspectorController.resetResolution();
	    }
	    
	});
	
	
	ToolItem drillDownButton = new ToolItem(this.toolBar, SWT.PUSH|SWT.BORDER);
	drillDownButton.setImage(NovaSpy.getDefault().getImage(NovaSpyConstants.IMG_DRILLDOWN));
	drillDownButton.addSelectionListener(new SelectionAdapter(){
	    @Override
	    public void widgetSelected(SelectionEvent e) {
	       TraceInspectorSubView.this.traceInspectorController.drillDown();
	    }
	    
	});
	
	ToolItem drillUpButton = new ToolItem(this.toolBar, SWT.PUSH|SWT.BORDER);
	drillUpButton.setImage(NovaSpy.getDefault().getImage(NovaSpyConstants.IMG_DRILLUP));
	drillUpButton.addSelectionListener(new SelectionAdapter(){
	    @Override
	    public void widgetSelected(SelectionEvent e) {
	       TraceInspectorSubView.this.traceInspectorController.drillUp();
	    }
	    
	});

	ToolItem heatButton = new ToolItem(this.toolBar, SWT.PUSH|SWT.BORDER|SWT.FLAT);
	heatButton.setImage(NovaSpy.getDefault().getImage(NovaSpyConstants.IMG_HEAT));
	heatButton.addSelectionListener(new SelectionAdapter(){
	    @Override
	    public void widgetSelected(SelectionEvent e) {
	       TraceInspectorSubView.this.traceInspectorController.toggleHeat();
	    }
	    
	});

// Detailtable construction
	
	Table detailTable = new Table(composite, SWT.NO_SCROLL);
	GridData detailTableData = new GridData(SWT.FILL, SWT.BEGINNING, false, false);

	detailTable.setLayoutData(detailTableData);	
	detailTable.setLinesVisible(false);
	
        TableColumn labelColumn = new TableColumn(detailTable, SWT.NONE);	
	TableColumn dataColumn = new TableColumn(detailTable, SWT.NONE);
	
	TableItem detailClassNameItem = new TableItem(detailTable, SWT.NONE);	
	detailClassNameItem.setText(0, "Class:");
	detailClassNameItem.setText(1, "");

	TableItem detailMethodNameItem = new TableItem(detailTable, SWT.NONE);
	detailMethodNameItem.setText(0, "Method:");
	detailMethodNameItem.setText(1, "");
	
	TableItem detailDurationItem = new TableItem(detailTable, SWT.NONE);
	detailDurationItem.setText(0, "Duration:");
	detailDurationItem.setText(1, "");
	

	for(TableColumn column:detailTable.getColumns()) {
	    column.pack();
	}
	detailTable.pack();
	
// Canvas Construction	
		
	canvasControl = new Canvas(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER );
	GridData canvasData = new GridData(SWT.FILL, SWT.FILL, true, true);
	canvasData.horizontalSpan = 2;
	canvasControl.setLayoutData(canvasData);
	
	canvasControl.addPaintListener(new PaintListener() {
	    @Override
	    public void paintControl(PaintEvent e) {
		canvasAdapter.setGC(e.gc);
		traceInspectorController.paint();
	    }
	});

	canvasControl.addMouseMoveListener(new MouseMoveListener() {

	    @Override
	    public void mouseMove(MouseEvent e) {
	    }

	});

	canvasControl.addMouseListener(new MouseAdapter() {

	    @Override
	    public void mouseUp(MouseEvent e) {
		traceInspectorController.highlightInvocationBlock(e.x, e.y);
	    }

	    @Override
	    public void mouseDoubleClick(MouseEvent e) {
		traceInspectorController.drillDown(e.x, e.y);
	    }

	});
	
	canvasAdapter = new SWTCanvasAdapter();
	canvasAdapter.setDetailtable(detailTable);
	this.traceInspectorController = new TraceInspectorController(this, canvasAdapter);	

	this.traceInspectorController.setInputDefinition(super.getRootEditor().getInputDefinition());
	this.control = composite;
    }

    @Override
    public void doRefresh() {
	canvasControl.redraw();
	canvasControl.update();
    }

    @Override
    public Control getControl() {
	return control;
    }

    @Override
    public Set<PreferenceId> getPreferenceIds() {
	return Collections.EMPTY_SET;
    }

    @Override
    public ISelectionProvider getSelectionProvider() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void preferenceEventFired(PreferenceEvent preferenceEvent) {
	// TODO Auto-generated method stub

    }

    @Override
    public void setDataInput(List<? extends DefaultData> data) {
	invocationSequence = (InvocationSequenceData) data.get(0);
	traceInspectorController.setInvocationSequence(invocationSequence);
	canvasControl.setData(invocationSequence);
	canvasControl.redraw();
	canvasControl.update();
	return;
    }
}
