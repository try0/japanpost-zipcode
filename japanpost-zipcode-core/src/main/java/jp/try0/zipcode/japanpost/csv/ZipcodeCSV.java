package jp.try0.zipcode.japanpost.csv;

import java.io.File;
import java.io.IOException;
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
public class ZipcodeCSV {

	/**
	 * utf_ken_all.csvを取得します。<br>
	 * 
	 * @return
	 * @throws IOException
	 */
	public static File getOrDownload() throws IOException {
		// 一時ディレクトリーに保持している場合はそちらを使用する。
		File file = ZipcodeCSVDownloader.TMP_FILE_PATH.toFile();
		if (file.exists()) {
			FileTime fileTime = Files.getLastModifiedTime(ZipcodeCSVDownloader.TMP_FILE_PATH);
			Instant instant = fileTime.toInstant();
			LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
			int cacheDays = ZipcodeCSVProperties.get().getCacheDays();
			if (localDateTime.isAfter(LocalDateTime.now().minusDays(cacheDays))) {
				return file;
			}
		}

		ZipcodeCSVDownloader downloader = ZipcodeCSVDownloader.getInstance();
		var zipfile = downloader.download();
		return ZipcodeFiles.unzip(zipfile);
	}

	/**
	 * CSV行データを取得します。
	 * 
	 * @return
	 * @throws IOException
	 */
	public static Iterable<ZipcodeCSVRow> getRows() throws IOException {
		File csvFile = getOrDownload();
		ZipcodeCSVParser parser = new ZipcodeCSVParser(csvFile);
		return parser.parse();
	}

	/**
	 * CSV行データを取得します。
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<ZipcodeCSVRow> getRowsAsList() throws IOException {
		List<ZipcodeCSVRow> rows = new ArrayList<>();
		getRows().forEach(rows::add);
		return rows;
	}
}
