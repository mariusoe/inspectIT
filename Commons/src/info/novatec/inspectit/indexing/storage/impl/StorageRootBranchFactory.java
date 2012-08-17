package info.novatec.inspectit.indexing.storage.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.indexer.impl.InvocationChildrenIndexer;
import info.novatec.inspectit.indexing.indexer.impl.MethodIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.ObjectTypeIndexer;
import info.novatec.inspectit.indexing.indexer.impl.PlatformIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.SqlStringIndexer;
import info.novatec.inspectit.indexing.indexer.impl.TimestampIndexer;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * Factory for producing {@link IStorageTreeComponent}.
 * 
 * @author Ivan Senic
 * 
 */
@Component("storageRootBranchFactory")
public class StorageRootBranchFactory implements FactoryBean<IStorageTreeComponent<DefaultData>> {

	/**
	 * {@inheritDoc}
	 */
	public IStorageTreeComponent<DefaultData> getObject() throws Exception {
		StorageBranchIndexer<DefaultData> sqlStringIndexer = new StorageBranchIndexer<DefaultData>(new SqlStringIndexer<DefaultData>());
		StorageBranchIndexer<DefaultData> methodIdentIndexer = new StorageBranchIndexer<DefaultData>(new MethodIdentIndexer<DefaultData>(), sqlStringIndexer);
		StorageBranchIndexer<DefaultData> timestampIndexer = new StorageBranchIndexer<DefaultData>(new TimestampIndexer<DefaultData>(), methodIdentIndexer);
		StorageBranchIndexer<DefaultData> objectTypeIndexer = new StorageBranchIndexer<DefaultData>(new ObjectTypeIndexer<DefaultData>(), timestampIndexer);
		StorageBranchIndexer<DefaultData> invocationChildrenIndexer = new StorageBranchIndexer<DefaultData>(new InvocationChildrenIndexer<DefaultData>(), objectTypeIndexer);
		StorageBranchIndexer<DefaultData> platformIndexer = new StorageBranchIndexer<DefaultData>(new PlatformIdentIndexer<DefaultData>(), invocationChildrenIndexer);
		return new StorageBranch<DefaultData>(platformIndexer);
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<?> getObjectType() {
		return IStorageTreeComponent.class;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSingleton() {
		return false;
	}

}
