package rocks.inspectit.server.anomaly.context.matcher;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * @author Marius Oehler
 *
 */
@Component
@Scope("prototype")
public interface IAnomalyContextMatcher {

	boolean matches(DefaultData defaultData);

	IAnomalyContextMatcher createCopy();
}
