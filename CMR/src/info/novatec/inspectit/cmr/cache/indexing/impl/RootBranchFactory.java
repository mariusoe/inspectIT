package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.indexing.AbstractIndexer.ChildBranchType;
import info.novatec.inspectit.cmr.cache.indexing.IBranchIndexer;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
import info.novatec.inspectit.cmr.cache.indexing.impl.RootBranchFactory.RootBranch;
import info.novatec.inspectit.communication.DefaultData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates the root branch for indexing tree. This root branch will be injected in
 * Spring as a bean.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class RootBranchFactory implements FactoryBean<RootBranch<DefaultData>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RootBranch<DefaultData> getObject() throws Exception {
		return new RootBranch<DefaultData>(new PlatformIdentIndexer<DefaultData>(ChildBranchType.NORMAL_BRANCH, new ObjectTypeIndexer<DefaultData>(ChildBranchType.NORMAL_BRANCH,
				new TimestampIndexer<DefaultData>())));
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
	 * Root branch. It has additional functionality of generating IDs for the elements that need to
	 * be put into the indexing tree.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public class RootBranch<E extends DefaultData> extends Branch<E> {

		/**
		 * Runnable for cutting the empty tree components.
		 */
		private Runnable clearEmptyComponentsRunnable = new Runnable() {

			@Override
			public void run() {
				RootBranch.this.clearEmptyComponents();
			}
		};

		/**
		 * Future that holds state of clear empty components runnable.
		 */
		private Future<?> clearEmptyComponentsFuture;

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

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void cleanWithRunnable(ExecutorService executorService) {
			super.cleanWithRunnable(executorService);
			if (clearEmptyComponentsFuture == null || clearEmptyComponentsFuture.isDone()) {
				// Submit runnable only if the future is signaling that the last one was done.
				clearEmptyComponentsFuture = executorService.submit(clearEmptyComponentsRunnable);
			}
		}

	}

}
