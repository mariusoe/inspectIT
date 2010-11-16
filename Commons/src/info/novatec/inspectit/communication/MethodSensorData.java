package info.novatec.inspectit.communication;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.cmr.cache.indexing.IIndexQuery;
import info.novatec.inspectit.communication.data.ParameterContentData;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The {@link MethodSensorData} abstract class is extended by all data & value
 * objects which are used for gathered measurements from instrumented methods.
 * Thus an additional identifier is necessary to store the unique method
 * identifier.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class MethodSensorData extends DefaultData {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 7655082885002510364L;

	/**
	 * The unique identifier of the method.
	 */
	private long methodIdent;

	/**
	 * Contains optional information about the contents of some fields /
	 * parameters etc.
	 */
	private Set parameterContentData = new HashSet(0);

	/**
	 * Default no-args constructor.
	 */
	public MethodSensorData() {
	}

	/**
	 * Constructor which accepts four parameters to initialize itself.
	 * 
	 * @param timeStamp
	 *            The timestamp.
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param sensorTypeIdent
	 *            The unique identifier of the sensor type.
	 * @param methodIdent
	 *            The unique identifier of the method.
	 */
	public MethodSensorData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent);

		this.methodIdent = methodIdent;
	}

	/**
	 * Constructor which accepts four parameters to initialize itself.
	 * 
	 * @param timeStamp
	 *            The timestamp.
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param sensorTypeIdent
	 *            The unique identifier of the sensor type.
	 * @param methodIdent
	 *            The unique identifier of the method.
	 */
	public MethodSensorData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, List parameterContentData) {
		this(timeStamp, platformIdent, sensorTypeIdent, methodIdent);

		if (null != parameterContentData) {
			this.parameterContentData = new HashSet(parameterContentData);
		}
	}

	public long getMethodIdent() {
		return methodIdent;
	}

	public void setMethodIdent(long methodIdent) {
		this.methodIdent = methodIdent;
	}

	public void addParameterContentData(ParameterContentData parameterContent) {
		parameterContentData.add(parameterContent);
	}

	public Set getParameterContentData() {
		return parameterContentData;
	}

	public void setParameterContentData(Set parameterContentData) {
		this.parameterContentData = parameterContentData;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (methodIdent ^ (methodIdent >>> 32));
		result = prime * result + ((parameterContentData == null) ? 0 : parameterContentData.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MethodSensorData other = (MethodSensorData) obj;
		if (methodIdent != other.methodIdent) {
			return false;
		}
		if (parameterContentData == null) {
			if (other.parameterContentData != null) {
				return false;
			}
		} else if (!parameterContentData.equals(other.parameterContentData)) {
			return false;
		}
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		long size = super.getObjectSize(objectSizes);
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 1, 0);
		if (null != parameterContentData && parameterContentData instanceof HashSet) {
			size += objectSizes.getSizeOf((HashSet) parameterContentData);
			Iterator iterator = parameterContentData.iterator();
			while (iterator.hasNext()) {
				try {
					ParameterContentData parameterContentData = (ParameterContentData) iterator.next();
					size += parameterContentData.getObjectSize(objectSizes);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
		return objectSizes.alignTo8Bytes(size);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isQueryComplied(IIndexQuery query) {
		if (query.getMethodIdent() != 0 && query.getMethodIdent() != methodIdent) {
			return false;
		}
		return super.isQueryComplied(query);
	}

}
