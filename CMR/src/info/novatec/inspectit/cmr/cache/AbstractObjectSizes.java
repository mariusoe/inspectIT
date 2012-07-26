package info.novatec.inspectit.cmr.cache;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
	private static final long BOOLEAN_SIZE = 1, CHAR_SIZE = 2, SHORT_SIZE = 2, INT_SIZE = 4, FLOAT_SIZE = 4, LONG_SIZE = 8, DOUBLE_SIZE = 8;

	/**
	 * Default capacity of array list.
	 */
	private static final int ARRAY_LIST_INITIAL_CAPACITY = 10;

	/**
	 * Default capacity of {@link HashMap} and {@link ConcurrentHashMap}.
	 */
	private static final int MAP_INITIAL_CAPACITY = 16;

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
	public long getSizeOf(String str) {
		if (null == str) {
			return 0;
		}
		long size = this.getSizeOfObjectHeader();
		size += this.getPrimitiveTypesSize(1, 0, 3, 0, 0, 0);
		size += this.getSizeOfPrimitiveArray(str.length(), CHAR_SIZE);
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSizeOf(String... strings) {
		Set<Integer> identityHashCodeSet = new HashSet<Integer>();
		long size = 0L;
		for (String str : strings) {
			if (null == str) {
				continue;
			}
			if (identityHashCodeSet.add(System.identityHashCode(str))) {
				size += getSizeOf(str);
			}
		}
		return size;
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

		long size = this.getSizeOfObjectHeader();
		// java.sql.Timestamp
		size += this.getPrimitiveTypesSize(0, 0, 1, 0, 0, 0);
		// java.util.Date
		size += this.getPrimitiveTypesSize(1, 0, 0, 0, 1, 0);
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSizeOf(List<?> arrayList) {
		return this.getSizeOf(arrayList, ARRAY_LIST_INITIAL_CAPACITY);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSizeOf(List<?> arrayList, int initialCapacity) {
		if (null == arrayList) {
			return 0;
		}
		int capacity = getArrayCapacity(arrayList.size(), initialCapacity);
		long size = this.getSizeOfObjectHeader() + this.getPrimitiveTypesSize(1, 0, 2, 0, 0, 0) + this.getSizeOfArray(capacity);
		return alignTo8Bytes(size);
	}

	/**
	 * Returns the capacity of the array list from its size. Note that this calculation will be
	 * correct only if the array list in initialized with default capacity of
	 * {@value #ARRAY_LIST_INITIAL_CAPACITY}.
	 * 
	 * @param size
	 *            Array List size.
	 * @param initialCapacity
	 *            Initial capacity of Array list.
	 * @return Capacity of the array that holds elements.
	 */
	private int getArrayCapacity(int size, int initialCapacity) {
		while (initialCapacity < size) {
			initialCapacity = initialCapacity + (initialCapacity >> 1);
		}
		return initialCapacity;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSizeOfHashSet(int hashSetSize) {
		return this.getSizeOfHashSet(hashSetSize, MAP_INITIAL_CAPACITY);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSizeOfHashSet(int hashSetSize, int initialCapacity) {
		long size = this.getSizeOfObjectHeader();
		size += this.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		size += this.getSizeOfHashMap(hashSetSize, initialCapacity);

		// One object is used as the value in the map for all entries. This object is shared between
		// all HashSet instances, but we have to calculate it for each instance.
		if (hashSetSize > 0) {
			size += this.getSizeOfObjectObject();
		}
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSizeOfHashMap(int hashMapSize) {
		return this.getSizeOfHashMap(hashMapSize, MAP_INITIAL_CAPACITY);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSizeOfHashMap(int hashMapSize, int initialCapacity) {
		long size = this.getSizeOfObjectHeader();
		size += this.getPrimitiveTypesSize(4, 0, 3, 1, 0, 0);
		int mapCapacity = this.getHashMapCapacityFromSize(hashMapSize, initialCapacity);

		// size of the map array for the entries
		size += this.getSizeOfArray(mapCapacity);

		// size of the entries
		size += hashMapSize * this.getSizeOfHashMapEntry();

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
		long size = this.getSizeOfObjectHeader();
		size += this.getPrimitiveTypesSize(6, 0, 2, 0, 0, 0);

		// array of segments based on capacity
		size += this.getSizeOfArray(concurrencyLevel);

		// approximate capacity of each segment
		int segmentCapacity = getSegmentCapacityFromSize(mapSize / concurrencyLevel, MAP_INITIAL_CAPACITY / concurrencyLevel);
		// size of each segment based on the capacity, times number of segments
		size += concurrencyLevel * this.getSizeOfConcurrentSeqment(segmentCapacity);

		// and for each object in the map there is the reference to the HashEntry in Segment that we
		// need to add
		// size += mapSize * alignTo8Bytes(this.getReferenceSize());
		size += mapSize * this.getSizeOfHashMapEntry();

		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public long alignTo8Bytes(long size) {
		long d = size % 8;
		if (d == 0) {
			return size;
		} else {
			return size + 8 - d;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSizeOfObjectObject() {
		return alignTo8Bytes(this.getSizeOfObjectHeader());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSizeOfLongObject() {
		long size = this.getSizeOfObjectHeader() + LONG_SIZE;
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSizeOfIntegerObject() {
		long size = this.getSizeOfObjectHeader() + INT_SIZE;
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSizeOfShortObject() {
		long size = this.getSizeOfObjectHeader() + SHORT_SIZE;
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSizeOfCharacterObject() {
		long size = this.getSizeOfObjectHeader() + CHAR_SIZE;
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
	 * Calculates the size of the array with out objects in the array - <b> Can only be used on
	 * non-primitive arrays </b>.
	 * 
	 * @param arraySize
	 *            Size of array (length).
	 * @return Size in bytes.
	 */
	public long getSizeOfArray(int arraySize) {
		long size = this.getSizeOfObjectHeader();
		size += this.getPrimitiveTypesSize(arraySize, 0, 1, 0, 0, 0);
		return alignTo8Bytes(size);
	}

	/**
	 * Calculates the size of the primitive array with the primitives in the array.
	 * 
	 * @param arraySize
	 *            Size of array (length).
	 * @param primitiveSize
	 *            Size in bytes of the primitive type in array
	 * @return Size in bytes.
	 */
	protected long getSizeOfPrimitiveArray(int arraySize, long primitiveSize) {
		long size = this.getSizeOfObjectHeader();
		size += this.getPrimitiveTypesSize(0, 0, 1, 0, 0, 0);
		size += arraySize * primitiveSize;
		return alignTo8Bytes(size);
	}

	/**
	 * Calculates the size of the {@link ConcurrentHashMap} segment.
	 * 
	 * @param seqmentCapacity
	 *            Capacity that segment has.
	 * 
	 * @return Size in bytes.
	 */
	private long getSizeOfConcurrentSeqment(int seqmentCapacity) {
		long size = this.getSizeOfObjectHeader();
		size += this.getPrimitiveTypesSize(2, 0, 3, 1, 0, 0);

		// plus the sync in the reentrant lock
		size += alignTo8Bytes(this.getSizeOfObjectHeader() + this.getPrimitiveTypesSize(3, 0, 1, 0, 0, 0));

		// plus just the empty array because we don't know how many objects segment has, this is
		// calculated additionally in concurrent map
		size += this.getSizeOfArray(seqmentCapacity);
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
		long size = this.getSizeOfObjectHeader();
		size += this.getPrimitiveTypesSize(3, 0, 1, 0, 0, 0);
		return alignTo8Bytes(size);
	}

	/**
	 * Returns the capacity of the HashMap from it size. The calculations take the default capacity
	 * of 16 and default load factor of 0.75.
	 * 
	 * @param hashMapSize
	 *            Size of hash map.
	 * @param initialCapacity
	 *            Inital map capacity.
	 * @return Returns the capacity of the HashMap from it size. The calculations take the default
	 *         capacity of 16 and default load factor of 0.75.
	 */
	protected int getHashMapCapacityFromSize(int hashMapSize, int initialCapacity) {
		int capacity = 1;
		if (initialCapacity > 0) {
			capacity = initialCapacity;
		}
		float loadFactor = 0.75f;
		int threshold = (int) (capacity * loadFactor);
		while (threshold < hashMapSize) {
			capacity *= 2;
			threshold = (int) (capacity * loadFactor);
		}
		return capacity;
	}

	/**
	 * Returns the concurrent hash map segment capacity from its size and initial capacity.
	 * 
	 * @param seqmentSize
	 *            Number of elements in the segment.
	 * @param initialCapacity
	 *            Initial capacity.
	 * @return Size in bytes.
	 */
	private int getSegmentCapacityFromSize(int seqmentSize, int initialCapacity) {
		int capacity = initialCapacity;
		float loadFactor = 0.75f;
		int threshold = (int) (capacity * loadFactor);
		while (threshold + 1 <= seqmentSize) {
			capacity *= 2;
			threshold = (int) (capacity * loadFactor);
		}
		return capacity;
	}

}
