package info.novatec.novaspy.rcp.model;

import info.novatec.novaspy.cmr.model.MethodIdent;
import info.novatec.novaspy.cmr.model.MethodSensorTypeIdent;
import info.novatec.novaspy.cmr.model.PlatformIdent;
import info.novatec.novaspy.cmr.model.PlatformSensorTypeIdent;
import info.novatec.novaspy.cmr.model.SensorTypeIdent;
import info.novatec.novaspy.rcp.NovaSpy;
import info.novatec.novaspy.rcp.NovaSpyConstants;
import info.novatec.novaspy.rcp.editor.InputDefinition;
import info.novatec.novaspy.rcp.editor.InputDefinition.IdDefinition;
import info.novatec.novaspy.rcp.formatter.SensorTypeAvailabilityEnum;
import info.novatec.novaspy.rcp.model.combinedmetrics.DeferredCombinedMetricsComposite;
import info.novatec.novaspy.rcp.repository.RepositoryDefinition;
import info.novatec.novaspy.rcp.view.server.ServerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.Assert;

/**
 * The manager is used to create a tree model currently used by the
 * {@link ServerView}.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhöfner
 * 
 */
public class TreeModelManager {

	/**
	 * The repository definition used by this tree.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * The format string of the output.
	 */
	private static final String METHOD_FORMAT = "%s(%s)";

	/**
	 * Every tree model manager needs a reference to a
	 * {@link RepositoryDefinition} which reflects a CMR.
	 * 
	 * @param repositoryDefinition
	 *            The definition of the repository / CMR.
	 */
	public TreeModelManager(RepositoryDefinition repositoryDefinition) {
		Assert.isNotNull(repositoryDefinition);

		this.repositoryDefinition = repositoryDefinition;
	}

	/**
	 * Returns the root elements of this model.
	 * 
	 * @return The root elements.
	 */
	@SuppressWarnings("unchecked")
	public Object[] getRootElements() {
		List<Component> agents = new ArrayList<Component>();

		for (PlatformIdent platformIdent : repositoryDefinition.getGlobalDataAccessService().getConnectedAgents()) {
			Composite agentTree = new Composite();
			agentTree.setName(platformIdent.getAgentName());
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Name: ");
			stringBuilder.append(platformIdent.getAgentName());
			stringBuilder.append("\n");
			stringBuilder.append("\n");
			Collections.sort(platformIdent.getDefinedIPs());
			for (String ip : (List<String>) platformIdent.getDefinedIPs()) {
				stringBuilder.append(ip);
				if (platformIdent.getDefinedIPs().indexOf(ip) != platformIdent.getDefinedIPs().size() - 1) {
					stringBuilder.append("\n");
				}
			}
			agentTree.setTooltip(stringBuilder.toString());
			agentTree.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_AGENT));

			// Add all sub-trees to this Agent
			agentTree.addChild(getInstrumentedMethodsTree(platformIdent));
			agentTree.addChild(getInvocationSequenceTree(platformIdent));
			agentTree.addChild(getSqlTree(platformIdent));
			agentTree.addChild(getSystemOverviewTree(platformIdent));
			agentTree.addChild(getExceptionTracerTree(platformIdent));
			agentTree.addChild(getCombinedMetricsTree(platformIdent));

			// add it to the list of the agents
			agents.add(agentTree);
		}

		return agents.toArray();
	}

	/**
	 * Creates the deferred sub-tree for instrumented methods.
	 * 
	 * @param platformIdent
	 *            The platform ID used to create the sub-tree.
	 * @return a list containing the root and all children representing the
	 *         instrumented methods in the target VM.
	 */
	private Component getInstrumentedMethodsTree(PlatformIdent platformIdent) {
		DeferredInstrumentationBrowserComposite instrumentedMethods = new DeferredInstrumentationBrowserComposite();
		instrumentedMethods.setName("Instrumentation Browser");
		instrumentedMethods.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_INSTRUMENTATION_BROWSER));
		instrumentedMethods.setPlatformIdent(platformIdent);
		instrumentedMethods.setRepositoryDefinition(repositoryDefinition);

		return instrumentedMethods;
	}

	/**
	 * Returns the invocation sequence tree.
	 * 
	 * @param platformIdent
	 *            The platform ident used to create the tree.
	 * @return The invocation sequence tree.
	 */
	@SuppressWarnings("unchecked")
	private Component getInvocationSequenceTree(PlatformIdent platformIdent) {
		Composite invocationSequence = new Composite();
		invocationSequence.setName("Invocation Sequences");
		invocationSequence.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_INVOCATION));

		Component showAll = new Leaf();
		showAll.setName("Show All");
		showAll.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_SHOW_ALL));

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(repositoryDefinition);
		inputDefinition.setId(SensorTypeEnum.INVOCATION_SEQUENCE);
		inputDefinition.setPartName("Invocation Sequences (Show All)");
		inputDefinition.setPartTooltip("Invocation Sequences (Show All)");
		inputDefinition.setImageDescriptor(SensorTypeEnum.INVOCATION_SEQUENCE.getImageDescriptor());
		inputDefinition.setHeaderText("Invocation Sequences");
		inputDefinition.setHeaderDescription("Show All (" + platformIdent.getAgentName() + ")");

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		inputDefinition.setIdDefinition(idDefinition);
		showAll.setInputDefinition(inputDefinition);

		Component search = new Leaf();
		search.setName("Search");
		search.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_SEARCH));

		Composite browser = new Composite();
		browser.setName("Browser");
		browser.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_INSTRUMENTATION_BROWSER));

		Set<MethodIdent> methodIdents = platformIdent.getMethodIdents();
		Map<String, List<MethodIdent>> filteredMap = new TreeMap<String, List<MethodIdent>>();
		for (MethodIdent methodIdent : methodIdents) {
			long invocationSensorTypeId = checkSensorTypeExistence(SensorTypeEnum.INVOCATION_SEQUENCE, methodIdent);
			if (invocationSensorTypeId != -1) {
				// first sort and filter the method ident list
				if (!filteredMap.containsKey(methodIdent.getPackageName())) {
					filteredMap.put(methodIdent.getPackageName(), new ArrayList<MethodIdent>());
				}
				filteredMap.get(methodIdent.getPackageName()).add(methodIdent);
			}
		}

		for (Map.Entry<String, List<MethodIdent>> entry : filteredMap.entrySet()) {
			browser.addChild(getInvocationPackageTree(entry.getValue()));
		}

		invocationSequence.addChild(showAll);
		invocationSequence.addChild(search);
		invocationSequence.addChild(browser);

		return invocationSequence;
	}

	/**
	 * Returns the invocation package sub-tree.
	 * 
	 * @param methodIdents
	 *            The {@link MethodIdent} objects to create this sub-tree.
	 * @return The invocation package sub-tree.
	 */
	private Component getInvocationPackageTree(List<MethodIdent> methodIdents) {
		Composite targetPackage = new Composite();
		targetPackage.setName(methodIdents.get(0).getPackageName());
		targetPackage.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_PACKAGE));

		Map<String, List<MethodIdent>> filter = new TreeMap<String, List<MethodIdent>>();
		for (MethodIdent methodIdent : methodIdents) {
			// first sort and filter the method ident list
			if (!filter.containsKey(methodIdent.getClassName())) {
				filter.put(methodIdent.getClassName(), new ArrayList<MethodIdent>());
			}
			filter.get(methodIdent.getClassName()).add(methodIdent);
		}

		for (Map.Entry<String, List<MethodIdent>> entry : filter.entrySet()) {
			targetPackage.addChild(getInvocationClassTree(entry.getValue()));
		}

		return targetPackage;
	}

	/**
	 * Returns the invocation class sub-tree.
	 * 
	 * @param methodIdents
	 *            The {@link MethodIdent} objects to create this sub-tree.
	 * @return The invocation class sub-tree.
	 */
	private Component getInvocationClassTree(List<MethodIdent> methodIdents) {
		Composite targetClass = new Composite();
		targetClass.setName(methodIdents.get(0).getClassName());
		targetClass.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_CLASS));

		for (MethodIdent methodIdent : methodIdents) {
			targetClass.addChild(getInvocationMethodTree(methodIdent));
		}

		Collections.sort(targetClass.getChildren(), new Comparator<Component>() {
			public int compare(Component componentOne, Component componentTwo) {
				return componentOne.getName().compareTo(componentTwo.getName());
			}
		});

		return targetClass;
	}

	/**
	 * Returns the invocation method sub-tree.
	 * 
	 * @param methodIdent
	 *            The {@link MethodIdent} object to create this sub-tree.
	 * @return The invocation method sub-tree.
	 */
	private Component getInvocationMethodTree(MethodIdent methodIdent) {
		Component targetMethod = new Leaf();
		if (null != methodIdent.getParameters()) {
			String parameters = methodIdent.getParameters().toString();
			parameters = parameters.substring(1, parameters.length() - 1);

			targetMethod.setName(String.format(METHOD_FORMAT, methodIdent.getMethodName(), parameters));
		} else {
			targetMethod.setName(String.format(METHOD_FORMAT, methodIdent.getMethodName(), ""));
		}
		targetMethod.setImageDescriptor(ModifiersImageFactory.getImageDescriptor(methodIdent.getModifiers()));

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(repositoryDefinition);
		inputDefinition.setId(SensorTypeEnum.INVOCATION_SEQUENCE);
		inputDefinition.setPartName("Invocation Sequence");
		inputDefinition.setPartTooltip("Invocation Sequence");
		inputDefinition.setImageDescriptor(SensorTypeEnum.INVOCATION_SEQUENCE.getImageDescriptor());
		inputDefinition.setHeaderText("Invocation Sequence");
		inputDefinition.setHeaderDescription(methodIdent.getPlatformIdent().getAgentName());

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(methodIdent.getPlatformIdent().getId());
		idDefinition.setMethodId(methodIdent.getId());
		idDefinition.setSensorTypeId(checkSensorTypeExistence(SensorTypeEnum.INVOCATION_SEQUENCE, methodIdent));

		inputDefinition.setIdDefinition(idDefinition);
		targetMethod.setInputDefinition(inputDefinition);

		return targetMethod;
	}

	/**
	 * Checks if the {@link SensorTypeEnum} is used for this {@link MethodIdent}
	 * .
	 * 
	 * @param sensorType
	 *            The {@link SensorTypeEnum} object to compare to.
	 * @param methodIdent
	 *            The {@link MethodIdent} object from the server to check
	 *            against the sensor type.
	 * @return Returns the value of the sensor type ID.
	 */
	@SuppressWarnings("unchecked")
	private long checkSensorTypeExistence(SensorTypeEnum sensorType, MethodIdent methodIdent) {
		for (MethodSensorTypeIdent methodSensorTypeIdent : (Set<MethodSensorTypeIdent>) methodIdent.getMethodSensorTypeIdents()) {
			SensorTypeEnum methodsensorType = SensorTypeEnum.get(methodSensorTypeIdent.getFullyQualifiedClassName());
			if (sensorType.equals(methodsensorType)) {
				return methodSensorTypeIdent.getId();
			}
		}
		return -1;
	}

	/**
	 * Returns the SQL tree.
	 * 
	 * @param platformIdent
	 *            The platform ident used to create the tree.
	 * @return The sql tree.
	 */
	private Component getSqlTree(PlatformIdent platformIdent) {
		Composite invocationSequence = new Composite();
		invocationSequence.setName("SQL Statements");
		invocationSequence.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_DATABASE));

		Component showAll = new Leaf();
		showAll.setName("Show All");
		showAll.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_SHOW_ALL));

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(repositoryDefinition);
		inputDefinition.setId(SensorTypeEnum.SQL);
		inputDefinition.setPartName("SQL Statements (Show All)");
		inputDefinition.setPartTooltip("SQL Statements (Show All)");
		inputDefinition.setImageDescriptor(SensorTypeEnum.SQL.getImageDescriptor());
		inputDefinition.setHeaderText("SQL Statements");
		inputDefinition.setHeaderDescription("Show All (" + platformIdent.getAgentName() + ")");

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		inputDefinition.setIdDefinition(idDefinition);
		showAll.setInputDefinition(inputDefinition);

		Composite filters = new Composite();
		filters.setName("Predefined Filters");
		filters.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_FILTER));

		Component lastHour = new Leaf();
		lastHour.setName("Last Hour");
		lastHour.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_LAST_HOUR));

		Component thisDay = new Leaf();
		thisDay.setName("This Day");
		thisDay.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_THIS_DAY));

		Component lastWeek = new Leaf();
		lastWeek.setName("Last Week");
		lastWeek.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_LAST_WEEK));

		filters.addChild(lastHour);
		filters.addChild(thisDay);
		filters.addChild(lastWeek);

		Component search = new Leaf();
		search.setName("Search");
		search.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_SEARCH));

		invocationSequence.addChild(showAll);
		invocationSequence.addChild(filters);
		invocationSequence.addChild(search);

		return invocationSequence;
	}

	/**
	 * Creates the sub-tree for the platform sensors.
	 * 
	 * @param platformIdent
	 *            The platform ident.
	 * @return An instance of {@link Component}.
	 */
	@SuppressWarnings("unchecked")
	private Component getSystemOverviewTree(PlatformIdent platformIdent) {
		Composite systemOverview = new Composite();
		systemOverview.setName("System Overview");
		systemOverview.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_SYSTEM_OVERVIEW));

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
		systemOverview.addChild(getPlatformSensorClassesLeaf(platformIdent, platformSensorTypeIdentList));
		systemOverview.addChild(getPlatformSensorCpuLeaf(platformIdent, platformSensorTypeIdentList));
		systemOverview.addChild(getPlatformSensorMemoryLeaf(platformIdent, platformSensorTypeIdentList));
		systemOverview.addChild(getPlatformSensorThreadLeaf(platformIdent, platformSensorTypeIdentList));
		systemOverview.addChild(getPlatformSensorVMSummaryLeaf(platformIdent, platformSensorTypeIdentList));

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
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorCpuLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents) {
		Component cpuOverview = new Leaf();
		boolean sensorTypeAvailable = false;
		cpuOverview.setName("CPU");
		cpuOverview.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_CPU_OVERVIEW));

		for (PlatformSensorTypeIdent platformSensorTypeIdent : platformSensorTypeIdents) {
			if (platformSensorTypeIdent.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.CPU_INFORMATION.getFqn())) {
				sensorTypeAvailable = true;

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(repositoryDefinition);
				inputDefinition.setId(SensorTypeEnum.CPU_INFORMATION);
				inputDefinition.setPartName("CPU");
				inputDefinition.setPartTooltip("CPU");
				inputDefinition.setImageDescriptor(SensorTypeEnum.CPU_INFORMATION.getImageDescriptor());
				inputDefinition.setHeaderText("CPU Information");
				inputDefinition.setHeaderDescription(platformIdent.getAgentName());

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(platformSensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				cpuOverview.setInputDefinition(inputDefinition);
				break;
			}
		}

		if (!sensorTypeAvailable) {
			cpuOverview.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_ITEM_NA_GREY));
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
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorClassesLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents) {
		Component classesOverview = new Leaf();
		boolean sensorTypeAvailable = false;
		classesOverview.setName("Classes");
		classesOverview.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_CLASS_OVERVIEW));

		for (PlatformSensorTypeIdent platformSensorTypeIdent : platformSensorTypeIdents) {
			if (platformSensorTypeIdent.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.CLASSLOADING_INFORMATION.getFqn())) {
				sensorTypeAvailable = true;

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(repositoryDefinition);
				inputDefinition.setId(SensorTypeEnum.CLASSLOADING_INFORMATION);
				inputDefinition.setPartName("Class Loading");
				inputDefinition.setPartTooltip("Class Loading");
				inputDefinition.setImageDescriptor(SensorTypeEnum.CLASSLOADING_INFORMATION.getImageDescriptor());
				inputDefinition.setHeaderText("Class Loading Information");
				inputDefinition.setHeaderDescription(platformIdent.getAgentName());

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(platformSensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				classesOverview.setInputDefinition(inputDefinition);
				break;
			}
		}

		if (!sensorTypeAvailable) {
			classesOverview.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_ITEM_NA_GREY));
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
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorMemoryLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents) {
		Component memoryOverview = new Leaf();
		boolean sensorTypeAvailable = false;
		memoryOverview.setName("Memory");
		memoryOverview.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_MEMORY_OVERVIEW));

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
				inputDefinition.setRepositoryDefinition(repositoryDefinition);
				inputDefinition.setId(SensorTypeEnum.MEMORY_INFORMATION);
				inputDefinition.setPartName("Memory");
				inputDefinition.setPartTooltip("Memory");
				inputDefinition.setImageDescriptor(SensorTypeEnum.MEMORY_INFORMATION.getImageDescriptor());
				inputDefinition.setHeaderText("Memory Information");
				inputDefinition.setHeaderDescription(platformIdent.getAgentName());

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(platformSensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				memoryOverview.setInputDefinition(inputDefinition);
				break;
			}
		}

		if (!sensorTypeAvailable) {
			memoryOverview.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_ITEM_NA_GREY));
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
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorThreadLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents) {
		Component threadsOverview = new Leaf();
		boolean sensorTypeAvailable = false;
		threadsOverview.setName("Threads");
		threadsOverview.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_THREADS_OVERVIEW));

		for (PlatformSensorTypeIdent platformSensorTypeIdent : platformSensorTypeIdents) {
			if (platformSensorTypeIdent.getFullyQualifiedClassName().equalsIgnoreCase(SensorTypeEnum.THREAD_INFORMATION.getFqn())) {
				sensorTypeAvailable = true;

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(repositoryDefinition);
				inputDefinition.setId(SensorTypeEnum.THREAD_INFORMATION);
				inputDefinition.setPartName("Threads");
				inputDefinition.setPartTooltip("Threads");
				inputDefinition.setImageDescriptor(SensorTypeEnum.THREAD_INFORMATION.getImageDescriptor());
				inputDefinition.setHeaderText("Thread Information");
				inputDefinition.setHeaderDescription(platformIdent.getAgentName());

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(platformIdent.getId());
				idDefinition.setSensorTypeId(platformSensorTypeIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				threadsOverview.setInputDefinition(inputDefinition);
				break;
			}
		}

		if (!sensorTypeAvailable) {
			threadsOverview.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_ITEM_NA_GREY));
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
	 * @return An instance of {@link Component}.
	 */
	private Component getPlatformSensorVMSummaryLeaf(PlatformIdent platformIdent, List<PlatformSensorTypeIdent> platformSensorTypeIdents) {
		Component vmSummary = new Leaf();
		boolean sensorTypeAvailable = false;
		vmSummary.setName("VM Summary");
		vmSummary.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_INFORMATION));

		if (platformSensorTypeIdents.size() > 0) {
			sensorTypeAvailable = true;

			InputDefinition inputDefinition = new InputDefinition();
			inputDefinition.setRepositoryDefinition(repositoryDefinition);
			inputDefinition.setId(SensorTypeEnum.SYSTEM_INFORMATION);
			inputDefinition.setPartName("System Information");
			inputDefinition.setPartTooltip("System Information");
			inputDefinition.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_INFORMATION));
			inputDefinition.setHeaderText("System Information");
			inputDefinition.setHeaderDescription(platformIdent.getAgentName());

			IdDefinition idDefinition = new IdDefinition();
			idDefinition.setPlatformId(platformIdent.getId());

			inputDefinition.setIdDefinition(idDefinition);
			vmSummary.setInputDefinition(inputDefinition);
		}

		if (!sensorTypeAvailable) {
			vmSummary.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_ITEM_NA_GREY));
			vmSummary.setTooltip(SensorTypeAvailabilityEnum.SENSOR_NA.getMessage());
		}

		return vmSummary;
	}

	/**
	 * Returns the exception tracer tree.
	 * 
	 * @param platformIdent
	 *            The {@link PlatformIdent} object used to create the tree.
	 * @return The exception tracer tree.
	 */
	private Component getExceptionTracerTree(PlatformIdent platformIdent) {
		Composite exceptionTracer = new Composite();
		exceptionTracer.setName("Exception Tracer");
		exceptionTracer.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_EXCEPTION_TRACER));

		exceptionTracer.addChild(getExceptionTree(platformIdent));
		exceptionTracer.addChild(getExceptionOverview(platformIdent));

		return exceptionTracer;
	}

	/**
	 * Returns the Exception Tree.
	 * 
	 * @param platformIdent
	 *            The {@link PlatformIdent} object.
	 * @return The Exception Tree.
	 */
	private Component getExceptionTree(PlatformIdent platformIdent) {
		Component exceptionTree = new Leaf();
		exceptionTree.setName("Exception Tree");
		exceptionTree.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_EXCEPTION_TREE));

		InputDefinition exceptionTreeInputDefinition = new InputDefinition();
		exceptionTreeInputDefinition.setRepositoryDefinition(repositoryDefinition);
		exceptionTreeInputDefinition.setId(SensorTypeEnum.EXCEPTION_TRACER);
		exceptionTreeInputDefinition.setPartName("Exception Tracer (Exception Tree)");
		exceptionTreeInputDefinition.setPartTooltip("Exception Tracer (Exception Tree)");
		exceptionTreeInputDefinition.setImageDescriptor(SensorTypeEnum.EXCEPTION_TRACER.getImageDescriptor());
		exceptionTreeInputDefinition.setHeaderText("Exception Tracer");
		exceptionTreeInputDefinition.setHeaderDescription("Exception Tracer (" + platformIdent.getAgentName() + ")");

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());

		exceptionTreeInputDefinition.setIdDefinition(idDefinition);
		exceptionTree.setInputDefinition(exceptionTreeInputDefinition);

		return exceptionTree;
	}

	/**
	 * Returns the Exception Tracer overview.
	 * 
	 * @param platformIdent
	 *            The {@link PlatformIdent} object.
	 * @return The Exception Tracer overview.
	 */
	private Component getExceptionOverview(PlatformIdent platformIdent) {
		Component exceptionOverview = new Leaf();
		exceptionOverview.setName("Exception Overview");
		exceptionOverview.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_CLASS_OVERVIEW));

		InputDefinition exceptionOverviewInputDefinition = new InputDefinition();
		exceptionOverviewInputDefinition.setRepositoryDefinition(repositoryDefinition);
		exceptionOverviewInputDefinition.setId(SensorTypeEnum.EXCEPTION_TRACER_OVERVIEW);
		exceptionOverviewInputDefinition.setPartName("Exception Tracer (Exception Overview)");
		exceptionOverviewInputDefinition.setPartTooltip("Exception Tracer (Exception Overview)");
		exceptionOverviewInputDefinition.setImageDescriptor(SensorTypeEnum.EXCEPTION_TRACER_OVERVIEW.getImageDescriptor());
		exceptionOverviewInputDefinition.setHeaderText("Exception Tracer");
		exceptionOverviewInputDefinition.setHeaderDescription("Exception Tracer (" + platformIdent.getAgentName() + ")");

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(platformIdent.getId());
		exceptionOverviewInputDefinition.setIdDefinition(idDefinition);
		exceptionOverview.setInputDefinition(exceptionOverviewInputDefinition);

		return exceptionOverview;
	}

	/**
	 * Returns the combined metrics tree.
	 * 
	 * @param platformIdent
	 *            The {@link PlatformIdent} object.
	 * @return The combined metrics tree.
	 */
	private Component getCombinedMetricsTree(PlatformIdent platformIdent) {
		DeferredCombinedMetricsComposite combinedMetrics = new DeferredCombinedMetricsComposite();
		combinedMetrics.setName("Combined Metrics");
		combinedMetrics.setImageDescriptor(NovaSpy.getDefault().getImageDescriptor(NovaSpyConstants.IMG_TIMER));
		combinedMetrics.setPlatformIdent(platformIdent);
		combinedMetrics.setRepositoryDefinition(repositoryDefinition);

		return combinedMetrics;
	}
}
