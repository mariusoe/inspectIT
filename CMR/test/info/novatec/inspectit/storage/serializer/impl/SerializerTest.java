package info.novatec.inspectit.storage.serializer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;
import info.novatec.inspectit.cmr.util.HibernateUtil;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.communication.data.CompilationInformationData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.RuntimeInformationData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.communication.data.VmArgumentData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;
import info.novatec.inspectit.communication.data.cmr.RecordingData;
import info.novatec.inspectit.indexing.indexer.impl.InvocationChildrenIndexer;
import info.novatec.inspectit.indexing.indexer.impl.MethodIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.ObjectTypeIndexer;
import info.novatec.inspectit.indexing.indexer.impl.PlatformIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.SensorTypeIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.SqlStringIndexer;
import info.novatec.inspectit.indexing.indexer.impl.TimestampIndexer;
import info.novatec.inspectit.indexing.storage.impl.ArrayBasedStorageLeaf;
import info.novatec.inspectit.indexing.storage.impl.SimpleStorageDescriptor;
import info.novatec.inspectit.indexing.storage.impl.StorageBranch;
import info.novatec.inspectit.indexing.storage.impl.StorageBranchIndexer;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
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
import info.novatec.inspectit.storage.serializer.schema.SchemaManagerTestProvider;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Test the implementation of the {@link ISerializer} for correctness.
 * 
 * @author Ivan Senic
 * 
 */
public class SerializerTest {

	/**
	 * Classes to be tested in the {@link #testClassesForPlanSerialization(Class)}, so to be sure
	 * that every class can be serialized by our Kryo implementation.
	 */
	public static final Object[][] TESTING_CLASSES = new Object[][] { { TimerData.class }, { SqlStatementData.class }, { ExceptionSensorData.class }, { InvocationSequenceData.class },
			{ ClassLoadingInformationData.class }, { CompilationInformationData.class }, { MemoryInformationData.class }, { RuntimeInformationData.class }, { SystemInformationData.class },
			{ ThreadInformationData.class }, { HttpTimerData.class }, { ParameterContentData.class }, { VmArgumentData.class }, { PlatformIdent.class }, { MethodIdent.class },
			{ MethodSensorTypeIdent.class }, { PlatformSensorTypeIdent.class }, { SimpleStorageDescriptor.class }, { ArrayBasedStorageLeaf.class }, { StorageData.class }, { LocalStorageData.class },
			{ PlatformIdentIndexer.class }, { ObjectTypeIndexer.class }, { MethodIdentIndexer.class }, { SensorTypeIdentIndexer.class }, { TimestampIndexer.class },
			{ InvocationChildrenIndexer.class }, { StorageBranch.class }, { StorageBranchIndexer.class }, { BooleanStorageLabel.class }, { DateStorageLabel.class }, { NumberStorageLabel.class },
			{ StringStorageLabel.class }, { AssigneeLabelType.class }, { CreationDateLabelType.class }, { CustomBooleanLabelType.class }, { CustomDateLabelType.class },
			{ CustomNumberLabelType.class }, { CustomStringLabelType.class }, { ExploredByLabelType.class }, { RatingLabelType.class }, { StatusLabelType.class }, { UseCaseLabelType.class },
			{ SqlStringIndexer.class }, { BooleanStorageLabel.class }, { DateStorageLabel.class }, { NumberStorageLabel.class }, { StringStorageLabel.class }, { CustomDateLabelType.class },
			{ CmrStatusData.class }, { AgentStatusData.class }, { RecordingData.class }, { CustomBooleanLabelType.class }, { CustomNumberLabelType.class }, { CustomStringLabelType.class },
			{ AssigneeLabelType.class }, { RatingLabelType.class }, { ExploredByLabelType.class }, { CreationDateLabelType.class }, { StatusLabelType.class }, { UseCaseLabelType.class } };

	/**
	 * Serializer.
	 */
	private SerializationManager serializer;

	/**
	 * Byte buffer.
	 */
	private ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024 * 1024 * 20);

	/**
	 * Instantiates the {@link SerializationManager}.
	 * 
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@BeforeClass
	public void initSerializer() throws IOException {
		ClassSchemaManager schemaManager = SchemaManagerTestProvider.getClassSchemaManagerForTests();
		serializer = new SerializationManager();
		serializer.hibernateUtil = new HibernateUtil();
		serializer.schemaManager = schemaManager;
		serializer.initKryo();
	}

	/**
	 * Prepare the buffer before the test.
	 */
	@BeforeMethod
	public void prepareBuffer() {
		byteBuffer.clear();
	}

	/**
	 * Tests if the data deserialzied from empty buffer is <code>null</code>.
	 * 
	 * @throws SerializationException
	 *             Serialization Exception
	 */
	@Test
	public void emptyBufferSerialization() throws SerializationException {
		// I need to create a new buffer, because clear on the buffer will not actually erase the
		// data in the buffer, but only move the pointers
		ByteBuffer newByteBuffer = ByteBuffer.allocateDirect(1024);
		ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(newByteBuffer);
		Input input = new Input(byteBufferInputStream);
		Object data = serializer.deserialize(input);
		assertThat(data, is(nullValue()));
	}

	/**
	 * Tests if the data de-serialzed from buffer with random data is <code>null</code>.
	 * 
	 * @throws SerializationException
	 *             Serialization Exception
	 */
	@Test
	public void radomBufferDataSerialization() throws SerializationException {

		for (int i = 0; i < 64; i++) {
			byteBuffer.putInt(i);
		}
		byteBuffer.flip();
		ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(byteBuffer);
		Input input = new Input(byteBufferInputStream);
		Object data = serializer.deserialize(input);
		assertThat(data, is(nullValue()));
	}

	/**
	 * Provides classes to be tested.
	 * 
	 * @return Provides classes to be tested.
	 */
	@DataProvider(name = "classProvider")
	public Object[][] classprovider() {
		return TESTING_CLASSES;
	}

	/**
	 * Tests the class that extends the {@link DefaultData} class via reflection. Note that tested
	 * class can not be abstract.
	 * 
	 * @param testingClass
	 *            Class to test.
	 * @throws InstantiationException
	 *             InstantiationException
	 * @throws IllegalAccessException
	 *             IllegalAccessException
	 * @throws SerializationException
	 *             SerializationException
	 */
	@Test(dataProvider = "classProvider")
	public void classesPlanSerialization(Class<?> testingClass) throws InstantiationException, IllegalAccessException, SerializationException {
		Object object = testingClass.newInstance();
		ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(byteBuffer);
		Output output = new Output(byteBufferOutputStream);
		serializer.serialize(object, output);
		byteBuffer.flip();
		ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(byteBuffer);
		Input input = new Input(byteBufferInputStream);
		Object deserialized = serializer.deserialize(input);
		assertThat(deserialized, is(equalTo(object)));
	}

	/**
	 * Tests that the Hibernate {@link PersistentList} can be serialized, but in way that
	 * deserialized class will be java list and but not {@link PersistentList}.
	 * 
	 * @throws SerializationException
	 *             SerializationException
	 */
	@Test
	public void hibernatePersistentList() throws SerializationException {
		PersistentList object = new PersistentList();
		ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(byteBuffer);
		Output output = new Output(byteBufferOutputStream);
		serializer.serialize(object, output);
		byteBuffer.flip();
		ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(byteBuffer);
		Input input = new Input(byteBufferInputStream);
		Object deserialized = serializer.deserialize(input);
		assertThat(deserialized, is(not(instanceOf(PersistentList.class))));
		assertThat(deserialized, is(instanceOf(List.class)));
	}

	/**
	 * Tests that the Hibernate {@link PersistentSet} can be serialized, but in way that
	 * deserialized class will be java set and but not {@link PersistentSet}.
	 * 
	 * @throws SerializationException
	 *             SerializationException
	 */
	@Test
	public void hibernatePersistentSet() throws SerializationException {
		PersistentSet object = new PersistentSet();
		ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(byteBuffer);
		Output output = new Output(byteBufferOutputStream);
		serializer.serialize(object, output);
		byteBuffer.flip();
		ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(byteBuffer);
		Input input = new Input(byteBufferInputStream);
		Object deserialized = serializer.deserialize(input);
		assertThat(deserialized, is(not(instanceOf(PersistentSet.class))));
		assertThat(deserialized, is(instanceOf(Set.class)));
	}

	/**
	 * Tests that the Hibernate {@link PersistentMap} can be serialized, but in way that
	 * deserialized class will be java map and but not {@link PersistentMap}.
	 * 
	 * @throws SerializationException
	 *             SerializationException
	 */
	@Test
	public void hibernatePersistentMap() throws SerializationException {
		PersistentMap object = new PersistentMap();
		ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(byteBuffer);
		Output output = new Output(byteBufferOutputStream);
		serializer.serialize(object, output);
		byteBuffer.flip();
		ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(byteBuffer);
		Input input = new Input(byteBufferInputStream);
		Object deserialized = serializer.deserialize(input);
		assertThat(deserialized, is(not(instanceOf(PersistentMap.class))));
		assertThat(deserialized, is(instanceOf(Map.class)));
	}

}
