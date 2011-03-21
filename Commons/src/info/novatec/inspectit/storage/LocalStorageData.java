package info.novatec.inspectit.storage;

/**
 * Local storage data holds all information about a storage that will be saved on the client
 * machine.
 * 
 * @author Ivan Senic
 * 
 */
public class LocalStorageData extends AbstractStorageData {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -6129025210768974889L;
	
	/**
	 * Is the storage completely locally available (no connection to CMR needed).
	 */
	private boolean fullyDownloaded;

	/**
	 * No-argument constructor.
	 */
	public LocalStorageData() {
	}

	/**
	 * Creates a {@link LocalStorageData} from a corresponding {@link StorageData}.
	 * 
	 * @param storageData
	 *            {@link StorageData}.
	 */
	public LocalStorageData(StorageData storageData) {
		this.setId(storageData.getId());
		this.setName(storageData.getName());
	}

	/**
	 * @return the fullyDownloaded
	 */
	public boolean isFullyDownloaded() {
		return fullyDownloaded;
	}

	/**
	 * @param fullyDownloaded
	 *            the fullyDownloaded to set
	 */
	public void setFullyDownloaded(boolean fullyDownloaded) {
		this.fullyDownloaded = fullyDownloaded;
	}

}
