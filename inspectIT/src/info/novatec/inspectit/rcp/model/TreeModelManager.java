package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITConstants;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.formatter.SensorTypeAvailabilityEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

/**
 * The manager is used to create a tree model currently used by the {@link ServerView}.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenh��fner
 * @author Stefan Siegl
 */
public class TreeModelManager {

	/**
	 * The repository definition used by this tree.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * Platform ident.
	 */
	private PlatformIdent platformIdent;

	/**
	 * Every tree model manager needs a reference to a {@link RepositoryDefinition} which reflects a
	 * CMR.
	 * 
	 * @param repositoryDefinition
	 *            The definition of the repository / CMR.
	 * @param platformIdent
	 *            {@link PlatformIdent} to create tree for.
	 */
	public TreeModelManager(RepositoryDefinition repositoryDefinition, PlatformIdent platformIdent) {
		Assert.isNotNull(repositoryDefinition);

		this.repositoryDefinition = repositoryDefinition;
		this.platformIdent = platformIdent;
	}

	/**
	 * Returns the root elements of this model.
	 * 
	 * @return The root elements.
	 */
	public Object[] getRootElements() {
		List<Component> components = new ArrayList<Component>();
		if (null != platformIdent) {
			// Add all sub-trees to this Agent
			components.add(getInstrumentedMethodsTree(platformIdent, repositoryDefinition));
			components.add(getInvocationSequenceTree(platformIdent, repositoryDefinition));
			components.add(getSqlTree(platformIdent, repositoryDefinition));
			components.add(getTimerTree(platformIdent, repositoryDefinition));
			components.add(getHttpTimerTree(platformIdent, repositoryDefinition));
			components.add(getExceptionSensorTree(platformIdent, repositoryDefinition));
			components.add(getSystemOverviewTree(platformIdent, repositoryDefinition));
		}
		return components.toArray();
	}

	/**
	 * Creates the deferred sub-tree for instrumented methods.
	 * 
	 * @param platformIdent
	 *            The platform ID used to create the sub-tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return a list containing the root and all children representing the instrumented methods in
	 *         the target VM.
	 */
	protected Component getInstrumentedMethodsTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		DeferredBrowserComposite instrumentedMethods = new DeferredBrowserComposite();
		instrumentedMethods.setName("Instrumentation Browser");
		instrumentedMethods.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_INSTRUMENTATION_BROWSER));
		instrumentedMethods.setPlatformIdent(platformIdent);
		instrumentedMethods.setRepositoryDefinition(definition);

		return instrumentedMethods;
	}

	/**
	 * Returns the invocation sequence tree.
	 * 
	 * @param platformIdent
	 *            The platform ident used to create the tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The invocation sequence tree.
	 */
	protected Component getInvocationSequenceTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite invocationSequence = new Composite();
		invocationSequence.setName("Invocation Sequences");
		invocationSequence.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_INVOCATION));

		Component showAll = new Leaf();
		showAll.setName("Show All");
		showAll.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_SHOW_ALL));

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(definition);
		inputDefinition.setId(SensorTypeEnum.INVOCATION_SEQUENCE);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setPartName("Invocation Sequences (Show All)");
		editorPropertiesData.setPartTooltip("Invocation Sequences (Show All)");
		editorPropertiesData.setImageDescriptor(SensorTypeEnum.INVOCATION_SEQUENCE.getImageDescriptor());
		editorPropertiesData.setHeaderText("Invocation Sequences");
		editorPropertiesData.setHeaderDescription("Show All (" + platformIdent.getAgentName() + ")");
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		inputDefinition.setIdDefinition(idDefinition);
		showAll.setInputDefinition(inputDefinition);

		FilteredDeferredBrowserComposite browser = new FilteredDeferredBrowserComposite(SensorTypeEnum.INVOCATION_SEQUENCE);
		browser.setPlatformIdent(platformIdent);
		browser.setRepositoryDefinition(repositoryDefinition);
		browser.setName("Browser");
		browser.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_INSTRUMENTATION_BROWSER));

		invocationSequence.addChild(showAll);
		invocationSequence.addChild(browser);

		return invocationSequence;
	}

	/**
	 * 
	 * Returns the SQL tree.
	 * 
	 * @param platformIdent
	 *            The platform ident used to create the tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The sql tree.
	 */
	private Component getSqlTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite invocationSequence = new Composite();
		invocationSequence.setName("SQL Statements");
		invocationSequence.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_DATABASE));

		Component showAll = new Leaf();
		showAll.setName("Show All");
		showAll.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_SHOW_ALL));

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(definition);
		inputDefinition.setId(SensorTypeEnum.SQL);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setPartName("SQL Statements (Show All)");
		editorPropertiesData.setPartTooltip("SQL Statements (Show All)");
		editorPropertiesData.setImageDescriptor(SensorTypeEnum.SQL.getImageDescriptor());
		editorPropertiesData.setHeaderText("SQL Statements");
		editorPropertiesData.setHeaderDescription("Show All (" + platformIdent.getAgentName() + ")");
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		inputDefinition.setIdDefinition(idDefinition);
		showAll.setInputDefinition(inputDefinition);

		/*
		 * Composite filters = new Composite(); filters.setName("Predefined Filters");
		 * filters.setImageDescriptor
		 * (InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_FILTER)); Component
		 * lastHour = new Leaf(); lastHour.setName("Last Hour");
		 * lastHour.setImageDescriptor(InspectIT
		 * .getDefault().getImageDescriptor(InspectITConstants.IMG_LAST_HOUR)); Component thisDay =
		 * new Leaf(); thisDay.setName("This Day");
		 * thisDay.setImageDescriptor(InspectIT.getDefault()
		 * .getImageDescriptor(InspectITConstants.IMG_THIS_DAY)); Component lastWeek = new Leaf();
		 * lastWeek.setName("Last Week");
		 * lastWeek.setImageDescriptor(InspectIT.getDefault().getImageDescriptor
		 * (InspectITConstants.IMG_LAST_WEEK)); filters.addChild(lastHour);
		 * filters.addChild(thisDay); filters.addChild(lastWeek); Component search = new Leaf();
		 * search.setName("Search");
		 * search.setImageDescriptor(InspectIT.getDefault().getImageDescriptor
		 * (InspectITConstants.IMG_SEARCH));
		 */

		invocationSequence.addChild(showAll);
		/*
		 * invocationSequence.addChild(filters); invocationSequence.addChild(search);
		 */

		return invocationSequence;
	}

	/**
	 * Creates the sub-tree for the platform sensors.
	 * 
	 * @param platformIdent
	 *            The platform ident.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return An instance of {@link Component}.
	 */
	private Component getSystemOverviewTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite systemOverview = new Composite();
		systemOverview.setName("System Overview");
		systemOverview.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_SYSTEM_OVERVIEW));

		Set<SensorTypeIdent> sensorTypeIdents = platformIdent.getSensorTypeIdents();
		List<PlatformSensorTypeIdent> platformSensorTypeIdentList = new ArrayList<PlatformSensorTypeIdent>();

		// get all platform sensor types
		for (SensorTypeIdent sensorTypeIdent : sensorTypeIdents) {
			if (sensorTypeIdent instanceof PlatformSensorTypeIdent) {
				PlatformSensorTypeIdent platformSensorTypeIdent = (PlatformSensorTypeIdent) sensorTypeIdent;
				platformSensorTypeIdentList.add(platformSensorTypeIdent);
			}
		}

		// sort the platform sensor types
		Collections.sort(platformSensorTypeIdentList, new Comparator<PlatformSensorTypeIdent>() {
			public int compare(PlatformSensorTypeIdent one, PlatformSensorTypeIdent two) {
				return one.getFullyQualifiedClassName().compareTo(two.getFullyQualifiedClassName());
			}
		});

		// add the tree elements
		systemOverview.addChild(getPlatformSensorClassesLeaf(platformIdent, platformSensorTypeIdentList, definition));
		systemOverview.addChild(getPlatformSensorCpuLeaf(platformIdent, platformSensorTypeIdentList, definition));
		systemOverview.addChild(getPlatformSensorMemoryLeaf(platformIdent, platformSensorTypeIdentList, definition));
		systemOverview.addChild(getPlatformSensorThreadLeaf(platformIdent, platformSensorTypeIdentList, definition));
		systemOverview.addChild(getPlatformSensorVMSummaryLeaf(platformIdent, platformSensorTypeIdentList, definition));

		// sort the tree elements
		Collections.sort(systemOverview.getChildren(), new Comparator<Component>() {
			public int compare(Component componentOne, Component componentTwo) {
				return componentOne.getName().compareTo(componentTwo.getName());
			}
		});

		return systemOverview;
	}

	/**
	 * Creates the cpu leaf.
	 * 
	 * @param platformIdent
	 *            The platform ident object.
	 * 
	 * @param platformSensorTypeIdents
	 *            The list of {@link PlatformSensorTypeIdent}.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorCpuLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents, RepositoryDefinition definition) {
		Component cpuOverview = new Leaf();
		boolean sensorTypeAvailable = false;
		cpuOverview.setName("CPU");
		cpuOverview.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_CPU_OVERVIEW));

		for (PlatformSensorTypeIdent platformSensorTypeIdent : platformSensorTypeIdents) {
			if (platformSensorTypeIdent.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.CPU_INFORMATION.getFqn())) {
				sensorTypeAvailable = true;

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(definition);
				inputDefinition.setId(SensorTypeEnum.CPU_INFORMATION);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setPartName("CPU");
				editorPropertiesData.setPartTooltip("CPU");
				editorPropertiesData.setImageDescriptor(SensorTypeEnum.CPU_INFORMATION.getImageDescriptor());
				editorPropertiesData.setHeaderText("CPU Information");
				editorPropertiesData.setHeaderDescription(platformIdent.getAgentName());
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(platformSensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				cpuOverview.setInputDefinition(inputDefinition);
				break;
			}
		}

		if (!sensorTypeAvailable) {
			cpuOverview.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_ITEM_NA_GREY));
			cpuOverview.setTooltip(SensorTypeAvailabilityEnum.CPU_INF_NA.getMessage());
		}

		return cpuOverview;
	}

	/**
	 * Creates the platform sensor classes leaf.
	 * 
	 * @param platformIdent
	 *            The platform ident object.
	 * 
	 * @param platformSensorTypeIdents
	 *            The list of {@link PlatformSensorTypeIdent}.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorClassesLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents, RepositoryDefinition definition) {
		Component classesOverview = new Leaf();
		boolean sensorTypeAvailable = false;
		classesOverview.setName("Classes");
		classesOverview.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_CLASS_OVERVIEW));

		for (PlatformSensorTypeIdent platformSensorTypeIdent : platformSensorTypeIdents) {
			if (platformSensorTypeIdent.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.CLASSLOADING_INFORMATION.getFqn())) {
				sensorTypeAvailable = true;

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(definition);
				inputDefinition.setId(SensorTypeEnum.CLASSLOADING_INFORMATION);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setPartName("Class Loading");
				editorPropertiesData.setPartTooltip("Class Loading");
				editorPropertiesData.setImageDescriptor(SensorTypeEnum.CLASSLOADING_INFORMATION.getImageDescriptor());
				editorPropertiesData.setHeaderText("Class Loading Information");
				editorPropertiesData.setHeaderDescription(platformIdent.getAgentName());
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(platformSensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				classesOverview.setInputDefinition(inputDefinition);
				break;
			}
		}

		if (!sensorTypeAvailable) {
			classesOverview.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_ITEM_NA_GREY));
			classesOverview.setTooltip(SensorTypeAvailabilityEnum.CLASS_INF_NA.getMessage());
		}

		return classesOverview;
	}

	/**
	 * Creates the platform sensor memory leaf.
	 * 
	 * @param platformIdent
	 *            The platform ident object.
	 * 
	 * @param platformSensorTypeIdents
	 *            The list of {@link PlatformSensorTypeIdent}.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorMemoryLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents, RepositoryDefinition definition) {
		Component memoryOverview = new Leaf();
		boolean sensorTypeAvailable = false;
		memoryOverview.setName("Memory");
		memoryOverview.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_MEMORY_OVERVIEW));

		for (PlatformSensorTypeIdent platformSensorTypeIdent : platformSensorTypeIdents) {
			if (platformSensorTypeIdent.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.MEMORY_INFORMATION.getFqn())) {
				sensorTypeAvailable = true;
				List<PlatformSensorTypeIdent> platformSensorTypeIdentList = new ArrayList<PlatformSensorTypeIdent>();
				// add sensor types to local list
				platformSensorTypeIdentList.add(platformSensorTypeIdent);
				for (PlatformSensorTypeIdent platformSensorTypeIdent2 : platformSensorTypeIdents) {
					if (platformSensorTypeIdent2.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.SYSTEM_INFORMATION.getFqn())) {
						platformSensorTypeIdentList.add(platformSensorTypeIdent2);
					}
				}

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(definition);
				inputDefinition.setId(SensorTypeEnum.MEMORY_INFORMATION);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setPartName("Memory");
				editorPropertiesData.setPartTooltip("Memory");
				editorPropertiesData.setImageDescriptor(SensorTypeEnum.MEMORY_INFORMATION.getImageDescriptor());
				editorPropertiesData.setHeaderText("Memory Information");
				editorPropertiesData.setHeaderDescription(platformIdent.getAgentName());
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(platformSensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				memoryOverview.setInputDefinition(inputDefinition);
				break;
			}
		}

		if (!sensorTypeAvailable) {
			memoryOverview.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_ITEM_NA_GREY));
			memoryOverview.setTooltip(SensorTypeAvailabilityEnum.MEMORY_INF_NA.getMessage());
		}

		return memoryOverview;
	}

	/**
	 * Creates the platform sensor thread leaf.
	 * 
	 * @param platformIdent
	 *            The platform ident object.
	 * 
	 * @param platformSensorTypeIdents
	 *            The list of {@link PlatformSensorTypeIdent}.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorThreadLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents, RepositoryDefinition definition) {
		Component threadsOverview = new Leaf();
		boolean sensorTypeAvailable = false;
		threadsOverview.setName("Threads");
		threadsOverview.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_THREADS_OVERVIEW));

		for (PlatformSensorTypeIdent platformSensorTypeIdent : platformSensorTypeIdents) {
			if (platformSensorTypeIdent.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.THREAD_INFORMATION.getFqn())) {
				sensorTypeAvailable = true;

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(definition);
				inputDefinition.setId(SensorTypeEnum.THREAD_INFORMATION);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setPartName("Threads");
				editorPropertiesData.setPartTooltip("Threads");
				editorPropertiesData.setImageDescriptor(SensorTypeEnum.THREAD_INFORMATION.getImageDescriptor());
				editorPropertiesData.setHeaderText("Thread Information");
				editorPropertiesData.setHeaderDescription(platformIdent.getAgentName());
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(platformSensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				threadsOverview.setInputDefinition(inputDefinition);
				break;
			}
		}

		if (!sensorTypeAvailable) {
			threadsOverview.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_ITEM_NA_GREY));
			threadsOverview.setTooltip(SensorTypeAvailabilityEnum.THREAD_INF_NA.getMessage());
		}

		return threadsOverview;
	}

	/**
	 * Creates the platform sensor VM Summary leaf.
	 * 
	 * @param platformIdent
	 *            The platform ident object.
	 * 
	 * @param platformSensorTypeIdents
	 *            The list of {@link PlatformSensorTypeIdent}.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorVMSummaryLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents, RepositoryDefinition definition) {
		Component vmSummary = new Leaf();
		boolean sensorTypeAvailable = false;
		vmSummary.setName("VM Summary");
		vmSummary.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_INFORMATION));

		if (platformSensorTypeIdents.size() > 0) {
			sensorTypeAvailable = true;

			InputDefinition inputDefinition = new InputDefinition();
			inputDefinition.setRepositoryDefinition(definition);
			inputDefinition.setId(SensorTypeEnum.SYSTEM_INFORMATION);

			EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
			editorPropertiesData.setPartName("System Information");
			editorPropertiesData.setPartTooltip("System Information");
			editorPropertiesData.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_INFORMATION));
			editorPropertiesData.setHeaderText("System Information");
			editorPropertiesData.setHeaderDescription(platformIdent.getAgentName());
			inputDefinition.setEditorPropertiesData(editorPropertiesData);

			IdDefinition idDefinition = new IdDefinition();
			idDefinition.setPlatformId(platformIdent.getId());

			inputDefinition.setIdDefinition(idDefinition);
			vmSummary.setInputDefinition(inputDefinition);
		}

		if (!sensorTypeAvailable) {
			vmSummary.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_ITEM_NA_GREY));
			vmSummary.setTooltip(SensorTypeAvailabilityEnum.SENSOR_NA.getMessage());
		}

		return vmSummary;
	}

	/**
	 * Returns the exception sensor tree.
	 * 
	 * @param platformIdent
	 *            The {@link PlatformIdent} object used to create the tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The exception sensor tree.
	 */
	private Component getExceptionSensorTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite exceptionSensor = new Composite();
		exceptionSensor.setName("Exceptions");
		boolean sensorTypeAvailable = false;

		Set<SensorTypeIdent> sensorTypeIdents = platformIdent.getSensorTypeIdents();
		for (SensorTypeIdent sensorTypeIdent : sensorTypeIdents) {
			if (SensorTypeEnum.EXCEPTION_SENSOR.getFqn().equals(sensorTypeIdent.getFullyQualifiedClassName())) {
				sensorTypeAvailable = true;
				break;
			}
		}

		if (sensorTypeAvailable) {
			exceptionSensor.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_EXCEPTION_SENSOR));
			exceptionSensor.addChild(getUngroupedExceptionOverview(platformIdent, definition));
			exceptionSensor.addChild(getGroupedExceptionOverview(platformIdent, definition));
		} else {
			exceptionSensor.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_ITEM_NA_GREY));
			exceptionSensor.setTooltip(SensorTypeAvailabilityEnum.EXCEPTION_SENSOR_NA.getMessage());
		}

		return exceptionSensor;
	}

	/**
	 * Returns the ungrouped Exception Overview.
	 * 
	 * @param platformIdent
	 *            The {@link PlatformIdent} object.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The Exception Tree.
	 */
	private Component getUngroupedExceptionOverview(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Component ungroupedExceptionOverview = new Leaf();
		ungroupedExceptionOverview.setName("Show All");
		ungroupedExceptionOverview.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_SHOW_ALL));

		InputDefinition ungroupedExceptionOverviewInputDefinition = new InputDefinition();
		ungroupedExceptionOverviewInputDefinition.setRepositoryDefinition(definition);
		ungroupedExceptionOverviewInputDefinition.setId(SensorTypeEnum.EXCEPTION_SENSOR);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setPartName("Exceptions (Show All)");
		editorPropertiesData.setPartTooltip("Exceptions (Show All)");
		editorPropertiesData.setImageDescriptor(SensorTypeEnum.EXCEPTION_SENSOR.getImageDescriptor());
		editorPropertiesData.setHeaderText("Exceptions");
		editorPropertiesData.setHeaderDescription("Exceptions (" + platformIdent.getAgentName() + ")");
		ungroupedExceptionOverviewInputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		ungroupedExceptionOverviewInputDefinition.setIdDefinition(idDefinition);
		ungroupedExceptionOverview.setInputDefinition(ungroupedExceptionOverviewInputDefinition);

		return ungroupedExceptionOverview;
	}

	/**
	 * Returns the grouped Exception Overview.
	 * 
	 * @param platformIdent
	 *            The {@link PlatformIdent} object.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The Exception Sensor overview.
	 */
	private Component getGroupedExceptionOverview(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Component groupedExceptionOverview = new Leaf();
		groupedExceptionOverview.setName("Grouped");
		groupedExceptionOverview.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_FILTER));

		InputDefinition groupedExceptionOverviewInputDefinition = new InputDefinition();
		groupedExceptionOverviewInputDefinition.setRepositoryDefinition(definition);
		groupedExceptionOverviewInputDefinition.setId(SensorTypeEnum.EXCEPTION_SENSOR_GROUPED);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setPartName("Exceptions (Grouped)");
		editorPropertiesData.setPartTooltip("Exceptions (Grouped)");
		editorPropertiesData.setImageDescriptor(SensorTypeEnum.EXCEPTION_SENSOR_GROUPED.getImageDescriptor());
		editorPropertiesData.setHeaderText("Exceptions");
		editorPropertiesData.setHeaderDescription("Exceptions (" + platformIdent.getAgentName() + ")");
		groupedExceptionOverviewInputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());
		groupedExceptionOverviewInputDefinition.setIdDefinition(idDefinition);
		groupedExceptionOverview.setInputDefinition(groupedExceptionOverviewInputDefinition);

		return groupedExceptionOverview;
	}

	/**
	 * Returns the Timer data tree.
	 * 
	 * @param platformIdent
	 *            The platform ident used to create the tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The timer data tree.
	 */
	private Component getTimerTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite timerDataComposite = new Composite();
		timerDataComposite.setName("Timer Data");
		timerDataComposite.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_TIMER));

		Component showAll = new Leaf();
		showAll.setName("Show All");
		showAll.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_SHOW_ALL));

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(definition);
		inputDefinition.setId(SensorTypeEnum.AGGREGATED_TIMER_DATA);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setPartName("Aggregated Timer Data (Show All)");
		editorPropertiesData.setPartTooltip("Aggregated Timer Data (Show All)");
		editorPropertiesData.setImageDescriptor(SensorTypeEnum.AGGREGATED_TIMER_DATA.getImageDescriptor());
		editorPropertiesData.setHeaderText("Aggregated Timer Data");
		editorPropertiesData.setHeaderDescription("Show All (" + platformIdent.getAgentName() + ")");
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		inputDefinition.setIdDefinition(idDefinition);
		showAll.setInputDefinition(inputDefinition);

		FilteredDeferredBrowserComposite browser = new FilteredDeferredBrowserComposite(SensorTypeEnum.TIMER);
		browser.setPlatformIdent(platformIdent);
		browser.setRepositoryDefinition(repositoryDefinition);
		browser.setName("Browser");
		browser.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_INSTRUMENTATION_BROWSER));

		timerDataComposite.addChild(showAll);
		timerDataComposite.addChild(browser);

		return timerDataComposite;
	}

	/**
	 * Returns the Http Timer data tree.
	 * 
	 * @param platformIdent
	 *            The platform ident used to create the tree.
	 * @param definition
	 *            The {@link RepositoryDefinition} object.
	 * @return The timer data tree.
	 */
	private Component getHttpTimerTree(PlatformIdent platformIdent, RepositoryDefinition definition) {
		Composite timerDataComposite = new Composite();
		timerDataComposite.setName("Http Timer Data");
		timerDataComposite.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_HTTP));

		Component urlAggregationView = new Leaf();
		urlAggregationView.setName("URI Aggregation");
		urlAggregationView.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_HTTP_AGGREGATE));
		urlAggregationView.setTooltip("Aggregates all http requests that are currently in the buffer based on its URI");

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(definition);
		inputDefinition.setId(SensorTypeEnum.HTTP_TIMER_SENSOR);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setPartName("Http (" + platformIdent.getAgentName() + ")");
		editorPropertiesData.setPartTooltip("Aggregates all http requests that are currently in the buffer based on its URI");
		editorPropertiesData.setImageDescriptor(SensorTypeEnum.HTTP_TIMER_SENSOR.getImageDescriptor());
		editorPropertiesData.setHeaderText("URI based aggregation");
		editorPropertiesData.setHeaderDescription("Show All (" + platformIdent.getAgentName() + ")");
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		inputDefinition.setIdDefinition(idDefinition);
		urlAggregationView.setInputDefinition(inputDefinition);

		timerDataComposite.addChild(urlAggregationView);

		Component taggedView = new Leaf();
		taggedView.setName("Use Case Aggregation");
		taggedView.setImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITConstants.IMG_HTTP_TAGGED));
		taggedView.setTooltip("Aggregates all http request that are currently in the buffer based on a the concrete value of the inspectIT Tag Header (called \""
				+ HttpTimerData.INSPECTIT_TAGGING_HEADER + "\")");

		inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(definition);
		inputDefinition.setId(SensorTypeEnum.TAGGED_HTTP_TIMER_SENSOR);

		editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setPartName("Use Case Http (" + platformIdent.getAgentName() + ")");
		editorPropertiesData.setPartTooltip("Aggregates all http request that are currently in the buffer based on a the concrete value of the inspectIT Tag Header (called \""
				+ HttpTimerData.INSPECTIT_TAGGING_HEADER + "\")");
		editorPropertiesData.setImageDescriptor(SensorTypeEnum.TAGGED_HTTP_TIMER_SENSOR.getImageDescriptor());
		editorPropertiesData.setHeaderText("Use Case based aggregation");
		editorPropertiesData.setHeaderDescription("Show All (" + platformIdent.getAgentName() + ")");
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		inputDefinition.setIdDefinition(idDefinition);
		taggedView.setInputDefinition(inputDefinition);

		timerDataComposite.addChild(taggedView);

		return timerDataComposite;
	}

}
