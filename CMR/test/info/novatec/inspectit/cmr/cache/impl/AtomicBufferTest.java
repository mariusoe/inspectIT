package info.novatec.inspectit.cmr.cache.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.cache.IBufferElement;
import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;

import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.LogFactory;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testing of the functionality of the {@link AtomicBuffer}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class AtomicBufferTest extends AbstractTestNGLogSupport {

	/**
	 * Class under test.
	 */
	private AtomicBuffer<DefaultData> buffer;

	@Mock
	private BufferProperties bufferProperties;

	@Mock
	private IObjectSizes objectSizes;

	@Mock
	private IBufferTreeComponent<DefaultData> indexingTree;

	/**
	 * Init.
	 * 
	 * @throws Exception
	 */
	@BeforeMethod
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		buffer = new AtomicBuffer<>();
		buffer.bufferProperties = bufferProperties;
		buffer.objectSizes = objectSizes;
		buffer.indexingTree = indexingTree;
		buffer.log = LogFactory.getLog(AtomicBuffer.class);
		when(bufferProperties.getIndexingTreeCleaningThreads()).thenReturn(1);
		buffer.postConstruct();
	}

	/**
	 * Test that insertion will be in order.
	 */
	@Test
	public void insertElements() {
		DefaultData defaultData = mock(DefaultData.class);
		IBufferElement<DefaultData> element1 = new BufferElement<DefaultData>(defaultData);
		IBufferElement<DefaultData> element2 = new BufferElement<DefaultData>(defaultData);

		buffer.put(element1);
		buffer.put(element2);

		assertThat(buffer.getInsertedElemenets(), is(2L));
		assertThat(element1.getNextElement(), is(equalTo(element2)));
	}

	/**
	 * Tests that eviction will remove right amount of elements.
	 * 
	 * @throws Exception
	 */
	@Test
	public void eviction() throws Exception {
		// evict half of the buffer
		when(bufferProperties.getInitialBufferSize()).thenReturn(100L);
		when(bufferProperties.getEvictionOccupancyPercentage()).thenReturn(0.1f);
		when(bufferProperties.getEvictionFragmentSizePercentage()).thenReturn(0.5f);
		buffer.postConstruct();

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getObjectSize(objectSizes)).thenReturn(1L);

		BufferAnalyzer bufferAnalyzer = new BufferAnalyzer(buffer);
		bufferAnalyzer.start();

		for (int i = 0; i < 100; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			buffer.put(bufferElement);
		}

		// wait for the elements to be analyzed
		Thread.sleep(500);

		bufferAnalyzer.interrupt();

		buffer.evict();

		assertThat(buffer.getCurrentSize(), is(50L));
		assertThat(buffer.getInsertedElemenets(), is(100L));
		assertThat(buffer.getEvictedElemenets(), is(50L));
	}

	/**
	 * Tests that size of the elements is correctly analyzed and added to the buffer size.
	 * 
	 * @throws Exception
	 */
	@Test
	public void analysisAndSize() throws Exception {
		// eviction needed when 99% of the buffer is full
		when(bufferProperties.getInitialBufferSize()).thenReturn(100L);
		when(bufferProperties.getEvictionOccupancyPercentage()).thenReturn(0.99f);
		buffer.postConstruct();

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getObjectSize(objectSizes)).thenReturn(1L);

		BufferAnalyzer bufferAnalyzer = new BufferAnalyzer(buffer);
		bufferAnalyzer.start();
		IBufferElement<DefaultData> first = null;

		for (int i = 0; i < 99; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			if (0 == i) {
				first = bufferElement;
			}
			buffer.put(bufferElement);
		}

		// wait for the elements to be analyzed
		Thread.sleep(500);

		assertThat(buffer.getCurrentSize(), is(99L));
		assertThat(buffer.getOccupancyPercentage(), is(0.99f));
		assertThat(buffer.shouldEvict(), is(false));

		// add one for activating eviction
		IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
		buffer.put(bufferElement);

		// wait for the elements to be analyzed
		Thread.sleep(500);

		assertThat(buffer.getCurrentSize(), is(100L));
		assertThat(buffer.getOccupancyPercentage(), is(1f));
		assertThat(buffer.shouldEvict(), is(true));

		bufferAnalyzer.interrupt();

		for (int i = 0; i < 100; i++) {
			assertThat(first.isAnalyzed(), is(true));
			first = first.getNextElement();
		}
	}

	/**
	 * Tests that expansion rate will be used on elements size.
	 * 
	 * @throws Exception
	 */
	@Test
	public void analysisAndSizeWithExpansionRate() throws Exception {
		float expansionRate = 0.1f;
		long elementSize = 10;
		when(objectSizes.getObjectSecurityExpansionRate()).thenReturn(expansionRate);
		buffer.postConstruct();

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getObjectSize(objectSizes)).thenReturn(elementSize);

		BufferAnalyzer bufferAnalyzer = new BufferAnalyzer(buffer);
		bufferAnalyzer.start();

		for (int i = 0; i < 100; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			buffer.put(bufferElement);
		}

		// wait for the elements to be analyzed
		Thread.sleep(500);

		bufferAnalyzer.interrupt();

		assertThat(buffer.getCurrentSize(), is((long) (100 * elementSize * (1 + expansionRate))));
	}

	/**
	 * Test that elements are correctly indexed.
	 * 
	 * @throws Exception
	 */
	@Test
	public void indexing() throws Exception {
		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getObjectSize(objectSizes)).thenReturn(1L);

		BufferAnalyzer bufferAnalyzer = new BufferAnalyzer(buffer);
		bufferAnalyzer.start();

		BufferIndexer bufferIndexer = new BufferIndexer(buffer);
		bufferIndexer.start();

		IBufferElement<DefaultData> first = null;

		for (int i = 0; i < 100; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			if (0 == i) {
				first = bufferElement;
			}
			buffer.put(bufferElement);
		}

		// wait for the elements to be analyzed and indexed
		Thread.sleep(500);

		bufferAnalyzer.interrupt();
		bufferIndexer.interrupt();

		for (int i = 0; i < 100; i++) {
			assertThat(first.isIndexed(), is(true));
			first = first.getNextElement();
		}

		assertThat(buffer.getIndexedElements(), is(100L));
		verify(indexingTree, times(100)).put(defaultData);
	}

	/**
	 * Tests that the tree size calculations and maintenance is done.
	 * 
	 * @throws Exception
	 */
	@Test
	public void indexingTreeMaintenance() throws Exception {
		// when adding 30 bytes, maintenance should be done
		// indexing tree always reports 10 bytes size
		when(bufferProperties.getInitialBufferSize()).thenReturn(100L);
		when(bufferProperties.getEvictionOccupancyPercentage()).thenReturn(0.5f);
		when(bufferProperties.getEvictionFragmentSizePercentage()).thenReturn(0.35f);
		when(bufferProperties.getFlagsSetOnBytes(anyLong())).thenReturn(30L);
		when(indexingTree.getComponentSize(objectSizes)).thenReturn(10L);
		buffer.postConstruct();

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getObjectSize(objectSizes)).thenReturn(1L);

		BufferAnalyzer bufferAnalyzer = new BufferAnalyzer(buffer);
		bufferAnalyzer.start();

		BufferIndexer bufferIndexer = new BufferIndexer(buffer);
		bufferIndexer.start();

		for (int i = 0; i < 100; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			buffer.put(bufferElement);
		}

		// wait for the elements to be analyzed and indexed
		Thread.sleep(500);

		assertThat(buffer.getCurrentSize(), is(100L + 10L));
		verify(indexingTree, atLeast(1)).getComponentSize(objectSizes);

		// evict
		assertThat(buffer.shouldEvict(), is(true));
		buffer.evict();

		// now add one more element to active the cleaning of the indexing tree
		buffer.put(new BufferElement<DefaultData>(defaultData));

		// wait for the element to be analyzed and indexed
		Thread.sleep(500);

		verify(indexingTree, times(1)).cleanWithRunnable(Mockito.<ExecutorService> anyObject());
	}
}
