package jp.try0.zipcode.japanpost.csv;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;

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

	@Test
	public void invalidSystemPropertyReportsItsKey() {
		props.set(SystemProperties.CACHE_DAYS, "abc");

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> ZipcodeCSVProperties.get().getCacheDays());

		assertTrue(e.getMessage().contains(SystemProperties.CACHE_DAYS), e.getMessage());
	}

	@Test
	public void negativeSystemPropertyIsRejected() {
		props.set(SystemProperties.READ_TIMEOUT, "-1");

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> ZipcodeCSVProperties.get().getReadTimeout());

		assertTrue(e.getMessage().contains(SystemProperties.READ_TIMEOUT), e.getMessage());
	}

	// ---- Javaコードでの設定 ----

	@Test
	public void builderSetsAllValues(@TempDir Path dir) {
		ZipcodeCSVProperties p = ZipcodeCSVProperties.builder()
				.downloadUrl("http://example.com/utf_ken_all.zip")
				.csvEncoding(Charset.forName("Windows-31J"))
				.cacheDays(3)
				.cacheDir(dir)
				.fallbackToCache(true)
				.connectionTimeout(Duration.ofSeconds(2))
				.readTimeout(Duration.ofMillis(1500))
				.build();

		assertAll(
				() -> assertEquals("http://example.com/utf_ken_all.zip", p.getDownloadUrl()),
				() -> assertEquals(Charset.forName("Windows-31J"), Charset.forName(p.getCSVEncoding())),
				() -> assertEquals(3, p.getCacheDays()),
				() -> assertEquals(dir, p.getCacheDir()),
				() -> assertTrue(p.isFallbackToCache()),
				() -> assertEquals(2000, p.getConnectionTimeout()),
				() -> assertEquals(1500, p.getReadTimeout()));
	}

	@Test
	public void builderValuesTakePrecedenceOverSystemProperties(@TempDir Path dir) {
		props.set(SystemProperties.CACHE_DIR, dir.resolve("system").toString())
				.set(SystemProperties.CACHE_DAYS, "7");

		ZipcodeCSVProperties p = ZipcodeCSVProperties.builder().cacheDir(dir.resolve("code")).build();

		assertEquals(dir.resolve("code"), p.getCacheDir());
		assertEquals(7, p.getCacheDays(), "コードで設定していない項目はシステムプロパティーを使用する");
	}

	@Test
	public void unsetValuesFallBackToDefaults() {
		props.set(SystemProperties.READ_TIMEOUT, null);

		assertEquals(Integer.parseInt(ZipcodeCSVProperties.READ_TIMEOUT),
				ZipcodeCSVProperties.builder().build().getReadTimeout());
	}

	@Test
	public void builderRejectsInvalidValues() {
		ZipcodeCSVProperties.Builder builder = ZipcodeCSVProperties.builder();

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> builder.cacheDays(-1)),
				() -> assertThrows(IllegalArgumentException.class, () -> builder.readTimeout(Duration.ofMillis(-1))),
				() -> assertThrows(IllegalArgumentException.class,
						() -> builder.connectionTimeout(Duration.ofDays(30))),
				() -> assertThrows(NullPointerException.class, () -> builder.cacheDir(null)));
	}

	/**
	 * {@link ZipcodeCSV}が設定を保持するため、シリアライズできること（{@link Path}はSerializableではない）
	 */
	@Test
	public void propertiesAreSerializable(@TempDir Path dir) throws Exception {
		ZipcodeCSVProperties p = ZipcodeCSVProperties.builder()
				.cacheDir(dir)
				.csvEncoding(Charset.forName("Windows-31J"))
				.build();

		ZipcodeCSVProperties copy = deserialize(serialize(p));

		assertEquals(dir, copy.getCacheDir());
		assertEquals(p.getCSVEncoding(), copy.getCSVEncoding());
	}

	private static byte[] serialize(Object object) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
			out.writeObject(object);
		}
		return bytes.toByteArray();
	}

	@SuppressWarnings("unchecked")
	private static <T> T deserialize(byte[] bytes) throws Exception {
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			return (T) in.readObject();
		}
	}
}
