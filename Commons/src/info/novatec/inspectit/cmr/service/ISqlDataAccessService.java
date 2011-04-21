package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.SqlStatementData;

import java.util.Date;
import java.util.List;

/**
 * @author Patrice Bouillet
 * 
 */
public interface ISqlDataAccessService {

	/**
	 * Returns a list of the SQL statements for a given template. In this template, only the
	 * platform id is extracted.
	 * 
	 * @param sqlStatementData
	 *            The template containing the platform id.
	 * @return The list of the SQL statements.
	 */
	List getAggregatedSqlStatements(SqlStatementData sqlStatementData);

	/**
	 * Returns a list of the SQL statements for a given template in a time frame. In this template,
	 * only the platform id is extracted.
	 * 
	 * @param sqlStatementData
	 *            The template containing the platform id.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return The list of the SQL statements.
	 */
	List getAggregatedSqlStatements(SqlStatementData sqlStatementData, Date fromDate, Date toDate);
}
