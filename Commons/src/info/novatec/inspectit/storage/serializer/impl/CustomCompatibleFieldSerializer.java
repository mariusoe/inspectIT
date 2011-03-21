package info.novatec.inspectit.storage.serializer.impl;

import static com.esotericsoftware.minlog.Log.TRACE;
import static com.esotericsoftware.minlog.Log.trace;
import info.novatec.inspectit.storage.serializer.schema.ClassSchema;
import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.esotericsoftware.kryo.Context;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.RegisteredClass;
import com.esotericsoftware.kryo.NotNull;
import com.esotericsoftware.kryo.Optional;
import com.esotericsoftware.kryo.SerializationException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.serialize.FieldSerializer;
import com.esotericsoftware.kryo.serialize.IntSerializer;
import com.esotericsoftware.kryo.util.Util;
import com.esotericsoftware.reflectasm.FieldAccess;

/**
 * Serializes objects using direct field assignment, with limited support for forward and backward
 * compatibility. Fields can be added or removed without invalidating previously serialized bytes.
 * Note that changing the type of a field is not supported.
 * <p>
 * There is additional overhead compared to {@link FieldSerializer}. A header is output the first
 * time an object of a given type is serialized. The header consists of an int for the number of
 * fields, then a String for each field name. Also, to support skipping the bytes for a field that
 * no longer exists, for each field value an int is written that is the length of the value in
 * bytes.
 * <p>
 * <i>Most part of the source code is copied from {@link CompatibleFieldSerializer}.</i>
 * 
 * @author Nathan Sweet <misc@n4te.com>
 * @author Ivan Senic <ivan.senic@novatec-gmbh.de>
 */
public class CustomCompatibleFieldSerializer extends Serializer {

	/**
	 * Reference to {@link Kryo}.
	 */
	private final Kryo kryo;

	/**
	 * Type of class to serialize.
	 */
	private final Class<?> type;

	/**
	 * Array of class cached fields.
	 */
	private CachedField[] fields;

	/**
	 * Schema gotten from Schema manager.
	 */
	private ClassSchema schema;

	/**
	 * Access.
	 */
	private Object access;

	/**
	 * If fields can be null.
	 */
	private boolean fieldsCanBeNull = true, setFieldsAsAccessible = true;

	/**
	 * Default constructor.
	 * 
	 * @param kryo
	 *            Kryo reference.
	 * @param type
	 *            Type of class.
	 * @param schemaManager
	 *            Schema manager.
	 */
	public CustomCompatibleFieldSerializer(Kryo kryo, Class<?> type, ClassSchemaManager schemaManager) {
		this.kryo = kryo;
		this.type = type;
		schema = schemaManager.getSchema(type.getName());
		if (schema == null) {
			throw new IllegalArgumentException("Schema for the class '" + type.getName() + "' does not exists in provided schema manager.");
		}
		rebuildCachedFields();
	}

	/**
	 * Rebuilds cache fields.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void rebuildCachedFields() {
		if (type.isInterface()) {
			fields = new CachedField[0]; // No fields to serialize.
			return;
		}

		// Collect all fields.
		ArrayList<Field> allFields = new ArrayList<Field>();
		Class<?> nextClass = type;
		while (nextClass != Object.class) {
			Collections.addAll(allFields, nextClass.getDeclaredFields());
			nextClass = nextClass.getSuperclass();
		}

		ArrayList<CachedField> publicFields = new ArrayList<CachedField>();
		PriorityQueue<CachedField> cachedFields = new PriorityQueue(Math.max(1, allFields.size()), new Comparator<CachedField>() {
			public int compare(CachedField o1, CachedField o2) {
				// Fields are sorted by alpha so the order of the data is known.
				return o1.field.getName().compareTo(o2.field.getName());
			}
		});
		Context context = Kryo.getContext();
		for (int i = 0, n = allFields.size(); i < n; i++) {
			Field field = allFields.get(i);

			int modifiers = field.getModifiers();
			if (Modifier.isTransient(modifiers)) {
				continue;
			}
			if (Modifier.isStatic(modifiers)) {
				continue;
			}
			if (field.isSynthetic()) {
				continue;
			}

			// Added by ISE
			// if it is not in schema we skip it
			if (null == schema.getFieldMarker(field.getName())) {
				continue;
			}

			if (!field.isAccessible()) {
				if (!setFieldsAsAccessible) {
					continue;
				}
				try {
					field.setAccessible(true);
				} catch (AccessControlException ex) {
					continue;
				}
			}

			Optional optional = field.getAnnotation(Optional.class);
			if (optional != null && context.get(optional.value()) == null) {
				continue;
			}

			Class<?> fieldClass = field.getType();

			CachedField cachedField = new CachedField();
			cachedField.field = field;

			// Added by ISE
			// set the marker for the field
			cachedField.setFieldMarker(schema.getFieldMarker(field.getName()));

			if (fieldsCanBeNull) {
				cachedField.canBeNull = !fieldClass.isPrimitive() && !field.isAnnotationPresent(NotNull.class);
			} else {
				cachedField.canBeNull = false;
			}

			// Always use the same serializer for this field if the field's class is final.
			if (isFinal(fieldClass)) {
				cachedField.fieldClass = fieldClass;
			}

			cachedFields.add(cachedField);
			if (Modifier.isPublic(modifiers) && Modifier.isPublic(fieldClass.getModifiers())) {
				publicFields.add(cachedField);
			}
		}

		if (!Util.isAndroid && Modifier.isPublic(type.getModifiers()) && !publicFields.isEmpty()) {
			// Use ReflectASM for any public fields.
			try {
				access = FieldAccess.get(type);
				for (int i = 0, n = publicFields.size(); i < n; i++) {
					CachedField cachedField = publicFields.get(i);
					cachedField.accessIndex = ((FieldAccess) access).getIndex(cachedField.field.getName());
				}
			} catch (RuntimeException ignored) {
			}
		}

		int fieldCount = cachedFields.size();
		fields = new CachedField[fieldCount];
		for (int i = 0; i < fieldCount; i++) {
			fields[i] = cachedFields.poll();
		}
	}

	/**
	 * Sets the default value for {@link CachedField#setCanBeNull(boolean)}.
	 * 
	 * @param fieldsCanBeNull
	 *            False if none of the fields are null. Saves 1 byte per field. True if it is not
	 *            known (default).
	 */
	public void setFieldsCanBeNull(boolean fieldsCanBeNull) {
		this.fieldsCanBeNull = fieldsCanBeNull;
		rebuildCachedFields();
	}

	/**
	 * Controls which fields are accessed.
	 * 
	 * @param setFieldsAsAccessible
	 *            If true, all non-transient fields (inlcuding private fields) will be serialized
	 *            and {@link Field#setAccessible(boolean) set as accessible} (default). If false,
	 *            only fields in the public API will be serialized.
	 */
	public void setFieldsAsAccessible(boolean setFieldsAsAccessible) {
		this.setFieldsAsAccessible = setFieldsAsAccessible;
		rebuildCachedFields();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeObjectData(ByteBuffer buffer, Object object) {
		Context context = Kryo.getContext();
		if (context.getTemp(this, "schemaWritten") == null) {
			context.putTemp(this, "schemaWritten", Boolean.TRUE);
			if (TRACE) {
				trace("kryo", "Writing " + fields.length + " field names.");
			}
			IntSerializer.put(buffer, fields.length, true);
			for (int i = 0, n = fields.length; i < n; i++) {
				IntSerializer.put(buffer, fields[i].getFieldMarker(), true);
			}
		}

		for (int i = 0, n = fields.length; i < n; i++) {
			CachedField cachedField = fields[i];
			try {
				if (TRACE) {
					trace("kryo", "Writing field: " + cachedField + " (" + object.getClass().getName() + ")");
				}

				Object value = cachedField.get(object);
				if (value == null) {
					kryo.writeClass(buffer, null);
					continue;
				}

				int start = buffer.position();
				try {
					buffer.position(start + 1);
				} catch (IllegalArgumentException ex) {
					new BufferOverflowException();
				}

				Serializer serializer = cachedField.serializer;
				if (cachedField.fieldClass == null) {
					RegisteredClass registeredClass = kryo.writeClass(buffer, value.getClass());
					if (serializer == null) {
						serializer = registeredClass.getSerializer();
					}
					serializer.writeObjectData(buffer, value);
				} else {
					if (serializer == null) {
						serializer = kryo.getRegisteredClass(cachedField.fieldClass).getSerializer();
						cachedField.serializer = serializer;
					}
					if (!cachedField.canBeNull) {
						serializer.writeObjectData(buffer, value);
					} else {
						serializer.writeObject(buffer, value);
					}
				}

				int dataLength = buffer.position() - start - 1;
				if (dataLength <= 127) {
					// Ideally it fits in one byte.
					buffer.put(start, (byte) dataLength);
				} else {
					// Shift the data over to make room for the length.
					byte[] temp = context.getByteArray(dataLength);
					buffer.position(start + 1);
					buffer.get(temp, 0, dataLength);
					buffer.position(start);
					IntSerializer.put(buffer, dataLength, true);
					buffer.put(temp, 0, dataLength);
				}
			} catch (IllegalAccessException ex) {
				throw new SerializationException("Error accessing field in class: " + object.getClass().getName(), ex);
			} catch (SerializationException ex) {
				ex.addTrace(cachedField + " (" + object.getClass().getName() + ")");
				throw ex;
			} catch (RuntimeException runtimeEx) {
				SerializationException ex = new SerializationException(runtimeEx);
				ex.addTrace(cachedField + " (" + object.getClass().getName() + ")");
				throw ex;
			}
		}
		if (TRACE) {
			trace("kryo", "Wrote object: " + object);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T readObjectData(ByteBuffer buffer, Class<T> type) {
		return readObjectData(newInstance(kryo, type), buffer, type);
	}

	/**
	 * @see Serializer#readObjectData(ByteBuffer, Class)
	 * @param object
	 *            Object to read to.
	 * @param buffer
	 *            Buffer to read from.
	 * @param type
	 *            Type of the class.
	 * @param <T>
	 *            Type of object.
	 * @return Read object.
	 */
	protected <T> T readObjectData(T object, ByteBuffer buffer, Class<T> type) {
		Context context = Kryo.getContext();
		CachedField[] fields = (CachedField[]) context.getTemp(this, "schema");
		if (fields == null) {
			int length = IntSerializer.get(buffer, true);
			if (TRACE) {
				trace("kryo", "Reading " + length + " field names.");
			}

			// Changed by ISE
			int[] markers = new int[length];
			for (int i = 0; i < length; i++) {
				markers[i] = IntSerializer.get(buffer, true);
			}

			fields = new CachedField[length];
			CachedField[] allFields = this.fields;
			outer: for (int i = 0, n = markers.length; i < n; i++) {
				int fieldMarker = markers[i];
				for (int ii = 0, nn = allFields.length; ii < nn; ii++) {
					if (allFields[ii].getFieldMarker() == fieldMarker) {
						fields[i] = allFields[ii];
						continue outer;
					}
				}
				if (TRACE) {
					trace("kryo", "Ignoring obsolete field with marker: " + fieldMarker);
				}
			}
			context.putTemp(this, "schema", fields);
		}

		for (int i = 0, n = fields.length; i < n; i++) {
			int dataLength = IntSerializer.get(buffer, true);

			CachedField cachedField = fields[i];
			try {
				if (cachedField == null) {
					if (TRACE) {
						trace("kryo", "Skipping obsolete field bytes: " + dataLength);
					}
					try {
						buffer.position(buffer.position() + dataLength);
					} catch (IllegalArgumentException ex) {
						new BufferOverflowException();
					}
					continue;
				}

				if (TRACE) {
					trace("kryo", "Reading field: " + cachedField + " (" + type.getName() + ")");
				}

				Object value;

				if (dataLength == 0) {
					value = null;
				} else {
					Class<?> concreteType = cachedField.fieldClass;
					Serializer serializer = cachedField.serializer;
					if (concreteType == null) {
						RegisteredClass registeredClass = kryo.readClass(buffer);
						if (registeredClass == null) {
							value = null;
						} else {
							concreteType = registeredClass.getType();
							if (serializer == null) {
								serializer = registeredClass.getSerializer();
							}
							value = serializer.readObjectData(buffer, concreteType);
						}
					} else {
						if (serializer == null) {
							serializer = kryo.getRegisteredClass(concreteType).getSerializer();
							cachedField.serializer = serializer;
						}
						if (!cachedField.canBeNull) {
							value = serializer.readObjectData(buffer, concreteType);
						} else {
							value = serializer.readObject(buffer, concreteType);
						}
					}
				}

				cachedField.set(object, value);
			} catch (IllegalAccessException ex) {
				throw new SerializationException("Error accessing field in class: " + type.getName(), ex);
			} catch (SerializationException ex) {
				ex.addTrace(cachedField + " (" + type.getName() + ")");
				throw ex;
			} catch (RuntimeException runtimeEx) {
				SerializationException ex = new SerializationException(runtimeEx);
				ex.addTrace(cachedField + " (" + type.getName() + ")");
				throw ex;
			}
		}
		if (TRACE) {
			trace("kryo", "Read object: " + object);
		}
		return object;
	}

	/**
	 * Allows specific fields to be optimized.
	 * 
	 * @param fieldName
	 *            Name of the field.
	 * @return {@link CachedField}.
	 */
	public CachedField getField(String fieldName) {
		for (CachedField cachedField : fields) {
			if (cachedField.field.getName().equals(fieldName)) {
				return cachedField;
			}
		}
		throw new IllegalArgumentException("Field \"" + fieldName + "\" not found on class: " + type.getName());
	}

	/**
	 * Removes a field so that it won't be serialized.
	 * 
	 * @param fieldName
	 *            Name of the field.
	 */
	public void removeField(String fieldName) {
		for (int i = 0; i < fields.length; i++) {
			CachedField cachedField = fields[i];
			if (cachedField.field.getName().equals(fieldName)) {
				CachedField[] newFields = new CachedField[fields.length - 1];
				System.arraycopy(fields, 0, newFields, 0, i);
				System.arraycopy(fields, i + 1, newFields, i, newFields.length - i);
				fields = newFields;
				return;
			}
		}
		throw new IllegalArgumentException("Field \"" + fieldName + "\" not found on class: " + type.getName());
	}

	/**
	 * @return the kryo
	 */
	protected Kryo getKryo() {
		return kryo;
	}

	/**
	 * Controls how a field will be serialized.
	 */
	public class CachedField {

		/**
		 * Field.
		 */
		private Field field;

		/**
		 * Class that field belongs to.
		 */
		private Class<?> fieldClass;

		/**
		 * Serializer used for the field.
		 */
		private Serializer serializer;

		/**
		 * If it can be null.
		 */
		private boolean canBeNull;

		/**
		 * Access index for faster retrieval.
		 */
		private int accessIndex = -1;

		/**
		 * Marker for field.
		 */
		private int fieldMarker;

		/**
		 * @param fieldClass
		 *            The concrete class of the values for this field. This saves 1-2 bytes. The
		 *            serializer registered for the specified class will be used. Only set to a
		 *            non-null value if the field type in the class definition is final or the
		 *            values for this field will not vary.
		 */
		public void setClass(Class<?> fieldClass) {
			this.fieldClass = fieldClass;
			this.serializer = null;
		}

		/**
		 * @param fieldClass
		 *            The concrete class of the values for this field. This saves 1-2 bytes. Only
		 *            set to a non-null value if the field type in the class definition is final or
		 *            the values for this field will not vary.
		 * @param serializer
		 *            Serilizer to use.
		 */
		public void setClass(Class<?> fieldClass, Serializer serializer) {
			this.fieldClass = fieldClass;
			this.serializer = serializer;
		}

		/**
		 * @param canBeNull
		 *            Sets if it can be null.
		 */
		public void setCanBeNull(boolean canBeNull) {
			this.canBeNull = canBeNull;
		}

		/**
		 * @return the fieldMarker
		 */
		public int getFieldMarker() {
			return fieldMarker;
		}

		/**
		 * @param fieldMarker
		 *            the fieldMarker to set
		 */
		public void setFieldMarker(int fieldMarker) {
			this.fieldMarker = fieldMarker;
		}

		@Override
		public String toString() {
			return field.getName();
		}

		/**
		 * Returns the value of the field for the given object.
		 * 
		 * @param object
		 *            Object to get field value for.
		 * @return Field value.
		 * @throws IllegalAccessException
		 *             If {@link IllegalAccessException} occurs.
		 */
		protected Object get(Object object) throws IllegalAccessException {
			if (accessIndex != -1) {
				return ((FieldAccess) access).get(object, accessIndex);
			}
			return field.get(object);
		}

		/**
		 * Sets the value for the field of the given object.
		 * 
		 * @param object
		 *            Object to set field value for.
		 * @param value
		 *            Value.
		 * @throws IllegalAccessException
		 *             If {@link IllegalAccessException} occurs.
		 */
		protected void set(Object object, Object value) throws IllegalAccessException {
			if (accessIndex != -1) {
				((FieldAccess) access).set(object, accessIndex, value);
			} else {
				field.set(object, value);
			}
		}
	}
}
