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
import java.util.Objects;
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
	 * システムプロパティー・既定値の設定（{@link ZipcodeCSVProperties#get()}）で、utf_ken_all.csvを取得します。
	 *
	 * @return utf_ken_all.csv
	 * @throws IOException         ダウンロード・解凍に失敗した場合
	 * @throws ZipcodeCSVException HTTPステータスが200以外の場合、またはZipファイルにCSVが含まれていない場合
	 * @see #getOrDownload(ZipcodeCSVProperties)
	 */
	public static ZipcodeCSV getOrDownload() throws IOException {
		return getOrDownload(ZipcodeCSVProperties.get());
	}

	/**
	 * 設定に従って、utf_ken_all.csvを取得します。<br>
	 * キャッシュディレクトリー（{@link ZipcodeCSVProperties#getCacheDir()}）のCSVが
	 * 有効期間内（{@link ZipcodeCSVProperties#getCacheDays()}）であればそれを使用し、それ以外はダウンロードします。<br>
	 * ダウンロードに失敗した場合、{@link ZipcodeCSVProperties#isFallbackToCache()}がtrueで期限切れのキャッシュがあれば、
	 * 警告をログに出力してキャッシュを使用します。<br>
	 * 取得したCSVの読み込みにも、この設定（文字コード）を使用します。
	 *
	 * @param properties 設定
	 * @return utf_ken_all.csv
	 * @throws IOException         ダウンロード・解凍に失敗した場合
	 * @throws ZipcodeCSVException HTTPステータスが200以外の場合、またはZipファイルにCSVが含まれていない場合
	 */
	public static ZipcodeCSV getOrDownload(ZipcodeCSVProperties properties) throws IOException {
		Path csvPath = ZipcodeFiles.getCSVPath(properties);
		if (Files.isRegularFile(csvPath) && isFresh(csvPath, properties.getCacheDays())) {
			return new ZipcodeCSV(csvPath.toFile(), properties);
		}

		try {
			File zipFile = ZipcodeCSVDownloader.get().download(properties);
			File csvFile = ZipcodeFiles.unzip(zipFile, properties);
			if (csvFile == null) {
				throw new ZipcodeCSVException("Zipファイルにutf_ken_all.csvが含まれていません: " + zipFile);
			}
			return new ZipcodeCSV(csvFile, properties);
		} catch (IOException | ZipcodeCSVException e) {
			if (properties.isFallbackToCache() && Files.isRegularFile(csvPath)) {
				LOGGER.log(Level.WARNING, "ダウンロードに失敗したため、期限切れのキャッシュを使用します: " + csvPath, e);
				return new ZipcodeCSV(csvPath.toFile(), properties);
			}
			throw e;
		}
	}

	private static boolean isFresh(Path csvPath, int cacheDays) throws IOException {
		Instant modified = Files.getLastModifiedTime(csvPath).toInstant();
		return modified.isAfter(Instant.now().minus(Duration.ofDays(cacheDays)));
	}

	/**
	 * キャッシュディレクトリーから、このライブラリーが作成したファイルを削除します。
	 *
	 * @throws IOException 削除に失敗した場合
	 */
	public static void deleteFiles() throws IOException {
		ZipcodeFiles.clean();
	}

	/**
	 * 設定のキャッシュディレクトリーから、このライブラリーが作成したファイルを削除します。
	 *
	 * @param properties 設定
	 * @throws IOException 削除に失敗した場合
	 */
	public static void deleteFiles(ZipcodeCSVProperties properties) throws IOException {
		ZipcodeFiles.clean(properties);
	}

	/**
	 * CSVファイル
	 */
	File csvFile;

	/**
	 * 設定
	 */
	final ZipcodeCSVProperties properties;

	/**
	 * コンストラクター
	 *
	 * @param csvFile CSVファイル
	 */
	protected ZipcodeCSV(File csvFile) {
		this(csvFile, ZipcodeCSVProperties.get());
	}

	/**
	 * コンストラクター
	 *
	 * @param csvFile    CSVファイル
	 * @param properties 設定（読み込み時の文字コードに使用します）
	 */
	protected ZipcodeCSV(File csvFile, ZipcodeCSVProperties properties) {
		this.csvFile = csvFile;
		this.properties = Objects.requireNonNull(properties, "properties");
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
		return new ZipcodeCSVParser(csvFile, properties).openRowStream();
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
