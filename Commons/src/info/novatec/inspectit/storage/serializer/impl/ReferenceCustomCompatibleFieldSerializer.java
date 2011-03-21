package info.novatec.inspectit.storage.serializer.impl;

import static com.esotericsoftware.minlog.Log.TRACE;
import static com.esotericsoftware.minlog.Log.trace;
import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;

import java.nio.ByteBuffer;
import java.util.IdentityHashMap;

import com.esotericsoftware.kryo.Context;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializationException;
import com.esotericsoftware.kryo.serialize.FieldSerializer;
import com.esotericsoftware.kryo.serialize.IntSerializer;
import com.esotericsoftware.kryo.util.IntHashMap;

/**
 * Serializes objects using direct field assignment, handling object references and cyclic graphs.
 * Each object serialized requires 1 byte more than FieldSerializer. Each appearance of an object in
 * the graph after the first is stored as an integer ordinal.
 * <p>
 * Note that serializing references can be convenient, but can sometimes be redundant information.
 * If this is the case and serialized size is a priority, references should not be serialized. Code
 * can sometimes be hand written to reconstruct the references after deserialization.
 * <p>
 * <i>Most part of the source code is copied from {@link FieldSerializer}.</i>
 *
 * @see FieldSerializer
 * @author Nathan Sweet <misc@n4te.com>
 * @author Ivan Senic <ivan.senic@novatec-gmbh.de>
 */
public class ReferenceCustomCompatibleFieldSerializer extends CustomCompatibleFieldSerializer {

	/**
	 * Default constructor.
	 *
	 * @param kryo
	 *            Kryo object.
	 * @param type
	 *            Type of class.
	 * @param schemaManager
	 *            Schema manager holding the schema for the given type.
	 *
	 * @see CustomCompatibleFieldSerializer#CustomCompatibleFieldSerializer(Kryo, Class,
	 *      ClassSchemaManager)
	 */
	public ReferenceCustomCompatibleFieldSerializer(Kryo kryo, Class<?> type, ClassSchemaManager schemaManager) {
		super(kryo, type, schemaManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeObjectData(ByteBuffer buffer, Object object) {
		Context context = Kryo.getContext();
		References references = (References) context.getTemp("references");
		if (references == null) {
			// Use non-temporary storage to avoid repeated allocation.
			references = (References) context.get("references");
			if (references == null) {
				references = new References();
				context.put("references", references);
			} else {
				references.reset();
			}
			context.putTemp("references", references);
		}
		Integer reference = references.objectToReference.get(object);
		if (reference != null) {
			IntSerializer.put(buffer, reference, true);
			if (TRACE) {
				trace("kryo", "Wrote object reference " + reference + ": " + object);
			}
			return;
		}

		buffer.put((byte) 0);
		references.referenceCount++;
		references.objectToReference.put(object, references.referenceCount);

		super.writeObjectData(buffer, object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T readObjectData(ByteBuffer buffer, Class<T> type) {
		Context context = Kryo.getContext();
		References references = (References) context.getTemp("references");
		if (references == null) {
			// Use non-temporary storage to avoid repeated allocation.
			references = (References) context.get("references");
			if (references == null) {
				references = new References();
				context.put("references", references);
			} else {
				references.reset();
			}
			context.putTemp("references", references);
		}

		int reference = IntSerializer.get(buffer, true);
		if (reference != 0) {
			T object = (T) references.referenceToObject.get(reference);
			if (object == null) {
				throw new SerializationException("Invalid object reference: " + reference);
			}
			if (TRACE) {
				trace("kryo", "Read object reference " + reference + ": " + object);
			}
			return object;
		}

		T object = newInstance(super.getKryo(), type);

		references.referenceCount++;
		references.referenceToObject.put(references.referenceCount, object);

		return super.readObjectData(object, buffer, type);
	}

	/**
	 * References class for the cyclic dependencies tracking.
	 */
	static class References {

		/**
		 * Object to reference map.
		 */
		private IdentityHashMap<Object, Integer> objectToReference = new IdentityHashMap<Object, Integer>();

		/**
		 * Reference to object in the {@link IntHashMap}.
		 */
		private IntHashMap<Object> referenceToObject = new IntHashMap<Object>();

		/**
		 * Reference count.
		 */
		private int referenceCount = 1;

		/**
		 * Resets the reference count.
		 */
		public void reset() {
			objectToReference.clear();
			referenceToObject.clear();
			referenceCount = 1;
		}
	}
}
