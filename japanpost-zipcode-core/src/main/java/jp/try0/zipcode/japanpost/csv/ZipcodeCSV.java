package jp.try0.zipcode.japanpost.csv;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;
import jp.try0.zipcode.japanpost.csv.download.ZipcodeCSVDownloader;
import jp.try0.zipcode.japanpost.csv.parser.ZipcodeCSVParser;
import jp.try0.zipcode.japanpost.util.ZipcodeFiles;

/**
 * utf_ken_all.zip 郵便番号データ（1レコード1行、UTF-8形式）のユーティリティー<br>
 * 行データの読み込み方法は{@link ZipcodeCSVRowSource}を参照してください。
 */
public class ZipcodeCSV implements ZipcodeCSVRowSource, Serializable {

	/**
	 * utf_ken_all.csvを取得します。<br>
	 * 
	 * @return
	 * @throws IOException
	 */
	public static ZipcodeCSV getOrDownload() throws IOException {
		// 一時ディレクトリーに保持している場合はそちらを使用する。
		File csvFile = ZipcodeFiles.CSV_PATH.toFile();
		if (csvFile.exists()) {
			FileTime fileTime = Files.getLastModifiedTime(ZipcodeFiles.CSV_PATH);
			Instant instant = fileTime.toInstant();
			LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
			int cacheDays = ZipcodeCSVProperties.get().getCacheDays();
			if (localDateTime.isAfter(LocalDateTime.now().minusDays(cacheDays))) {
				return new ZipcodeCSV(csvFile);
			}
		}

		ZipcodeCSVDownloader downloader = ZipcodeCSVDownloader.get();
		var zipfile = downloader.download();
		return new ZipcodeCSV(ZipcodeFiles.unzip(zipfile));
	}

	/**
	 * ローカルのファイルを削除します。
	 * 
	 * @throws IOException
	 */
	public static void deleteFiles() throws IOException {
		ZipcodeFiles.clean();
	}

	/**
	 * CSVファイル
	 */
	File csvFile;

	/**
	 * コンストラクター
	 * 
	 * @param csvFile
	 */
	protected ZipcodeCSV(File csvFile) {
		this.csvFile = csvFile;
	}

	/**
	 * 保持しているCSVファイルを削除します。<br>
	 *
	 * @throws IOException
	 */
	public void delete() throws IOException {
		Files.deleteIfExists(csvFile.toPath());
	}

	@Override
	public Stream<ZipcodeCSVRow> openRowStream() throws IOException {
		return new ZipcodeCSVParser(csvFile).openRowStream();
	}

	/**
	 * ファイルを取得します。
	 * 
	 * @return CSVファイル
	 */
	public File getFile() {
		return csvFile;
	}
}
