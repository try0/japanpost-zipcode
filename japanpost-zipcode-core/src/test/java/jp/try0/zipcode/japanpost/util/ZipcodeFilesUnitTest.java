package jp.try0.zipcode.japanpost.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
}
