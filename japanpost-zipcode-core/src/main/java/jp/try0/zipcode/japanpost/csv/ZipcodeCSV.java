package jp.try0.zipcode.japanpost.csv;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;
import jp.try0.zipcode.japanpost.csv.download.ZipcodeCSVDownloader;
import jp.try0.zipcode.japanpost.csv.parser.ZipcodeCSVParser;
import jp.try0.zipcode.japanpost.util.ZipcodeFiles;

/**
 * utf_ken_all.zip 郵便番号データ（1レコード1行、UTF-8形式）のユーティリティー
 */
public class ZipcodeCSV implements Serializable {

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
	 * ローカルのutf_ken_all.csvを削除します。<br>
	 * 
	 * @throws IOException
	 */
	public void delete() throws IOException {
		if (csvFile.exists()) {
			Files.delete(ZipcodeFiles.CSV_PATH);
		}
	}

	/**
	 * CSV行データを取得します。
	 * 
	 * @return 行Iterable
	 * @throws IOException
	 */
	public Iterable<ZipcodeCSVRow> getRows() throws IOException {
		ZipcodeCSVParser parser = new ZipcodeCSVParser(csvFile);
		return parser.parse();
	}

	/**
	 * CSV行データを取得します。
	 * 
	 * @return 行リスト
	 * @throws IOException
	 */
	public List<ZipcodeCSVRow> getRowsAsList() throws IOException {
		List<ZipcodeCSVRow> rows = new ArrayList<>();
		getRows().forEach(rows::add);
		return rows;
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
