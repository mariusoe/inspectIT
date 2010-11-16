package info.novatec.inspectit.cmr.util;

import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;

/**
 * Class that is used for providing the correct instance of {@link IIndexQuery} via Spring
 * framework.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class IndexQueryProvider {

	/**
	 * 
	 * @return Returns the correctly instated instance of {@link IIndexQuery} that can be used in
	 *         for querying the indexing tree.
	 */
	public abstract IIndexQuery createNewIndexQuery();

}
