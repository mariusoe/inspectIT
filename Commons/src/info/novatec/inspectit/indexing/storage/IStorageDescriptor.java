package info.novatec.inspectit.indexing.storage;

/**
 * Interface for the peace of data describing one entry/object in the data file. The descriptor
 * provides information where in the file the data is located ({@link #getPosition()}), what is the
 * data size ({@link #getSize()} and what is the channel ID where the data is saved (
 * {@link #getChannelId()}.
 * 
 * @author Ivan Senic
 * 
 */
public interface IStorageDescriptor extends Comparable<IStorageDescriptor> {

	/**
	 * @return the channelId
	 */
	int getChannelId();

	/**
	 * @param channelId
	 *            the channelId to set
	 */
	void setChannelId(int channelId);

	/**
	 * @return the position
	 */
	long getPosition();

	/**
	 * @param position
	 *            the position to set
	 */
	void setPosition(long position);

	/**
	 * @return the size
	 */
	long getSize();

	/**
	 * @param size
	 *            the size to set
	 */
	void setSize(long size);

}