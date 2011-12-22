package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;

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
 * This class represents the textual view of the {@link ClassLoadingInformation}
 * sensor-type.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ClassesInputController extends AbstractTextInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.text.classes";

	/**
	 * The name for the section.
	 */
	private static final String SECTION_CLASSES = "Classes";

	/**
	 * The template of the {@link ClassLoadingInformationData} object.
	 */
	private ClassLoadingInformationData classLoadingObj;

	/**
	 * The label for loaded classes.
	 */
	private Label loadedClassCount;

	/**
	 * The label for total loaded classes.
	 */
	private Label totalLoadedClassCount;

	/**
	 * The label for unloaded classes.
	 */
	private Label unloadedClassCount;

	/**
	 * The {@link HashMap} containing the different sections.
	 */
	private Map<String, Composite> sections = new HashMap<String, Composite>();

	/**
	 * The global data access service.
	 */
	private IGlobalDataAccessService dataAccessService;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);
		
		classLoadingObj = new ClassLoadingInformationData();
		classLoadingObj.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		addSection(parent, toolkit, SECTION_CLASSES);

		if (sections.containsKey(SECTION_CLASSES)) {
			// creates the labels
			addItemToSection(toolkit, SECTION_CLASSES, "Current loaded classes: ");
			loadedClassCount = toolkit.createLabel(sections.get(SECTION_CLASSES), "n/a");
			loadedClassCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_CLASSES, "Total loaded classes: ");
			totalLoadedClassCount = toolkit.createLabel(sections.get(SECTION_CLASSES), "n/a");
			totalLoadedClassCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_CLASSES, "Total unloaded classes: ");
			unloadedClassCount = toolkit.createLabel(sections.get(SECTION_CLASSES), "n/a");
			unloadedClassCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
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
		ClassLoadingInformationData data = (ClassLoadingInformationData) dataAccessService.getLastDataObject(classLoadingObj);

		if (null != data) {
			// updates the labels
			int count = data.getCount();
			loadedClassCount.setText(NumberFormatter.formatInteger(data.getTotalLoadedClassCount() / count));
			totalLoadedClassCount.setText(NumberFormatter.formatLong(data.getTotalTotalLoadedClassCount() / count));
			unloadedClassCount.setText(NumberFormatter.formatLong(data.getTotalUnloadedClassCount() / count));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

}
