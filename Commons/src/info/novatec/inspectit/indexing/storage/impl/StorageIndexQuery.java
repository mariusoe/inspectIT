package info.novatec.inspectit.indexing.storage.impl;

import info.novatec.inspectit.indexing.impl.IndexQuery;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Extended index query that fits better when querying the {@link IStorageTreeComponent}.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageIndexQuery extends IndexQuery {

	/**
	 * Should only invocation without children be queried.
	 */
	private boolean onlyInvocationsWithoutChildren;

	/**
	 * List of the objects IDs to be included.
	 */
	private List<Long> includeIds;

	/**
	 * List of the objects IDs to be excluded.
	 */
	private List<Long> excludeIds;

	/**
	 * Wanted sql string.
	 */
	private String sql;

	/**
	 * @return the onlyInvocationsWithoutChildren
	 */
	public boolean isOnlyInvocationsWithoutChildren() {
		return onlyInvocationsWithoutChildren;
	}

	/**
	 * @param onlyInvocationsWithoutChildren
	 *            the onlyInvocationsWithoutChildren to set
	 */
	public void setOnlyInvocationsWithoutChildren(boolean onlyInvocationsWithoutChildren) {
		this.onlyInvocationsWithoutChildren = onlyInvocationsWithoutChildren;
	}

	/**
	 * @return the includeIds
	 */
	public List<Long> getIncludeIds() {
		return includeIds;
	}

	/**
	 * @param includeIds
	 *            the includeIds to set
	 */
	public void setIncludeIds(List<Long> includeIds) {
		this.includeIds = includeIds;
	}

	/**
	 * @return the excludeIds
	 */
	public List<Long> getExcludeIds() {
		return excludeIds;
	}

	/**
	 * @param excludeIds
	 *            the excludeIds to set
	 */
	public void setExcludeIds(List<Long> excludeIds) {
		this.excludeIds = excludeIds;
	}

	/**
	 * Gets {@link #sql}.
	 * 
	 * @return {@link #sql}
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * Sets {@link #sql}.
	 * 
	 * @param sql
	 *            New value for {@link #sql}
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("minId", getMinId());
		toStringBuilder.append("platformIdent", getPlatformIdent());
		toStringBuilder.append("sensorTypeIdent", getSensorTypeIdent());
		toStringBuilder.append("methodIdent", getMethodIdent());
		toStringBuilder.append("objectClasses", getObjectClasses());
		toStringBuilder.append("fromDate", getFromDate());
		toStringBuilder.append("toDate", getToDate());
		toStringBuilder.append("indexingRestrictionList", getIndexingRestrictionList());
		toStringBuilder.append("onlyInvocationsWithoutChildren", onlyInvocationsWithoutChildren);
		toStringBuilder.append("includeIds", includeIds);
		toStringBuilder.append("excludeIds", excludeIds);
		toStringBuilder.append("sql", sql);
		return toStringBuilder.toString();
	}
}
