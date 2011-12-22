package info.novatec.inspectit.rcp.repository.service.cmr.proxy;

import info.novatec.inspectit.rcp.repository.service.cmr.CmrService;
import info.novatec.inspectit.rcp.repository.service.cmr.ICmrService;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.remoting.RemoteConnectFailureException;

/**
 * {@link IntroductionInterceptor} that delegates the call to the concrete service of a
 * {@link CmrService} class.
 * 
 * @author Ivan Senic
 * 
 */
public class ServiceInterfaceDelegateInterceptor implements MethodInterceptor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Object thisObject = methodInvocation.getThis();
		if (thisObject instanceof ICmrService) {
			ICmrService cmrService = (ICmrService) thisObject;
			if (!methodInvocation.getMethod().getDeclaringClass().equals(ICmrService.class)) {
				Object concreteService = cmrService.getService();
				Object returnVal = invokeUsingReflection(concreteService, methodInvocation.getMethod(), methodInvocation.getArguments());
				return returnVal;
			} else {
				return methodInvocation.proceed();
			}
		} else {
			throw new Exception("ServiceInterfaceIntroductionInterceptor not bounded to the ICmrService class.");
		}
	}

	/**
	 * Invokes the concrete object using reflection.
	 * 
	 * @param concreteService
	 *            Service to invoke.
	 * @param method
	 *            Method to invoke.
	 * @param arguments
	 *            Arguments.
	 * @throws Throwable
	 *             If any other exception occurs.
	 * @return Return value.
	 */
	private Object invokeUsingReflection(Object concreteService, Method method, Object[] arguments) throws Throwable {
		try {
			return method.invoke(concreteService, arguments);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof RemoteConnectFailureException) {
				throw ((RemoteConnectFailureException) cause);
			} else {
				throw e;
			}
		}
	}

}
