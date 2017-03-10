package rocks.inspectit.server.influx.util;

import org.influxdb.dto.Point;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marius Oehler
 *
 */
public final class InfluxUtils {

	private static Logger log = LoggerFactory.getLogger(InfluxUtils.class);

	private InfluxUtils() {
	}

	public static Point build(Point.Builder builder) {
		try {
			return builder.build();
		} catch (IllegalArgumentException e) {
			if (Log.isDebugEnabled()) {
				log.debug("InfluxDB point could not be created.", e);
			}
			return null;
		}
	}

}
