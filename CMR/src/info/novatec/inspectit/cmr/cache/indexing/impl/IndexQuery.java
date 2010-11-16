package info.novatec.inspectit.cmr.cache.indexing.impl;

import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.cmr.cache.indexing.restriction.IIndexQueryRestriction;
import info.novatec.inspectit.cmr.cache.indexing.restriction.IIndexQueryRestrictionProcessor;
import info.novatec.inspectit.communication.DefaultData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link IndexQuery} represent an object that is used in querying the tree structure of the buffer.
 * 
 * @author Ivan Senic
 * 
 */
public class IndexQuery implements IIndexQuery {

	/**
	 * Minimum id that returned objects should have.
	 */
	private long minId;

	/**
	 * Platform id.
	 */
	private long platformIdent;

	/**
	 * Sensor type id.
	 */
	private long sensorTypeIdent;

	/**
	 * Method id.
	 */
	private long methodIdent;

	/**
	 * Object class type.
	 */
	private Class<?> objectClass;

	/**
	 * From date.
	 */
	private Timestamp fromDate;

	/**
	 * Till date.
	 */
	private Timestamp toDate;

	/**
	 * List of restrictions for this query.
	 */
	private List<IIndexQueryRestriction> indexingRestrictionList = new ArrayList<IIndexQueryRestriction>();

	/**
	 * Processor that checks if the given restrictions that are set in the query are fulfilled for
	 * any object.
	 */
	private IIndexQueryRestrictionProcessor restrictionProcessor;

	/**
	 * {@inheritDoc}
	 */
	public long getMinId() {
		return minId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMinId(long minId) {
		this.minId = minId;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPlatformIdent() {
		return platformIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPlatformIdent(long platformIdent) {
		this.platformIdent = platformIdent;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getSensorTypeIdent() {
		return sensorTypeIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSensorTypeIdent(long sensorTypeIdent) {
		this.sensorTypeIdent = sensorTypeIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getMethodIdent() {
		return methodIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMethodIdent(long methodIdent) {
		this.methodIdent = methodIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<?> getObjectClass() {
		return objectClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public void setObjectClass(Class objectClass) {
		this.objectClass = objectClass;
	}

	/**
	 * {@inheritDoc}
	 */
	public Timestamp getFromDate() {
		return fromDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFromDate(Timestamp fromDate) {
		this.fromDate = fromDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public Timestamp getToDate() {
		return toDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setToDate(Timestamp toDate) {
		this.toDate = toDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addIndexingRestriction(IIndexQueryRestriction indexingRestriction) {
		indexingRestrictionList.add(indexingRestriction);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isIntervalSet() {
		return ((null != fromDate) && (null != toDate) && fromDate.before(toDate));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInInterval(Timestamp timestamp) {
		if (isIntervalSet()) {
			if (null == timestamp) {
				return false;
			} else {
				if (fromDate.compareTo(timestamp) > 0) {
					return false;
				}
				if (toDate.compareTo(timestamp) < 0) {
					return false;
				}
				return true;
			}
		} else {
			return true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean areAllRestrictionsFulfilled(DefaultData defaultData) {
		return restrictionProcessor.areAllRestrictionsFulfilled(defaultData, indexingRestrictionList);
	}

	/**
	 * 
	 * @param restrictionProcessor Restriction processor to use.
	 */
	public void setRestrictionProcessor(IIndexQueryRestrictionProcessor restrictionProcessor) {
		this.restrictionProcessor = restrictionProcessor;
	}
	
}
