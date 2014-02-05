package info.novatec.inspectit.rcp;

import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Our extension to the {@link WorkbenchAdvisor} where we define the workbench related things like
 * default perspective, unchecked exceptions handling, etc.
 * 
 * @author Ivan Senic
 * 
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ApplicationWorkbenchAdvisor.class);

	/**
	 * The initial perspective ID.
	 */
	private static final String PERSPECTIVE_ID = "inspectit.perspective";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		// save the state of the application regarding its position etc.
		configurer.setSaveAndRestore(true);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void eventLoopException(Throwable exception) {
		super.eventLoopException(exception);
		LOG.error("Unchecked exception occurred during program execution.", exception);
		InspectIT.getDefault().createErrorDialog("Unchecked exception occurred during program execution.", exception, -1);
	}

}
