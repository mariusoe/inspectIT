package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.SqlStatementData;

import java.util.List;

/**
 * @author Patrice Bouillet
 * 
 */
public interface SqlDataDao {

	/**
	 * Returns a list of the SQL statements for a given template. In this
	 * template, only the platform id is extracted.
	 * 
	 * @param sqlStatementData
	 *            The template containing the platform id.
	 * @return The list of the SQL statements.
	 */
	List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData sqlStatementData);

}
