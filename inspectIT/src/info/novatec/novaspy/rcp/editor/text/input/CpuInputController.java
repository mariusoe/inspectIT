package info.novatec.novaspy.rcp.editor.text.input;

import info.novatec.novaspy.communication.data.CpuInformationData;
import info.novatec.novaspy.rcp.editor.InputDefinition;
import info.novatec.novaspy.rcp.formatter.NumberFormatter;
import info.novatec.novaspy.rcp.repository.service.GlobalDataAccessService;

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
 * This class represents the textual view of the {@link CpuInformation}
 * sensor-type.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class CpuInputController implements TextInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "novaspy.subview.text.cpu";

	/**
	 * The name of the section.
	 */
	private static final String SECTION_CPU = "CPU";

	/**
	 * The template of the {@link CpuInformationData} object.
	 */
	private CpuInformationData cpuObj;

	/**
	 * The label for the cpu usage.
	 */
	private Label cpuUsage;

	/**
	 * The label for the process cpu time.
	 */
	private Label processCpuTime;

	/**
	 * The {@link HashMap} containing the different sections.
	 */
	private Map<String, Composite> sections = new HashMap<String, Composite>();

	/**
	 * The global data access service.
	 */
	private GlobalDataAccessService dataAccessService;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		cpuObj = new CpuInformationData();
		cpuObj.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		addSection(parent, toolkit, SECTION_CPU);

		if (sections.containsKey(SECTION_CPU)) {
			// creates the labels
			addItemToSection(toolkit, SECTION_CPU, "Cpu Usage: ");
			cpuUsage = toolkit.createLabel(sections.get(SECTION_CPU), "n/a", SWT.LEFT);
			cpuUsage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_CPU, "Process Cpu Time: ");
			processCpuTime = toolkit.createLabel(sections.get(SECTION_CPU), "n/a", SWT.LEFT);
			processCpuTime.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
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
		CpuInformationData data = (CpuInformationData) dataAccessService.getLastDataObject(cpuObj);

		if (null != data) {
			int count = data.getCount();
			cpuUsage.setText(NumberFormatter.formatCpuPercent(data.getTotalCpuUsage() / count));
			processCpuTime.setText(NumberFormatter.formatNanosToSeconds((data.getTotalProcessCpuTime() / count)));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

}
