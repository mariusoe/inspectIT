package info.novatec.inspectit.storage.serializer.impl;

import java.nio.ByteBuffer;

import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;

import com.esotericsoftware.kryo.Kryo;

/**
 * {@link CustomCompatibleFieldSerializer} for the {@link InvocationSequenceData} that in the
 * de-serialization process, connects the parents with the nested sequences.
 *
 * @author Ivan Senic
 *
 */
public class InvocationSequenceCustomCompatibleFieldSerializer extends CustomCompatibleFieldSerializer {

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
	public InvocationSequenceCustomCompatibleFieldSerializer(Kryo kryo, Class<?> type, ClassSchemaManager schemaManager) {
		super(kryo, type, schemaManager);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T readObjectData(ByteBuffer buffer, Class<T> type) {
		T object = super.readObjectData(buffer, type);
		if (object instanceof InvocationSequenceData) {
			connectChildren((InvocationSequenceData) object);
		}
		return object;
	}

	/**
	 * Sets the parent to all nested sequences of the invocation to the correct one.
	 *
	 * @param parent
	 *            Parent to start from.
	 */
	private void connectChildren(InvocationSequenceData parent) {
		if (null != parent.getNestedSequences()) {
			for (InvocationSequenceData child : parent.getNestedSequences()) {
				child.setParentSequence(parent);
				connectChildren(child);
			}
		}
	}

}
