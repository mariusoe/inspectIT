package info.novatec.inspectit.storage.serializer.impl;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;
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
import info.novatec.inspectit.indexing.indexer.impl.InvocationChildrenIndexer;
import info.novatec.inspectit.indexing.indexer.impl.MethodIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.ObjectTypeIndexer;
import info.novatec.inspectit.indexing.indexer.impl.PlatformIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.SensorTypeIdentIndexer;
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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
			{ CustomNumberLabelType.class }, { CustomStringLabelType.class }, { ExploredByLabelType.class }, { RatingLabelType.class }, { StatusLabelType.class }, { UseCaseLabelType.class } };

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
	 * Tests if the {@link SerializationException} will be thrown if {@link ByteBuffer} is to small.
	 * 
	 * @throws SerializationException
	 *             Serialization Exception
	 */
	@Test(expectedExceptions = { SerializationException.class })
	public void smallBufferSerialization() throws SerializationException {
		String s = "String greater than one byte";
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1);
		serializer.serialize(s, byteBuffer);
		Assert.assertTrue(true);
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
		Object data = serializer.deserialize(ByteBuffer.allocateDirect(1000));
		Assert.assertEquals(data, null);
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
		Object data = serializer.deserialize(byteBuffer);
		Assert.assertEquals(data, null);
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
		serializer.serialize(object, byteBuffer);
		byteBuffer.flip();
		Object deserialized = serializer.deserialize(byteBuffer);
		Assert.assertEquals(object, deserialized);
	}

}
