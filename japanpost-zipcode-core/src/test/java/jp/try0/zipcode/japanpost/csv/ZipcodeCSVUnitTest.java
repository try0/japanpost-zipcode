package jp.try0.zipcode.japanpost.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;
import jp.try0.zipcode.japanpost.testutil.SystemProperties;
import jp.try0.zipcode.japanpost.testutil.TestHttpServer;
import jp.try0.zipcode.japanpost.util.ZipcodeFiles;

/**
 * ネットワークを使わない{@link ZipcodeCSV}のテスト<br>
 * ダウンロードはローカルのHTTPサーバー、キャッシュは一時ディレクトリーを使用します。
 */
public class ZipcodeCSVUnitTest {

	@TempDir
	Path tempDir;

	private final SystemProperties props = new SystemProperties();

	private TestHttpServer server;

	@BeforeEach
	public void setUp() throws IOException {
		server = TestHttpServer.start();
		props.set(SystemProperties.CACHE_DIR, tempDir.resolve("cache").toString())
				.set(SystemProperties.DOWNLOAD_URL, server.url())
				.set(SystemProperties.CACHE_DAYS, "10")
				.set(SystemProperties.FALLBACK_TO_CACHE, null);
	}

	@AfterEach
	public void tearDown() {
		server.close();
		props.close();
	}

	private File sampleCsv() throws Exception {
		return Path.of(getClass().getResource("/utf_ken_all_sample.csv").toURI()).toFile();
	}

	/**
	 * キャッシュにテスト用CSVを置き、指定日数前の更新日時にします。
	 */
	private void putCache(int daysAgo) throws Exception {
		Path csv = ZipcodeFiles.getCSVPath();
		Files.createDirectories(csv.getParent());
		Files.write(csv, TestHttpServer.sampleCsv());
		Files.setLastModifiedTime(csv, FileTime.from(Instant.now().minus(Duration.ofDays(daysAgo))));
	}

	// ---- getOrDownload ----

	@Test
	public void getOrDownloadDownloadsIntoCacheDir() throws Exception {
		server.respond(200, TestHttpServer.zip("utf_ken_all.csv", TestHttpServer.sampleCsv()));

		ZipcodeCSV csv = ZipcodeCSV.getOrDownload();

		assertEquals(ZipcodeFiles.getCSVPath().toFile(), csv.getFile());
		assertEquals(3, csv.getRowsAsList().size());
		assertEquals(1, server.requestCount());
	}

	@Test
	public void getOrDownloadUsesFreshCacheWithoutDownloading() throws Exception {
		putCache(1);

		ZipcodeCSV csv = ZipcodeCSV.getOrDownload();

		assertEquals(3, csv.getRowsAsList().size());
		assertEquals(0, server.requestCount());
	}

	@Test
	public void getOrDownloadThrowsWhenDownloadFailsByDefault() throws Exception {
		putCache(30);
		server.respond(500, new byte[0]);

		assertThrows(ZipcodeCSVException.class, ZipcodeCSV::getOrDownload);
	}

	@Test
	public void getOrDownloadUsesStaleCacheWhenDownloadFailsIfEnabled() throws Exception {
		props.set(SystemProperties.FALLBACK_TO_CACHE, "true");
		putCache(30);
		server.respond(500, new byte[0]);

		ZipcodeCSV csv = ZipcodeCSV.getOrDownload();

		assertEquals(ZipcodeFiles.getCSVPath().toFile(), csv.getFile());
		assertEquals(3, csv.getRowsAsList().size());
		assertEquals(1, server.requestCount(), "ダウンロードを試行していない");
	}

	@Test
	public void getOrDownloadThrowsWhenDownloadFailsWithoutCacheEvenIfEnabled() {
		props.set(SystemProperties.FALLBACK_TO_CACHE, "true");
		server.respond(500, new byte[0]);

		assertThrows(ZipcodeCSVException.class, ZipcodeCSV::getOrDownload);
	}

	@Test
	public void getOrDownloadThrowsWhenZipHasNoCSV() throws Exception {
		server.respond(200, TestHttpServer.zip("other.txt", "dummy".getBytes(StandardCharsets.UTF_8)));

		assertThrows(ZipcodeCSVException.class, ZipcodeCSV::getOrDownload);
	}

	// ---- 設定オブジェクト（システムプロパティーには使われない値を設定しておく） ----

	private static final Charset SJIS = Charset.forName("Windows-31J");

	@Test
	public void getOrDownloadUsesGivenProperties() throws Exception {
		Path cacheDir = tempDir.resolve("code-cache");
		props.set(SystemProperties.DOWNLOAD_URL, "http://127.0.0.1:1/unused.zip");
		server.respond(200, TestHttpServer.zip("utf_ken_all.csv", TestHttpServer.sampleCsv()));
		ZipcodeCSVProperties properties = ZipcodeCSVProperties.builder()
				.downloadUrl(server.url())
				.cacheDir(cacheDir)
				.build();

		ZipcodeCSV csv = ZipcodeCSV.getOrDownload(properties);

		assertEquals(cacheDir.resolve("utf_ken_all.csv").toFile(), csv.getFile());
		assertEquals(3, csv.getRowsAsList().size());
	}

	@Test
	public void getOrDownloadUsesFallbackOfGivenProperties() throws Exception {
		Path cacheDir = tempDir.resolve("code-cache");
		Files.createDirectories(cacheDir);
		Path csvPath = Files.write(cacheDir.resolve("utf_ken_all.csv"), TestHttpServer.sampleCsv());
		Files.setLastModifiedTime(csvPath, FileTime.from(Instant.now().minus(Duration.ofDays(30))));
		server.respond(500, new byte[0]);
		ZipcodeCSVProperties properties = ZipcodeCSVProperties.builder()
				.cacheDir(cacheDir)
				.fallbackToCache(true)
				.build();

		ZipcodeCSV csv = ZipcodeCSV.getOrDownload(properties);

		assertEquals(csvPath.toFile(), csv.getFile());
		assertEquals(1, server.requestCount(), "ダウンロードを試行していない");
	}

	@Test
	public void rowsAreReadWithEncodingOfGivenProperties() throws Exception {
		ZipcodeCSV csv = ZipcodeCSV.getOrDownload(sjisCacheProperties());

		assertEquals("北海道", csv.getRowsAsList().get(0).getPrefectureName());
		assertEquals(0, server.requestCount());
	}

	@Test
	public void deserializedCSVKeepsProperties() throws Exception {
		ZipcodeCSV csv = ZipcodeCSV.getOrDownload(sjisCacheProperties());

		ZipcodeCSV copy = deserialize(serialize(csv));

		assertEquals("北海道", copy.getRowsAsList().get(0).getPrefectureName());
	}

	@Test
	public void deleteFilesUsesGivenProperties() throws Exception {
		Path cacheDir = tempDir.resolve("code-cache");
		Files.createDirectories(cacheDir);
		Files.writeString(cacheDir.resolve("utf_ken_all.csv"), "csv");

		ZipcodeCSV.deleteFiles(ZipcodeCSVProperties.builder().cacheDir(cacheDir).build());

		assertFalse(Files.exists(cacheDir));
	}

	/**
	 * Shift_JISのCSVを有効期間内のキャッシュとして置き、それを読む設定を作成します。
	 */
	private ZipcodeCSVProperties sjisCacheProperties() throws Exception {
		Path cacheDir = tempDir.resolve("sjis-cache");
		Files.createDirectories(cacheDir);
		String csv = new String(TestHttpServer.sampleCsv(), StandardCharsets.UTF_8);
		Files.write(cacheDir.resolve("utf_ken_all.csv"), csv.getBytes(SJIS));
		return ZipcodeCSVProperties.builder().cacheDir(cacheDir).csvEncoding(SJIS).build();
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

	// ---- 行データ・ファイル操作 ----

	@Test
	public void deleteRemovesItsOwnFile() throws Exception {
		File csvFile = Files.writeString(tempDir.resolve("own.csv"), "a,b,c").toFile();
		ZipcodeCSV csv = new ZipcodeCSV(csvFile);

		csv.delete();

		assertFalse(csvFile.exists(), "保持しているCSVファイルが削除されていない");
	}

	@Test
	public void openRowStreamReadsAllRows() throws Exception {
		try (Stream<ZipcodeCSVRow> rows = new ZipcodeCSV(sampleCsv()).openRowStream()) {
			assertEquals(3, rows.count());
		}
	}

	@Test
	public void withRowsReadsRowsOfItsFile() throws Exception {
		long tokyo = new ZipcodeCSV(sampleCsv())
				.withRows(rows -> rows.filter(row -> "東京都".equals(row.getPrefectureName())).count());

		assertEquals(1, tokyo);
	}

	@Test
	public void getRowsAsListReadsAllRows() throws Exception {
		assertEquals(3, new ZipcodeCSV(sampleCsv()).getRowsAsList().size());
	}
}
