package info.novatec.novaspy.rcp.editor.exception;

import info.novatec.novaspy.communication.data.ExceptionSensorData;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

public class DeferredExceptionTree implements IDeferredWorkbenchAdapter {

	/**
	 * {@inheritDoc}
	 */
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		ExceptionSensorData exceptionSensorData = (ExceptionSensorData) object;
		List<ExceptionSensorData> exceptionSensorDataList = new ArrayList<ExceptionSensorData>();
		exceptionSensorDataList.add(exceptionSensorData.getChild());
		monitor.beginTask("Loading of Exception Sensor Data Objects...", exceptionSensorDataList.size());

		collector.add(exceptionSensorDataList.toArray(), monitor);

		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	public ISchedulingRule getRule(Object object) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isContainer() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getChildren(Object o) {
		ExceptionSensorData exceptionSensorData = (ExceptionSensorData) o;
		List<ExceptionSensorData> exceptionSensorDataList = new ArrayList<ExceptionSensorData>();
		exceptionSensorDataList.add(exceptionSensorData.getChild());

		return exceptionSensorDataList.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel(Object o) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParent(Object o) {
		return o;
	}

}
