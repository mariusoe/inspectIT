package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.communication.data.CompilationInformationData;
import info.novatec.inspectit.communication.data.CpuInformationData;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.communication.data.RuntimeInformationData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.communication.data.VmArgumentData;
import info.novatec.inspectit.rcp.editor.InputDefinition;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.repository.service.GlobalDataAccessService;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * This class represents the textual view of all platform-sensor-types. The
 * shown informations are static and dynamic
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class VmSummaryInputController implements TextInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.text.vmsummary";

	/**
	 * The name of the vm section.
	 */
	private static final String SECTION_VM = "VM";

	/**
	 * The name of the classes section.
	 */
	private static final String SECTION_CLASSES = "Classes";

	/**
	 * The name of the memory section.
	 */
	private static final String SECTION_MEMORY = "Memory";

	/**
	 * The name of the threads section.
	 */
	private static final String SECTION_THREADS = "Threads";

	/**
	 * The name of the operating system section.
	 */
	private static final String SECTION_OS = "Operating System";

	/**
	 * The name of the classpath section.
	 */
	private static final String SECTION_CLASSPATH = "Class Path";

	/**
	 * The name of the vm arguments section.
	 */
	private static final String VM_ARGS = "VM Arguments";

	/**
	 * The string representing that something is not available.
	 */
	private static final String NOT_AVAILABLE = "N/A";

	/**
	 * The form containing all labels and sections.
	 */
	private ScrolledForm scrolledForm;

	/**
	 * The template of the {@link SystemInformationData} object.
	 */
	private SystemInformationData systemObj;

	/**
	 * The template of the {@link RuntimeInformationData} object.
	 */
	private RuntimeInformationData runtimeObj;

	/**
	 * The template of the {@link MemoryInformationData} object.
	 */
	private MemoryInformationData memoryObj;

	/**
	 * The template of the {@link ClassLoadingInformationData} object.
	 */
	private ClassLoadingInformationData classLoadingObj;

	/**
	 * The template of the {@link CpuInformationData} object.
	 */
	private CpuInformationData cpuObj;

	/**
	 * The template of the {@link CompilationInformationData} object.
	 */
	private CompilationInformationData compilationObj;

	/**
	 * The template of the {@link ThreadInformationData} object.
	 */
	private ThreadInformationData threadObj;

	/**
	 * The {@link HashMap} containing the different sections.
	 */
	private Map<String, Composite> sections = new HashMap<String, Composite>();

	/**
	 * The {@link HashMap} containing the minimized sections.
	 */
	private Map<String, Composite> minimizedSections = new HashMap<String, Composite>();

	/**
	 * The label for loaded classes.
	 */
	private Label loadedClassCount;

	/**
	 * The label for total loaeded classes.
	 */
	private Label totalLoadedClassCount;

	/**
	 * The label for unloaded classes.
	 */
	private Label unloadedClassCount;

	/**
	 * The label for the uptime of the virtual machine.
	 */
	private Label uptime;

	/**
	 * The label for the process cpu time.
	 */
	private Label processCpuTime;

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
	 * The label for total compilation time.
	 */
	private Label totalCompilationTime;

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
	 * The global data access service.
	 */
	private GlobalDataAccessService dataAccessService;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		long platformId = inputDefinition.getIdDefinition().getPlatformId();

		systemObj = new SystemInformationData();
		systemObj.setPlatformIdent(platformId);

		classLoadingObj = new ClassLoadingInformationData();
		classLoadingObj.setPlatformIdent(platformId);

		cpuObj = new CpuInformationData();
		cpuObj.setPlatformIdent(platformId);

		compilationObj = new CompilationInformationData();
		compilationObj.setPlatformIdent(platformId);

		memoryObj = new MemoryInformationData();
		memoryObj.setPlatformIdent(platformId);

		runtimeObj = new RuntimeInformationData();
		runtimeObj.setPlatformIdent(platformId);

		threadObj = new ThreadInformationData();
		threadObj.setPlatformIdent(platformId);

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		scrolledForm = toolkit.createScrolledForm(parent);
		scrolledForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrolledForm.getBody().setLayout(new GridLayout(1, true));
		scrolledForm.getBody().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		this.initializeInput(scrolledForm.getBody(), toolkit);
	}

	/**
	 * Returns the new composite with initialized input and sections with text
	 * labels.
	 * 
	 * @param parent
	 *            The parent used to draw the elements to.
	 * @param toolkit
	 *            The form toolkit.
	 * 
	 */
	private void initializeInput(Composite parent, FormToolkit toolkit) {
		int labelStyle = SWT.LEFT;
		int minTitleColumnWidth = 170;
		int minInformationColumnWidth = 230;

		// add sections
		addSection(parent, toolkit, SECTION_VM);
		addSection(parent, toolkit, SECTION_OS);
		addSection(parent, toolkit, SECTION_MEMORY);
		addSection(parent, toolkit, SECTION_CLASSES);
		addSection(parent, toolkit, SECTION_THREADS);

		// static text informations
		addStaticInformations(parent, toolkit);

		// dynamic text informations
		if (sections.containsKey(SECTION_CLASSES)) {
			// creates the labels for the 'classes' section
			addItemToSection(toolkit, SECTION_CLASSES, "Current loaded classes: ", minTitleColumnWidth);
			loadedClassCount = toolkit.createLabel(sections.get(SECTION_CLASSES), NOT_AVAILABLE, labelStyle);
			loadedClassCount.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));

			addItemToSection(toolkit, SECTION_CLASSES, "Total loaded classes: ", minTitleColumnWidth);
			totalLoadedClassCount = toolkit.createLabel(sections.get(SECTION_CLASSES), NOT_AVAILABLE, labelStyle);
			totalLoadedClassCount.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));

			addItemToSection(toolkit, SECTION_CLASSES, "Total unloaded classes: ", minTitleColumnWidth);
			unloadedClassCount = toolkit.createLabel(sections.get(SECTION_CLASSES), NOT_AVAILABLE, labelStyle);
			unloadedClassCount.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));
		}

		if (sections.containsKey(SECTION_VM)) {
			// creates the labels for the 'vm' section
			addItemToSection(toolkit, SECTION_VM, "Total compile time: ", minTitleColumnWidth);
			totalCompilationTime = toolkit.createLabel(sections.get(SECTION_VM), NOT_AVAILABLE, labelStyle);
			totalCompilationTime.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));

			addItemToSection(toolkit, SECTION_VM, "Uptime: ", minTitleColumnWidth);
			uptime = toolkit.createLabel(sections.get(SECTION_VM), NOT_AVAILABLE, labelStyle);
			uptime.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));

			addItemToSection(toolkit, SECTION_VM, "Process Cpu Time: ", minTitleColumnWidth);
			processCpuTime = toolkit.createLabel(sections.get(SECTION_VM), NOT_AVAILABLE, labelStyle);
			processCpuTime.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));
		}

		if (sections.containsKey(SECTION_MEMORY)) {
			// creates the labels for the 'memory' section
			addItemToSection(toolkit, SECTION_MEMORY, "Free physical memory: ", minTitleColumnWidth);
			freePhysMemory = toolkit.createLabel(sections.get(SECTION_MEMORY), NOT_AVAILABLE, labelStyle);
			freePhysMemory.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));

			addItemToSection(toolkit, SECTION_MEMORY, "Free swap space: ", minTitleColumnWidth);
			freeSwapSpace = toolkit.createLabel(sections.get(SECTION_MEMORY), NOT_AVAILABLE, labelStyle);
			freeSwapSpace.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));

			addItemToSection(toolkit, SECTION_MEMORY, "Committed heap size: ", minTitleColumnWidth);
			committedHeapMemorySize = toolkit.createLabel(sections.get(SECTION_MEMORY), NOT_AVAILABLE, labelStyle);
			committedHeapMemorySize.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));

			addItemToSection(toolkit, SECTION_MEMORY, "Committed non-heap size: ", minTitleColumnWidth);
			committedNonHeapMemorySize = toolkit.createLabel(sections.get(SECTION_MEMORY), NOT_AVAILABLE, labelStyle);
			committedNonHeapMemorySize.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));

			addItemToSection(toolkit, SECTION_MEMORY, "Used heap size: ", minTitleColumnWidth);
			usedHeapMemorySize = toolkit.createLabel(sections.get(SECTION_MEMORY), NOT_AVAILABLE, labelStyle);
			usedHeapMemorySize.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));

			addItemToSection(toolkit, SECTION_MEMORY, "Used non-heap size: ", minTitleColumnWidth);
			usedNonHeapMemorySize = toolkit.createLabel(sections.get(SECTION_MEMORY), NOT_AVAILABLE, labelStyle);
			usedNonHeapMemorySize.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));
		}

		if (sections.containsKey(SECTION_THREADS)) {
			// creates the labels for the 'threads' section
			addItemToSection(toolkit, SECTION_THREADS, "Live threads: ", minTitleColumnWidth);
			liveThreadCount = toolkit.createLabel(sections.get(SECTION_THREADS), NOT_AVAILABLE, labelStyle);
			liveThreadCount.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));

			addItemToSection(toolkit, SECTION_THREADS, "Daemon threads: ", minTitleColumnWidth);
			daemonThreadCount = toolkit.createLabel(sections.get(SECTION_THREADS), NOT_AVAILABLE, labelStyle);
			daemonThreadCount.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));

			addItemToSection(toolkit, SECTION_THREADS, "Peak: ", minTitleColumnWidth);
			peakThreadCount = toolkit.createLabel(sections.get(SECTION_THREADS), NOT_AVAILABLE, labelStyle);
			peakThreadCount.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));

			addItemToSection(toolkit, SECTION_THREADS, "Total threads started: ", minTitleColumnWidth);
			totalStartedThreadCount = toolkit.createLabel(sections.get(SECTION_THREADS), NOT_AVAILABLE, labelStyle);
			totalStartedThreadCount.setLayoutData(new GridData(minInformationColumnWidth, SWT.DEFAULT));
		}
	}

	/**
	 * Adds some static text informations to the parent component.
	 * 
	 * @param parent
	 *            The parent used to draw the elements to.
	 * @param toolkit
	 *            The form toolkit.
	 */
	@SuppressWarnings("unchecked")
	private void addStaticInformations(Composite parent, FormToolkit toolkit) {
		SystemInformationData data = (SystemInformationData) dataAccessService.getLastDataObject(systemObj);
		int minTitleColumnWidth = 170;
		int minInformationColumnWidth = 230;

		String processId = NOT_AVAILABLE;
		String pcName = NOT_AVAILABLE;

		if (null != data) {
			// add the panels
			addMinimizedSection(parent, toolkit, SECTION_CLASSPATH, 1);
			addMinimizedSection(parent, toolkit, VM_ARGS, 2);

			// split vm name
			String vmFullName = data.getVmName();
			String[] vmNames = vmFullName.split("@");
			if ((vmNames != null) && (vmNames.length > 1)) {
				processId = vmNames[0];
				pcName = vmNames[1];
			}

			addItemToSection(toolkit, SECTION_VM, "Vendor: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_VM, data.getVmVendor(), minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_VM, "Version: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_VM, data.getVmVersion(), minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_VM, "Process Id: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_VM, processId, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_VM, "Pc Name: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_VM, pcName, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_VM, "Jit Compiler Name: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_VM, data.getJitCompilerName(), minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_VM, "Specification Name: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_VM, data.getVmSpecName(), minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, "Max heap size: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, NumberFormatter.formatBytesToKBytes(data.getMaxHeapMemorySize()), minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, "Max non-heap size: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, NumberFormatter.formatBytesToKBytes(data.getMaxNonHeapMemorySize()), minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, "Total physical memory: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, NumberFormatter.formatBytesToKBytes(data.getTotalPhysMemory()), minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, "Total swap space: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, NumberFormatter.formatBytesToKBytes(data.getTotalSwapSpace()), minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_OS, "Operating System: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_OS, data.getOsName() + " " + data.getOsVersion(), minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_OS, "Available processors: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_OS, NumberFormatter.formatInteger(data.getAvailableProcessors()), minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_OS, "Architecture: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_OS, data.getArchitecture(), minInformationColumnWidth);

			// some classpath informations with formatting
			addItemToMinimizedSection(toolkit, SECTION_CLASSPATH, "Class path: ");
			StringTokenizer classpathTokenizer = new StringTokenizer(data.getClassPath(), ":");
			while (classpathTokenizer.hasMoreTokens()) {
				addItemToMinimizedSection(toolkit, SECTION_CLASSPATH, " \t" + classpathTokenizer.nextToken());
			}

			addItemToMinimizedSection(toolkit, SECTION_CLASSPATH, "Boot class path: ");
			StringTokenizer bootClasspathTokenizer = new StringTokenizer(data.getBootClassPath(), ":");
			while (bootClasspathTokenizer.hasMoreTokens()) {
				addItemToMinimizedSection(toolkit, SECTION_CLASSPATH, " \t" + bootClasspathTokenizer.nextToken());
			}

			addItemToMinimizedSection(toolkit, SECTION_CLASSPATH, "Library Path: ");
			StringTokenizer libPathTokenizer = new StringTokenizer(data.getLibraryPath(), ":");
			while (libPathTokenizer.hasMoreTokens()) {
				addItemToMinimizedSection(toolkit, SECTION_CLASSPATH, " \t" + libPathTokenizer.nextToken());
			}

			// sorting vm arguments
			TreeSet<VmArgumentData> treeSet = new TreeSet(new Comparator<VmArgumentData>() {
				public int compare(VmArgumentData one, VmArgumentData two) {
					return one.getVmName().compareTo(two.getVmName());
				}
			});
			treeSet.addAll(data.getVmSet());

			// showing vm arguments
			for (VmArgumentData argumentData : treeSet) {
				if (!argumentData.getVmName().endsWith("path") && (!argumentData.getVmName().endsWith("separator"))) {
					addItemToMinimizedSection(toolkit, VM_ARGS, argumentData.getVmName() + ":");
					addItemToMinimizedSection(toolkit, VM_ARGS, argumentData.getVmValue());
				}
			}
		} else {
			// if 'not available' then create the labels
			addMinimizedSectionNotAvailable(parent, toolkit, SECTION_CLASSPATH + " (n/a)", 1);
			addMinimizedSectionNotAvailable(parent, toolkit, VM_ARGS + " (n/a)", 2);

			addItemToSection(toolkit, SECTION_VM, "Vendor: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_VM, NOT_AVAILABLE, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_VM, "Version: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_VM, NOT_AVAILABLE, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_VM, "Process Id: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_VM, processId, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_VM, "Pc Name: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_VM, pcName, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_VM, "Jit Compiler Name: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_VM, NOT_AVAILABLE, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_VM, "Specification Name: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_VM, NOT_AVAILABLE, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, "Max heap size: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, NOT_AVAILABLE, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, "Max non-heap size: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, NOT_AVAILABLE, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, "Total physical memory: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, NOT_AVAILABLE, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, "Total swap space: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_MEMORY, NOT_AVAILABLE, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_OS, "Operating System: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_OS, NOT_AVAILABLE, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_OS, "Available processors: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_OS, NOT_AVAILABLE, minInformationColumnWidth);
			addItemToSection(toolkit, SECTION_OS, "Architecture: ", minTitleColumnWidth);
			addItemToSection(toolkit, SECTION_OS, NOT_AVAILABLE, minInformationColumnWidth);
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
	 * @param minColumnWidth
	 *            the minimum width of the column.
	 */
	private void addItemToSection(FormToolkit toolkit, String sectionTitle, String text, int minColumnWidth) {
		if (sections.containsKey(sectionTitle)) {
			Label label = toolkit.createLabel(sections.get(sectionTitle), text, SWT.LEFT);
			label.setLayoutData(new GridData(minColumnWidth, SWT.DEFAULT));
		}
	}

	/**
	 * Adds minimized section to bundle some content.
	 * 
	 * @param parent
	 *            The parent used to draw the elements to.
	 * @param toolkit
	 *            The form toolkit.
	 * @param sectionTitle
	 *            The title of the section.
	 * @param numColums
	 *            The number of columns to span.
	 */
	private void addMinimizedSection(Composite parent, FormToolkit toolkit, String sectionTitle, int numColums) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.TWISTIE);
		section.setText(sectionTitle);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Composite sectionComposite = toolkit.createComposite(section);
		GridLayout gridLayout = new GridLayout(numColums, false);
		gridLayout.marginLeft = 5;
		gridLayout.marginTop = 5;
		sectionComposite.setLayout(gridLayout);
		section.setClient(sectionComposite);

		if (!minimizedSections.containsKey(sectionTitle)) {
			minimizedSections.put(sectionTitle, sectionComposite);
		}
	}

	/**
	 * Adds a section which is not available due to not activated platform
	 * sensor types.
	 * 
	 * @param parent
	 *            The parent used to draw the elements to.
	 * @param toolkit
	 *            The form toolkit.
	 * @param sectionTitle
	 *            The title of the section.
	 * @param numColums
	 *            the number of columns to span.
	 */
	private void addMinimizedSectionNotAvailable(Composite parent, FormToolkit toolkit, String sectionTitle, int numColums) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.TWISTIE);
		section.setText(sectionTitle);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Composite sectionComposite = toolkit.createComposite(section);
		GridLayout gridLayout = new GridLayout(numColums, false);
		gridLayout.marginLeft = 5;
		gridLayout.marginTop = 5;
		sectionComposite.setLayout(gridLayout);

		section.setClient(sectionComposite);

		if (!minimizedSections.containsKey(sectionTitle)) {
			minimizedSections.put(sectionTitle, sectionComposite);
		}
	}

	/**
	 * Adds an item to the specified section.
	 * 
	 * @param toolkit
	 *            The form toolkit.
	 * @param sectionTitle
	 *            The title of the section.
	 * @param text
	 *            The text which will be shown.
	 */
	private void addItemToMinimizedSection(FormToolkit toolkit, String sectionTitle, String text) {
		if (minimizedSections.containsKey(sectionTitle)) {
			Label label = toolkit.createLabel(minimizedSections.get(sectionTitle), text, SWT.LEFT);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		ClassLoadingInformationData classLoadingData = (ClassLoadingInformationData) dataAccessService.getLastDataObject(classLoadingObj);
		CpuInformationData cpuData = (CpuInformationData) dataAccessService.getLastDataObject(cpuObj);
		CompilationInformationData compilationData = (CompilationInformationData) dataAccessService.getLastDataObject(compilationObj);
		MemoryInformationData memoryData = (MemoryInformationData) dataAccessService.getLastDataObject(memoryObj);
		RuntimeInformationData runtimeData = (RuntimeInformationData) dataAccessService.getLastDataObject(runtimeObj);
		ThreadInformationData threadData = (ThreadInformationData) dataAccessService.getLastDataObject(threadObj);

		updateLabels(classLoadingData, cpuData, compilationData, memoryData, runtimeData, threadData);
	}

	/**
	 * Updates the labels with the dynamic informations.
	 * 
	 * @param classLoadingData
	 *            The {@link List} containing the
	 *            {@link ClassLoadingInformationData} objects.
	 * @param cpuData
	 *            The {@link List} containing the {@link CpuInformationData}
	 *            objects.
	 * @param compilationData
	 *            The {@link List} containing the
	 *            {@link CompilationInformationData} objects.
	 * @param memoryData
	 *            The {@link List} containing the {@link MemoryInformationData}
	 *            objects.
	 * @param runtimeData
	 *            The {@link List} containing the {@link RuntimeInformationData}
	 *            objects.
	 * @param threadData
	 *            The {@link List} containing the {@link ThreadInformationData}
	 *            objects.
	 */
	private void updateLabels(ClassLoadingInformationData classLoadingData, CpuInformationData cpuData, CompilationInformationData compilationData, MemoryInformationData memoryData,
			RuntimeInformationData runtimeData, ThreadInformationData threadData) {
		if (classLoadingData != null) {
			int count = classLoadingData.getCount();
			loadedClassCount.setText(NumberFormatter.formatInteger(classLoadingData.getTotalLoadedClassCount() / count));
			totalLoadedClassCount.setText(NumberFormatter.formatLong(classLoadingData.getTotalTotalLoadedClassCount() / count));
			unloadedClassCount.setText(NumberFormatter.formatLong(classLoadingData.getTotalUnloadedClassCount() / count));
		}

		if (cpuData != null) {
			processCpuTime.setText(NumberFormatter.formatNanosToSeconds(cpuData.getTotalProcessCpuTime() / cpuData.getCount()));
		}

		if (compilationData != null) {
			totalCompilationTime.setText(NumberFormatter.formatMillisToSeconds(compilationData.getTotalTotalCompilationTime() / compilationData.getCount()));
		}

		if (memoryData != null) {
			int count = memoryData.getCount();
			freePhysMemory.setText(NumberFormatter.formatBytesToKBytes(memoryData.getTotalFreePhysMemory() / count));
			freeSwapSpace.setText(NumberFormatter.formatBytesToKBytes(memoryData.getTotalFreeSwapSpace() / count));
			committedHeapMemorySize.setText(NumberFormatter.formatBytesToKBytes(memoryData.getTotalComittedHeapMemorySize() / count));
			committedNonHeapMemorySize.setText(NumberFormatter.formatBytesToKBytes(memoryData.getTotalComittedNonHeapMemorySize() / count));
			usedHeapMemorySize.setText(NumberFormatter.formatBytesToKBytes(memoryData.getTotalUsedHeapMemorySize() / count));
			usedNonHeapMemorySize.setText(NumberFormatter.formatBytesToKBytes(memoryData.getTotalUsedNonHeapMemorySize() / count));
		}

		if (runtimeData != null) {
			uptime.setText(NumberFormatter.millisecondsToString(runtimeData.getTotalUptime() / runtimeData.getCount()));
		}

		if (threadData != null) {
			int count = threadData.getCount();
			liveThreadCount.setText(NumberFormatter.formatInteger(threadData.getTotalThreadCount() / count));
			daemonThreadCount.setText(NumberFormatter.formatInteger(threadData.getTotalDaemonThreadCount() / count));
			peakThreadCount.setText(NumberFormatter.formatInteger(threadData.getTotalPeakThreadCount() / count));
			totalStartedThreadCount.setText(NumberFormatter.formatLong(threadData.getTotalTotalStartedThreadCount() / count));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

}
