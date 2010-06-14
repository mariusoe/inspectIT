package info.novatec.inspectit.rcp.editor;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.composite.GridCompositeSubView;
import info.novatec.inspectit.rcp.editor.composite.SashCompositeSubView;
import info.novatec.inspectit.rcp.editor.composite.TabbedCompositeSubView;
import info.novatec.inspectit.rcp.editor.exception.input.ExceptionMessagesTreeInputController;
import info.novatec.inspectit.rcp.editor.exception.input.ExceptionOverviewInputController;
import info.novatec.inspectit.rcp.editor.exception.input.ExceptionTreeDetailInputController;
import info.novatec.inspectit.rcp.editor.exception.input.ExceptionTreeOverviewInputController;
import info.novatec.inspectit.rcp.editor.exception.input.ExceptionTreeStackTraceInputController;
import info.novatec.inspectit.rcp.editor.graph.GraphSubView;
import info.novatec.inspectit.rcp.editor.table.TableSubView;
import info.novatec.inspectit.rcp.editor.table.input.AggregatedTimerSummaryInputController;
import info.novatec.inspectit.rcp.editor.table.input.CombinedMetricsInputController;
import info.novatec.inspectit.rcp.editor.table.input.InvocOverviewInputController;
import info.novatec.inspectit.rcp.editor.table.input.MethodInvocInputController;
import info.novatec.inspectit.rcp.editor.table.input.SqlInputController;
import info.novatec.inspectit.rcp.editor.table.input.SqlInvocInputController;
import info.novatec.inspectit.rcp.editor.text.TextSubView;
import info.novatec.inspectit.rcp.editor.text.input.ClassesInputController;
import info.novatec.inspectit.rcp.editor.text.input.CpuInputController;
import info.novatec.inspectit.rcp.editor.text.input.MemoryInputController;
import info.novatec.inspectit.rcp.editor.text.input.ThreadsInputController;
import info.novatec.inspectit.rcp.editor.text.input.VmSummaryInputController;
import info.novatec.inspectit.rcp.editor.traceinspector.TraceInspectorSubView;
import info.novatec.inspectit.rcp.editor.tree.TreeSubView;
import info.novatec.inspectit.rcp.editor.tree.input.InvocDetailInputController;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

/**
 * The factory for the creation of a {@link ISubView}.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public final class SubViewFactory {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private SubViewFactory() {
	}

	/**
	 * Creates a default {@link ISubView} object based on the passed
	 * {@link SensorTypeEnum}.
	 * 
	 * @param sensorTypeEnum
	 *            The sensor type on which the default view controller is based
	 *            on.
	 * @return An instance of a {@link ISubView}.
	 */
	public static ISubView createSubView(SensorTypeEnum sensorTypeEnum) {
		switch (sensorTypeEnum) {
		case AVERAGE_TIMER:
			// same as Timer
		case TIMER:
			GridCompositeSubView timerSubView = new GridCompositeSubView();
			timerSubView.addSubView(new GraphSubView(sensorTypeEnum), new GridData(SWT.FILL, SWT.FILL, true, true));
			ISubView aggregatedTimerSummarySubView = new TableSubView(new AggregatedTimerSummaryInputController());
			timerSubView.addSubView(aggregatedTimerSummarySubView, new GridData(SWT.FILL, SWT.FILL, true, false));
			return timerSubView;
		case CLASSLOADING_INFORMATION:
			GridCompositeSubView classLoadingSubView = new GridCompositeSubView();
			classLoadingSubView.addSubView(new GraphSubView(sensorTypeEnum), new GridData(SWT.FILL, SWT.FILL, true, true));
			classLoadingSubView.addSubView(new TextSubView(new ClassesInputController()), new GridData(SWT.FILL, SWT.FILL, true, false));
			return classLoadingSubView;
		case MEMORY_INFORMATION:
			GridCompositeSubView memorySubView = new GridCompositeSubView();
			memorySubView.addSubView(new GraphSubView(sensorTypeEnum), new GridData(SWT.FILL, SWT.FILL, true, true));
			memorySubView.addSubView(new TextSubView(new MemoryInputController()), new GridData(SWT.FILL, SWT.FILL, true, false));
			return memorySubView;
		case CPU_INFORMATION:
			GridCompositeSubView cpuSubView = new GridCompositeSubView();
			cpuSubView.addSubView(new GraphSubView(sensorTypeEnum), new GridData(SWT.FILL, SWT.FILL, true, true));
			cpuSubView.addSubView(new TextSubView(new CpuInputController()), new GridData(SWT.FILL, SWT.FILL, true, false));
			return cpuSubView;
		case SYSTEM_INFORMATION:
			return new TextSubView(new VmSummaryInputController());
		case THREAD_INFORMATION:
			GridCompositeSubView threadSubView = new GridCompositeSubView();
			threadSubView.addSubView(new GraphSubView(sensorTypeEnum), new GridData(SWT.FILL, SWT.FILL, true, true));
			threadSubView.addSubView(new TextSubView(new ThreadsInputController()), new GridData(SWT.FILL, SWT.FILL, true, false));
			return threadSubView;
		case INVOCATION_SEQUENCE:
			SashCompositeSubView invocSubView = new SashCompositeSubView();
			ISubView invocOverview = new TableSubView(new InvocOverviewInputController());
			TabbedCompositeSubView invocTabbedSubView = new TabbedCompositeSubView();
			ISubView invocDetails = new TreeSubView(new InvocDetailInputController());
			ISubView invocSql = new TableSubView(new SqlInvocInputController());
			ISubView invocMethods = new TableSubView(new MethodInvocInputController());
			ISubView traceInspector = new TraceInspectorSubView();

			invocTabbedSubView.addSubView(invocDetails, "Call Hierarchy", InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_CALL_HIERARCHY));
			invocTabbedSubView.addSubView(invocSql, "SQL", InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_DATABASE));
			invocTabbedSubView.addSubView(invocMethods, "Methods", InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_METHOD_PUBLIC));
			// invocTabbedSubView.addSubView(traceInspector, "Trace Inspector",
			// InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_CALL_HIERARCHY));

			invocSubView.addSubView(invocOverview, 1);
			invocSubView.addSubView(invocTabbedSubView, 2);

			return invocSubView;
		case SQL:
			SashCompositeSubView sqlSashSubView = new SashCompositeSubView();
			sqlSashSubView.addSubView(new TableSubView(new SqlInputController()));
			return sqlSashSubView;
		case EXCEPTION_TRACER:
			SashCompositeSubView exceptionTracerSubView = new SashCompositeSubView();
			ISubView exceptionTreeOverview = new TableSubView(new ExceptionTreeOverviewInputController());
			TabbedCompositeSubView exceptionTreeTabbedSubView = new TabbedCompositeSubView();
			ISubView exceptionTreeDetails = new TreeSubView(new ExceptionTreeDetailInputController());
			ISubView stackTraceInput = new TableSubView(new ExceptionTreeStackTraceInputController());

			exceptionTreeTabbedSubView.addSubView(exceptionTreeDetails, "Details", InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_CALL_HIERARCHY));
			exceptionTreeTabbedSubView.addSubView(stackTraceInput, "Stack Trace", InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_STACKTRACE));

			exceptionTracerSubView.addSubView(exceptionTreeOverview, 1);
			exceptionTracerSubView.addSubView(exceptionTreeTabbedSubView, 2);
			return exceptionTracerSubView;
		case EXCEPTION_TRACER_OVERVIEW:
			SashCompositeSubView exceptionTracerOverviewSubView = new SashCompositeSubView();
			ISubView exceptionOverview = new TableSubView(new ExceptionOverviewInputController());
			ISubView exceptionMessagesTree = new TreeSubView(new ExceptionMessagesTreeInputController());

			exceptionTracerOverviewSubView.addSubView(exceptionOverview, 1);
			exceptionTracerOverviewSubView.addSubView(exceptionMessagesTree, 2);
			return exceptionTracerOverviewSubView;
		case MARVIN_WORKFLOW:
			GridCompositeSubView combinedMetricsSubView = new GridCompositeSubView();
			ISubView combinedMetricsSummarySubView = new TableSubView(new CombinedMetricsInputController());

			combinedMetricsSubView.addSubView(new GraphSubView(sensorTypeEnum), new GridData(SWT.FILL, SWT.FILL, true, true));
			combinedMetricsSubView.addSubView(combinedMetricsSummarySubView, new GridData(SWT.FILL, SWT.FILL, true, false));
			return combinedMetricsSubView;
		default:
			throw new IllegalArgumentException("Could not create sub-view. Not supported: " + sensorTypeEnum.toString());
		}
	}

	/**
	 * Returns an instance of {@link ISubView}.
	 * 
	 * @param fqn
	 *            the fully-qualified name.
	 * @return An instance of {@link ISubView}.
	 */
	public static ISubView createSubView(String fqn) {
		return createSubView(SensorTypeEnum.get(fqn));
	}

}
