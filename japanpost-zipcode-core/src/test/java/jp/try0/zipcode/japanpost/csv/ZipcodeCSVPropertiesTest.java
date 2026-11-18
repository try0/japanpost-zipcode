package jp.try0.zipcode.japanpost.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.try0.zipcode.japanpost.testutil.SystemProperties;

public class ZipcodeCSVPropertiesTest {

	private final SystemProperties props = new SystemProperties();

	@AfterEach
	public void restore() {
		props.close();
	}

	@Test
	public void cacheDirDefaultsToTmpDir() {
		props.set(SystemProperties.CACHE_DIR, null);

		assertEquals(Path.of(System.getProperty("java.io.tmpdir"), "jp.try0.zipcode"),
				ZipcodeCSVProperties.get().getCacheDir());
	}

	@Test
	public void cacheDirCanBeConfigured(@TempDir Path dir) {
		props.set(SystemProperties.CACHE_DIR, dir.toString());

		assertEquals(dir, ZipcodeCSVProperties.get().getCacheDir());
	}

	@Test
	public void fallbackToCacheIsDisabledByDefault() {
		props.set(SystemProperties.FALLBACK_TO_CACHE, null);

		assertFalse(ZipcodeCSVProperties.get().isFallbackToCache());
	}

	@Test
	public void fallbackToCacheCanBeEnabled() {
		props.set(SystemProperties.FALLBACK_TO_CACHE, "true");

		assertTrue(ZipcodeCSVProperties.get().isFallbackToCache());
	}
}
