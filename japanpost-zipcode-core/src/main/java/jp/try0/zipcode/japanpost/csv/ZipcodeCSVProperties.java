package jp.try0.zipcode.japanpost.csv;

import java.net.HttpURLConnection;
import java.nio.file.Path;

/**
 * 設定値
 *
 */
public class ZipcodeCSVProperties {
	/**
	 * https://www.post.japanpost.jp/service/search/zipcode/download/utf/zip/utf_ken_all.zip
	 */
	public static final String DOWNLOAD_URL = "https://www.post.japanpost.jp/service/search/zipcode/download/utf/zip/utf_ken_all.zip";
	public static final String CSV_ENCODING = "UTF-8";
	public static final String CSV_CACHE_DAYS = "10";
	public static final String CSV_FALLBACK_TO_CACHE = "false";
	public static final String CONNECTION_TIMEOUT = "10000";
	public static final String READ_TIMEOUT = "60000";

	static ZipcodeCSVProperties INSTANCE = new ZipcodeCSVProperties();

	public static ZipcodeCSVProperties get() {
		return INSTANCE;
	}

	private String getOr(String key, String defaultValue) {
		try {
			String url = System.getProperty("jp.try0.zipcode." + key);
			if (url == null || url.isEmpty()) {
				return defaultValue;
			}
			return url;
		} catch (Exception ignore) {
			return defaultValue;
		}
	}

	/**
	 * ダウンロードURL（{@link #DOWNLOAD_URL}）を取得します。<br>
	 * システムプロパティーにjp.try0.zipcode.url.utf_ken_allが指定されている場合、そちらを優先します。
	 * 
	 * @return utf_ken_all.zipのダウンロードURL
	 */
	public String getDownloadUrl() {
		return getOr("url.utf_ken_all", DOWNLOAD_URL);
	}

	/**
	 * CSVファイルの文字コード（{@link #CSV_ENCODING}）を取得します。<br>
	 * システムプロパティーにjp.try0.zipcode.csv.encodingが指定されている場合、そちらを優先します。
	 *
	 * @return CSVファイルの文字コード
	 */
	public String getCSVEncoding() {
		return getOr("csv.encoding", CSV_ENCODING);
	}

	/**
	 * 一時ディレクトリーから参照する期間（{@link #CSV_CACHE_DAYS}）を取得します。<br>
	 * システムプロパティーにjp.try0.zipcode.csv.cache_daysが指定されている場合、そちらを優先します。
	 *
	 * @return 参照する日数
	 */
	public int getCacheDays() {
		return Integer.valueOf(getOr("csv.cache_days", CSV_CACHE_DAYS));
	}

	/**
	 * ダウンロードしたファイルの保存先（キャッシュ）ディレクトリーを取得します。<br>
	 * 既定は java.io.tmpdir 配下の jp.try0.zipcode です。<br>
	 * システムプロパティーにjp.try0.zipcode.csv.cache_dirが指定されている場合、そちらを優先します。
	 *
	 * @return キャッシュディレクトリー
	 */
	public Path getCacheDir() {
		String dir = getOr("csv.cache_dir", null);
		if (dir == null) {
			return Path.of(System.getProperty("java.io.tmpdir"), "jp.try0.zipcode");
		}
		return Path.of(dir);
	}

	/**
	 * ダウンロードに失敗した場合に、期限切れのキャッシュを使用するか（{@link #CSV_FALLBACK_TO_CACHE}）を取得します。<br>
	 * システムプロパティーにjp.try0.zipcode.csv.fallback_to_cacheが指定されている場合、そちらを優先します。
	 *
	 * @return trueの場合は期限切れのキャッシュを使用、falseの場合は例外をスロー
	 */
	public boolean isFallbackToCache() {
		return Boolean.parseBoolean(getOr("csv.fallback_to_cache", CSV_FALLBACK_TO_CACHE));
	}

	/**
	 * {@link HttpURLConnection#setConnectTimeout(int)}に指定する値を取得します。
	 * 
	 * @return タイムアウトミリ秒
	 */
	public int getConnectionTimeout() {
		return Integer.valueOf(getOr("http.connection_timeout", CONNECTION_TIMEOUT));
	}

	/**
	 * {@link HttpURLConnection#setReadTimeout(int)}に指定する値を取得します。
	 * 
	 * @return タイムアウトミリ秒
	 */
	public int getReadTimeout() {
		return Integer.valueOf(getOr("http.read_timeout", READ_TIMEOUT));
	}
}
