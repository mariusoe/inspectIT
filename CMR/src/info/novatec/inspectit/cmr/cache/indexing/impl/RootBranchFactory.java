package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.indexing.AbstractIndexer.ChildBranchType;
import info.novatec.inspectit.cmr.cache.indexing.IBranchIndexer;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;

import org.springframework.beans.factory.FactoryBean;

/**
 * Factory that creates the root branch for indexing tree. This root branch will be injected in
 * Spring as a bean.
 * 
 * @author Ivan Senic
 * 
 */
public class RootBranchFactory implements FactoryBean {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getObject() throws Exception {
		return new RootBranch<MethodSensorData>(new PlatformIdentIndexer<MethodSensorData>(ChildBranchType.NORMAL_BRANCH, 
				new ObjectTypeIndexer<MethodSensorData>(ChildBranchType.LEAFING_BRANCH,
						new MethodIdentIndexer<MethodSensorData>(ChildBranchType.LEAFING_BRANCH, 
								new TimestampIndexer<MethodSensorData>()))));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getObjectType() {
		return ITreeComponent.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * Root branch. It has additional functionality of generating IDs for the elements that
	 * need to be put into the indexing tree.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class RootBranch<E extends DefaultData> extends Branch<E> {


		/**
		 * Default constructor.
		 * 
		 * @param branchIndexer
		 *            Branch indexer for root branch.
		 */
		public RootBranch(IBranchIndexer<E> branchIndexer) {
			super(branchIndexer);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This method also sets the ID of the element that is put into the indexing tree.
		 */
		@Override
		public void put(E element) throws IndexingException {
			if (null == element) {
				throw new IndexingException("Null object can not be indexed.");
			}
			super.put(element);
		}
		
		
	}

}
