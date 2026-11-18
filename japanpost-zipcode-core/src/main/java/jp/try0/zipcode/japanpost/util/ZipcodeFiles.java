package jp.try0.zipcode.japanpost.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import jp.try0.zipcode.japanpost.csv.ZipcodeCSVProperties;

/**
 * ファイルユーティリティ<br>
 * キャッシュディレクトリーでは、このライブラリーが作成するファイルのみ書き込み・削除します。
 */
public final class ZipcodeFiles {

	private static final String ZIP_FILE_NAME = "utf_ken_all.zip";
	private static final String CSV_FILE_NAME = "utf_ken_all.csv";

	/**
	 * 書き込み途中の一時ファイル名（utf_ken_all*.tmp）
	 */
	private static final String TEMP_FILE_PREFIX = "utf_ken_all";
	private static final String TEMP_FILE_SUFFIX = ".tmp";

	public static final int BUFFER = 4096;

	private ZipcodeFiles() {
		throw new IllegalStateException();
	}

	/**
	 * キャッシュディレクトリーを取得します。
	 *
	 * @return キャッシュディレクトリー
	 * @see ZipcodeCSVProperties#getCacheDir()
	 */
	public static Path getCacheDir() {
		return ZipcodeCSVProperties.get().getCacheDir();
	}

	/**
	 * ダウンロードしたZipファイルの保存先を取得します。
	 *
	 * @return utf_ken_all.zipのパス
	 */
	public static Path getZipPath() {
		return getCacheDir().resolve(ZIP_FILE_NAME);
	}

	/**
	 * 解凍したCSVファイルの保存先を取得します。
	 *
	 * @return utf_ken_all.csvのパス
	 */
	public static Path getCSVPath() {
		return getCacheDir().resolve(CSV_FILE_NAME);
	}

	/**
	 * Zipファイルからutf_ken_all.csvを取り出し、キャッシュディレクトリーに保存します。<br>
	 * utf_ken_all.csv以外のエントリーは書き出しません。
	 *
	 * @param file Zipファイル
	 * @return 保存したCSVファイル。Zipファイルにutf_ken_all.csvが含まれない場合はnull
	 * @throws IOException 読み込み・書き込みに失敗した場合
	 */
	public static File unzip(File file) throws IOException {
		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(file.toPath())))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory() && CSV_FILE_NAME.equals(entry.getName())) {
					Path csvPath = getCSVPath();
					write(zis, csvPath, -1);
					return csvPath.toFile();
				}
			}
		}

		return null;
	}

	/**
	 * 入力を一時ファイルに書き込んだ後、保存先を置き換えます。<br>
	 * 書き込み途中のファイルを他から参照されることはありません。
	 * 失敗した場合は一時ファイルを削除し、保存先の既存ファイルは変更しません。
	 *
	 * @param in             入力（クローズしません）
	 * @param target         保存先
	 * @param expectedLength 想定するバイト数。不明な場合は負の値
	 * @throws IOException 読み込み・書き込みに失敗した場合、またはバイト数が想定と異なる場合
	 */
	public static void write(InputStream in, Path target, long expectedLength) throws IOException {
		Path dir = target.toAbsolutePath().getParent();
		Files.createDirectories(dir);
		Path temp = Files.createTempFile(dir, TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
		try {
			long length = Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
			if (expectedLength >= 0 && length != expectedLength) {
				throw new IOException("途中で終了しました（" + length + "/" + expectedLength + "バイト）: " + target);
			}
			replace(temp, target);
		} finally {
			Files.deleteIfExists(temp);
		}
	}

	private static void replace(Path source, Path target) throws IOException {
		try {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
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
	 * キャッシュディレクトリーから、このライブラリーが作成したファイルを削除します。<br>
	 * キャッシュディレクトリーが空になった場合は、ディレクトリーも削除します。
	 *
	 * @throws IOException 削除に失敗した場合
	 */
	public static void clean() throws IOException {
		Path dir = getCacheDir();
		if (!Files.isDirectory(dir)) {
			return;
		}

		Files.deleteIfExists(dir.resolve(ZIP_FILE_NAME));
		Files.deleteIfExists(dir.resolve(CSV_FILE_NAME));
		try (DirectoryStream<Path> temps = Files.newDirectoryStream(dir, TEMP_FILE_PREFIX + "*" + TEMP_FILE_SUFFIX)) {
			for (Path temp : temps) {
				Files.deleteIfExists(temp);
			}
		}

		try {
			Files.deleteIfExists(dir);
		} catch (DirectoryNotEmptyException ignore) {
			// 利用者のファイルが残っている
		}
	}

}
