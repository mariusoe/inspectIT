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
	 * {@link Exception#Exception(String)}
	 */
	public IndexingException(String message) {
		super(message);
	}

	/**
	 * {@link Exception#Exception(String, Throwable)}
	 */
	public IndexingException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
