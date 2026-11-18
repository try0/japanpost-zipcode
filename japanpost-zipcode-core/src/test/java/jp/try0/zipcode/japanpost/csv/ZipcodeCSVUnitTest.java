package jp.try0.zipcode.japanpost.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
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
