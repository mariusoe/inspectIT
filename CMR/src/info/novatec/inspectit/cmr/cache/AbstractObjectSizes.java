package info.novatec.inspectit.cmr.cache;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		long size = 20 + getReferenceSize() + 12 + getReferenceSize() * arrayList.size();
		return alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The formula used is: 12 bytes + one reference for HashSet object size of HashMap object
	 * because HashSet uses it.
	 */
	@SuppressWarnings("rawtypes")
	public long getSizeOf(Set hashSet) {
		if (null == hashSet) {
			return 0;
		}
		int hashSetSize = hashSet.size();
		long size = 12 + getReferenceSize();
		long hashMapSize = alignTo8Bytes(28 + 3 * getReferenceSize() + alignTo8Bytes(12 + hashSetSize * getReferenceSize()) + (hashSetSize * alignTo8Bytes(12 + 3 * getReferenceSize())));
		return alignTo8Bytes(size + hashMapSize);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The formula used is: 28 bytes + three references for HashMap object 12 bytes for elements
	 * array reference size * map size map size * HashMap.Entry size (12 bytes + three references).
	 */
	@SuppressWarnings("rawtypes")
	public long getSizeOf(Map hashMap) {
		if (null == hashMap) {
			return 0;
		}
		int hashMapSize = hashMap.size();
		long size = 28 + 3 * getReferenceSize() + alignTo8Bytes(12 + hashMapSize * getReferenceSize()) + (hashMapSize * alignTo8Bytes(12 + 3 * getReferenceSize()));
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

}
