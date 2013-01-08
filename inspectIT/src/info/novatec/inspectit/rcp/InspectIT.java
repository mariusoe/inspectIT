package info.novatec.inspectit.rcp;

import info.novatec.inspectit.rcp.repository.CmrRepositoryManager;
import info.novatec.inspectit.rcp.storage.InspectITStorageManager;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The main plugin class to be used in the desktop.
 */
public class InspectIT extends AbstractUIPlugin {

	/**
	 * The id of this plugin.
	 */
	public static final String ID = "info.novatec.inspectit.rcp";

	/**
	 * The shared instance.
	 */
	private static InspectIT plugin;

	/**
	 * The global repository management tool. It is used to create and save the connection to the
	 * CMR.
	 */
	private volatile CmrRepositoryManager cmrRepositoryManager;

	/**
	 * Preferences store for the plug-in.
	 */
	private volatile ScopedPreferenceStore preferenceStore;

	/**
	 * The global storage manager.
	 */
	private volatile InspectITStorageManager storageManager;

	/**
	 * List of property change listener in the plug-in.
	 * <p>
	 * Currently the property change mechanism of Eclipse RCP is not used in inspectIT. However, it
	 * might be used in future.
	 */
	private List<IPropertyChangeListener> propertyChangeListeners = new ArrayList<IPropertyChangeListener>();

	/**
	 * This method is called upon plug-in activation.
	 * 
	 * @param context
	 *            the Context.
	 * 
	 * @throws Exception
	 *             in case of error.
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped.
	 * 
	 * @param context
	 *            the Context.
	 * 
	 * @throws Exception
	 *             in case of error.
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		if (null != cmrRepositoryManager) {
			cmrRepositoryManager.cancelAllUpdateRepositoriesJobs();
		}
		super.stop(context);
		plugin = null; // NOPMD
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return Returns the shared instance.
	 */
	public static InspectIT getDefault() {
		return plugin;
	}

	/**
	 * Registers the {@link IPropertyChangeListener} with the plug-in. Has no effect if the listener
	 * is already registered.
	 * 
	 * @param propertyChangeListener
	 *            {@link IPropertyChangeListener} to add.
	 */
	public void addPropertyChangeListener(IPropertyChangeListener propertyChangeListener) {
		if (!propertyChangeListeners.contains(propertyChangeListener)) {
			propertyChangeListeners.add(propertyChangeListener);
		}
	}

	/**
	 * Unregisters the {@link IPropertyChangeListener} from the plug-in.
	 * 
	 * @param propertyChangeListener
	 *            {@link IPropertyChangeListener} to remove.
	 */
	public void removePropertyChangeListener(IPropertyChangeListener propertyChangeListener) {
		propertyChangeListeners.remove(propertyChangeListener);
	}

	/**
	 * Delegates the {@link PropertyChangeEvent} to all listeners.
	 * 
	 * @param event
	 *            Event to delegate.
	 */
	public void firePropertyChangeEvent(PropertyChangeEvent event) {
		for (IPropertyChangeListener listener : propertyChangeListeners) {
			listener.propertyChange(event);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		Field[] allFields = InspectITImages.class.getFields();
		for (Field field : allFields) {
			if (field.getName().startsWith("IMG") && String.class.equals(field.getType())) {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				try {
					String key = (String) field.get(null);
					URL url = getBundle().getEntry(key);
					reg.put(key, ImageDescriptor.createFromURL(url));
				} catch (Exception e) {
					continue;
				}
			}
		}
	}

	/**
	 * Returns an image from the image registry by resolving the passed image key.
	 * <p>
	 * <b>Images retrieved by this method should not be disposed, because they are shared resources
	 * in the plugin and will be disposed with the disposal of the display.</b>
	 * 
	 * @param imageKey
	 *            The key of the image to look for in the registry.
	 * @return The generated image.
	 */
	public Image getImage(String imageKey) {
		return getImageRegistry().get(imageKey);
	}

	/**
	 * Returns the image descriptor for the given key. The key can be one of the IMG_ definitions in
	 * {@link InspectITImages}.
	 * <p>
	 * <b>Every new image created with the given {@link ImageDescriptor} should be disposed by the
	 * caller.</b>
	 * 
	 * @param imageKey
	 *            The image key.
	 * @return The image descriptor for the given image key.
	 */
	public ImageDescriptor getImageDescriptor(String imageKey) {
		return getImageRegistry().getDescriptor(imageKey);
	}

	/**
	 * Returns a service, if one is registered with the bundle context.
	 * 
	 * @param clazz
	 *            Class of service.
	 * @param <E>
	 *            Type
	 * @return Service or <code>null</code> is service is not registered at the moment.
	 */
	public static <E> E getService(Class<E> clazz) {
		ServiceReference<E> reference = getDefault().getBundle().getBundleContext().getServiceReference(clazz);
		if (null != reference) {
			return getDefault().getBundle().getBundleContext().getService(reference);
		}
		throw new RuntimeException("Requested service of the class " + clazz.getName() + " is not registered in the bundle.");
	}

	/**
	 * {@inheritDoc}
	 */
	public ScopedPreferenceStore getPreferenceStore() {
		if (null == preferenceStore) {
			synchronized (this) {
				if (null == preferenceStore) {
					preferenceStore = new ScopedPreferenceStore(ConfigurationScope.INSTANCE, ID);
				}
			}
		}
		return preferenceStore;
	}

	/**
	 * @return Returns the CMR repository manager.
	 */
	public CmrRepositoryManager getCmrRepositoryManager() {
		if (null == cmrRepositoryManager) {
			synchronized (this) {
				if (null == cmrRepositoryManager) {
					cmrRepositoryManager = new CmrRepositoryManager();
				}
			}
		}
		return cmrRepositoryManager;
	}

	/**
	 * 
	 * @return Returns the {@link InspectITStorageManager}.
	 */
	public InspectITStorageManager getInspectITStorageManager() {
		if (null == storageManager) {
			synchronized (this) {
				if (null == storageManager) {
					storageManager = getService(InspectITStorageManager.class);
					storageManager.startUp();
				}
			}
		}
		return storageManager;
	}

	/**
	 * Creates a simple error dialog.
	 * 
	 * @param message
	 *            The message of the dialog.
	 * @param throwable
	 *            The exception to display
	 * @param code
	 *            The code of the error. <b>-1</b> is a marker that the code has to be added later.
	 */
	public void createErrorDialog(String message, Throwable throwable, int code) {
		IStatus status = new Status(IStatus.ERROR, ID, code, message, throwable);
		StatusManager.getManager().handle(status, StatusManager.SHOW);
	}

	/**
	 * Creates a simple error dialog without exception.
	 * 
	 * @param message
	 *            The message of the dialog.
	 * @param code
	 *            The code of the error. <b>-1</b> is a marker that the code has to be added later.
	 */
	public void createErrorDialog(String message, int code) {
		// Status sets exception to <code>null</code> internally.
		IStatus status = new Status(IStatus.ERROR, ID, code, message, null);
		StatusManager.getManager().handle(status, StatusManager.SHOW);
	}

	/**
	 * Creates a simple info dialog.
	 * 
	 * @param message
	 *            The message of the dialog.
	 * @param code
	 *            The code of the error. <b>-1</b> is a marker that the code has to be added later.
	 */
	public void createInfoDialog(String message, int code) {
		MessageDialog.openInformation(null, "Information", message);
	}

}
