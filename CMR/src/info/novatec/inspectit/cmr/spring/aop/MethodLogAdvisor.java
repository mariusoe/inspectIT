package info.novatec.inspectit.cmr.spring.aop;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.stereotype.Component;

/**
 * This logging advisor will be automatically loaded as long as there is an
 * {@link DefaultAdvisorAutoProxyCreator} being created as a spring bean and this is being defined
 * as a spring bean, too.
 * 
 * @author Patrice Bouillet
 * 
 */
@Component
public class MethodLogAdvisor extends AbstractPointcutAdvisor {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 4313128580119972746L;

	/**
	 * The used pointcut. It will intercept all method calls which are annotated with the
	 * {@link MethodLog} annotation.
	 */
	private static final Pointcut POINTCUT = new AnnotationMatchingPointcut(null, MethodLog.class);

	/**
	 * The interceptor which will print some useful logging output if the log level permits.
	 */
	private static final Advice ADVICE = new MethodLogInterceptor();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pointcut getPointcut() {
		return POINTCUT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Advice getAdvice() {
		return ADVICE;
	}

}
