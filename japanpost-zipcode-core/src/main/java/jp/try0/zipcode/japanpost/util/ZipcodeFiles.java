package jp.try0.zipcode.japanpost.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * ファイルユーティリティ
 */
public final class ZipcodeFiles {

	public static final Path TMP_DIR_PATH = Path.of(System.getProperty("java.io.tmpdir"), "jp.try0.zipcode");
	public static final Path WORK_PATH = Path.of(TMP_DIR_PATH.toString(), "work");
	public static final Path ZIP_PATH = Path.of(TMP_DIR_PATH.toString(), "utf_ken_all.zip");
	public static final Path CSV_PATH = Path.of(TMP_DIR_PATH.toString(), "utf_ken_all.csv");

	private ZipcodeFiles() {
		throw new IllegalStateException();
	}

	public static final int BUFFER = 4096;

	/**
	 * Zipファイルを解凍してutf_ken_all.csvを取得します。
	 *
	 * @param file
	 * @return
	 * @throws java.io.IOException
	 */
	public static final File unzip(File file) throws java.io.IOException {

		WORK_PATH.toFile().mkdirs();

		try (FileInputStream fis = new FileInputStream(file);
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				int count;
				byte data[] = new byte[BUFFER];
				File entryFile = Path.of(WORK_PATH.toString(), entry.getName()).toFile();
				if (entry.isDirectory()) {
					entryFile.mkdir();
					continue;
				}

				try (FileOutputStream fos = new FileOutputStream(entryFile);
						BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);) {
					while ((count = zis.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
				}
				zis.closeEntry();

				String entryName = entry.getName();
				if (entryName.equals("utf_ken_all.csv")) {
					var csvPath = Files.copy(entryFile.toPath(), CSV_PATH, StandardCopyOption.REPLACE_EXISTING);
					return csvPath.toFile();
				}
			}
		}

		return null;
	}

	/**
	 * Zipファイルの場合、解凍してファイルを１つ取得します。
	 * 
	 * @param file
	 * @return
	 */
	public static File unzipIfNeeded(File file) {
		if (ZipcodeFiles.isZipFile(file)) {
			try {
				File csvFile = ZipcodeFiles.unzip(file);
				if (csvFile == null) {
					throw new IllegalArgumentException("ZipファイルからCSVを抽出できませんでした。");
				}

				return csvFile;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return file;
	}

	/**
	 * Zipファイルか否か判定します。
	 *
	 * @param file
	 * @return
	 */
	public static boolean isZipFile(File file) {
		try (ZipFile zipFile = new ZipFile(file)) {
			return zipFile.entries().hasMoreElements();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 一時ディレクトリーを削除します。
	 * 
	 * @throws IOException
	 */
	public static void clean() throws IOException {
		File libTmpDir = ZipcodeFiles.TMP_DIR_PATH.toFile();
		if (libTmpDir.exists()) {
			Files.delete(ZipcodeFiles.TMP_DIR_PATH);
		}
	}

}
