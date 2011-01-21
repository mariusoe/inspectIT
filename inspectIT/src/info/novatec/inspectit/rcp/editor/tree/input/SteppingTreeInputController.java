package info.novatec.inspectit.rcp.editor.tree.input;

import info.novatec.inspectit.rcp.editor.tree.SteppingTreeSubView;

import java.util.List;

/**
 * An extension of {@link TreeInputController} that provides the necesarry functionality for
 * supporting {@link SteppingTreeSubView}.
 * 
 * @author Ivan Senic
 * 
 */
public interface SteppingTreeInputController extends TreeInputController {

	/**
	 * 
	 * @return List of the objects that are possible to be located in the tree.
	 */
	List<Object> getSteppingObjectList();

	/**
	 * Counts number of occurrences of one stepping element in the current tree input.
	 * 
	 * @param element
	 *            Template element to count occurrences for.
	 * @return Number of occurrences.
	 */
	int countOccurrences(Object element);

	/**
	 * Checks if the supplied occurrence of one stepping element in reachable in the current tree
	 * input.
	 * 
	 * @param element
	 *            Template element.
	 * @param occurance
	 *            Wanted occurrence.
	 * @return True if wanted occurrence for the object is reachable, otherwise false.
	 */
	boolean isElementOccurrenceReachable(Object element, int occurance);

	/**
	 * Returns the concrete element from the tree input that correspond to the template element and
	 * wanted occurrence. This element can be further used to expand the tree viewer to it.
	 * 
	 * @param template
	 *            Template element.
	 * @param occurance
	 *            Wanted occurrence.
	 * @return Concrete element or null if the wanted occurrence is not reachable.
	 */
	Object getElement(Object template, int occurance);

	/**
	 * Returns the textual representation of the stepping element.
	 * 
	 * @param element
	 *            Element to get the representation for.
	 * @return Textual representation.
	 */
	String getElementTextualRepresentation(Object element);
}
