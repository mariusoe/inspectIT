package info.novatec.inspectit.cmr.dao.ci;

import info.novatec.inspectit.communication.data.ci.ProfileData;
import info.novatec.inspectit.communication.exception.EntityNotFoundException;

/**
 * This DAO is used to handle all {@link ProfileData} objects.
 * 
 * @author Matthias Huber
 * 
 */
public interface ProfileDataDao {

	/**
	 * Adds an {@link ProfileData} object into the database.
	 * 
	 * @param profileData
	 *            The {@link ProfileData} object to add.
	 * @return The assigned id of the stored {@link ProfileData} object.
	 */
	long addProfile(ProfileData profileData);

	/**
	 * Deletes the {@link ProfileData} object wth the given id.
	 * 
	 * @param profileId
	 *            The id of the {@link ProfileData} object to delete.
	 * @throws EntityNotFoundException
	 *             If no {@link ProfileData} object matching the given id could be found.
	 */
	void deleteProfile(long profileId) throws EntityNotFoundException;

	/**
	 * Returns the {@link ProfileData} object with the given id.
	 * 
	 * @param profileId
	 *            The id of the {@link ProfileData} object.
	 * @return The {@link ProfileData} object with the given id or null if no object could be found.
	 */
	ProfileData getProfile(long profileId);

	/**
	 * Updates this {@link ProfileData} object.
	 * 
	 * @param profileData
	 *            The {@link ProfileData} object to update.
	 */
	void updateProfile(ProfileData profileData);

}
