package info.novatec.inspectit.cmr.cache;

/**
 * This class has some changes in the calculations for the memory size of the objects when IBM JVM
 * is run.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractObjectSizesIbm extends AbstractObjectSizes {

	/**
	 * {@inheritDoc}
	 * <p>
	 * IBM JVM does not keep size of array in one int field.
	 */
	@Override
	public long getSizeOfArray(int arraySize) {
		long size = this.getSizeOfObjectHeader();
		size += this.getPrimitiveTypesSize(arraySize, 0, 0, 0, 0, 0);
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * IBM JVM does not keep size of array in one int field.
	 */
	@Override
	protected long getSizeOfPrimitiveArray(int arraySize, long primitiveSize) {
		long size = this.getSizeOfObjectHeader();
		size += arraySize * primitiveSize;
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The HashSet in IBM JVM has the shared map value as static variable.
	 */
	@Override
	public long getSizeOfHashSet(int hashSetSize, int initialCapacity) {
		long size = this.getSizeOfObjectHeader();
		size += this.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		size += this.getSizeOfHashMap(hashSetSize, initialCapacity);
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The IBM JVM has the object header of 8 bytes, but the minimum object size is 16 bytes. Don't
	 * know why this is.
	 */
	@Override
	public long alignTo8Bytes(long size) {
		long s = super.alignTo8Bytes(size);
		if (size < 16) {
			return 16;
		} else {
			return s;
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * HashMap from IBM JVM handles in different way the map capacity calculations.
	 */
	@Override
	protected int getHashMapCapacityFromSize(int hashMapSize, int initialCapacity) {
		return super.getHashMapCapacityFromSize(hashMapSize, calculateHashMapCapacity(initialCapacity));
	}

	/**
	 * This method is same as the HashMap implementation of IBM JVM calculates the capacity of
	 * HashMap with given specified capacity from constructors.
	 * 
	 * @param specifiedCapacity
	 *            Capacity specified in map constructor.
	 * @return Real starting capacity.
	 */
	private static int calculateHashMapCapacity(int specifiedCapacity) {
		if (specifiedCapacity >= 1 << 30) {
			return 1 << 30;
		}
		if (specifiedCapacity == 0) {
			return 16;
		}
		if (specifiedCapacity == 1) {
			return 2;
		}
		specifiedCapacity = specifiedCapacity - 1;
		specifiedCapacity |= specifiedCapacity >> 1;
		specifiedCapacity |= specifiedCapacity >> 2;
		specifiedCapacity |= specifiedCapacity >> 4;
		specifiedCapacity |= specifiedCapacity >> 8;
		specifiedCapacity |= specifiedCapacity >> 16;
		return specifiedCapacity + 1;
	}
}
