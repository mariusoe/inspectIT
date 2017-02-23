package rocks.inspectit.server.anomaly.notification;

/**
 * @author Marius Oehler
 *
 */
public interface IAnomalyNotification {

	void onStart();

	void onUpgrade();

	void onDowngrade();

	void onEnd();

}
