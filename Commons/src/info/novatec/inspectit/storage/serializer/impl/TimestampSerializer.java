package info.novatec.inspectit.storage.serializer.impl;

import static com.esotericsoftware.minlog.Log.TRACE;
import static com.esotericsoftware.minlog.Log.trace;

import java.nio.ByteBuffer;
import java.sql.Timestamp;

import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.LongSerializer;

/**
 * Serializes instances of {@link java.sql.Timestamp}.
 * 
 * @author Ivan Senic
 */
public class TimestampSerializer extends Serializer {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T readObjectData(ByteBuffer buffer, Class<T> paramClass) {
		Timestamp timestamp = new Timestamp(LongSerializer.get(buffer, true));
		if (TRACE) {
			trace("kryo", "Read timestamp: " + timestamp);
		}
		return (T) timestamp;
	}

	/**
	 * {@inheritDoc}
	 */
	public void writeObjectData(ByteBuffer buffer, Object object) {
		LongSerializer.put(buffer, ((Timestamp) object).getTime(), true);
		if (TRACE) {
			trace("kryo", "Wrote timestamp: " + object);
		}
	}

}
