package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.communication.data.SystemInformationData;
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
 * This class represents the textual view of the {@link MemoryInformation}
 * sensor-type.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class MemoryInputController extends AbstractTextInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.text.memory";

	/**
	 * The name of the section.
	 */
	private static final String SECTION_MEMORY = "Memory";

	/**
	 * The template of the {@link MemoryInformationData} object.
	 */
	private MemoryInformationData memoryObj;

	/**
	 * The template of the {@link SystemInformationData} object.
	 */
	private SystemInformationData systemObj;

	/**
	 * The label for free physical memory.
	 */
	private Label freePhysMemory;

	/**
	 * The label for free swap space.
	 */
	private Label freeSwapSpace;

	/**
	 * The label for committed heap memory size.
	 */
	private Label committedHeapMemorySize;

	/**
	 * The label for committed non-heap memory size.
	 */
	private Label committedNonHeapMemorySize;

	/**
	 * The label for used heap memory size.
	 */
	private Label usedHeapMemorySize;

	/**
	 * The label for used non-heap memory size.
	 */
	private Label usedNonHeapMemorySize;

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
		super.setInputDefinition(inputDefinition);
		
		memoryObj = new MemoryInformationData();
		memoryObj.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		systemObj = new SystemInformationData();
		systemObj.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		addSection(parent, toolkit, SECTION_MEMORY);

		SystemInformationData systemData = (SystemInformationData) dataAccessService.getLastDataObject(systemObj);
		if (systemData != null) {
			// adds some static informations
			addItemToSection(toolkit, SECTION_MEMORY, "Max heap size: ");
			addItemToSection(toolkit, SECTION_MEMORY, NumberFormatter.formatBytesToKBytes(systemData.getMaxHeapMemorySize()));
			addItemToSection(toolkit, SECTION_MEMORY, "Max non-heap size: ");
			addItemToSection(toolkit, SECTION_MEMORY, NumberFormatter.formatBytesToKBytes(systemData.getMaxNonHeapMemorySize()));
			addItemToSection(toolkit, SECTION_MEMORY, "Total physical memory: ");
			addItemToSection(toolkit, SECTION_MEMORY, NumberFormatter.formatBytesToKBytes(systemData.getTotalPhysMemory()));
			addItemToSection(toolkit, SECTION_MEMORY, "Total swap space: ");
			addItemToSection(toolkit, SECTION_MEMORY, NumberFormatter.formatBytesToKBytes(systemData.getTotalSwapSpace()));
		} else {
			// if no static informations available
			addItemToSection(toolkit, SECTION_MEMORY, "Max heap size: ");
			addItemToSection(toolkit, SECTION_MEMORY, "n/a");
			addItemToSection(toolkit, SECTION_MEMORY, "Max non-heap size: ");
			addItemToSection(toolkit, SECTION_MEMORY, "n/a");
			addItemToSection(toolkit, SECTION_MEMORY, "Total physical memory: ");
			addItemToSection(toolkit, SECTION_MEMORY, "n/a");
			addItemToSection(toolkit, SECTION_MEMORY, "Total swap space: ");
			addItemToSection(toolkit, SECTION_MEMORY, "n/a");
		}

		if (sections.containsKey(SECTION_MEMORY)) {
			// creates some labels
			addItemToSection(toolkit, SECTION_MEMORY, "Free physical memory: ");
			freePhysMemory = toolkit.createLabel(sections.get(SECTION_MEMORY), "n/a", SWT.LEFT);
			freePhysMemory.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_MEMORY, "Free swap space: ");
			freeSwapSpace = toolkit.createLabel(sections.get(SECTION_MEMORY), "n/a", SWT.LEFT);
			freeSwapSpace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_MEMORY, "Committed heap size: ");
			committedHeapMemorySize = toolkit.createLabel(sections.get(SECTION_MEMORY), "n/a", SWT.LEFT);
			committedHeapMemorySize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_MEMORY, "Committed non-heap size: ");
			committedNonHeapMemorySize = toolkit.createLabel(sections.get(SECTION_MEMORY), "n/a", SWT.LEFT);
			committedNonHeapMemorySize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_MEMORY, "Used heap size: ");
			usedHeapMemorySize = toolkit.createLabel(sections.get(SECTION_MEMORY), "n/a", SWT.LEFT);
			usedHeapMemorySize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_MEMORY, "Used non-heap size: ");
			usedNonHeapMemorySize = toolkit.createLabel(sections.get(SECTION_MEMORY), "n/a", SWT.LEFT);
			usedNonHeapMemorySize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
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
		MemoryInformationData data = (MemoryInformationData) dataAccessService.getLastDataObject(memoryObj);

		if (null != data) {
			// updates the labels
			int count = data.getCount();
			freePhysMemory.setText(NumberFormatter.formatBytesToKBytes(data.getTotalFreePhysMemory() / count));
			freeSwapSpace.setText(NumberFormatter.formatBytesToKBytes(data.getTotalFreeSwapSpace() / count));
			committedHeapMemorySize.setText(NumberFormatter.formatBytesToKBytes(data.getTotalComittedHeapMemorySize() / count));
			committedNonHeapMemorySize.setText(NumberFormatter.formatBytesToKBytes(data.getTotalComittedNonHeapMemorySize() / count));
			usedHeapMemorySize.setText(NumberFormatter.formatBytesToKBytes(data.getTotalUsedHeapMemorySize() / count));
			usedNonHeapMemorySize.setText(NumberFormatter.formatBytesToKBytes(data.getTotalUsedNonHeapMemorySize() / count));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

}
