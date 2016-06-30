/**
 *
 */
package rocks.inspectit.server.alearting.adapter.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.amatkivskiy.gitter.sdk.credentials.GitterDeveloperCredentials;
import com.amatkivskiy.gitter.sdk.credentials.SimpleGitterCredentialsProvider;
import com.amatkivskiy.gitter.sdk.model.response.AccessTokenResponse;
import com.amatkivskiy.gitter.sdk.model.response.UserResponse;
import com.amatkivskiy.gitter.sdk.model.response.room.RoomResponse;
import com.amatkivskiy.gitter.sdk.sync.client.SyncGitterApiClient;
import com.amatkivskiy.gitter.sdk.sync.client.SyncGitterAuthenticationClient;

import rocks.inspectit.server.alearting.adapter.IAlertAdapter;
import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Alerting adapter for Gitter.
 *
 * @author Marius Oehler
 *
 */
public final class GitterAlertingAdapter implements IAlertAdapter {

	/**
	 * Logger for the class.
	 */
	@Log
	private Logger log;

	/**
	 * Specifies whether Gitter alerting is enabled.
	 */
	@Value("${anomaly.alerting.gitter.enabled}")
	private boolean enabled;

	/**
	 * The OAuth key.
	 */
	@Value("${anomaly.alerting.gitter.oauthKey}")
	private String oauthKey;

	/**
	 * The OAuth secret.
	 */
	@Value("${anomaly.alerting.gitter.oauthSecret}")
	private String oauthSecret;

	/**
	 * The OAuth redirect URL.
	 */
	@Value("${anomaly.alerting.gitter.oauthRedirectUrl}")
	private String oauthRedirectUrl;

	/**
	 * The OAuth code.
	 */
	@Value("${anomaly.alerting.gitter.oauthCode}")
	private String oauthCode;

	/**
	 * The room uri.
	 */
	@Value("${anomaly.alerting.gitter.roomUri}")
	private String roomUri;

	/**
	 * The access token to use the Gitter API.
	 */
	private String oauthAccessToken;

	/**
	 * The id of the current room.
	 */
	private String roomId;

	/**
	 * The Gitter client.
	 */
	private SyncGitterApiClient gitterClient;

	/**
	 * The date format that is used for the message's prefix.
	 */
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * The current user.
	 */
	private UserResponse currentUser;

	/**
	 * Hidden constructor.
	 */
	private GitterAlertingAdapter() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean connect() {
		oauthAccessToken = getAccessToken();

		if (oauthAccessToken == null) {
			return false;
		}

		if (log.isInfoEnabled()) {
			log.info("||-Gitter client has been succesfully authenticated");
		}

		gitterClient = new SyncGitterApiClient.Builder().withAccountToken(oauthAccessToken).build();

		currentUser = gitterClient.getCurrentUser();

		if (log.isInfoEnabled()) {
			log.info("||-Connected to Gitter with user {} ({})", currentUser.displayName, currentUser.username);
		}

		joinRoom(roomUri);

		return true;
	}

	/**
	 * Exchange the OAuth code for an access token.
	 *
	 * @return The access token.
	 */
	private String getAccessToken() {
		GitterDeveloperCredentials.init(new SimpleGitterCredentialsProvider(oauthKey, oauthSecret, oauthRedirectUrl));
		SyncGitterAuthenticationClient authenticationClient = new SyncGitterAuthenticationClient.Builder().build();

		AccessTokenResponse accessTokenResponse;
		try {
			accessTokenResponse = authenticationClient.getAccessToken(oauthCode);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("||-Could not connected to Gitter. ", e);
			}
			return null;
		}

		return accessTokenResponse.accessToken;
	}

	/**
	 * Joins the specified room.
	 *
	 * @param roomUri
	 *            URI of the room to join.
	 * @return Returns true if the room could been joined.
	 */
	private boolean joinRoom(String roomUri) {
		List<RoomResponse> currentRooms = gitterClient.getCurrentUserRooms();

		roomId = null;

		for (RoomResponse room : currentRooms) {
			if (room.uri != null && room.uri.equals(roomUri)) {
				roomId = room.id;
				break;
			}
		}

		if (roomId == null) {
			try {
				RoomResponse room = gitterClient.joinRoom(roomUri);
				roomId = room.id;
			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error("||-Could not joined the specified Gitter room. ", e);
				}
				return false;
			}
		}

		sendMessage("Hello, I'm inspectIT.");

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendMessage(String message) {
		if (enabled) {
			if (log.isDebugEnabled()) {
				log.debug("Send Gitter alert message");
			}

			gitterClient.sendMessage(roomId, "[" + dateFormat.format(new Date()) + " - Anomaly] " + message);
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Gitter alerting is disabled");
			}
		}
	}

	/**
	 * Is called when the roomUri is changed.
	 */
	@PropertyUpdate(properties = { "anomaly.alerting.gitter.roomUri" })
	private void onRoomChange() {
		gitterClient.leaveRoom(roomId, currentUser.id);

		joinRoom(roomUri);
	}

	/**
	 * Is called when the roomUri is changed.
	 */
	@PropertyUpdate(properties = { "anomaly.alerting.gitter.enabled" })
	private void onEnabled() {
		if (enabled) {
			if (gitterClient == null) {
				connect();
			}
		} else {
			if (gitterClient != null) {
				gitterClient = null;
			}
		}
	}
}
