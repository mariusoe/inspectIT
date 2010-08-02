package info.novatec.inspectit.cmr.util.aop;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

/**
 * This logging advisor will be automatically loaded as long as there is an
 * {@link DefaultAdvisorAutoProxyCreator} being created as a spring bean and this is being defined
 * as a spring bean, too.
 * 
 * @author Patrice Bouillet
 * 
 */
public class LoggingAdvisor extends AbstractPointcutAdvisor {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 4313128580119972746L;

	/**
	 * The used pointcut. It will intercept all method calls which are annotated with the
	 * {@link Log} annotation.
	 */
	private static final Pointcut pointcut = new AnnotationMatchingPointcut(null, Log.class);

	/**
	 * The interceptor which will print some useful logging output if the log level permits.
	 */
	private static final Advice advice = new LoggingInterceptor();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pointcut getPointcut() {
		return pointcut;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Advice getAdvice() {
		return advice;
	}

}
