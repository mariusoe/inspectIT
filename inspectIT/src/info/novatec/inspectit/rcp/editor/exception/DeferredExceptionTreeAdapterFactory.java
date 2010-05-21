package info.novatec.inspectit.rcp.editor.exception;

import info.novatec.inspectit.communication.data.ExceptionSensorData;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

/**
 * Adapter Factory which is used to create the {@link DeferredExceptionTree}
 * objects if the adaptable object is of type {@link ExceptionSensorData}.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class DeferredExceptionTreeAdapterFactory implements IAdapterFactory {

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IDeferredWorkbenchAdapter.class == adapterType) {
			if (adaptableObject instanceof ExceptionSensorData) {
				return new DeferredStackTraces();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
		return new Class[] { IDeferredWorkbenchAdapter.class };
	}

}
