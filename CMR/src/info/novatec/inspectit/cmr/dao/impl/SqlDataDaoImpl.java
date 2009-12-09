package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.SqlDataDao;
import info.novatec.inspectit.communication.data.SqlStatementData;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Patrice Bouillet
 * 
 */
public class SqlDataDaoImpl extends HibernateDaoSupport implements SqlDataDao {

	/**
	 * The query string used to aggregate the sql information in the db
	 * directly.
	 */
	private static final String AGGREGATED_SQL_STATEMENTS = "select distinct sqlData.sql as sql, sqlData.platformIdent as platformIdent, sqlData.sensorTypeIdent as sensorTypeIdent, sqlData.methodIdent as methodIdent, sqlData.preparedStatement as preparedStatement, sum(sqlData.count) as count, min(sqlData.min) as min, max(sqlData.max) as max, sum(sqlData.duration) as duration, (sum(sqlData.duration) / sum(sqlData.count)) as average from SqlStatementData sqlData where sqlData.platformIdent=? group by sqlData.sql order by sqlData.sql";

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<SqlStatementData> getAggregatedSqlStatements(final SqlStatementData sqlStatementData) {
		List<SqlStatementData> result = (List<SqlStatementData>) getHibernateTemplate().executeWithNativeSession(new HibernateCallback() {
			public Object doInHibernate(Session session) throws SQLException {
				Query query = session.createQuery(AGGREGATED_SQL_STATEMENTS);
				query.setLong(0, sqlStatementData.getPlatformIdent());
				return query.setResultTransformer(Transformers.aliasToBean(SqlStatementData.class)).list();
			}
		});

		return result;
	}

}
