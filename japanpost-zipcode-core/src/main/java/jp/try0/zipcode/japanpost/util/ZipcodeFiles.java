package jp.try0.zipcode.japanpost.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * ファイルユーティリティ
 */
public final class ZipcodeFiles {

	private ZipcodeFiles() {
		throw new IllegalStateException();
	}

	public static final int BUFFER = 4096;

	/**
	 * Zipファイルを解凍してファイルを１つ取得します。
	 *
	 * @param file
	 * @return
	 * @throws java.io.IOException
	 */
	public static final File unzip(File file) throws java.io.IOException {

		try (FileInputStream fis = new FileInputStream(file);
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				int count;
				byte data[] = new byte[BUFFER];
				File entryFile = new File(System.getProperty("java.io.tmpdir") + "/" + entry.getName());
				if (entry.isDirectory()) {
					entryFile.mkdir();
					continue;
				}

				FileOutputStream fos = new FileOutputStream(entryFile);
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = zis.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				zis.closeEntry();

				String entryName = entry.getName();

				return entryFile;
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

}
