package info.novatec.inspectit.kryonet;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Implementation of the {@link IExtendedSerialization} with some additional methods we need for
 * (de-)serializing the object during the communication. The idea is not to (de-)serialize from/to
 * buffer, but to use the streams which would give us opportunity to transfer objects of unlimited
 * size.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("all")
public class ExtendedSerializationImpl implements IExtendedSerialization {

	/**
	 * Kryo to use for the serialization.
	 */
	private final Kryo kryo;

	/**
	 * @param kryo
	 *            Kryo to use for the serialization.
	 */
	public ExtendedSerializationImpl(Kryo kryo) {
		this.kryo = kryo;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented. It's not used because we won't write to the buffer, but to the output
	 * stream. But it's defined in the Serialization interface, so we must implement it.
	 */
	public void write(Connection connection, ByteBuffer buffer, Object object) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not implemented. It's not used because we won't read from the buffer, but from the input
	 * stream. But it's defined in the Serialization interface, so we must implement it.
	 */
	public Object read(Connection connection, ByteBuffer buffer) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void writeLength(ByteBuffer buffer, int length) {
		buffer.putInt(length);
	}

	/**
	 * {@inheritDoc}
	 */
	public int readLength(ByteBuffer buffer) {
		return buffer.getInt();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns 4 as the original Kryo serialization. This should represent number of bytes needed
	 * for storing the length of the bytes to send.
	 */
	public int getLengthLength() {
		return 4;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public synchronized void write(Connection connection, OutputStream outputStream, Object object) {
		Output output = new Output(outputStream);
		kryo.getContext().put("connection", connection);
		kryo.writeClassAndObject(output, object);
		output.flush();
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Object read(Connection connection, InputStream inputStream) {
		Input input = new Input(inputStream);
		kryo.getContext().put("connection", connection);
		return kryo.readClassAndObject(input);
	}

}
