package jp.try0.zipcode.japanpost.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jp.try0.zipcode.japanpost.testutil.SystemProperties;
import jp.try0.zipcode.japanpost.testutil.TestHttpServer;

/**
 * キャッシュの保存先を一時ディレクトリーに切り替えて検証します。
 */
public final class ZipcodeFilesUnitTest {

	@TempDir
	Path tempDir;

	Path cacheDir;

	private final SystemProperties props = new SystemProperties();

	@BeforeEach
	public void useTempCacheDir() {
		cacheDir = tempDir.resolve("cache");
		props.set(SystemProperties.CACHE_DIR, cacheDir.toString());
	}

	@AfterEach
	public void restore() {
		props.close();
	}

	private static List<String> fileNames(Path dir) throws IOException {
		if (!Files.exists(dir)) {
			return List.of();
		}
		try (Stream<Path> files = Files.list(dir)) {
			return files.map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());
		}
	}

	private Path writeZip(String... entryNames) throws IOException {
		Path zip = tempDir.resolve("test.zip");
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {
			for (String name : entryNames) {
				zos.putNextEntry(new ZipEntry(name));
				zos.write(("content of " + name).getBytes(StandardCharsets.UTF_8));
				zos.closeEntry();
			}
		}
		return zip;
	}

	@Test
	public void isZipFileReturnsTrueForZip() throws IOException {
		assertTrue(ZipcodeFiles.isZipFile(writeZip("utf_ken_all.csv").toFile()));
	}

	@Test
	public void isZipFileReturnsFalseForPlainFile() throws IOException {
		Path csv = Files.writeString(tempDir.resolve("sample.csv"), "a,b,c");

		assertFalse(ZipcodeFiles.isZipFile(csv.toFile()));
	}

	@Test
	public void isZipFileReturnsFalseForMissingFile() {
		assertFalse(ZipcodeFiles.isZipFile(tempDir.resolve("missing.zip").toFile()));
	}

	@Test
	public void pathsFollowConfiguredCacheDir() {
		assertEquals(cacheDir, ZipcodeFiles.getCacheDir());
		assertEquals(cacheDir.resolve("utf_ken_all.zip"), ZipcodeFiles.getZipPath());
		assertEquals(cacheDir.resolve("utf_ken_all.csv"), ZipcodeFiles.getCSVPath());
	}

	@Test
	public void unzipExtractsCSVIntoCacheDir() throws Exception {
		Path zip = tempDir.resolve("utf_ken_all.zip");
		Files.write(zip, TestHttpServer.zip("utf_ken_all.csv", TestHttpServer.sampleCsv()));

		File csv = ZipcodeFiles.unzip(zip.toFile());

		assertEquals(ZipcodeFiles.getCSVPath().toFile(), csv);
		assertArrayEquals(TestHttpServer.sampleCsv(), Files.readAllBytes(csv.toPath()));
		assertEquals(List.of("utf_ken_all.csv"), fileNames(cacheDir), "作業用のファイルが残っている");
	}

	@Test
	public void unzipWritesNothingButCSV() throws IOException {
		String escapingEntry = "../zipslip-" + UUID.randomUUID() + ".txt";
		Path zip = writeZip(escapingEntry, "other.txt");

		File csv = ZipcodeFiles.unzip(zip.toFile());

		assertNull(csv);
		assertFalse(Files.exists(cacheDir.resolve(escapingEntry).normalize()), "キャッシュディレクトリーの外へ書き込まれた");
		assertEquals(List.of(), fileNames(cacheDir));
	}

	@Test
	public void cleanDeletesCacheFilesAndEmptyCacheDir() throws IOException {
		Files.createDirectories(cacheDir);
		Files.writeString(ZipcodeFiles.getZipPath(), "zip");
		Files.writeString(ZipcodeFiles.getCSVPath(), "csv");
		Files.writeString(cacheDir.resolve("utf_ken_all12345.tmp"), "中断したダウンロード");

		ZipcodeFiles.clean();

		assertFalse(Files.exists(cacheDir));
	}

	/**
	 * 利用者のファイルがあるディレクトリーを保存先に指定しても、ライブラリーのファイル以外は削除しないこと
	 */
	@Test
	public void cleanKeepsUnrelatedFiles() throws IOException {
		Files.createDirectories(cacheDir.resolve("work"));
		Files.writeString(ZipcodeFiles.getCSVPath(), "csv");
		Files.writeString(cacheDir.resolve("mine.txt"), "利用者のファイル");
		Files.writeString(cacheDir.resolve("work").resolve("mine.txt"), "利用者のファイル");

		ZipcodeFiles.clean();

		assertEquals(List.of("mine.txt", "work"), fileNames(cacheDir));
		assertEquals(List.of("mine.txt"), fileNames(cacheDir.resolve("work")));
	}

	@Test
	public void cleanDoesNothingWhenCacheDirIsMissing() {
		assertDoesNotThrow(ZipcodeFiles::clean);
	}
}
