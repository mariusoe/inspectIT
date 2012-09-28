package info.novatec.inspectit.storage.serializer.impl;

import info.novatec.inspectit.cmr.storage.util.IObjectCloner;
import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;

import com.esotericsoftware.kryo.Kryo;

/**
 * Special serializer that creates the clone of the object that needs to be serialized.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class CloningCustomCompatibleFieldSerializer<E> extends CustomCompatibleFieldSerializer<E> {

	/**
	 * {@link IObjectCloner} for cloning.
	 */
	private IObjectCloner objectCloner;

	/**
	 * Thread local for avoiding the cloning of the already cloned object.
	 */
	private static ThreadLocal<Boolean> threadLast = new ThreadLocal<Boolean>();

	/**
	 * Default constructor.
	 * 
	 * @param kryo
	 *            Kryo instance
	 * @param type
	 *            Class to be serialized
	 * @param schemaManager
	 *            {@link ClassSchemaManager} holding information about values.
	 * @param objectCloner
	 *            {@link IObjectCloner} for cloning.
	 */
	public CloningCustomCompatibleFieldSerializer(Kryo kryo, Class<?> type, ClassSchemaManager schemaManager, IObjectCloner objectCloner) {
		this(kryo, type, schemaManager, false, objectCloner);
	}

	/**
	 * 
	 * @param kryo
	 *            Kryo instance
	 * @param type
	 *            Class to be serialized
	 * @param schemaManager
	 *            {@link ClassSchemaManager} holding information about values.
	 * @param useSuperclassSchema
	 *            If the superclass schema should be used if the one for the class is not available.
	 * @param objectCloner
	 *            {@link IObjectCloner} for cloning.
	 */
	public CloningCustomCompatibleFieldSerializer(Kryo kryo, Class<?> type, ClassSchemaManager schemaManager, boolean useSuperclassSchema, IObjectCloner objectCloner) {
		super(kryo, type, schemaManager, useSuperclassSchema);
		this.objectCloner = objectCloner;
		threadLast.set(Boolean.FALSE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void write(Kryo kryo, com.esotericsoftware.kryo.io.Output output, E object) {
		Boolean sameObject = threadLast.get();
		if (sameObject != null && !sameObject.booleanValue()) {
			threadLast.set(Boolean.TRUE);
			try {
				E clone = objectCloner.clone(object);
				super.write(kryo, output, clone);
			} catch (Exception e) {
				super.write(kryo, output, object);
			}
			threadLast.set(Boolean.FALSE);
		} else {
			super.write(kryo, output, object);
		}
	};

}
