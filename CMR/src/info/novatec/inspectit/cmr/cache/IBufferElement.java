package info.novatec.inspectit.cmr.cache;

/**
 * Interface that defines behavior of one element in the buffer.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of object that buffer element is holding.
 */
public interface IBufferElement<E> {

	/**
	 * Returns the object hold by the buffer element.
	 * 
	 * @return Object hold by the element.
	 */
	E getObject();

	/**
	 * Returns the size of buffer element.
	 * 
	 * @return Size of the buffer element in bytes.
	 */
	long getBufferElementSize();

	/**
	 * Sets the size of buffer element.
	 * 
	 * @param size
	 *            Size of the buffer element in bytes.
	 */
	void setBufferElementSize(long size);

	/**
	 * Calculate the size of the whole buffer element with its object and sets it.
	 * 
	 * @param objectSizes
	 *            A proper instance of {@link IObjectSizes} that correspond to the JVM.
	 */
	void calculateAndSetBufferElementSize(IObjectSizes objectSizes);

	/**
	 * Returns the next buffer element.
	 * 
	 * @return Next element in respect to the buffer logic where elements are connected or null if
	 *         this element is last inserted element in the buffer.
	 */
	IBufferElement<E> getNextElement();

	/**
	 * Connects this buffer element to the given buffer element.
	 * 
	 * @param element
	 *            Element that will be logical next element for this buffer element.
	 */
	void setNextElement(IBufferElement<E> element);

	/**
	 * Returns if the element has been analyzed.
	 * 
	 * @return True if analyzed, otherwise no.
	 */
	boolean isAnalyzed();

	/**
	 * Sets if the element has been analyzed.
	 * 
	 * @param analyzed
	 *            Boolean that marks element as analyzed or not.
	 */
	void setAnalyzed(boolean analyzed);

	/**
	 * Returns if the element has been evicted.
	 * 
	 * @return True if evicted, otherwise no.
	 */
	boolean isEvicted();

	/**
	 * Sets if the element has been evicted.
	 * 
	 * @param evicted
	 *            Boolean that marks element as analyzed or not.
	 */
	void setEvicted(boolean evicted);

	/**
	 * Returns if the element has been indexed.
	 * 
	 * @return True if indexed, otherwise no.
	 */
	boolean isIndexed();

	/**
	 * Sets if the element has been indexed.
	 * 
	 * @param indexed
	 *            True if indexed, otherwise no.
	 */
	void setIndexed(boolean indexed);
}
