package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.indexing.AbstractIndexer.ChildBranchType;
import info.novatec.inspectit.cmr.cache.indexing.ITreeComponent;
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
		return new Branch<MethodSensorData>(
				new PlatformIdentIndexer<MethodSensorData>(ChildBranchType.NORMAL_BRANCH, 
						new MethodIdentIndexer<MethodSensorData>(ChildBranchType.LEAFING_BRANCH,
								new TimestampIndexer<MethodSensorData>())));
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

}
