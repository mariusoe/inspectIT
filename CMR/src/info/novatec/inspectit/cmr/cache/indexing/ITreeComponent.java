package info.novatec.inspectit.cmr.cache.indexing;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.cmr.cache.indexing.impl.IndexingException;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Interface that defines the operations that each component in indexed tree has to implement.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of the object that the tree component is able to index.
 */
public interface ITreeComponent<E> {

	/**
	 * Put the element in the tree component.
	 * 
	 * @param element
	 *            Element to index.
	 * @throws IndexingException
	 *             Exception is thrown if the element can not be properly indexed.
	 */
	void put(E element) throws IndexingException;

	/**
	 * Get the element from tree component by passing the template object. The template object
	 * should have as large as possible information set, because then the method will be performed
	 * much faster. If passed element is null, null is returned.
	 * 
	 * @param template
	 *            Template to get.
	 * @return Found element, or null if element does not exists in the tree.
	 */
	E get(E template);

	/**
	 * Get the element from tree component by passing the template object and removes it from tree
	 * component. The template object should have as large as possible information set, because then
	 * the method will be performed much faster. If passed element is null, null is returned.
	 * 
	 * @param template
	 *            Template to get and remove.
	 * @return Found element, or null if element does not exists in the tree.
	 */
	E getAndRemove(E template);

	/**
	 * Returns the list of elements that satisfies the query. The query object should define as
	 * large as possible information set, because then the search is performed faster.
	 * 
	 * @param query
	 *            Query.
	 * @return List of elements, or empty list if nothing is found.
	 */
	List<E> query(IIndexQuery query);

	/**
	 * Cleans the tree component and its "children" from any weak references whose referenced
	 * objects has been garbage collected.
	 * 
	 * @return True if this tree component has no indexed objects any more (thus it is available for
	 *         deletion) or false otherwise.
	 */
	boolean clean();

	/**
	 * Cleans the indexing tree by submitting the {@link Runnable} to the provided
	 * {@link ExecutorService}.
	 * 
	 * @param executorService
	 *            Executor service that will run the {@link Runnable}.
	 */
	void cleanWithRunnable(ExecutorService executorService);

	/**
	 * Deletes all tree child tree components that have no indexing object any more.
	 * 
	 * @return True if this tree component has no indexed objects any more (thus it is available for
	 *         deletion) or false otherwise.
	 */
	boolean clearEmptyComponents();

	/**
	 * Removes all indexing objects from this tree component. After calling this method tree
	 * component will have zero indexed elements in it.
	 */
	void clearAll();

	/**
	 * Computes the size of the {@link ITreeComponent} with underlined {@link ITreeComponent} sizes
	 * also, but without referenced elements.
	 * 
	 * @param objectSizes Instance of {@link IObjectSizes}.
	 * @return Size of tree component in bytes.
	 */
	long getComponentSize(IObjectSizes objectSizes);

	/**
	 * Returns number of elements that are indexed in this tree component.
	 * 
	 * @return Number of indexed elements.
	 */
	long getNumberOfElements();

}
