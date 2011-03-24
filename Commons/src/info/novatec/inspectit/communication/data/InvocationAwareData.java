package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.MethodSensorData;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is an abstract class for all object that can be found in invocations and should be aware of
 * it.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class InvocationAwareData extends MethodSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = 1321146768671989693L;

	/**
	 * Count of how many times this object is found in invocations. Note that this number can be
	 * larger than the number of invocations parents in the {@link #invocationParentsList} while
	 * this list contains only distinct invocation objects.
	 */
	private long objectsInInvocationsCount;

	/**
	 * Set of invocation IDs that contain this object.
	 */
	private Set invocationParentsIdSet;

	/**
	 * Default no-args constructor.
	 */
	public InvocationAwareData() {
	}

	public InvocationAwareData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	public InvocationAwareData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List parameterContentData) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent, parameterContentData);
	}

	/**
	 * Adds one invocation sequence data ID to the set of invocation IDs where this object is found.
	 * 
	 * @param id
	 *            Invocation id.
	 */
	public void addInvocationParentId(Long id) {
		if (null != id) {
			objectsInInvocationsCount++;
			if (null == invocationParentsIdSet) {
				invocationParentsIdSet = new HashSet();
			}
			invocationParentsIdSet.add(id);
		}
	}

	public Set getInvocationParentsIdSet() {
		return invocationParentsIdSet;
	}

	public void setInvocationParentsIdSet(Set invocationParentsIdSet) {
		this.invocationParentsIdSet = invocationParentsIdSet;
	}

	public long getObjectsInInvocationsCount() {
		return objectsInInvocationsCount;
	}

	public void setObjectsInInvocationsCount(long objectsInInvocationsCount) {
		this.objectsInInvocationsCount = objectsInInvocationsCount;
	}

	/**
	 * Returns the percentage of objects that are found in invocations as double.
	 * 
	 * @return Double ranging from 0 to 1.
	 */
	public abstract double getInvocationAffiliationPercentage();

	/**
	 * {@inheritDoc}
	 */
	public boolean isOnlyFoundInInvocations() {
		return getInvocationAffiliationPercentage() == 1d;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isOnlyFoundOutsideInvocations() {
		return getInvocationAffiliationPercentage() == 0d;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((invocationParentsIdSet == null) ? 0 : invocationParentsIdSet.hashCode());
		result = prime * result + (int) (objectsInInvocationsCount ^ (objectsInInvocationsCount >>> 32));
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
		InvocationAwareData invocationAwareData = (InvocationAwareData) obj;
		if (objectsInInvocationsCount != invocationAwareData.getObjectsInInvocationsCount()) {
			return false;
		}
		if (null == invocationParentsIdSet) {
			if (null != invocationAwareData.getInvocationParentsIdSet()) {
				return false;
			}
		} else {
			if (!invocationParentsIdSet.equals(invocationAwareData.getInvocationParentsIdSet())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		long size = super.getObjectSize(objectSizes);
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 1, 0);
		if (null != invocationParentsIdSet) {
			// I don't calculate the size of the invocation objects in the list because these should
			// be calculated separately
			size += objectSizes.getSizeOfHashSet(invocationParentsIdSet.size());
			size += invocationParentsIdSet.size() * objectSizes.getSizeOfLongObject();
		}
		return objectSizes.alignTo8Bytes(size);
	}
}
