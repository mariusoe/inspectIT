package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.InspectIT;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * Handler that opens that InspectIT Documentation page on Confluence.
 * 
 * @author Ivan Senic
 * 
 */
public class OpenDocumentationHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
		try {
			IWebBrowser browser = browserSupport.createBrowser(null);
			URL url = new URL("https://confluence.novatec-gmbh.de/display/INSPECTITDOC");
			browser.openURL(url);
		} catch (PartInitException e) {
			InspectIT.getDefault().createErrorDialog(e.getMessage(), e, -1);
		} catch (MalformedURLException e) {
			InspectIT.getDefault().createErrorDialog(e.getMessage(), e, -1);
		}
		return null;
	}

}
