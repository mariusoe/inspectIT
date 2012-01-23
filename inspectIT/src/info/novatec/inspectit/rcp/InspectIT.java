package info.novatec.inspectit.rcp;

import info.novatec.inspectit.rcp.repository.RepositoryManager;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
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
	private volatile RepositoryManager repositoryManager;

	/**
	 * Preferences store for the plug-in.
	 */
	private volatile ScopedPreferenceStore preferenceStore;

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
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the default.
	 */
	public static InspectIT getDefault() {
		return plugin;
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
		addImageToRegistry(reg, InspectITConstants.IMG_SERVER_REFRESH);
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
	}

	/**
	 * Returns an image from the image registry by resolving the passed image key.
	 * 
	 * @param imageKey
	 *            The key of the image to look for in the registry.
	 * @return The generated image.
	 */
	public Image getImage(String imageKey) {
		return getImageDescriptor(imageKey).createImage();
	}

	/**
	 * Returns the image descriptor for the given key. The key can be one of the IMG_ definitions in
	 * {@link InspectITConstants}.
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
	 * Returns the repository manager.
	 * 
	 * @return The repository manager.
	 */
	public RepositoryManager getRepositoryManager() {
		if (null == repositoryManager) {
			synchronized (this) {
				if (null == repositoryManager) {
					repositoryManager = new RepositoryManager();
					repositoryManager.startup();
				}
			}
		}
		return repositoryManager;
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
