package info.novatec.inspectit.storage.serializer.impl;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.communication.data.CompilationInformationData;
import info.novatec.inspectit.communication.data.CpuInformationData;
import info.novatec.inspectit.communication.data.DatabaseAggregatedTimerData;
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
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData.AgentConnection;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;
import info.novatec.inspectit.communication.data.cmr.LicenseInfoData;
import info.novatec.inspectit.communication.data.cmr.RecordingData;
import info.novatec.inspectit.communication.data.cmr.WritingStatus;
import info.novatec.inspectit.indexing.aggregation.impl.ExceptionDataAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.HttpTimerDataAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.SqlStatementDataAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.TimerDataAggregator;
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
import info.novatec.inspectit.storage.label.management.impl.AddLabelManagementAction;
import info.novatec.inspectit.storage.label.management.impl.RemoveLabelManagementAction;
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
import info.novatec.inspectit.storage.processor.impl.DataAggregatorProcessor;
import info.novatec.inspectit.storage.processor.impl.DataSaverProcessor;
import info.novatec.inspectit.storage.processor.impl.InvocationClonerDataProcessor;
import info.novatec.inspectit.storage.processor.impl.InvocationExtractorDataProcessor;
import info.novatec.inspectit.storage.processor.impl.TimeFrameDataProcessor;
import info.novatec.inspectit.storage.recording.RecordingProperties;
import info.novatec.inspectit.storage.recording.RecordingState;
import info.novatec.inspectit.storage.serializer.HibernateAwareClassResolver;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;
import info.novatec.inspectit.util.IHibernateUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.ClassResolver;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ReferenceResolver;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.LongArraySerializer;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.ObjectArraySerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.ClassSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.DateSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.EnumSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.esotericsoftware.kryo.util.DefaultClassResolver;
import com.esotericsoftware.kryo.util.MapReferenceResolver;

/**
 * Implementation of the {@link ISerializer} that uses Kryo library for serializing the objects. <br>
 * <br>
 * <b>This class is not thread safe and should be used with special attention. The class can be used
 * only by one thread while the serialization/de-serialization process lasts.</b>
 * 
 * @author Ivan Senic
 * 
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class SerializationManager implements ISerializer, InitializingBean {

	/**
	 * Main {@link Kryo} instance.
	 */
	private Kryo kryo;

	/**
	 * Schema manager that holds all schemas for the {@link DefaultData} objects to be serialized.
	 */
	@Autowired
	ClassSchemaManager schemaManager;

	/**
	 * {@link IHibernateUtil} if needed for Hibernate persistent collections/maps solving.
	 */
	@Autowired
	IHibernateUtil hibernateUtil;

	/**
	 * Initialize {@link Kryo} properties.
	 */
	protected void initKryo() {
		// if hibernateUtil is provided, we create special kind of class resolver
		ClassResolver classResolver;
		if (null != hibernateUtil) {
			classResolver = new HibernateAwareClassResolver(hibernateUtil);
		} else {
			classResolver = new DefaultClassResolver();
		}

		// we disable references for DefaultData objects because they are not needed
		// invocations will be handled manually
		ReferenceResolver referenceResolver = new MapReferenceResolver() {
			@SuppressWarnings("rawtypes")
			@Override
			public boolean useReferences(Class paramClass) {
				if (DefaultData.class.isAssignableFrom(paramClass)) {
					return false;
				} else {
					return super.useReferences(paramClass);
				}
			}
		};
		kryo = new Kryo(classResolver, referenceResolver);
		kryo.setRegistrationRequired(false);
		registerClasses(kryo);
	}

	/**
	 * Registers all necessary classes to the {@link Kryo} instance;
	 * 
	 * ATTENTION!
	 * 
	 * Please do not change the order of the registered classes. If new classes need to be
	 * registered, please add this registration at the end. Otherwise the old data will not be able
	 * to be de-serialized. If some class is not need to be register any more, do not remove the
	 * registration. If the class is not available any more, add arbitrary class to its position, so
	 * that the order can be maintained. Do not add unnecessary classes to the registration list.
	 * 
	 * NOTE: By default, all primitives (including wrappers) and java.lang.String are registered.
	 * Any other class, including JDK classes like ArrayList and even arrays such as String[] or
	 * int[] must be registered.
	 * 
	 * NOTE: If it is known up front what classes need to be serialized, registering the classes is
	 * ideal. However, in some cases the classes to serialize are not known until it is time to
	 * perform the serialization. When setRegistrationOptional is true, registered classes are still
	 * written as an integer. However, unregistered classes are written as a String, using the name
	 * of the class. This is much less efficient, but can't always be avoided.
	 * 
	 * @param kryo
	 *            Kryo that needs to be prepared.
	 */
	private void registerClasses(Kryo kryo) {
		/** Java native classes */
		kryo.register(Class.class, new ClassSerializer());
		kryo.register(ArrayList.class, new HibernateAwareCollectionSerializer(hibernateUtil));
		kryo.register(CopyOnWriteArrayList.class, new CollectionSerializer());
		kryo.register(HashSet.class, new HibernateAwareCollectionSerializer(hibernateUtil));
		kryo.register(HashMap.class, new HibernateAwareMapSerializer(hibernateUtil));
		kryo.register(ConcurrentHashMap.class, new MapSerializer());
		kryo.register(Timestamp.class, new TimestampSerializer());
		kryo.register(Date.class, new DateSerializer());
		kryo.register(AtomicLong.class, new FieldSerializer<AtomicLong>(kryo, AtomicLong.class));
		/** Arrays */
		kryo.register(long[].class, new LongArraySerializer());
		kryo.register(SimpleStorageDescriptor[].class, new ObjectArraySerializer());
		/** inspectIT model classes */
		kryo.register(PlatformIdent.class, new CustomCompatibleFieldSerializer<PlatformIdent>(kryo, PlatformIdent.class, schemaManager));
		kryo.register(MethodIdent.class, new CustomCompatibleFieldSerializer<MethodIdent>(kryo, MethodIdent.class, schemaManager));
		kryo.register(SensorTypeIdent.class, new CustomCompatibleFieldSerializer<SensorTypeIdent>(kryo, SensorTypeIdent.class, schemaManager));
		kryo.register(MethodSensorTypeIdent.class, new CustomCompatibleFieldSerializer<MethodSensorTypeIdent>(kryo, MethodSensorTypeIdent.class, schemaManager));
		kryo.register(PlatformSensorTypeIdent.class, new FieldSerializer<PlatformSensorTypeIdent>(kryo, PlatformSensorTypeIdent.class));
		/** Common data classes */
		kryo.register(MutableInt.class, new FieldSerializer<MutableInt>(kryo, MutableInt.class));
		kryo.register(InvocationSequenceData.class, new InvocationSequenceCustomCompatibleFieldSerializer(kryo, InvocationSequenceData.class, schemaManager));
		kryo.register(TimerData.class, new CustomCompatibleFieldSerializer<TimerData>(kryo, TimerData.class, schemaManager));
		kryo.register(HttpTimerData.class, new CustomCompatibleFieldSerializer<HttpTimerData>(kryo, HttpTimerData.class, schemaManager));
		kryo.register(SqlStatementData.class, new CustomCompatibleFieldSerializer<SqlStatementData>(kryo, SqlStatementData.class, schemaManager));
		kryo.register(ExceptionSensorData.class, new CustomCompatibleFieldSerializer<ExceptionSensorData>(kryo, ExceptionSensorData.class, schemaManager));
		kryo.register(ExceptionEvent.class, new EnumSerializer(ExceptionEvent.class));
		kryo.register(ParameterContentData.class, new CustomCompatibleFieldSerializer<ParameterContentData>(kryo, ParameterContentData.class, schemaManager));
		kryo.register(MemoryInformationData.class, new CustomCompatibleFieldSerializer<MemoryInformationData>(kryo, MemoryInformationData.class, schemaManager));
		kryo.register(CpuInformationData.class, new CustomCompatibleFieldSerializer<CpuInformationData>(kryo, CpuInformationData.class, schemaManager));
		kryo.register(SystemInformationData.class, new CustomCompatibleFieldSerializer<SystemInformationData>(kryo, SystemInformationData.class, schemaManager));
		kryo.register(VmArgumentData.class, new CustomCompatibleFieldSerializer<VmArgumentData>(kryo, VmArgumentData.class, schemaManager));
		kryo.register(ThreadInformationData.class, new CustomCompatibleFieldSerializer<ThreadInformationData>(kryo, ThreadInformationData.class, schemaManager));
		kryo.register(RuntimeInformationData.class, new CustomCompatibleFieldSerializer<RuntimeInformationData>(kryo, RuntimeInformationData.class, schemaManager));
		kryo.register(CompilationInformationData.class, new CustomCompatibleFieldSerializer<CompilationInformationData>(kryo, CompilationInformationData.class, schemaManager));
		kryo.register(ClassLoadingInformationData.class, new CustomCompatibleFieldSerializer<ClassLoadingInformationData>(kryo, ClassLoadingInformationData.class, schemaManager));
		kryo.register(ParameterContentType.class, new EnumSerializer(ParameterContentType.class));
		/** Storage classes */
		kryo.register(StorageBranch.class, new CustomCompatibleFieldSerializer<StorageBranch<?>>(kryo, StorageBranch.class, schemaManager));
		kryo.register(StorageBranchIndexer.class, new CustomCompatibleFieldSerializer<StorageBranchIndexer<?>>(kryo, StorageBranchIndexer.class, schemaManager));
		kryo.register(SimpleStorageDescriptor.class, new CustomCompatibleFieldSerializer<SimpleStorageDescriptor>(kryo, SimpleStorageDescriptor.class, schemaManager));
		kryo.register(ArrayBasedStorageLeaf.class, new CustomCompatibleFieldSerializer<ArrayBasedStorageLeaf<?>>(kryo, ArrayBasedStorageLeaf.class, schemaManager));
		kryo.register(LeafWithNoDescriptors.class, new CustomCompatibleFieldSerializer<LeafWithNoDescriptors<?>>(kryo, LeafWithNoDescriptors.class, schemaManager));
		kryo.register(StorageData.class, new CustomCompatibleFieldSerializer<StorageData>(kryo, StorageData.class, schemaManager));
		kryo.register(LocalStorageData.class, new CustomCompatibleFieldSerializer<LocalStorageData>(kryo, LocalStorageData.class, schemaManager));
		kryo.register(StorageState.class, new EnumSerializer(StorageState.class));
		/** Storage labels */
		kryo.register(BooleanStorageLabel.class, new CustomCompatibleFieldSerializer<BooleanStorageLabel>(kryo, BooleanStorageLabel.class, schemaManager));
		kryo.register(DateStorageLabel.class, new CustomCompatibleFieldSerializer<DateStorageLabel>(kryo, DateStorageLabel.class, schemaManager));
		kryo.register(NumberStorageLabel.class, new CustomCompatibleFieldSerializer<NumberStorageLabel>(kryo, NumberStorageLabel.class, schemaManager));
		kryo.register(StringStorageLabel.class, new CustomCompatibleFieldSerializer<StringStorageLabel>(kryo, StringStorageLabel.class, schemaManager));
		/** Storage labels type */
		kryo.register(AssigneeLabelType.class, new CustomCompatibleFieldSerializer<AssigneeLabelType>(kryo, AssigneeLabelType.class, schemaManager, true));
		kryo.register(CreationDateLabelType.class, new CustomCompatibleFieldSerializer<CreationDateLabelType>(kryo, CreationDateLabelType.class, schemaManager, true));
		kryo.register(CustomBooleanLabelType.class, new CustomCompatibleFieldSerializer<CustomBooleanLabelType>(kryo, CustomBooleanLabelType.class, schemaManager, true));
		kryo.register(CustomDateLabelType.class, new CustomCompatibleFieldSerializer<CustomDateLabelType>(kryo, CustomDateLabelType.class, schemaManager, true));
		kryo.register(CustomNumberLabelType.class, new CustomCompatibleFieldSerializer<CustomNumberLabelType>(kryo, CustomNumberLabelType.class, schemaManager, true));
		kryo.register(CustomStringLabelType.class, new CustomCompatibleFieldSerializer<CustomStringLabelType>(kryo, CustomStringLabelType.class, schemaManager, true));
		kryo.register(ExploredByLabelType.class, new CustomCompatibleFieldSerializer<ExploredByLabelType>(kryo, ExploredByLabelType.class, schemaManager, true));
		kryo.register(RatingLabelType.class, new CustomCompatibleFieldSerializer<RatingLabelType>(kryo, RatingLabelType.class, schemaManager, true));
		kryo.register(StatusLabelType.class, new CustomCompatibleFieldSerializer<StatusLabelType>(kryo, StatusLabelType.class, schemaManager, true));
		kryo.register(UseCaseLabelType.class, new CustomCompatibleFieldSerializer<UseCaseLabelType>(kryo, UseCaseLabelType.class, schemaManager, true));
		/** Branch indexers */
		kryo.register(PlatformIdentIndexer.class, new FieldSerializer<PlatformIdentIndexer<?>>(kryo, PlatformIdentIndexer.class));
		kryo.register(ObjectTypeIndexer.class, new FieldSerializer<ObjectTypeIndexer<?>>(kryo, ObjectTypeIndexer.class));
		kryo.register(MethodIdentIndexer.class, new FieldSerializer<MethodIdentIndexer<?>>(kryo, MethodIdentIndexer.class));
		kryo.register(SensorTypeIdentIndexer.class, new FieldSerializer<SensorTypeIdentIndexer<?>>(kryo, SensorTypeIdentIndexer.class));
		kryo.register(TimestampIndexer.class, new CustomCompatibleFieldSerializer<TimestampIndexer<?>>(kryo, TimestampIndexer.class, schemaManager));
		kryo.register(InvocationChildrenIndexer.class, new FieldSerializer<InvocationChildrenIndexer<?>>(kryo, InvocationChildrenIndexer.class));
		kryo.register(SqlStringIndexer.class, new FieldSerializer<SqlStringIndexer<?>>(kryo, SqlStringIndexer.class));

		// aggregation classes
		kryo.register(AggregatedExceptionSensorData.class, new CustomCompatibleFieldSerializer<AggregatedExceptionSensorData>(kryo, AggregatedExceptionSensorData.class, schemaManager));
		kryo.register(DatabaseAggregatedTimerData.class, new CustomCompatibleFieldSerializer<DatabaseAggregatedTimerData>(kryo, DatabaseAggregatedTimerData.class, schemaManager, true));

		// classes needed for the HTTP calls from the UI
		kryo.register(RemoteInvocation.class, new FieldSerializer<RemoteInvocation>(kryo, RemoteInvocation.class));
		kryo.register(RemoteInvocationResult.class, new FieldSerializer<RemoteInvocationResult>(kryo, RemoteInvocationResult.class) {
			@Override
			protected RemoteInvocationResult create(Kryo arg0, Input input, Class<RemoteInvocationResult> type) {
				return new RemoteInvocationResult(null);
			}
		});

		// data classes between CMR and UI
		// this classes can be registered with FieldSerializer since they are not saved to disk
		kryo.register(LicenseInfoData.class, new FieldSerializer<LicenseInfoData>(kryo, LicenseInfoData.class));
		kryo.register(CmrStatusData.class, new FieldSerializer<CmrStatusData>(kryo, CmrStatusData.class));
		kryo.register(AgentStatusData.class, new FieldSerializer<AgentStatusData>(kryo, AgentStatusData.class));
		kryo.register(AgentConnection.class, new EnumSerializer(AgentConnection.class));
		kryo.register(RecordingData.class, new FieldSerializer<RecordingData>(kryo, RecordingData.class));
		kryo.register(WritingStatus.class, new EnumSerializer(WritingStatus.class));
		kryo.register(AddLabelManagementAction.class, new FieldSerializer<AddLabelManagementAction>(kryo, AddLabelManagementAction.class));
		kryo.register(RemoveLabelManagementAction.class, new FieldSerializer<RemoveLabelManagementAction>(kryo, RemoveLabelManagementAction.class));
		kryo.register(DataAggregatorProcessor.class, new FieldSerializer<DataAggregatorProcessor<?>>(kryo, DataAggregatorProcessor.class));
		kryo.register(DataSaverProcessor.class, new FieldSerializer<DataSaverProcessor>(kryo, DataSaverProcessor.class));
		kryo.register(InvocationClonerDataProcessor.class, new FieldSerializer<InvocationClonerDataProcessor>(kryo, InvocationClonerDataProcessor.class));
		kryo.register(InvocationExtractorDataProcessor.class, new FieldSerializer<InvocationExtractorDataProcessor>(kryo, InvocationExtractorDataProcessor.class));
		kryo.register(TimeFrameDataProcessor.class, new FieldSerializer<TimeFrameDataProcessor>(kryo, TimeFrameDataProcessor.class));
		kryo.register(TimerDataAggregator.class, new FieldSerializer<TimerDataAggregator>(kryo, TimerDataAggregator.class));
		kryo.register(SqlStatementDataAggregator.class, new FieldSerializer<SqlStatementDataAggregator>(kryo, SqlStatementDataAggregator.class));
		kryo.register(HttpTimerDataAggregator.class, new FieldSerializer<HttpTimerDataAggregator>(kryo, HttpTimerDataAggregator.class));
		kryo.register(ExceptionDataAggregator.class, new FieldSerializer<ExceptionDataAggregator>(kryo, ExceptionDataAggregator.class));

		// INSPECTIT-849 - HIbernate uses Arrays.asList which does not have no-arg constructor
		// we will create array list instead
		kryo.register(Arrays.asList().getClass(), new CollectionSerializer() {
			@Override
			@SuppressWarnings("rawtypes")
			protected Collection create(Kryo paramKryo, Input paramInput, Class<Collection> paramClass) {
				return new ArrayList<Object>();
			}
		});

		// added with INSPECTIT-723
		kryo.register(RecordingState.class, new EnumSerializer(RecordingState.class));
		kryo.register(RecordingProperties.class, new FieldSerializer<RecordingProperties>(kryo, RecordingProperties.class));
	}

	/**
	 * {@inheritDoc}
	 */
	public void serialize(Object object, Output output) throws SerializationException {
		try {
			kryo.writeClassAndObject(output, object);
			output.flush();
		} catch (Exception exception) {
			throw new SerializationException("Serialization failed.\n" + exception.getMessage(), exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object deserialize(Input input) throws SerializationException {
		Object object = null;
		try {
			object = kryo.readClassAndObject(input);
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
