package info.novatec.inspectit.cmr.util.test;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.cmr.util.PlatformIdentCache;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Small test for {@link PlatformIdentCache}.
 * 
 * @author Ivan Senic
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-property.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-model.xml", "classpath:spring/spring-context-util.xml" })
public class PlatformIdentCacheTest extends AbstractTestNGLogSupport {

	@Autowired
	private PlatformIdentCache platformIdentCache;

	@Mock
	private PlatformIdent platformIdent;

	@BeforeClass
	public void initMock() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests the simple set of actions on the {@link PlatformIdentCache}.
	 */
	@Test
	public void testCache() {
		Mockito.when(platformIdent.getId()).thenReturn(-1L);
		int initialSize = platformIdentCache.getSize();
		
		
		platformIdentCache.markClean(platformIdent);
		Assert.assertEquals(initialSize + 1, platformIdentCache.getSize());
		Assert.assertTrue(platformIdentCache.getCleanPlatformIdents().contains(platformIdent));
		Assert.assertTrue(platformIdentCache.getDirtyPlatformIdents().isEmpty());

		platformIdentCache.markClean(platformIdent);
		Assert.assertEquals(initialSize + 1, platformIdentCache.getSize());

		platformIdentCache.markDirty(platformIdent);
		Assert.assertEquals(initialSize + 1, platformIdentCache.getSize());
		Assert.assertTrue(platformIdentCache.getDirtyPlatformIdents().contains(platformIdent));

		platformIdentCache.remove(platformIdent);
		Assert.assertEquals(initialSize, platformIdentCache.getSize());
	}
}
