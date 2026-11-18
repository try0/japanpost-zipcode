package jp.try0.zipcode.japanpost.csv.download;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.try0.zipcode.japanpost.csv.ZipcodeCSVException;
import jp.try0.zipcode.japanpost.csv.ZipcodeCSVProperties;
import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;
import jp.try0.zipcode.japanpost.csv.parser.ZipcodeCSVParser;
import jp.try0.zipcode.japanpost.testutil.SystemProperties;
import jp.try0.zipcode.japanpost.testutil.TestHttpServer;
import jp.try0.zipcode.japanpost.util.ZipcodeFiles;

public class ZipcodeCSVDownloaderTest {

	@TempDir
	Path tempDir;

	Path cacheDir;

	private final SystemProperties props = new SystemProperties();

	private TestHttpServer server;

	@BeforeEach
	public void setUp() throws IOException {
		cacheDir = tempDir.resolve("cache");
		server = TestHttpServer.start();
		props.set(SystemProperties.CACHE_DIR, cacheDir.toString())
				.set(SystemProperties.DOWNLOAD_URL, server.url())
				.set(SystemProperties.CONNECTION_TIMEOUT, "2000")
				.set(SystemProperties.READ_TIMEOUT, "2000");
	}

	@AfterEach
	public void tearDown() {
		server.close();
		props.close();
	}

	private List<String> cacheFileNames() throws IOException {
		if (!Files.exists(cacheDir)) {
			return List.of();
		}
		try (Stream<Path> files = Files.list(cacheDir)) {
			return files.map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());
		}
	}

	@Test
	public void downloadSavesZipIntoCacheDir() throws Exception {
		byte[] zip = TestHttpServer.zip("utf_ken_all.csv", TestHttpServer.sampleCsv());
		server.respond(200, zip);

		File downloaded = ZipcodeCSVDownloader.get().download();

		assertEquals(ZipcodeFiles.getZipPath().toFile(), downloaded);
		assertArrayEquals(zip, Files.readAllBytes(downloaded.toPath()));
		assertEquals(List.of("utf_ken_all.zip"), cacheFileNames(), "作業用のファイルが残っている");
	}

	@Test
	public void downloadLeavesNoFileWhenServerReturnsError() throws Exception {
		server.respond(404, new byte[0]);

		assertThrows(ZipcodeCSVException.class, () -> ZipcodeCSVDownloader.get().download());

		assertEquals(List.of(), cacheFileNames());
	}

	@Test
	public void downloadKeepsPreviousZipWhenTransferIsInterrupted() throws Exception {
		Files.createDirectories(cacheDir);
		byte[] previous = "前回ダウンロードしたZip".getBytes(StandardCharsets.UTF_8);
		Files.write(ZipcodeFiles.getZipPath(), previous);
		server.respondTruncated(TestHttpServer.zip("utf_ken_all.csv", TestHttpServer.sampleCsv()));

		assertThrows(IOException.class, () -> ZipcodeCSVDownloader.get().download());

		assertArrayEquals(previous, Files.readAllBytes(ZipcodeFiles.getZipPath()), "前回のZipが壊された");
		assertEquals(List.of("utf_ken_all.zip"), cacheFileNames(), "途中までのファイルが残っている");
	}

	/**
	 * 接続は受け付けるが応答しないサーバーに対して、タイムアウトしてfalseを返すことを検証します。
	 */
	@Test
	public void existsDownloadUrlTimesOutWhenServerDoesNotRespond() throws Exception {
		try (ServerSocket silentServer = new ServerSocket(0, 50, InetAddress.getLoopbackAddress())) {
			props.set(SystemProperties.DOWNLOAD_URL, "http://127.0.0.1:" + silentServer.getLocalPort() + "/utf_ken_all.zip")
					.set(SystemProperties.CONNECTION_TIMEOUT, "500")
					.set(SystemProperties.READ_TIMEOUT, "500");

			assertTimeoutPreemptively(Duration.ofSeconds(10),
					() -> assertFalse(ZipcodeCSVDownloader.get().existsDownloadUrl()));
		}
	}

	@Test
	public void downloadUsesGivenProperties() throws Exception {
		Path dir = tempDir.resolve("code-cache");
		props.set(SystemProperties.DOWNLOAD_URL, "http://127.0.0.1:1/unused.zip");
		byte[] zip = TestHttpServer.zip("utf_ken_all.csv", TestHttpServer.sampleCsv());
		server.respond(200, zip);
		ZipcodeCSVProperties properties = ZipcodeCSVProperties.builder()
				.downloadUrl(server.url())
				.cacheDir(dir)
				.build();

		File downloaded = ZipcodeCSVDownloader.get().download(properties);

		assertEquals(dir.resolve("utf_ken_all.zip").toFile(), downloaded);
		assertArrayEquals(zip, Files.readAllBytes(downloaded.toPath()));
	}

	@Test
	public void existsDownloadUrlUsesTimeoutsOfGivenProperties() throws Exception {
		try (ServerSocket silentServer = new ServerSocket(0, 50, InetAddress.getLoopbackAddress())) {
			props.set(SystemProperties.CONNECTION_TIMEOUT, "60000")
					.set(SystemProperties.READ_TIMEOUT, "60000");
			ZipcodeCSVProperties properties = ZipcodeCSVProperties.builder()
					.downloadUrl("http://127.0.0.1:" + silentServer.getLocalPort() + "/utf_ken_all.zip")
					.connectionTimeout(Duration.ofMillis(500))
					.readTimeout(Duration.ofMillis(500))
					.build();

			assertTimeoutPreemptively(Duration.ofSeconds(10),
					() -> assertFalse(ZipcodeCSVDownloader.get().existsDownloadUrl(properties)));
		}
	}

	/**
	 * 日本郵便側でダウンロードURLが変更されていないか（リンク切れ）を検知します。
	 */
	@Test
	@Tag("network")
	public void downloadUrlExists() {
		props.set(SystemProperties.DOWNLOAD_URL, null);

		assertTrue(ZipcodeCSVDownloader.get().existsDownloadUrl(),
				"ダウンロードURLにアクセスできません: " + ZipcodeCSVProperties.get().getDownloadUrl());
	}

//	@Test
	public void downloadTest() {

		try {

			var zipFile = ZipcodeCSVDownloader.get().download();
			ZipcodeCSVParser parser = new ZipcodeCSVParser(zipFile);
			parser.forEachRow(ZipcodeCSVRow::debugPrint);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
