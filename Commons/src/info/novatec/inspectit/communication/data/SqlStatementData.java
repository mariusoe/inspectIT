package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SqlStatementData extends TimerData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = 8925352913101724757L;

	/**
	 * The SQL-String of the Statement.
	 */
	private String sql;

	/**
	 * Defines if this is a container for a prepared statement or not.
	 */
	private boolean preparedStatement = false;

	/**
	 * Contains the list of the parameter value objects.
	 */
	private List parameterValues;

	/**
	 * Default no-args constructor.
	 */
	public SqlStatementData() {
	}

	public SqlStatementData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	public SqlStatementData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, String sqlQueryString) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
		this.sql = sqlQueryString;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public void setPreparedStatement(boolean preparedStatement) {
		this.preparedStatement = preparedStatement;
	}

	public boolean isPreparedStatement() {
		return preparedStatement;
	}

	public void setParameterValues(List parameterValues) {
		this.parameterValues = parameterValues;
	}

	public List getParameterValues() {
		return parameterValues;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (preparedStatement ? 1231 : 1237);
		result = prime * result + ((sql == null) ? 0 : sql.hashCode());
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
		SqlStatementData other = (SqlStatementData) obj;
		if (preparedStatement != other.preparedStatement) {
			return false;
		}
		if (sql == null) {
			if (other.sql != null) {
				return false;
			}
		} else if (!sql.equals(other.sql)) {
			return false;
		}
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		long size =  super.getObjectSize(objectSizes);
		size += objectSizes.getPrimitiveTypesSize(2, 1, 0, 0, 0, 0);
		size += objectSizes.getSizeOf(sql);
		if (parameterValues != null && parameterValues instanceof ArrayList) {
			size += objectSizes.getSizeOf((ArrayList)parameterValues);
			Iterator iterator = parameterValues.iterator();
			while (iterator.hasNext()) {
				try {
					String str = (String) iterator.next();
					size += objectSizes.getSizeOf(str);
				}
				catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
		return objectSizes.alignTo8Bytes(size);
	}

}
