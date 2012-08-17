package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

import java.sql.Timestamp;
import java.util.List;

/**
 * {@link SqlStatementData} holds the information about the executed SQL. The executed SQL statement
 * is stored in the {@link #sql} field, while {@link #parameterValues} hold the values that
 * statement was executed with.
 * 
 * @author Ivan Senic
 * 
 */
public class SqlStatementData extends TimerData {

	/**
	 * The serial version UID for this class.
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
	private List<String> parameterValues;

	/**
	 * Complete SQL query string with '?' from {@link #sql} replaced with the parameter values in
	 * the {@link #parameterValues} list. Calculated only on first request.
	 */
	private transient String sqlWithParameterValues;

	/**
	 * Default no-args constructor.
	 */
	public SqlStatementData() {
	}

	/**
	 * Secondary constructor. Sets the main information about the data object. Same as calling
	 * {@link #SqlStatementData(Timestamp, long, long, long, null)}.
	 * 
	 * @param timeStamp
	 *            Time-stamp holding the information when the SQL execution started.
	 * @param platformIdent
	 *            Platform/agent.
	 * @param sensorTypeIdent
	 *            Assigned sensor type ident.
	 * @param methodIdent
	 *            Assigned method ident.
	 */
	public SqlStatementData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		this(timeStamp, platformIdent, sensorTypeIdent, methodIdent, null);
	}

	/**
	 * Additional constructor. Sets the main information about the data object and SQL query string.
	 * 
	 * @param timeStamp
	 *            Time-stamp holding the information when the SQL execution started.
	 * @param platformIdent
	 *            Platform/agent.
	 * @param sensorTypeIdent
	 *            Assigned sensor type ident.
	 * @param methodIdent
	 *            Assigned method ident.
	 * @param sqlQueryString
	 *            Query string.
	 */
	public SqlStatementData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent, String sqlQueryString) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
		this.sql = sqlQueryString;
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
	 * Gets {@link #preparedStatement}.
	 * 
	 * @return {@link #preparedStatement}
	 */
	public boolean isPreparedStatement() {
		return preparedStatement;
	}

	/**
	 * Sets {@link #preparedStatement}.
	 * 
	 * @param preparedStatement
	 *            New value for {@link #preparedStatement}
	 */
	public void setPreparedStatement(boolean preparedStatement) {
		this.preparedStatement = preparedStatement;
	}

	/**
	 * Gets {@link #parameterValues}.
	 * 
	 * @return {@link #parameterValues}
	 */
	public List<String> getParameterValues() {
		return parameterValues;
	}

	/**
	 * Sets {@link #parameterValues}.
	 * 
	 * @param parameterValues
	 *            New value for {@link #parameterValues}
	 */
	public void setParameterValues(List<String> parameterValues) {
		this.parameterValues = parameterValues;
	}

	/**
	 * @return Complete SQL query string with '?' from {@link #sql} replaced with the parameter
	 *         values in the {@link #parameterValues} list. Calculated only on first request.
	 */
	public String getSqlWithParameterValues() {
		if (null == parameterValues || parameterValues.isEmpty()) {
			return sql;
		} else {
			if (null == sqlWithParameterValues) {
				int index = 0;
				StringBuilder stringBuilder = new StringBuilder(sql.length());
				for (int i = 0; i < sql.length(); i++) {
					char c = sql.charAt(i);
					if ('?' == c) {
						String parameter = parameterValues.get(index);
						if ((null == parameter) || "".equals(parameter.trim())) {
							stringBuilder.append(c);
						} else {
							stringBuilder.append(parameter);
						}
						index = index + 1;
					} else {
						stringBuilder.append(c);
					}
				}
				sqlWithParameterValues = stringBuilder.toString();
			}
			return sqlWithParameterValues;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((parameterValues == null) ? 0 : parameterValues.hashCode());
		result = prime * result + (preparedStatement ? 1231 : 1237);
		result = prime * result + ((sql == null) ? 0 : sql.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
		if (parameterValues == null) {
			if (other.parameterValues != null) {
				return false;
			}
		} else if (!parameterValues.equals(other.parameterValues)) {
			return false;
		}
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
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, false);
		size += objectSizes.getPrimitiveTypesSize(3, 1, 0, 0, 0, 0);
		size += objectSizes.getSizeOf(sql);
		if (parameterValues != null) {
			size += objectSizes.getSizeOf(parameterValues);
			for (String str : parameterValues) {
				size += objectSizes.getSizeOf(str);
			}
		}
		if (null != sqlWithParameterValues) {
			size += objectSizes.getSizeOf(sqlWithParameterValues);
		}

		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

}
