package info.novatec.inspectit.agent.core;

import java.util.EventListener;
import java.util.List;

/**
 * The {@link ListListener} interface allows a class to react on events that
 * change a list.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface ListListener extends EventListener {

	/**
	 * The content of a list has changed.
	 * 
	 * @param list
	 *            The list which was changed.
	 */
	void contentChanged(List list);

}
