package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.SystemSensorData;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Here are the static informations of a system. These informations don't change
 * at runtime.
 * 
 * @author Eduard Tudenhöfner
 * 
 */
public class SystemInformationData extends SystemSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = -8294531858844656994L;

	/**
	 * The one-to-many association to {@link VmArgumentData}.
	 */
	private Set vmSet = new HashSet();

	/**
	 * The total amount of physical memory.
	 */
	private long totalPhysMemory = 0;

	/**
	 * The total amount of swap space.
	 */
	private long totalSwapSpace = 0;

	/**
	 * The name of the Just-in-time (JIT) compiler.
	 */
	private String jitCompilerName;

	/**
	 * The number of processors available to the virtual machine.
	 */
	private int availableProcessors = 0;

	/**
	 * The operating system architecture.
	 */
	private String architecture;

	/**
	 * The name of the operating system.
	 */
	private String osName;

	/**
	 * The version of the operating system.
	 */
	private String osVersion;

	/**
	 * The java class path, that is used by the system class loader to search
	 * for class files.
	 */
	private String classPath;

	/**
	 * The boot class path that is used by the bootstrap class loader to search
	 * for class files.
	 */
	private String bootClassPath;

	/**
	 * The java library path.
	 */
	private String libraryPath;

	/**
	 * The vendor of the virtual machine.
	 */
	private String vmVendor;

	/**
	 * The name of the virtual machine.
	 */
	private String vmName;

	/**
	 * The name representing the running virtual machine. for example:
	 * 12456@pc-name.
	 */
	private String vmSpecName;

	/**
	 * The version of the virtual machine.
	 */
	private String vmVersion;

	/**
	 * The initial amount of memory that the virtual machine requests from the
	 * operating system for heap memory management during startup.
	 */
	private long initHeapMemorySize = 0;

	/**
	 * The maximum amount of memory that can be used for heap memory management.
	 */
	private long maxHeapMemorySize = 0;

	/**
	 * The initial amount of memory that the virtual machine requests from the
	 * operating system for non-heap memory management during startup.
	 */
	private long initNonHeapMemorySize = 0;

	/**
	 * The maximum amount of memory that can be used for non-heap memory
	 * management.
	 */
	private long maxNonHeapMemorySize = 0;

	/**
	 * Default no-args constructor.
	 */
	public SystemInformationData() {
	}

	/**
	 * The constructor which needs three parameters.
	 * 
	 * @param timeStamp
	 *            The Timestamp.
	 * @param platformIdent
	 *            The PlatformIdent.
	 * @param sensorTypeIdent
	 *            The SensorTypeIdent.
	 */
	public SystemInformationData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent);
	}

	public long getTotalPhysMemory() {
		return totalPhysMemory;
	}

	public void setTotalPhysMemory(long totalPhysMemory) {
		this.totalPhysMemory = totalPhysMemory;
	}

	public long getTotalSwapSpace() {
		return totalSwapSpace;
	}

	public void setTotalSwapSpace(long totalSwapSpace) {
		this.totalSwapSpace = totalSwapSpace;
	}

	public String getJitCompilerName() {
		return jitCompilerName;
	}

	public void setJitCompilerName(String jitCompilerName) {
		this.jitCompilerName = jitCompilerName;
	}

	public int getAvailableProcessors() {
		return availableProcessors;
	}

	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getClassPath() {
		return classPath;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	public String getBootClassPath() {
		return bootClassPath;
	}

	public void setBootClassPath(String bootClassPath) {
		this.bootClassPath = bootClassPath;
	}

	public String getLibraryPath() {
		return libraryPath;
	}

	public void setLibraryPath(String libraryPath) {
		this.libraryPath = libraryPath;
	}

	public String getVmVendor() {
		return vmVendor;
	}

	public void setVmVendor(String vmVendor) {
		this.vmVendor = vmVendor;
	}

	public String getVmName() {
		return vmName;
	}

	public void setVmName(String vmName) {
		this.vmName = vmName;
	}

	public String getVmSpecName() {
		return vmSpecName;
	}

	public void setVmSpecName(String vmSpecName) {
		this.vmSpecName = vmSpecName;
	}

	public String getVmVersion() {
		return vmVersion;
	}

	public void setVmVersion(String vmVersion) {
		this.vmVersion = vmVersion;
	}

	public long getInitHeapMemorySize() {
		return initHeapMemorySize;
	}

	public void setInitHeapMemorySize(long initHeapMemorySize) {
		this.initHeapMemorySize = initHeapMemorySize;
	}

	public long getMaxHeapMemorySize() {
		return maxHeapMemorySize;
	}

	public void setMaxHeapMemorySize(long maxHeapMemorySize) {
		this.maxHeapMemorySize = maxHeapMemorySize;
	}

	public long getInitNonHeapMemorySize() {
		return initNonHeapMemorySize;
	}

	public void setInitNonHeapMemorySize(long initNonHeapMemorySize) {
		this.initNonHeapMemorySize = initNonHeapMemorySize;
	}

	public long getMaxNonHeapMemorySize() {
		return maxNonHeapMemorySize;
	}

	public void setMaxNonHeapMemorySize(long maxNonHeapMemorySize) {
		this.maxNonHeapMemorySize = maxNonHeapMemorySize;
	}

	public Set getVmSet() {
		return vmSet;
	}

	public void setVmSet(Set vmSet) {
		this.vmSet = vmSet;
	}

	public void addVMArguments(String vmArgumentName, String vmArgumentValue) {
		VmArgumentData vmArg = new VmArgumentData(vmArgumentName, vmArgumentValue);
		vmSet.add(vmArg);
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((architecture == null) ? 0 : architecture.hashCode());
		result = prime * result + availableProcessors;
		result = prime * result + ((bootClassPath == null) ? 0 : bootClassPath.hashCode());
		result = prime * result + ((classPath == null) ? 0 : classPath.hashCode());
		result = prime * result + (int) (initHeapMemorySize ^ (initHeapMemorySize >>> 32));
		result = prime * result + (int) (initNonHeapMemorySize ^ (initNonHeapMemorySize >>> 32));
		result = prime * result + ((jitCompilerName == null) ? 0 : jitCompilerName.hashCode());
		result = prime * result + ((libraryPath == null) ? 0 : libraryPath.hashCode());
		result = prime * result + (int) (maxHeapMemorySize ^ (maxHeapMemorySize >>> 32));
		result = prime * result + (int) (maxNonHeapMemorySize ^ (maxNonHeapMemorySize >>> 32));
		result = prime * result + ((osName == null) ? 0 : osName.hashCode());
		result = prime * result + ((osVersion == null) ? 0 : osVersion.hashCode());
		result = prime * result + (int) (totalPhysMemory ^ (totalPhysMemory >>> 32));
		result = prime * result + (int) (totalSwapSpace ^ (totalSwapSpace >>> 32));
		result = prime * result + ((vmName == null) ? 0 : vmName.hashCode());
		result = prime * result + ((vmSet == null) ? 0 : vmSet.hashCode());
		result = prime * result + ((vmSpecName == null) ? 0 : vmSpecName.hashCode());
		result = prime * result + ((vmVendor == null) ? 0 : vmVendor.hashCode());
		result = prime * result + ((vmVersion == null) ? 0 : vmVersion.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SystemInformationData other = (SystemInformationData) obj;
		if (architecture == null) {
			if (other.architecture != null) {
				return false;
			}
		} else if (!architecture.equals(other.architecture)) {
			return false;
		}
		if (availableProcessors != other.availableProcessors) {
			return false;
		}
		if (bootClassPath == null) {
			if (other.bootClassPath != null) {
				return false;
			}
		} else if (!bootClassPath.equals(other.bootClassPath)) {
			return false;
		}
		if (classPath == null) {
			if (other.classPath != null) {
				return false;
			}
		} else if (!classPath.equals(other.classPath)) {
			return false;
		}
		if (initHeapMemorySize != other.initHeapMemorySize) {
			return false;
		}
		if (initNonHeapMemorySize != other.initNonHeapMemorySize) {
			return false;
		}
		if (jitCompilerName == null) {
			if (other.jitCompilerName != null) {
				return false;
			}
		} else if (!jitCompilerName.equals(other.jitCompilerName)) {
			return false;
		}
		if (libraryPath == null) {
			if (other.libraryPath != null) {
				return false;
			}
		} else if (!libraryPath.equals(other.libraryPath)) {
			return false;
		}
		if (maxHeapMemorySize != other.maxHeapMemorySize) {
			return false;
		}
		if (maxNonHeapMemorySize != other.maxNonHeapMemorySize) {
			return false;
		}
		if (osName == null) {
			if (other.osName != null) {
				return false;
			}
		} else if (!osName.equals(other.osName)) {
			return false;
		}
		if (osVersion == null) {
			if (other.osVersion != null) {
				return false;
			}
		} else if (!osVersion.equals(other.osVersion)) {
			return false;
		}
		if (totalPhysMemory != other.totalPhysMemory) {
			return false;
		}
		if (totalSwapSpace != other.totalSwapSpace) {
			return false;
		}
		if (vmName == null) {
			if (other.vmName != null) {
				return false;
			}
		} else if (!vmName.equals(other.vmName)) {
			return false;
		}
		if (vmSet == null) {
			if (other.vmSet != null) {
				return false;
			}
		} else if (!vmSet.equals(other.vmSet)) {
			return false;
		}
		if (vmSpecName == null) {
			if (other.vmSpecName != null) {
				return false;
			}
		} else if (!vmSpecName.equals(other.vmSpecName)) {
			return false;
		}
		if (vmVendor == null) {
			if (other.vmVendor != null) {
				return false;
			}
		} else if (!vmVendor.equals(other.vmVendor)) {
			return false;
		}
		if (vmVersion == null) {
			if (other.vmVersion != null) {
				return false;
			}
		} else if (!vmVersion.equals(other.vmVersion)) {
			return false;
		}
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		long size =  super.getObjectSize(objectSizes);
		size += objectSizes.getPrimitiveTypesSize(12, 0, 1, 0, 6, 0);
		size += objectSizes.getSizeOf(architecture);
		size += objectSizes.getSizeOf(bootClassPath);
		size += objectSizes.getSizeOf(classPath);
		size += objectSizes.getSizeOf(jitCompilerName);
		size += objectSizes.getSizeOf(libraryPath);
		size += objectSizes.getSizeOf(osName);
		size += objectSizes.getSizeOf(osVersion);
		size += objectSizes.getSizeOf(vmName);
		size += objectSizes.getSizeOf(vmSpecName);
		size += objectSizes.getSizeOf(vmVendor);
		size += objectSizes.getSizeOf(vmVersion);
		if (null != vmSet && vmSet instanceof HashSet) {
			size += objectSizes.getSizeOfHashSet(vmSet.size());
			Iterator iterator = vmSet.iterator();
			while (iterator.hasNext()) {
				try{
					VmArgumentData vmArgumentData = (VmArgumentData) iterator.next();
					size += vmArgumentData.getObjectSize(objectSizes);
				}
				catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
		return objectSizes.alignTo8Bytes(size);
	}

}
