package info.novatec.inspectit.rcp.repository.service.cmr.proxy;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.service.cmr.ICmrService;

import java.net.ConnectException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.remoting.RemoteConnectFailureException;

import com.google.common.base.Defaults;

/**
 * Our service method interceptor that will catch {@link InspectITCommunicationException} and if the
 * problem was {@link RemoteConnectFailureException}, it will update the online status of the CMR.
 * This interceptor will also show a error message.
 * 
 * @author Ivan Senic
 * 
 */
public class ServiceMethodInterceptor implements MethodInterceptor {

	/**
	 * {@inheritDoc}
	 */
	public Object invoke(MethodInvocation paramMethodInvocation) throws Throwable {
		try {
			Object rval = paramMethodInvocation.proceed();
			CmrRepositoryDefinition cmrRepositoryDefinition = getRepositoryDefinition(paramMethodInvocation);
			if (null != cmrRepositoryDefinition && isServiceMethod(paramMethodInvocation)) {
				if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
					InspectIT.getDefault().getCmrRepositoryManager().forceCmrRepositoryOnlineStatusUpdate(cmrRepositoryDefinition);
				}
			} else if (null == cmrRepositoryDefinition) {
				throw new RuntimeException("Service proxy not bounded to the CMR repository definition");
			}
			return rval;
		} catch (RemoteConnectFailureException e) {
			handleConnectionFailure(paramMethodInvocation, e);
			return getDefaultReturnValue(paramMethodInvocation);
		} catch (ConnectException e) {
			handleConnectionFailure(paramMethodInvocation, e);
			return getDefaultReturnValue(paramMethodInvocation);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog("Exception thrown trying to invoke a service method.", e, -1);
			return getDefaultReturnValue(paramMethodInvocation);
		}
	}

	/**
	 * Handles the connection failure.
	 * 
	 * @param paramMethodInvocation
	 *            {@link MethodInvocation}.
	 * @param e
	 *            {@link Throwable}.
	 */
	private void handleConnectionFailure(MethodInvocation paramMethodInvocation, Throwable e) {
		CmrRepositoryDefinition cmrRepositoryDefinition = getRepositoryDefinition(paramMethodInvocation);
		if (null != cmrRepositoryDefinition) {
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
				InspectIT.getDefault().getCmrRepositoryManager().forceCmrRepositoryOnlineStatusUpdate(cmrRepositoryDefinition);
			}
			InspectIT.getDefault().createErrorDialog("The server: '" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + "' is currenlty unavailable.", e, -1);
		} else {
			throw new RuntimeException("Service proxy not bounded to the CMR repository definition");
		}
	}

	/**
	 * Is service method.
	 * 
	 * @param methodInvocation
	 *            Method invocation.
	 * @return Return if it is service method.
	 */
	private boolean isServiceMethod(MethodInvocation methodInvocation) {
		return !methodInvocation.getMethod().getDeclaringClass().equals(ICmrService.class);
	}

	/**
	 * Tries to get the {@link CmrRepositoryDefinition} from the proxied {@link ICmrService} object.
	 * 
	 * @param paramMethodInvocation
	 *            {@link MethodInvocation}.
	 * @return CMR invoked or null.
	 */
	private CmrRepositoryDefinition getRepositoryDefinition(MethodInvocation paramMethodInvocation) {
		if (paramMethodInvocation instanceof ReflectiveMethodInvocation) {
			ReflectiveMethodInvocation reflectiveMethodInvocation = (ReflectiveMethodInvocation) paramMethodInvocation;
			Object service = reflectiveMethodInvocation.getThis();
			if (service instanceof ICmrService) {
				ICmrService cmrService = (ICmrService) service;
				CmrRepositoryDefinition cmrRepositoryDefinition = cmrService.getCmrRepositoryDefinition();
				return cmrRepositoryDefinition;
			}
		}
		return null;
	}

	/**
	 * Checks if the return type of the {@link java.lang.reflect.Method} invoked by
	 * {@link MethodInvocation} is one of tree major collection types (List, Map, Set) and if it is
	 * returns the empty collection of correct type. Otherwise it returns null.
	 * 
	 * @param paramMethodInvocation
	 *            {@link MethodInvocation}
	 * @return If the method invoked by {@link MethodInvocation} is one of tree major collection
	 *         types (List, Map, Set) method returns the empty collection of correct type. Otherwise
	 *         it returns null.
	 */
	private Object getDefaultReturnValue(MethodInvocation paramMethodInvocation) {
		Class<?> returnType = paramMethodInvocation.getMethod().getReturnType();
		if (returnType.isAssignableFrom(List.class)) {
			return Collections.emptyList();
		} else if (returnType.isAssignableFrom(Map.class)) {
			return Collections.emptyMap();
		} else if (returnType.isAssignableFrom(Set.class)) {
			return Collections.emptySet();
		} else if (returnType.isPrimitive()) {
			try {
				return Defaults.defaultValue(returnType);
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}

	}

}
