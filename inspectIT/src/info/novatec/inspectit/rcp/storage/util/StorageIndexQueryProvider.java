package info.novatec.inspectit.rcp.storage.util;

import info.novatec.inspectit.indexing.query.provider.IIndexQueryProvider;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

/**
 * Abstract class for providing the {@link StorageIndexQuery} by Spring.
 * <p>
 * This class should be moved to the CS-Commons project as soon as Spring is integrated on the UI.
 * <p>
 * The enhancement is not working with current Spring usage.
 *
 * @author Ivan Senic
 *
 */
public abstract class StorageIndexQueryProvider implements IIndexQueryProvider<StorageIndexQuery> {

	/**
	 * Creates properly initialized {@link StorageIndexQuery}.
	 *
	 * @return Returns properly initialized {@link StorageIndexQuery}.
	 */
	public abstract StorageIndexQuery createNewStorageIndexQuery();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StorageIndexQuery getIndexQuery() {
		return createNewStorageIndexQuery();
	}
}
