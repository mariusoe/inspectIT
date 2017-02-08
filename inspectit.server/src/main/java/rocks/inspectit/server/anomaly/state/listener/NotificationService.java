package rocks.inspectit.server.anomaly.state.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.anomaly.DefinitionAwareFactory;
import rocks.inspectit.server.anomaly.notifier.AbstractNotifier;
import rocks.inspectit.server.anomaly.processing.ProcessingUnitGroupContext;
import rocks.inspectit.server.anomaly.state.IAnomalyStateListener;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.anomaly.definition.notification.NotificationDefinition;

/**
 * @author Marius Oehler
 *
 */
@Component
public class NotificationService implements IAnomalyStateListener {

	@Log
	private Logger log;

	@Autowired
	private DefinitionAwareFactory factory;

	private Map<String, List<AbstractNotifier<?>>> notifiers = new HashMap<>();

	private List<AbstractNotifier<?>> getNotifiers(ProcessingUnitGroupContext groupContext) {
		List<AbstractNotifier<?>> notifierList = notifiers.get(groupContext.getGroupId());

		if (notifierList == null) {
			notifierList = createNotifiers(groupContext);
		}

		return notifierList;
	}

	private List<AbstractNotifier<?>> createNotifiers(ProcessingUnitGroupContext groupContext) {
		List<AbstractNotifier<?>> createdNotifiers = new ArrayList<>();

		for (NotificationDefinition notificationDefinition : groupContext.getGroupConfiguration().getNotificationDefinitions()) {
			AbstractNotifier<?> notifier = factory.createNotifier(notificationDefinition);
			createdNotifiers.add(notifier);
		}

		notifiers.put(groupContext.getGroupId(), createdNotifiers);

		return createdNotifiers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart(ProcessingUnitGroupContext groupContext) {
		for (AbstractNotifier<?> notifier : getNotifiers(groupContext)) {
			notifier.onStart(groupContext.getAnomalyStateContext().getCurrentAnomaly());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onUpgrade(ProcessingUnitGroupContext groupContext) {
		for (AbstractNotifier<?> notifier : getNotifiers(groupContext)) {
			notifier.onUpgrade(groupContext.getAnomalyStateContext().getCurrentAnomaly());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDowngrade(ProcessingUnitGroupContext groupContext) {
		for (AbstractNotifier<?> notifier : getNotifiers(groupContext)) {
			notifier.onDowngrade(groupContext.getAnomalyStateContext().getCurrentAnomaly());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnd(ProcessingUnitGroupContext groupContext) {
		for (AbstractNotifier<?> notifier : getNotifiers(groupContext)) {
			notifier.onEnd(groupContext.getAnomalyStateContext().getCurrentAnomaly());
		}
	}
}
