package info.novatec.inspectit.cmr.cache;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface defines necessary methods that are needed for object size calculations.
 * 
 * @author Ivan Senic
 * 
 */
public interface IObjectSizes {

	/**
	 * Returns the size of the reference in the underlying VM.
	 * 
	 * @return Size of the reference in bytes.
	 */
	long getReferenceSize();

	/**
	 * Returns a size of a {@link Object} class. This value has to be added to all classes that are
	 * subclasses of {@link Object}.
	 * 
	 * @return Size of java {@link Object} object in bytes.
	 */
	long getSizeOfObject();

	/**
	 * Calculates the approximate size of the {@link String} object based on the number of string's
	 * length.
	 * 
	 * @param str
	 *            String which size has to be calculated.
	 * @return Size of {@link String} object in bytes, or 0 if passed string is null.
	 */
	long getSizeOf(String str);

	/**
	 * Calculates the approximate size of the {@link Timestamp} object.
	 * 
	 * @param timestamp
	 *            Timestamp which size has to be calculated.
	 * @return Size of {@link Timestamp} object in bytes, or 0 if passed object is null.
	 */
	long getSizeOf(Timestamp timestamp);

	/**
	 * Calculates the approximate size of the {@link ArrayList} object. The calculation does not
	 * include the size of elements that are in the list. The calculation may not be correct for
	 * other list types.
	 * 
	 * @param arrayList
	 *            ArrayList which size has to be calculated.
	 * @return Size of {@link ArrayList} object in bytes, or 0 if passed object is null.
	 */
	long getSizeOf(List arrayList);

	/**
	 * Calculates the approximate size of the {@link HashSet} object. The calculation does not
	 * include the size of elements that are in the set. The calculation may not be correct for
	 * other set types.
	 * 
	 * @param hashSet
	 *            HashSet which size has to be calculated.
	 * @return Size of {@link HashSet} object in bytes, or 0 if passed object is null.
	 */
	long getSizeOf(Set hashSet);

	/**
	 * Calculates the approximate size of the {@link HashMap} or {@link ConcurrentHashMap} object.
	 * The calculation does not include the size of elements that are in the map. The calculation
	 * may not be correct for other map types.
	 * 
	 * @param hashMap
	 *            HashMap which size has to be calculated.
	 * @return Size of {@link HashMap} or {@link ConcurrentHashMap} object in bytes, or 0 if passed
	 *         object is null.
	 */
	long getSizeOf(Map hashMap);

	/**
	 * Returns the object size based on the number of given primitive fields in the object's class.
	 * 
	 * @param referenceCount
	 *            Number of references to objects.
	 * @param booleanCount
	 *            Number of boolean fields.
	 * @param intCount
	 *            Number of int fields.
	 * @param floatCount
	 *            Number of float fields.
	 * @param longCount
	 *            Number of long fields.
	 * @param doubleCount
	 *            Number of double fields.
	 * @return Exact object size in bytes.
	 */
	long getPrimitiveTypesSize(int referenceCount, int booleanCount, int intCount, int floatCount, int longCount, int doubleCount);

	/**
	 * Returns the aligned objects size, because the object size in memory is always a multiple of 8
	 * bytes.
	 * 
	 * @param size
	 *            Initial non-aligned object size.
	 * @return Aligned object size.
	 */
	long alignTo8Bytes(long size);

	/**
	 * Provides the rate in percentages for object size expansion for security regarding the memory.
	 * If the object size is need to expand for 20%, this method will return 0.2.
	 * 
	 * @return Security expansion rate in percentages.
	 */
	float getObjectSecurityExpansionRate();

	/**
	 * Sets the rate in percentages for object size expansion.
	 * 
	 * @param objectSecurityExpansionRate
	 *            Expansion rate. If the expansion rate should be 20%, the given value should be
	 *            0.2.
	 */
	void setObjectSecurityExpansionRate(float objectSecurityExpansionRate);
}
