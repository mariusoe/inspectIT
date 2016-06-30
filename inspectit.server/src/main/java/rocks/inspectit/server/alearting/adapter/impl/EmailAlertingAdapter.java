/**
 *
 */
package rocks.inspectit.server.alearting.adapter.impl;

import javax.annotation.PostConstruct;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.alearting.adapter.IAlertAdapter;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Marius Oehler
 *
 */
public class EmailAlertingAdapter implements IAlertAdapter {

	/**
	 * Logger for the class.
	 */
	@Log
	private Logger log;

	/**
	 * Whether the email alerting is enabled.
	 */
	@Value("${anomaly.alerting.email.enabled}")
	private boolean enabled;

	/**
	 * Whether a test mail should be send at startup.
	 */
	@Value("${anomaly.alerting.email.testMail}")
	private boolean sendingTestMail;

	/**
	 * SMTP host.
	 */
	@Value("${anomaly.alerting.email.smtpHost}")
	private String smtpHost;

	/**
	 * SMTP port.
	 */
	@Value("${anomaly.alerting.email.smtpPort}")
	private int smtpPort;

	/**
	 * SMTP username.
	 */
	@Value("${anomaly.alerting.email.username}")
	private String username;

	/**
	 * The sender email address.
	 */
	@Value("${anomaly.alerting.email.emailFrom}")
	private String emailFrom;

	/**
	 * SMTP password.
	 */
	@Value("${anomaly.alerting.email.password}")
	private String password;

	/**
	 * If TSL should be used.
	 */
	@Value("${anomaly.alerting.email.useTSL}")
	private boolean useTSL;

	/**
	 * The receiver of the alerts.
	 */
	@Value("${anomaly.alerting.email.emailTo}")
	private String emailTo;

	/**
	 * {@inheritDoc}
	 */
	@PostConstruct
	public void afterPropertiesSet() {
		if (sendingTestMail) {
			sendTestMail();
		} else {
			if (enabled) {
				if (log.isInfoEnabled()) {
					log.info("||-Email alerting is enabled.");
				}
			} else {
				if (log.isInfoEnabled()) {
					log.info("||-Email alerting is disabled.");
				}
			}
		}
	}

	/**
	 * Sends a test email.
	 */
	private void sendTestMail() {
		if (log.isInfoEnabled()) {
			log.info("||-Sending a test email using the EmailAlertingAdapter.");
		}
		sendMessage("Hello, this is a test email from inspectIT.", true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendMessage(String message) {
		sendMessage(message, false);
	}

	/**
	 * Send the given message.
	 *
	 * @param message
	 *            the message to send
	 * @param forceSending
	 *            sends the message even the adapter is disabled
	 */
	private void sendMessage(String message, boolean forceSending) {
		if (enabled || forceSending) {
			Email email = new SimpleEmail();
			email.setHostName(smtpHost);
			email.setSmtpPort(smtpPort);
			email.setAuthenticator(new DefaultAuthenticator(username, password));
			email.setStartTLSEnabled(useTSL);

			try {
				email.setFrom(emailFrom);
				email.addTo(emailTo);

				email.setSubject("inspectIT Anomaly Alert");
				email.setMsg(message);

				email.send();
			} catch (EmailException e) {
				if (log.isErrorEnabled()) {
					log.error("Couldn't send the alrting email. ", e);
				}
			}
		}
	}
}
