package info.novatec.inspectit.rcp;

import info.novatec.inspectit.rcp.repository.CmrRepositoryManager;
import info.novatec.inspectit.rcp.storage.InspectITStorageManager;

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
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.context.ApplicationContext;

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
	 * The spring application context.
	 */
	private ApplicationContext applicationContext;

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

		// initialize spring
		BeanFactoryLocator beanFactoryLocator = SingletonBeanFactoryLocator.getInstance();
		BeanFactoryReference beanFactoryReference = beanFactoryLocator.useBeanFactory("ctx");
		applicationContext = (ApplicationContext) beanFactoryReference.getFactory();
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
		plugin = null;
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
	 * Helper method to add one of the IMG_ keys in {@link InspectITConstants} to the image
	 * registry.
	 * 
	 * @param registry
	 *            The image registry.
	 * @param imageKey
	 *            The image key.
	 */
	private void addImageToRegistry(ImageRegistry registry, String imageKey) {
		URL url = getBundle().getEntry(InspectITConstants.ICON_PATH + imageKey);
		registry.put(imageKey, ImageDescriptor.createFromURL(url));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		addImageToRegistry(reg, InspectITConstants.IMG_SERVER_ONLINE);
		addImageToRegistry(reg, InspectITConstants.IMG_SERVER_OFFLINE);
		addImageToRegistry(reg, InspectITConstants.IMG_SERVER_ONLINE_SMALL);
		addImageToRegistry(reg, InspectITConstants.IMG_SERVER_OFFLINE_SMALL);
		addImageToRegistry(reg, InspectITConstants.IMG_SERVER_REFRESH);
		addImageToRegistry(reg, InspectITConstants.IMG_SERVER_REFRESH_SMALL);
		addImageToRegistry(reg, InspectITConstants.IMG_SERVER_ADD);
		addImageToRegistry(reg, InspectITConstants.IMG_AGENT);
		addImageToRegistry(reg, InspectITConstants.IMG_INSTRUMENTATION_BROWSER);
		addImageToRegistry(reg, InspectITConstants.IMG_SYSTEM_OVERVIEW);
		addImageToRegistry(reg, InspectITConstants.IMG_PACKAGE);
		addImageToRegistry(reg, InspectITConstants.IMG_CLASS);
		addImageToRegistry(reg, InspectITConstants.IMG_METHOD_PUBLIC);
		addImageToRegistry(reg, InspectITConstants.IMG_METHOD_PROTECTED);
		addImageToRegistry(reg, InspectITConstants.IMG_METHOD_DEFAULT);
		addImageToRegistry(reg, InspectITConstants.IMG_METHOD_PRIVATE);
		addImageToRegistry(reg, InspectITConstants.IMG_CPU_OVERVIEW);
		addImageToRegistry(reg, InspectITConstants.IMG_CLASS_OVERVIEW);
		addImageToRegistry(reg, InspectITConstants.IMG_MEMORY_OVERVIEW);
		addImageToRegistry(reg, InspectITConstants.IMG_THREADS_OVERVIEW);
		addImageToRegistry(reg, InspectITConstants.IMG_VM_SUMMARY);
		addImageToRegistry(reg, InspectITConstants.IMG_ITEM_NA_RED);
		addImageToRegistry(reg, InspectITConstants.IMG_ITEM_NA_GREY);
		addImageToRegistry(reg, InspectITConstants.IMG_REFRESH);
		addImageToRegistry(reg, InspectITConstants.IMG_PREFERENCES);
		addImageToRegistry(reg, InspectITConstants.IMG_TIMER);
		addImageToRegistry(reg, InspectITConstants.IMG_INVOCATION);
		addImageToRegistry(reg, InspectITConstants.IMG_DATABASE);
		addImageToRegistry(reg, InspectITConstants.IMG_SEARCH);
		addImageToRegistry(reg, InspectITConstants.IMG_FILTER);
		addImageToRegistry(reg, InspectITConstants.IMG_SHOW_ALL);
		addImageToRegistry(reg, InspectITConstants.IMG_LAST_HOUR);
		addImageToRegistry(reg, InspectITConstants.IMG_THIS_DAY);
		addImageToRegistry(reg, InspectITConstants.IMG_LAST_WEEK);
		addImageToRegistry(reg, InspectITConstants.IMG_CALL_HIERARCHY);
		addImageToRegistry(reg, InspectITConstants.IMG_LIVE_MODE);
		addImageToRegistry(reg, InspectITConstants.IMG_INFORMATION);
		addImageToRegistry(reg, InspectITConstants.IMG_ZOOMIN);
		addImageToRegistry(reg, InspectITConstants.IMG_ZOOMOUT);
		addImageToRegistry(reg, InspectITConstants.IMG_ZOOMFIT);
		addImageToRegistry(reg, InspectITConstants.IMG_DRILLUP);
		addImageToRegistry(reg, InspectITConstants.IMG_DRILLDOWN);
		addImageToRegistry(reg, InspectITConstants.IMG_HEAT);
		addImageToRegistry(reg, InspectITConstants.IMG_ACTIVITY);
		addImageToRegistry(reg, InspectITConstants.IMG_WORKFLOW);
		addImageToRegistry(reg, InspectITConstants.IMG_EXCEPTION_TREE);
		addImageToRegistry(reg, InspectITConstants.IMG_STACKTRACE);
		addImageToRegistry(reg, InspectITConstants.IMG_EXCEPTION_SENSOR);
		addImageToRegistry(reg, InspectITConstants.VSA_LOGO);
		addImageToRegistry(reg, InspectITConstants.IMG_CREATED_EXCEPTION);
		addImageToRegistry(reg, InspectITConstants.OVERLAY_PRIORITY);
		addImageToRegistry(reg, InspectITConstants.OVERLAY_UP);
		addImageToRegistry(reg, InspectITConstants.OVERLAY_ERROR);
		addImageToRegistry(reg, InspectITConstants.IMG_NEXT);
		addImageToRegistry(reg, InspectITConstants.IMG_PREVIOUS);
		addImageToRegistry(reg, InspectITConstants.IMG_RIGHT_DOWN_ARROW);
		addImageToRegistry(reg, InspectITConstants.IMG_TRASH);
		addImageToRegistry(reg, InspectITConstants.IMG_TOOL);
		addImageToRegistry(reg, InspectITConstants.IMG_HTTP);
		addImageToRegistry(reg, InspectITConstants.IMG_HTTP_AGGREGATE);
		addImageToRegistry(reg, InspectITConstants.IMG_HTTP_TAGGED);
		addImageToRegistry(reg, InspectITConstants.IMG_HTTP_AGGREGATION_REQUESTMESSAGE);
		addImageToRegistry(reg, InspectITConstants.IMG_CHECKMARK);
		addImageToRegistry(reg, InspectITConstants.IMG_WINDOW);
		addImageToRegistry(reg, InspectITConstants.IMG_FONT);
		addImageToRegistry(reg, InspectITConstants.IMG_COLLAPSE);
		addImageToRegistry(reg, InspectITConstants.IMG_ADD);
		addImageToRegistry(reg, InspectITConstants.IMG_STOARGE_NEW);
		addImageToRegistry(reg, InspectITConstants.IMG_STOARGE_OPENED);
		addImageToRegistry(reg, InspectITConstants.IMG_STOARGE_RECORDING);
		addImageToRegistry(reg, InspectITConstants.IMG_STOARGE_CLOSED);
		addImageToRegistry(reg, InspectITConstants.IMG_STOARGE_AVAILABLE);
		addImageToRegistry(reg, InspectITConstants.IMG_STOARGE_NOT_AVAILABLE);
		addImageToRegistry(reg, InspectITConstants.IMG_ASSIGNEE_LABEL_ICON);
		addImageToRegistry(reg, InspectITConstants.IMG_MOUNTEDBY_LABEL_ICON);
		addImageToRegistry(reg, InspectITConstants.IMG_RATING_LABEL_ICON);
		addImageToRegistry(reg, InspectITConstants.IMG_STATUS_LABEL_ICON);
		addImageToRegistry(reg, InspectITConstants.IMG_USECASE_LABEL_ICON);
		addImageToRegistry(reg, InspectITConstants.IMG_USER_LABEL_ICON);
		addImageToRegistry(reg, InspectITConstants.IMG_PROPERTIES);
		addImageToRegistry(reg, InspectITConstants.IMG_CLOSE);
		addImageToRegistry(reg, InspectITConstants.IMG_CHECKMARK);
		addImageToRegistry(reg, InspectITConstants.IMG_RECORD);
		addImageToRegistry(reg, InspectITConstants.IMG_RECORD_GRAY);
		addImageToRegistry(reg, InspectITConstants.IMG_EVENT_GREEN);
		addImageToRegistry(reg, InspectITConstants.IMG_EVENT_RED);
		addImageToRegistry(reg, InspectITConstants.IMG_EVENT_YELLOW);
		addImageToRegistry(reg, InspectITConstants.IMG_STORAGE_OVERLAY);
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
	 * {@link InspectITConstants}.
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
	 * Returns the application context.
	 * 
	 * @return The application context.
	 */
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * {@inheritDoc}
	 */
	public ScopedPreferenceStore getPreferenceStore() {
		if (null == preferenceStore) {
			synchronized (this) {
				if (null == preferenceStore) {
					preferenceStore = new ScopedPreferenceStore(new ConfigurationScope(), ID);
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
					storageManager = (InspectITStorageManager) applicationContext.getBean("storageManager");
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
