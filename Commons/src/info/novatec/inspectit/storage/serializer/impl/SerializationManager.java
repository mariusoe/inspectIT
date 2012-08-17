package info.novatec.inspectit.storage.serializer.impl;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.communication.data.CompilationInformationData;
import info.novatec.inspectit.communication.data.CpuInformationData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationAwareData.MutableInt;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.ParameterContentType;
import info.novatec.inspectit.communication.data.RuntimeInformationData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.communication.data.VmArgumentData;
import info.novatec.inspectit.indexing.indexer.impl.InvocationChildrenIndexer;
import info.novatec.inspectit.indexing.indexer.impl.MethodIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.ObjectTypeIndexer;
import info.novatec.inspectit.indexing.indexer.impl.PlatformIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.SensorTypeIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.SqlStringIndexer;
import info.novatec.inspectit.indexing.indexer.impl.TimestampIndexer;
import info.novatec.inspectit.indexing.storage.impl.ArrayBasedStorageLeaf;
import info.novatec.inspectit.indexing.storage.impl.LeafWithNoDescriptors;
import info.novatec.inspectit.indexing.storage.impl.SimpleStorageDescriptor;
import info.novatec.inspectit.indexing.storage.impl.StorageBranch;
import info.novatec.inspectit.indexing.storage.impl.StorageBranchIndexer;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageData.StorageState;
import info.novatec.inspectit.storage.label.BooleanStorageLabel;
import info.novatec.inspectit.storage.label.DateStorageLabel;
import info.novatec.inspectit.storage.label.NumberStorageLabel;
import info.novatec.inspectit.storage.label.StringStorageLabel;
import info.novatec.inspectit.storage.label.type.impl.AssigneeLabelType;
import info.novatec.inspectit.storage.label.type.impl.CreationDateLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomBooleanLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomDateLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomNumberLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomStringLabelType;
import info.novatec.inspectit.storage.label.type.impl.ExploredByLabelType;
import info.novatec.inspectit.storage.label.type.impl.RatingLabelType;
import info.novatec.inspectit.storage.label.type.impl.StatusLabelType;
import info.novatec.inspectit.storage.label.type.impl.UseCaseLabelType;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.ArraySerializer;
import com.esotericsoftware.kryo.serialize.ClassSerializer;
import com.esotericsoftware.kryo.serialize.CollectionSerializer;
import com.esotericsoftware.kryo.serialize.DateSerializer;
import com.esotericsoftware.kryo.serialize.EnumSerializer;
import com.esotericsoftware.kryo.serialize.FieldSerializer;
import com.esotericsoftware.kryo.serialize.MapSerializer;

/**
 * Implementation of the {@link ISerializer} that uses Kryo library for serializing the objects.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class SerializationManager implements ISerializer, InitializingBean {

	/**
	 * {@link Kryo} instance.
	 */
	private Kryo kryo;

	/**
	 * Schema manager that holds all schemas for the {@link DefaultData} objects to be serialized.
	 */
	@Autowired
	ClassSchemaManager schemaManager;

	/**
	 * Initialize {@link Kryo} properties.
	 */
	protected void initKryo() {
		kryo = new Kryo();
		kryo.setRegistrationOptional(true);

		/**
		 * ATTENTION!
		 * 
		 * Please do not change the order of the registered classes. If new classes need to be
		 * registered, please add this registration at the end. Otherwise the old data will not be
		 * able to be de-serialized. If some class is not need to be register any more, do not
		 * remove the registration. If the class is not available any more, add arbitrary class to
		 * its position, so that the order can be maintained. Do not add unnecessary classes to the
		 * registration list.
		 * 
		 * NOTE: By default, all primitives (including wrappers) and java.lang.String are
		 * registered. Any other class, including JDK classes like ArrayList and even arrays such as
		 * String[] or int[] must be registered.
		 * 
		 * NOTE: If it is known up front what classes need to be serialized, registering the classes
		 * is ideal. However, in some cases the classes to serialize are not known until it is time
		 * to perform the serialization. When setRegistrationOptional is true, registered classes
		 * are still written as an integer. However, unregistered classes are written as a String,
		 * using the name of the class. This is much less efficient, but can't always be avoided.
		 */

		/** Java native classes */
		kryo.register(Class.class, new ClassSerializer(kryo), false);
		kryo.register(ArrayList.class, new CollectionSerializer(kryo), false);
		kryo.register(CopyOnWriteArrayList.class, new CollectionSerializer(kryo), false);
		kryo.register(HashSet.class, new CollectionSerializer(kryo), false);
		kryo.register(HashMap.class, new MapSerializer(kryo), false);
		kryo.register(ConcurrentHashMap.class, new MapSerializer(kryo), false);
		kryo.register(Timestamp.class, new TimestampSerializer(), false);
		kryo.register(Date.class, new DateSerializer(), false);
		kryo.register(AtomicLong.class, new FieldSerializer(kryo, AtomicLong.class), false);
		/** Arrays */
		kryo.register(long[].class, new ArraySerializer(kryo));
		kryo.register(SimpleStorageDescriptor[].class, new ArraySerializer(kryo));
		/** inspectIT model classes */
		kryo.register(PlatformIdent.class, new ReferenceCustomCompatibleFieldSerializer(kryo, PlatformIdent.class, schemaManager), false);
		kryo.register(MethodIdent.class, new ReferenceCustomCompatibleFieldSerializer(kryo, MethodIdent.class, schemaManager), false);
		kryo.register(SensorTypeIdent.class, new ReferenceCustomCompatibleFieldSerializer(kryo, SensorTypeIdent.class, schemaManager), false);
		kryo.register(MethodSensorTypeIdent.class, new ReferenceCustomCompatibleFieldSerializer(kryo, MethodSensorTypeIdent.class, schemaManager), false);
		kryo.register(PlatformSensorTypeIdent.class, new FieldSerializer(kryo, PlatformSensorTypeIdent.class), false);
		/** Common data classes */
		kryo.register(MutableInt.class, new FieldSerializer(kryo, MutableInt.class), false);
		kryo.register(InvocationSequenceData.class, new InvocationSequenceCustomCompatibleFieldSerializer(kryo, InvocationSequenceData.class, schemaManager), false);
		kryo.register(TimerData.class, new CustomCompatibleFieldSerializer(kryo, TimerData.class, schemaManager), false);
		kryo.register(HttpTimerData.class, new CustomCompatibleFieldSerializer(kryo, HttpTimerData.class, schemaManager), false);
		kryo.register(SqlStatementData.class, new CustomCompatibleFieldSerializer(kryo, SqlStatementData.class, schemaManager), false);
		kryo.register(ExceptionSensorData.class, new CustomCompatibleFieldSerializer(kryo, ExceptionSensorData.class, schemaManager), false);
		kryo.register(ExceptionEvent.class, new EnumSerializer(ExceptionEvent.class), false);
		kryo.register(ParameterContentData.class, new CustomCompatibleFieldSerializer(kryo, ParameterContentData.class, schemaManager), false);
		kryo.register(MemoryInformationData.class, new CustomCompatibleFieldSerializer(kryo, MemoryInformationData.class, schemaManager), false);
		kryo.register(CpuInformationData.class, new CustomCompatibleFieldSerializer(kryo, CpuInformationData.class, schemaManager), false);
		kryo.register(SystemInformationData.class, new CustomCompatibleFieldSerializer(kryo, SystemInformationData.class, schemaManager), false);
		kryo.register(VmArgumentData.class, new CustomCompatibleFieldSerializer(kryo, VmArgumentData.class, schemaManager), false);
		kryo.register(ThreadInformationData.class, new CustomCompatibleFieldSerializer(kryo, ThreadInformationData.class, schemaManager), false);
		kryo.register(RuntimeInformationData.class, new CustomCompatibleFieldSerializer(kryo, RuntimeInformationData.class, schemaManager), false);
		kryo.register(CompilationInformationData.class, new CustomCompatibleFieldSerializer(kryo, CompilationInformationData.class, schemaManager), false);
		kryo.register(ClassLoadingInformationData.class, new CustomCompatibleFieldSerializer(kryo, ClassLoadingInformationData.class, schemaManager), false);
		/** Storage classes */
		kryo.register(StorageBranch.class, new CustomCompatibleFieldSerializer(kryo, StorageBranch.class, schemaManager), false);
		kryo.register(StorageBranchIndexer.class, new CustomCompatibleFieldSerializer(kryo, StorageBranchIndexer.class, schemaManager), false);
		kryo.register(SimpleStorageDescriptor.class, new CustomCompatibleFieldSerializer(kryo, SimpleStorageDescriptor.class, schemaManager), false);
		kryo.register(ArrayBasedStorageLeaf.class, new CustomCompatibleFieldSerializer(kryo, ArrayBasedStorageLeaf.class, schemaManager), false);
		kryo.register(LeafWithNoDescriptors.class, new CustomCompatibleFieldSerializer(kryo, LeafWithNoDescriptors.class, schemaManager), false);
		kryo.register(StorageData.class, new CustomCompatibleFieldSerializer(kryo, StorageData.class, schemaManager), false);
		kryo.register(LocalStorageData.class, new CustomCompatibleFieldSerializer(kryo, LocalStorageData.class, schemaManager), false);
		kryo.register(StorageState.class, new EnumSerializer(StorageState.class), false);
		kryo.register(ParameterContentType.class, new EnumSerializer(ParameterContentType.class), false);
		/** Storage labels */
		kryo.register(BooleanStorageLabel.class, new FieldSerializer(kryo, BooleanStorageLabel.class), false);
		kryo.register(DateStorageLabel.class, new FieldSerializer(kryo, DateStorageLabel.class), false);
		kryo.register(NumberStorageLabel.class, new FieldSerializer(kryo, NumberStorageLabel.class), false);
		kryo.register(StringStorageLabel.class, new FieldSerializer(kryo, StringStorageLabel.class), false);
		/** Storage labels type */
		kryo.register(AssigneeLabelType.class, new FieldSerializer(kryo, AssigneeLabelType.class), false);
		kryo.register(CreationDateLabelType.class, new FieldSerializer(kryo, CreationDateLabelType.class), false);
		kryo.register(CustomBooleanLabelType.class, new FieldSerializer(kryo, CustomBooleanLabelType.class), false);
		kryo.register(CustomDateLabelType.class, new FieldSerializer(kryo, CustomDateLabelType.class), false);
		kryo.register(CustomNumberLabelType.class, new FieldSerializer(kryo, CustomNumberLabelType.class), false);
		kryo.register(CustomStringLabelType.class, new FieldSerializer(kryo, CustomStringLabelType.class), false);
		kryo.register(ExploredByLabelType.class, new FieldSerializer(kryo, ExploredByLabelType.class), false);
		kryo.register(RatingLabelType.class, new FieldSerializer(kryo, RatingLabelType.class), false);
		kryo.register(StatusLabelType.class, new FieldSerializer(kryo, StatusLabelType.class), false);
		kryo.register(UseCaseLabelType.class, new FieldSerializer(kryo, UseCaseLabelType.class), false);
		/** Branch indexers */
		kryo.register(PlatformIdentIndexer.class, new FieldSerializer(kryo, PlatformIdentIndexer.class), false);
		kryo.register(ObjectTypeIndexer.class, new FieldSerializer(kryo, ObjectTypeIndexer.class), false);
		kryo.register(MethodIdentIndexer.class, new FieldSerializer(kryo, MethodIdentIndexer.class), false);
		kryo.register(SensorTypeIdentIndexer.class, new FieldSerializer(kryo, SensorTypeIdentIndexer.class), false);
		kryo.register(TimestampIndexer.class, new CustomCompatibleFieldSerializer(kryo, TimestampIndexer.class, schemaManager), false);
		kryo.register(InvocationChildrenIndexer.class, new FieldSerializer(kryo, InvocationChildrenIndexer.class), false);
		kryo.register(SqlStringIndexer.class, new FieldSerializer(kryo, SqlStringIndexer.class), false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void serialize(Object object, ByteBuffer buffer) throws SerializationException {
		try {
			kryo.writeClassAndObject(buffer, object);
		} catch (Exception exception) {
			throw new SerializationException("Serialization failed.\n" + exception.getMessage(), exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object deserialize(ByteBuffer buffer) throws SerializationException {
		Object object = null;
		try {
			object = kryo.readClassAndObject(buffer);
		} catch (Exception exception) {
			throw new SerializationException("De-serialization failed.\n" + exception.getMessage(), exception);
		}
		return object;
	}

	/**
	 * <i>This setter can be removed when the Spring3.0 on the GUI side is working properly.</i>
	 * 
	 * @param schemaManager
	 *            the schemaManager to set
	 */
	public void setSchemaManager(ClassSchemaManager schemaManager) {
		this.schemaManager = schemaManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		initKryo();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("schemaManager", schemaManager);
		toStringBuilder.append("kryo", ToStringBuilder.reflectionToString(kryo));
		return toStringBuilder.toString();
	}

}
