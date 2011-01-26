package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.util.List;

/**
 * Service interface which defines the methods to retrieve data objects based on
 * the invocation recordings.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IInvocationDataAccessService {

	/**
	 * Returns a list of {@link InvocationSequenceData} objects which contain no
	 * associations to other objects. Thus this list can be used to get an
	 * overview of the available invocation sequences. The limit defines the
	 * size of the list.
	 * 
	 * @param platformId
	 *            The ID of the platform.
	 * @param methodId
	 *            The ID of the method.
	 * @param limit
	 *            The limit/size of the list.
	 * 
	 * @return Returns the list of invocation sequences.
	 */
	List getInvocationSequenceOverview(long platformId, long methodId, int limit);

	/**
	 * Returns a list of {@link InvocationSequenceData} objects which contain no
	 * associations to other objects. Thus this list can be used to get an
	 * overview of the available invocation sequences. The limit defines the
	 * size of the list.
	 * <p>
	 * Compared to the above method, this service method returns all invocations
	 * for a specific agent, not only the invocations for specific methods.
	 * 
	 * @param platformId
	 *            The ID of the platform.
	 * @param limit
	 *            The limit/size of the list.
	 * 
	 * @return Returns the list of invocation sequences.
	 */
	List getInvocationSequenceOverview(long platformId, int limit);

	/**
	 * This service method is used to get all the details of a specific
	 * invocation sequence.
	 * 
	 * @param template
	 *            The template data object.
	 * @return The detailed invocation sequence object.
	 */
	InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template);

}
