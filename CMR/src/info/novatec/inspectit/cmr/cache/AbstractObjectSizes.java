package info.novatec.inspectit.cmr.cache;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is an abstract class that holds general calculations and object sizes that are equal in both
 * 32-bit and 64-bit VM. Architecture specific calculations need to be done in implementing classes.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractObjectSizes implements IObjectSizes {

	/**
	 * General sizes of primitive types.
	 */
	private static final long BOOLEAN_SIZE = 1, INT_SIZE = 4, FLOAT_SIZE = 4, LONG_SIZE = 8, DOUBLE_SIZE = 8, OBJECT_SIZE = 8;

	/**
	 * The percentage of size expansion for each object. For security reasons. Default is 20%.
	 */
	private float objectSecurityExpansionRate = 0.2f;

	/**
	 * Returns the size of reference in bytes.
	 * 
	 * @return Size of reference.
	 */
	public abstract long getReferenceSize();

	/**
	 * {@inheritDoc}
	 */
	public long getSizeOfObject() {
		return OBJECT_SIZE;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The formula used is: 24 bytes for String object 12 bytes for char[] 2 bytes * number of chars
	 * in the string.
	 */
	public long getSizeOf(String str) {
		if (null == str) {
			return 0;
		}
		long size = 24 + str.length() * 2 + 12;
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSizeOf(Timestamp timestamp) {
		if (null == timestamp) {
			return 0;
		}
		// 72 is the number of bytes for instance of GregorianCalendar
		// inside Timestamp. However, I can not check if this is null or not.
		// In our objects I never found it to be instantiated, so I don't inculde it.
		long size = 20 + getReferenceSize();
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The formula used is: 20 bytes + one reference for ArrayList object 12 bytes for array
	 * reference size * array length.
	 */
	@SuppressWarnings("rawtypes")
	public long getSizeOf(List arrayList) {
		if (null == arrayList) {
			return 0;
		}
		long size = this.getSizeOfObject() + this.getPrimitiveTypesSize(1, 0, 2, 0, 0, 0) + this.getSizeOfArray(arrayList.size());
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSizeOfHashSet(int hashSetSize) {
		long size = this.getSizeOfObject();
		size += this.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		size += this.getSizeOfHashMap(hashSetSize);
		// for every object in the set there is additional empty object in the HashMap as value
		size += this.getSizeOfObject() * hashSetSize;
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSizeOfHashMap(int hashMapSize) {
		long size = this.getSizeOfObject();
		size += this.getPrimitiveTypesSize(5, 0, 3, 1, 0, 0);
		int mapCapacity = this.getHashMapCapacityFromSize(hashMapSize);

		// for entries
		size += this.getSizeOfArray(mapCapacity);
		size += hashMapSize * this.getSizeOfHashMapEntry();

		size += this.getSizeOfHashMapFrontCache(hashMapSize);

		// To each hash map I add 16 bytes because keySet, entrySet and values fields, that can each
		// hold 16 bytes 
		// These fields are null until these sets are requested by user.
		// Thus I add for one
		size += 16;
		
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSizeOfConcurrentHashMap(int mapSize, int concurrencyLevel) {
		long size = this.getSizeOfObject();
		size += this.getPrimitiveTypesSize(6, 0, 2, 0, 0, 0);

		// array of segments based on capacity
		size += this.getSizeOfArray(concurrencyLevel);
		size += concurrencyLevel * this.getSizeOfConcurrentSeqment();

		// and for each object in the map there is the reference to the HashEntry in Segment that we
		// need to add
		size += mapSize * alignTo8Bytes(this.getReferenceSize());
		size += mapSize * this.getSizeOfHashEntry();

		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public long alignTo8Bytes(long size) {
		return size + size % 8;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSizeOfLongObject() {
		long size = this.getSizeOfObject() + getPrimitiveTypesSize(0, 0, 0, 0, 1, 0);
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPrimitiveTypesSize(int referenceCount, int booleanCount, int intCount, int floatCount, int longCount, int doubleCount) {
		return referenceCount * getReferenceSize() + booleanCount * BOOLEAN_SIZE + intCount * INT_SIZE + floatCount * FLOAT_SIZE + longCount * LONG_SIZE + doubleCount * DOUBLE_SIZE;
	}

	/**
	 * {@inheritDoc}
	 */
	public float getObjectSecurityExpansionRate() {
		return objectSecurityExpansionRate;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setObjectSecurityExpansionRate(float objectSecurityExpansionRate) {
		this.objectSecurityExpansionRate = objectSecurityExpansionRate;
	}

	/**
	 * Calculates the size of the array with out objects in the array.
	 * 
	 * @param arraySize
	 *            Size of array (length).
	 * @return Size in bytes.
	 */
	private long getSizeOfArray(int arraySize) {
		long size = this.getSizeOfObject();
		size += this.getPrimitiveTypesSize(1 + arraySize, 0, 0, 0, 0, 0);
		return alignTo8Bytes(size);
	}

	/**
	 * Calculates the size of the {@link ConcurrentHashMap} seqment.
	 * 
	 * @return Size in bytes.
	 */
	private long getSizeOfConcurrentSeqment() {
		long size = this.getSizeOfObject();
		size += this.getPrimitiveTypesSize(2, 0, 3, 1, 0, 0);

		// plus the reentrant lock
		size += alignTo8Bytes(this.getPrimitiveTypesSize(2, 0, 3, 1, 0, 0));

		// plus just the empty array because we don't know how many objects segment has, this is
		// calculated additionally in concurrent map
		size += this.getSizeOfArray(0);
		return alignTo8Bytes(size);
	}

	/**
	 * Calculates the size of the hash entry.
	 * 
	 * @return Size in bytes.
	 */
	private long getSizeOfHashEntry() {
		long size = this.getSizeOfObject();
		size += this.getPrimitiveTypesSize(3, 0, 1, 0, 0, 0);
		return alignTo8Bytes(size);
	}

	/**
	 * Calculates the size of the hash map front cache.
	 * 
	 * @param hashMapSize
	 *            Size of hashMap.
	 * @return Size in bytes.
	 */
	private long getSizeOfHashMapFrontCache(int hashMapSize) {
		int mapCapacity = getHashMapCapacityFromSize(hashMapSize);
		long size = this.getSizeOfObject();
		size += this.getPrimitiveTypesSize(2, 0, 1, 0, 0, 0);
		size += this.getSizeOfArray(mapCapacity);
		return alignTo8Bytes(size);
	}

	/**
	 * Returns the size of a HashMap entry. Not that the key and value objects are not in this size.
	 * If HashSet is used the HashMapEntry value object will be a simple Object, thus this size has
	 * to be added to the HashSet.
	 * 
	 * @return Returns the size of a HashMap entry. Not that the key and value objects are not in
	 *         this size. If HashSet is used the HashMapEntry value object will be a simple Object,
	 *         thus this size has to be added to the HashSet.
	 */
	private long getSizeOfHashMapEntry() {
		long size = this.getSizeOfObject();
		size += this.getPrimitiveTypesSize(4, 0, 1, 0, 0, 0);
		return alignTo8Bytes(size);
	}

	/**
	 * Returns the capacity of the HashMap from it size. The calculations take the default capacity
	 * of 16 and default load factor of 0.75.
	 * 
	 * @param hashMapSize
	 *            Size of hash map.
	 * @return Returns the capacity of the HashMap from it size. The calculations take the default
	 *         capacity of 16 and default load factor of 0.75.
	 */
	private int getHashMapCapacityFromSize(int hashMapSize) {
		if (hashMapSize == 0) {
			return 0;
		}
		int capacity = 16;
		while (capacity * 0.75 < hashMapSize) {
			capacity *= 2;
		}
		return capacity;
	}

}
