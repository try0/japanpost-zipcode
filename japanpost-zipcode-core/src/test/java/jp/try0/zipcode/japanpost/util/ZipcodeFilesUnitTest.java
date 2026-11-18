package jp.try0.zipcode.japanpost.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * unzip()は共有の一時ディレクトリー（java.io.tmpdir）へ書き込むため、ここでは副作用のないメソッドのみ検証します。
 */
public final class ZipcodeFilesUnitTest {

	@TempDir
	Path tempDir;

	@Test
	public void isZipFileReturnsTrueForZip() throws IOException {
		Path zip = tempDir.resolve("sample.zip");
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {
			zos.putNextEntry(new ZipEntry("utf_ken_all.csv"));
			zos.write("dummy".getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		assertTrue(ZipcodeFiles.isZipFile(zip.toFile()));
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
	public void unzipIfNeededReturnsSameFileForNonZip() throws IOException {
		File csv = Files.writeString(tempDir.resolve("sample.csv"), "a,b,c").toFile();

		assertEquals(csv, ZipcodeFiles.unzipIfNeeded(csv));
	}

	@Test
	public void unzipRejectsEntryOutsideWorkDirectory() throws IOException {
		String entryName = "../zipslip-" + UUID.randomUUID() + ".txt";
		Path escaped = ZipcodeFiles.WORK_PATH.resolve(entryName).normalize();
		Path zip = tempDir.resolve("zipslip.zip");
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {
			zos.putNextEntry(new ZipEntry(entryName));
			zos.write("evil".getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		try {
			assertThrows(IOException.class, () -> ZipcodeFiles.unzip(zip.toFile()));
			assertFalse(Files.exists(escaped), "作業ディレクトリーの外へ書き込まれた: " + escaped);
		} finally {
			Files.deleteIfExists(escaped);
		}
	}

	/**
	 * 実際の一時ディレクトリー（java.io.tmpdir/jp.try0.zipcode）を削除します。
	 */
	@Test
	public void cleanDeletesNonEmptyTmpDirectory() throws IOException {
		Files.createDirectories(ZipcodeFiles.WORK_PATH);
		Files.writeString(ZipcodeFiles.WORK_PATH.resolve("dummy.txt"), "dummy");

		ZipcodeFiles.clean();

		assertFalse(Files.exists(ZipcodeFiles.TMP_DIR_PATH));
	}
}
