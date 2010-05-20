package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.repository.service.CachedGlobalDataAccessService;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * This class represents the textual view of the {@link ThreadInformation}
 * sensor-type.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ThreadsInputController implements TextInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.text.threads";

	/**
	 * The name of the section.
	 */
	private static final String SECTION_THREADS = "Threads";

	/**
	 * The template of the {@link ThreadInformationData} object.
	 */
	private ThreadInformationData threadObj;

	/**
	 * The label for live threads.
	 */
	private Label liveThreadCount;

	/**
	 * The label for daemon threads.
	 */
	private Label daemonThreadCount;

	/**
	 * The label for total started threads.
	 */
	private Label totalStartedThreadCount;

	/**
	 * The label for peak threads.
	 */
	private Label peakThreadCount;

	/**
	 * The {@link HashMap} containing the different sections.
	 */
	private Map<String, Composite> sections = new HashMap<String, Composite>();

	/**
	 * The global data access service.
	 */
	private CachedGlobalDataAccessService dataAccessService;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		threadObj = new ThreadInformationData();
		threadObj.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		addSection(parent, toolkit, SECTION_THREADS);

		if (sections.containsKey(SECTION_THREADS)) {
			// creates the labels
			addItemToSection(toolkit, SECTION_THREADS, "Live threads: ");
			liveThreadCount = toolkit.createLabel(sections.get(SECTION_THREADS), "n/a", SWT.LEFT);
			liveThreadCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_THREADS, "Daemon threads: ");
			daemonThreadCount = toolkit.createLabel(sections.get(SECTION_THREADS), "n/a", SWT.LEFT);
			daemonThreadCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_THREADS, "Peak: ");
			peakThreadCount = toolkit.createLabel(sections.get(SECTION_THREADS), "n/a", SWT.LEFT);
			peakThreadCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_THREADS, "Total threads started: ");
			totalStartedThreadCount = toolkit.createLabel(sections.get(SECTION_THREADS), "n/a", SWT.LEFT);
			totalStartedThreadCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
	}

	/**
	 * Adds a section to bundle some content.
	 * 
	 * @param parent
	 *            The parent used to draw the elements to.
	 * @param toolkit
	 *            The form toolkit.
	 * @param sectionTitle
	 *            The section title
	 */
	private void addSection(Composite parent, FormToolkit toolkit, String sectionTitle) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR);
		section.setText(sectionTitle);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Composite sectionComposite = toolkit.createComposite(section);
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.marginLeft = 5;
		gridLayout.marginTop = 5;
		sectionComposite.setLayout(gridLayout);
		section.setClient(sectionComposite);

		if (!sections.containsKey(sectionTitle)) {
			sections.put(sectionTitle, sectionComposite);
		}
	}

	/**
	 * Adds an item to the specified section.
	 * 
	 * @param toolkit
	 *            The form toolkit.
	 * @param sectionTitle
	 *            The section title.
	 * @param text
	 *            The text which will be shown.
	 */
	private void addItemToSection(FormToolkit toolkit, String sectionTitle, String text) {
		if (sections.containsKey(sectionTitle)) {
			Label label = toolkit.createLabel(sections.get(sectionTitle), text);
			label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		ThreadInformationData data = (ThreadInformationData) dataAccessService.getLastDataObject(threadObj);

		if (null != data) {
			// updates the labels
			int count = data.getCount();
			liveThreadCount.setText(NumberFormatter.formatInteger(data.getTotalThreadCount() / count));
			daemonThreadCount.setText(NumberFormatter.formatInteger(data.getTotalDaemonThreadCount() / count));
			totalStartedThreadCount.setText(NumberFormatter.formatLong(data.getTotalTotalStartedThreadCount() / count));
			peakThreadCount.setText(NumberFormatter.formatInteger(data.getTotalPeakThreadCount() / count));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}
}
