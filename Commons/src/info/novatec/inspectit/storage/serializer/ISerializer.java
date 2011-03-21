package info.novatec.inspectit.storage.serializer;


import java.nio.ByteBuffer;

/**
 * Generic interface for serializer to be used in inspectIT. The interface defines only two methods,
 * one for serialization and one from the serialization. The serializer is tightly coupled with the
 * {@link ByteBuffer}.
 * 
 * @author Ivan Senic
 * 
 */
public interface ISerializer {

	/**
	 * Serialize the object into bytes and puts the bytes in the supplied {@link ByteBuffer}. Note
	 * that the buffer has to have space for putting all the bytes that come from the serialization.
	 * Furthermore, all operations for preparing the buffer have to be performed before calling this
	 * method.
	 * 
	 * @param object
	 *            Object to serialize.
	 * @param byteBuffer
	 *            {@link ByteBuffer} to hold the serialized bytes.
	 * @throws SerializationException
	 *             Serialization exception is thrown when serialization could not be performed.
	 */
	void serialize(Object object, ByteBuffer byteBuffer) throws SerializationException;

	/**
	 * De-serialize the bytes provided by the {@link ByteBuffer}. It is responsibility of the caller
	 * to set up the buffer correctly, namely take care about the buffers current position. The way
	 * bytes are read, is defined in the implementing classes.
	 * 
	 * @param byteBuffer
	 *            Byte buffer that provides the bytes.
	 * @return Returns the de-serialized object.
	 * @throws SerializationException
	 *             If de-serialization fails.
	 */
	Object deserialize(ByteBuffer byteBuffer) throws SerializationException;

}
