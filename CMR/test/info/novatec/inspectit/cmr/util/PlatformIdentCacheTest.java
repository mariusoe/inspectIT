package info.novatec.inspectit.cmr.util;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.cmr.util.PlatformIdentCache;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Small test for {@link PlatformIdentCache}.
 * 
 * @author Ivan Senic
 * 
 */
public class PlatformIdentCacheTest extends AbstractTestNGLogSupport {

	private PlatformIdentCache platformIdentCache;

	@Mock
	private PlatformIdent platformIdent;

	@BeforeClass
	public void init() {
		MockitoAnnotations.initMocks(this);
		platformIdentCache = new PlatformIdentCache();
	}

	/**
	 * Tests the simple set of actions on the {@link PlatformIdentCache}.
	 */
	@Test
	public void cache() {
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
