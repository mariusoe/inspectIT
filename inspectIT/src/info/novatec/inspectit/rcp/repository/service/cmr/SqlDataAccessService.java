package info.novatec.inspectit.rcp.repository.service.cmr;

import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.rcp.InspectIT;

import java.util.Collections;
import java.util.List;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

/**
 * @author Patrice Bouillet
 * 
 */
public class SqlDataAccessService implements ISqlDataAccessService {

	/**
	 * The sql data access service name.
	 */
	private static final String SQL_DATA_ACCESS_SERVICE = "SqlDataAccessService";

	/**
	 * The proxy factory bean by Spring which initializes the sql data access
	 * service.
	 */
	private final HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean;

	/**
	 * The sql data access service exposed by the CMR and initialized by Spring.
	 */
	private final ISqlDataAccessService sqlDataAccessService;

	/**
	 * Default constructor needs the ip and the port of the service.
	 * 
	 * @param ip
	 *            The ip.
	 * @param port
	 *            The port.
	 */
	public SqlDataAccessService(String ip, int port) {
		httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
		httpInvokerProxyFactoryBean.setServiceInterface(ISqlDataAccessService.class);
		httpInvokerProxyFactoryBean.setServiceUrl("http://" + ip + ":" + port + "/remoting/" + SQL_DATA_ACCESS_SERVICE);
		httpInvokerProxyFactoryBean.afterPropertiesSet();

		sqlDataAccessService = (ISqlDataAccessService) httpInvokerProxyFactoryBean.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData template) {
		try {
			return sqlDataAccessService.getAggregatedSqlStatements(template);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("There was an error retrieving the aggregated sql statements from the CMR!", e, -1);
			return Collections.EMPTY_LIST;
		}
	}

}
