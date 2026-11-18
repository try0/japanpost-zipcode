package jp.try0.zipcode.japanpost.csv;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
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

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = System.getLogger(ZipcodeCSV.class.getName());

	/**
	 * utf_ken_all.csvを取得します。<br>
	 * キャッシュディレクトリー（{@link ZipcodeCSVProperties#getCacheDir()}）のCSVが
	 * 有効期間内（{@link ZipcodeCSVProperties#getCacheDays()}）であればそれを使用し、それ以外はダウンロードします。<br>
	 * ダウンロードに失敗した場合、{@link ZipcodeCSVProperties#isFallbackToCache()}がtrueで期限切れのキャッシュがあれば、
	 * 警告をログに出力してキャッシュを使用します。
	 *
	 * @return utf_ken_all.csv
	 * @throws IOException         ダウンロード・解凍に失敗した場合
	 * @throws ZipcodeCSVException HTTPステータスが200以外の場合、またはZipファイルにCSVが含まれていない場合
	 */
	public static ZipcodeCSV getOrDownload() throws IOException {
		ZipcodeCSVProperties props = ZipcodeCSVProperties.get();
		Path csvPath = ZipcodeFiles.getCSVPath();
		if (Files.isRegularFile(csvPath) && isFresh(csvPath, props.getCacheDays())) {
			return new ZipcodeCSV(csvPath.toFile());
		}

		try {
			File zipFile = ZipcodeCSVDownloader.get().download();
			File csvFile = ZipcodeFiles.unzip(zipFile);
			if (csvFile == null) {
				throw new ZipcodeCSVException("Zipファイルにutf_ken_all.csvが含まれていません: " + zipFile);
			}
			return new ZipcodeCSV(csvFile);
		} catch (IOException | ZipcodeCSVException e) {
			if (props.isFallbackToCache() && Files.isRegularFile(csvPath)) {
				LOGGER.log(Level.WARNING, "ダウンロードに失敗したため、期限切れのキャッシュを使用します: " + csvPath, e);
				return new ZipcodeCSV(csvPath.toFile());
			}
			throw e;
		}
	}

	private static boolean isFresh(Path csvPath, int cacheDays) throws IOException {
		Instant modified = Files.getLastModifiedTime(csvPath).toInstant();
		return modified.isAfter(Instant.now().minus(Duration.ofDays(cacheDays)));
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
