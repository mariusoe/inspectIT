package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.MethodSensorData;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	 * Map<Long, MutableInt> that contains the ID of invocation as a key and numbers of object
	 * appearances in this invocation.
	 */
	private Map invocationsParentsIdMap;

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
			if (null == invocationsParentsIdMap) {
				invocationsParentsIdMap = new HashMap();
			}
			MutableInt count = (MutableInt) invocationsParentsIdMap.get(id);
			if (null != count) {
				count.increase();
			} else {
				invocationsParentsIdMap.put(id, new MutableInt(1));
			}
		}
	}

	/**
	 * Returns set of invocation parents IDS.
	 * 
	 * @return Returns set of invocation parents IDS.
	 */
	public Set getInvocationParentsIdSet() {
		if (null != invocationsParentsIdMap) {
			return invocationsParentsIdMap.keySet(); 
		} else {
			return Collections.EMPTY_SET;
		}
	}

	/**
	 * @return the invocationsParentsIdMap
	 */
	protected Map getInvocationsParentsIdMap() {
		return invocationsParentsIdMap;
	}

	/**
	 * Returns how much objects are contained in the invocation parents.
	 * 
	 * @return Returns how much objects are contained in the invocation parents.
	 */
	public int getObjectsInInvocationsCount() {
		int count = 0;
		if (null != invocationsParentsIdMap) {
			Iterator it = invocationsParentsIdMap.values().iterator();
			while (it.hasNext()) {
				count += ((MutableInt) it.next()).getValue();
			}
		}
		return count;
	}

	/**
	 * Aggregates the data correlated to the invocation parents. Note that this method has to be
	 * called from the subclasses when they implement any kind of aggregation.
	 * 
	 * @param invocationAwareData
	 *            Data to aggregate to current object.
	 */
	public void aggregateInvocationAwareData(InvocationAwareData invocationAwareData) {
		if (null != invocationAwareData.getInvocationsParentsIdMap()) {
			if (null == invocationsParentsIdMap) {
				invocationsParentsIdMap = new HashMap();
			}
			Iterator it = invocationAwareData.getInvocationsParentsIdMap().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Entry) it.next();
				MutableInt count = (MutableInt) invocationsParentsIdMap.get(entry.getKey());
				if (null != count) {
					count.add(((MutableInt) entry.getValue()).getValue());
				} else {
					invocationsParentsIdMap.put(entry.getKey(), new MutableInt(((MutableInt) entry.getValue()).getValue()));
				}
			}
		}
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
		result = prime * result + ((invocationsParentsIdMap == null) ? 0 : invocationsParentsIdMap.hashCode());
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
		InvocationAwareData other = (InvocationAwareData) obj;
		if (invocationsParentsIdMap == null) {
			if (other.invocationsParentsIdMap != null) {
				return false;
			}
		} else if (!invocationsParentsIdMap.equals(other.invocationsParentsIdMap)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		long size = super.getObjectSize(objectSizes);
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		if (null != invocationsParentsIdMap) {
			size += objectSizes.getSizeOfHashMap(invocationsParentsIdMap.size());
			size += invocationsParentsIdMap.size() * objectSizes.getSizeOfLongObject();
			long sizeOfMutableInt = objectSizes.alignTo8Bytes(objectSizes.getSizeOfObject() + objectSizes.getPrimitiveTypesSize(0, 0, 1, 0, 0, 0));
			size += invocationsParentsIdMap.size() * sizeOfMutableInt;
		}
		return objectSizes.alignTo8Bytes(size);
	}

	/**
	 * Simple mutable integer class for internal purposes.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class MutableInt implements Serializable {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = -2367937702260302863L;
		
		/**
		 * Value.
		 */
		private int value;

		/**
		 * Constructor that sets initial value.
		 * 
		 * @param value
		 *            Initial value.
		 */
		public MutableInt(int value) {
			this.value = value;
		}

		/**
		 * @return the value
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Increases the value.
		 */
		public void increase() {
			value++;
		}
		
		/**
		 * Adds delta to the value.
		 * @param delta Delta.
		 */
		public void add(int delta) {
			value += delta;
		}

	}
}
