package info.novatec.novaspy.cmr.service;

import info.novatec.novaspy.communication.data.SqlStatementData;

import java.util.List;

/**
 * @author Patrice Bouillet
 * 
 */
public interface ISqlDataAccessService {

	/**
	 * Returns a list of the SQL statements for a given template. In this
	 * template, only the platform id is extracted.
	 * 
	 * @param sqlStatementData
	 *            The template containing the platform id.
	 * @return The list of the SQL statements.
	 */
	List getAggregatedSqlStatements(SqlStatementData sqlStatementData);

}
