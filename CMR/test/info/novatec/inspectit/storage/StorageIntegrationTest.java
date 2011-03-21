package info.novatec.inspectit.storage;

import info.novatec.inspectit.cmr.storage.CmrStorageManager;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;
import info.novatec.inspectit.storage.label.StringStorageLabel;
import info.novatec.inspectit.storage.label.type.impl.RatingLabelType;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.processor.impl.DataSaverProcessor;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests the complete CMR storage functionality.
 * 
 * @author Ivan Senic
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-storage-test.xml" })
public class StorageIntegrationTest extends AbstractTransactionalTestNGLogSupport {

	/**
	 * {@link StorageManager}.
	 */
	@Autowired
	private CmrStorageManager storageManager;

	/**
	 * {@link StorageReader} for reading the saved data.
	 */
	@Autowired
	private StorageReader storageReader;

	/**
	 * {@link ISerializer}.
	 */
	@Autowired
	private ISerializer serializer;

	/**
	 * Storage data to be used in testing.
	 */
	private StorageData storageData;

	/**
	 * List of invocations that will be written to storage and then read.
	 */
	private List<InvocationSequenceData> createdInvocations;

	/**
	 * Indexing tree of storage.
	 */
	private IStorageTreeComponent<?> storageIndexingTree;

	/**
	 * Data saver processor.
	 */
	private DataSaverProcessor dataSaverProcessor;

	/**
	 * Init.
	 */
	@BeforeClass
	public void createStorageData() {
		storageData = getStorageData();
		createdInvocations = new ArrayList<InvocationSequenceData>();
		List<Class<? extends DefaultData>> saverClasses = new ArrayList<Class<? extends DefaultData>>();
		saverClasses.add(InvocationSequenceData.class);
		dataSaverProcessor = new DataSaverProcessor(saverClasses);
	}

	/**
	 * Tests creation of storage.
	 * 
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@Test
	public void createStorageTest() throws IOException, SerializationException {
		storageManager.createStorage(storageData);

		File storageDir = getStorageFolder();
		Assert.assertTrue(storageDir.isDirectory());
		File[] storageFiles = storageDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(StorageFileExtensions.STORAGE_FILE_EXT);
			}
		});

		// only one storage file created
		Assert.assertTrue(storageFiles.length == 1);

		// get the data from file and check for equal
		byte[] storageDataBytes = Files.readAllBytes(storageFiles[0].toPath());
		ByteBuffer buffer = ByteBuffer.wrap(storageDataBytes);
		Object deserializedStorageData = serializer.deserialize(buffer);
		Assert.assertEquals(storageData, deserializedStorageData);

		storageManager.openStorage(storageData);
		Assert.assertTrue(storageData.isStorageOpened());
		Assert.assertFalse(storageData.isStorageClosed());

		// get the data from file and check for equal
		storageDataBytes = Files.readAllBytes(storageFiles[0].toPath());
		buffer = ByteBuffer.wrap(storageDataBytes);
		deserializedStorageData = serializer.deserialize(buffer);
		Assert.assertEquals(storageData, deserializedStorageData);

		// storage manager know for the storage
		Assert.assertTrue(storageManager.getExistingStorages().contains(storageData));
		Assert.assertTrue(storageManager.getOpenedStorages().contains(storageData));
		Assert.assertFalse(storageManager.getReadableStorages().contains(storageData));
		Assert.assertTrue(storageData.isStorageOpened());
		Assert.assertFalse(storageData.isStorageClosed());
	}

	/**
	 * Test write to storage.
	 * 
	 * @throws StorageException
	 *             If {@link StorageException} occurs.
	 */
	@Test(dependsOnMethods = { "createStorageTest" })
	public void testWrite() throws StorageException {
		Random random = new Random();
		int repeat = random.nextInt(100);
		List<AbstractDataProcessor> processors = new ArrayList<AbstractDataProcessor>();
		processors.add(dataSaverProcessor);
		for (int i = 0; i < repeat; i++) {
			InvocationSequenceData invoc = getInvocationSequenceDataInstance(random.nextInt(1000));
			createdInvocations.add(invoc);
		}
		storageManager.writeToStorage(storageData, createdInvocations, processors);
	}

	/**
	 * Test storage finalization.
	 * 
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws StorageException
	 *             If {@link StorageException} occurs.
	 */
	@Test(dependsOnMethods = { "testWrite" })
	public void finalizeWriteTest() throws IOException, SerializationException, StorageException {
		storageManager.closeStorage(storageData);

		Assert.assertFalse(storageData.isStorageOpened());
		Assert.assertTrue(storageData.isStorageClosed());

		File storageFolder = getStorageFolder();

		File[] indexFiles = storageFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(StorageFileExtensions.INDEX_FILE_EXT);
			}
		});
		Assert.assertTrue(indexFiles.length == 1);

		String indexFilePath = indexFiles[0].getPath();

		byte[] indexTreeBytes = Files.readAllBytes(Paths.get(indexFilePath));
		Assert.assertTrue(indexTreeBytes.length > 0);

		ByteBuffer buffer = ByteBuffer.wrap(indexTreeBytes);
		Object indexingTree = serializer.deserialize(buffer);
		Assert.assertTrue(indexingTree instanceof IStorageTreeComponent);

		storageIndexingTree = (IStorageTreeComponent<?>) indexingTree;

		Assert.assertTrue(storageManager.getReadableStorages().contains(storageData));
	}

	/**
	 * Tests reading of data from created storage.
	 * 
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	@Test(dependsOnMethods = { "finalizeWriteTest" })
	public void readTest() throws SerializationException {
		if (storageIndexingTree == null) {
			return;
		}

		StorageIndexQuery query = new StorageIndexQuery();
		List<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(InvocationSequenceData.class);
		query.setObjectClasses(searchedClasses);

		List<IStorageDescriptor> descriptors = storageIndexingTree.query(query);
		for (IStorageDescriptor descriptor : descriptors) {
			Assert.assertTrue(descriptor.getPosition() >= 0);
			Assert.assertTrue(descriptor.getSize() > 0, "Size of the descriptor is wrong. Size:" + descriptor.getSize());
		}

		byte[] result = storageReader.read(storageData, descriptors);
		ByteBuffer buffer = ByteBuffer.wrap(result);
		int count = 0;
		while (buffer.hasRemaining()) {
			Object invocation = serializer.deserialize(buffer);
			Assert.assertTrue(invocation instanceof InvocationSequenceData);
			Assert.assertTrue(createdInvocations.contains(invocation));
			count++;
		}
		Assert.assertEquals(createdInvocations.size(), count);
	}

	/**
	 * Test adding/removing of labels to a {@link StorageData} and sucessful saving to the disk.
	 * 
	 * @throws SerializationException
	 *             If serialization fails.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@Test
	public void testStorageLabels() throws IOException, SerializationException {
		RatingLabelType ratingLabelType = new RatingLabelType();
		StringStorageLabel label = new StringStorageLabel();
		label.setStorageLabelType(ratingLabelType);
		label.setStringValue("Rating");

		// test add
		storageManager.addLabelToStorage(storageData, label, true);
		for (StorageData storageToTest : storageManager.getExistingStorages()) {
			if (storageToTest.getId().equals(storageData.getId())) {
				Assert.assertTrue(storageToTest.isLabelPresent(ratingLabelType));
				Assert.assertTrue(storageToTest.getLabels(ratingLabelType).size() == 1);
				Assert.assertEquals(label, storageToTest.getLabels(ratingLabelType).get(0));
			}
		}

		// test overwrite
		label = new StringStorageLabel();
		label.setStorageLabelType(ratingLabelType);
		label.setStringValue("Rating1");
		storageManager.addLabelToStorage(storageData, label, true);
		for (StorageData storageToTest : storageManager.getExistingStorages()) {
			if (storageToTest.getId().equals(storageData.getId())) {
				Assert.assertTrue(storageToTest.isLabelPresent(ratingLabelType));
				Assert.assertTrue(storageToTest.getLabels(ratingLabelType).size() == 1);
				Assert.assertEquals(label, storageToTest.getLabels(ratingLabelType).get(0));
			}
		}

		// test no overwrite
		label = new StringStorageLabel();
		label.setStorageLabelType(ratingLabelType);
		label.setStringValue("Rating2");
		storageManager.addLabelToStorage(storageData, label, false);
		for (StorageData storageToTest : storageManager.getExistingStorages()) {
			if (storageToTest.getId().equals(storageData.getId())) {
				Assert.assertTrue(storageToTest.isLabelPresent(ratingLabelType));
				Assert.assertTrue(storageToTest.getLabels(ratingLabelType).size() == 1);
				Assert.assertNotSame(label, storageToTest.getLabels(ratingLabelType).get(0));
			}
		}

		// test remove
		label = new StringStorageLabel();
		label.setStorageLabelType(ratingLabelType);
		label.setStringValue("Rating1");
		Assert.assertTrue(storageManager.removeLabelFromStorage(storageData, label));
		for (StorageData storageToTest : storageManager.getExistingStorages()) {
			if (storageToTest.getId().equals(storageData.getId())) {
				Assert.assertFalse(storageToTest.isLabelPresent(ratingLabelType));
				Assert.assertTrue(storageToTest.getLabels(ratingLabelType).size() == 0);
			}
		}
	}

	/**
	 * Deletes created files after the test.
	 */
	@AfterTest
	public void deleteResources() {
		File storageFolder = getStorageFolder();
		if (storageFolder.exists()) {
			File[] files = storageFolder.listFiles();
			for (File file : files) {
				Assert.assertTrue(file.delete(), "Can not delete storage test file: " + file);
			}
			Assert.assertTrue(storageFolder.delete(), "Can not delete storage test folder.");
		}
	}

	/**
	 * Returns storage folder.
	 * 
	 * @return Returns storage folder.
	 */
	private File getStorageFolder() {
		return new File(storageManager.getStorageDefaultFolder() + File.separator + storageData.getStorageFolder() + File.separator);
	}

	/**
	 * @return Returns random storage data instance.
	 */
	private static StorageData getStorageData() {
		StorageData storageData = new StorageData();
		storageData.setName("My storage");
		return storageData;
	}

	/**
	 * 
	 * @return One {@link SqlStatementData} with random values.
	 */
	private static SqlStatementData getSqlStatementInstance() {
		Random random = new Random();
		SqlStatementData sqlData = new SqlStatementData(new Timestamp(random.nextLong()), random.nextLong(), random.nextLong(), random.nextLong(), "New Sql String");
		sqlData.setCount(random.nextLong());
		sqlData.setCpuDuration(random.nextDouble());
		sqlData.calculateCpuMax(random.nextDouble());
		sqlData.calculateCpuMin(random.nextDouble());
		sqlData.setDuration(random.nextDouble());
		sqlData.setExclusiveCount(random.nextLong());
		sqlData.setExclusiveDuration(random.nextDouble());
		sqlData.calculateExclusiveMax(random.nextDouble());
		sqlData.calculateExclusiveMin(random.nextDouble());
		sqlData.setId(random.nextLong());
		sqlData.addInvocationParentId(random.nextLong());
		sqlData.setPreparedStatement(true);
		return sqlData;
	}

	/**
	 * Returns the random {@link InvocationSequenceData} instance.
	 * 
	 * @param childCount
	 *            Desired child count.
	 * @return {@link InvocationSequenceData} instance.
	 */
	private static InvocationSequenceData getInvocationSequenceDataInstance(int childCount) {
		Random random = new Random();
		InvocationSequenceData invData = new InvocationSequenceData(new Timestamp(random.nextLong()), random.nextLong(), random.nextLong(), random.nextLong());
		invData.setDuration(random.nextDouble());
		invData.setId(random.nextLong());
		invData.setEnd(random.nextDouble());
		invData.setSqlStatementData(getSqlStatementInstance());
		if (childCount == 0) {
			return invData;
		}

		List<InvocationSequenceData> children = new ArrayList<InvocationSequenceData>();
		for (int i = 0; i < childCount;) {
			int childCountForChild = childCount / 10;
			if (childCountForChild + i + 1 > childCount) {
				childCountForChild = childCount - i - 1;
			}
			InvocationSequenceData child = getInvocationSequenceDataInstance(childCountForChild);
			child.setSqlStatementData(getSqlStatementInstance());
			child.setParentSequence(invData);
			children.add(child);
			i += childCountForChild + 1;

		}
		invData.setChildCount(childCount);
		invData.setNestedSequences(children);
		return invData;
	}
}
