package info.novatec.inspectit.cmr.cache.indexing.impl;

/**
 * Indexing exception class. Used for signaling problems with indexing elements.
 * 
 * @author Ivan Senic
 * 
 */
public class IndexingException extends Exception {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -8640380079673438659L;

	/**
	 * Same as {@link Exception#Exception(String)}.
	 * 
	 * @param message 
	 */
	public IndexingException(String message) {
		super(message);
	}

	/**
	 * Same as {@link Exception#Exception(String, Throwable)}.
	 * 
	 * @param message 
	 * @param throwable 
	 */
	public IndexingException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
