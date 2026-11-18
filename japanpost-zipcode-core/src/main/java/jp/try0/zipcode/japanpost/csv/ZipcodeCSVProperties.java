package jp.try0.zipcode.japanpost.csv;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

/**
 * 設定値<br>
 * 各項目は、Javaコードでの設定（{@link #builder()}） ＞ システムプロパティー ＞ 既定値 の順に優先します。
 *
 * <pre>
 * ZipcodeCSVProperties props = ZipcodeCSVProperties.builder()
 *         .cacheDir(Path.of("/var/cache/myapp/zipcode"))
 *         .fallbackToCache(true)
 *         .build();
 * ZipcodeCSV csv = ZipcodeCSV.getOrDownload(props);
 * </pre>
 *
 * 不変オブジェクトのため、複数のスレッドから共有できます。
 */
public final class ZipcodeCSVProperties implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * https://www.post.japanpost.jp/service/search/zipcode/download/utf/zip/utf_ken_all.zip
	 */
	public static final String DOWNLOAD_URL = "https://www.post.japanpost.jp/service/search/zipcode/download/utf/zip/utf_ken_all.zip";
	public static final String CSV_ENCODING = "UTF-8";
	public static final String CSV_CACHE_DAYS = "10";
	public static final String CSV_FALLBACK_TO_CACHE = "false";
	public static final String CONNECTION_TIMEOUT = "10000";
	public static final String READ_TIMEOUT = "60000";

	private static final String SYSTEM_PROPERTY_PREFIX = "jp.try0.zipcode.";

	/**
	 * Javaコードで何も設定していない（システムプロパティーと既定値のみの）設定
	 */
	private static final ZipcodeCSVProperties SYSTEM = new ZipcodeCSVProperties(new Builder());

	/**
	 * システムプロパティーと既定値による設定を取得します。<br>
	 * 引数に設定を取らないAPIは、この設定を使用します。
	 *
	 * @return 設定
	 */
	public static ZipcodeCSVProperties get() {
		return SYSTEM;
	}

	/**
	 * Javaコードで設定するためのビルダーを取得します。<br>
	 * 設定しなかった項目は、システムプロパティー、既定値の順に使用します。
	 *
	 * @return ビルダー
	 */
	public static Builder builder() {
		return new Builder();
	}

	// Javaコードで設定した値（未設定はnull）。シリアライズのため、PathやCharsetは文字列で保持する。
	private final String downloadUrl;
	private final String csvEncoding;
	private final Integer cacheDays;
	private final String cacheDir;
	private final Boolean fallbackToCache;
	private final Integer connectionTimeout;
	private final Integer readTimeout;

	private ZipcodeCSVProperties(Builder builder) {
		this.downloadUrl = builder.downloadUrl;
		this.csvEncoding = builder.csvEncoding;
		this.cacheDays = builder.cacheDays;
		this.cacheDir = builder.cacheDir;
		this.fallbackToCache = builder.fallbackToCache;
		this.connectionTimeout = builder.connectionTimeout;
		this.readTimeout = builder.readTimeout;
	}

	private static String getOr(String key, String defaultValue) {
		try {
			String value = System.getProperty(SYSTEM_PROPERTY_PREFIX + key);
			if (value == null || value.isEmpty()) {
				return defaultValue;
			}
			return value;
		} catch (Exception ignore) {
			return defaultValue;
		}
	}

	/**
	 * 0以上の整数のシステムプロパティーを取得します。
	 *
	 * @throws IllegalArgumentException 値が0以上の整数でない場合（メッセージにシステムプロパティー名を含みます）
	 */
	private static int getNonNegativeInt(String key, String defaultValue) {
		String value = getOr(key, defaultValue);
		int parsed;
		try {
			parsed = Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			throw invalidSystemProperty(key, value, e);
		}
		if (parsed < 0) {
			throw invalidSystemProperty(key, value, null);
		}
		return parsed;
	}

	private static IllegalArgumentException invalidSystemProperty(String key, String value, Throwable cause) {
		return new IllegalArgumentException(
				"システムプロパティー " + SYSTEM_PROPERTY_PREFIX + key + " には0以上の整数を指定してください: " + value, cause);
	}

	/**
	 * ダウンロードURL（{@link #DOWNLOAD_URL}）を取得します。<br>
	 * システムプロパティー：jp.try0.zipcode.url.utf_ken_all
	 *
	 * @return utf_ken_all.zipのダウンロードURL
	 */
	public String getDownloadUrl() {
		return downloadUrl != null ? downloadUrl : getOr("url.utf_ken_all", DOWNLOAD_URL);
	}

	/**
	 * CSVファイルの文字コード（{@link #CSV_ENCODING}）を取得します。<br>
	 * システムプロパティー：jp.try0.zipcode.csv.encoding
	 *
	 * @return CSVファイルの文字コード
	 */
	public String getCSVEncoding() {
		return csvEncoding != null ? csvEncoding : getOr("csv.encoding", CSV_ENCODING);
	}

	/**
	 * キャッシュを使用する日数（{@link #CSV_CACHE_DAYS}）を取得します。<br>
	 * システムプロパティー：jp.try0.zipcode.csv.cache_days
	 *
	 * @return キャッシュを使用する日数
	 */
	public int getCacheDays() {
		return cacheDays != null ? cacheDays : getNonNegativeInt("csv.cache_days", CSV_CACHE_DAYS);
	}

	/**
	 * ダウンロードしたファイルの保存先（キャッシュ）ディレクトリーを取得します。<br>
	 * 既定は java.io.tmpdir 配下の jp.try0.zipcode です。<br>
	 * システムプロパティー：jp.try0.zipcode.csv.cache_dir
	 *
	 * @return キャッシュディレクトリー
	 */
	public Path getCacheDir() {
		String dir = cacheDir != null ? cacheDir : getOr("csv.cache_dir", null);
		if (dir == null) {
			return Path.of(System.getProperty("java.io.tmpdir"), "jp.try0.zipcode");
		}
		return Path.of(dir);
	}

	/**
	 * ダウンロードに失敗した場合に、期限切れのキャッシュを使用するか（{@link #CSV_FALLBACK_TO_CACHE}）を取得します。<br>
	 * システムプロパティー：jp.try0.zipcode.csv.fallback_to_cache
	 *
	 * @return trueの場合は期限切れのキャッシュを使用、falseの場合は例外をスロー
	 */
	public boolean isFallbackToCache() {
		return fallbackToCache != null ? fallbackToCache
				: Boolean.parseBoolean(getOr("csv.fallback_to_cache", CSV_FALLBACK_TO_CACHE));
	}

	/**
	 * {@link HttpURLConnection#setConnectTimeout(int)}に指定する値（{@link #CONNECTION_TIMEOUT}）を取得します。<br>
	 * システムプロパティー：jp.try0.zipcode.http.connection_timeout
	 *
	 * @return タイムアウトミリ秒
	 */
	public int getConnectionTimeout() {
		return connectionTimeout != null ? connectionTimeout
				: getNonNegativeInt("http.connection_timeout", CONNECTION_TIMEOUT);
	}

	/**
	 * {@link HttpURLConnection#setReadTimeout(int)}に指定する値（{@link #READ_TIMEOUT}）を取得します。<br>
	 * システムプロパティー：jp.try0.zipcode.http.read_timeout
	 *
	 * @return タイムアウトミリ秒
	 */
	public int getReadTimeout() {
		return readTimeout != null ? readTimeout : getNonNegativeInt("http.read_timeout", READ_TIMEOUT);
	}

	/**
	 * {@link ZipcodeCSVProperties}のビルダー<br>
	 * 設定しなかった項目は、システムプロパティー、既定値の順に使用します。
	 */
	public static final class Builder {

		private String downloadUrl;
		private String csvEncoding;
		private Integer cacheDays;
		private String cacheDir;
		private Boolean fallbackToCache;
		private Integer connectionTimeout;
		private Integer readTimeout;

		private Builder() {
		}

		/**
		 * ダウンロードURLを設定します。
		 *
		 * @param url utf_ken_all.zipのダウンロードURL
		 * @return このビルダー
		 */
		public Builder downloadUrl(String url) {
			this.downloadUrl = Objects.requireNonNull(url, "url");
			return this;
		}

		/**
		 * CSVファイルの文字コードを設定します。
		 *
		 * @param charset 文字コード
		 * @return このビルダー
		 */
		public Builder csvEncoding(Charset charset) {
			this.csvEncoding = Objects.requireNonNull(charset, "charset").name();
			return this;
		}

		/**
		 * キャッシュを使用する日数を設定します。
		 *
		 * @param days 日数（0以上）
		 * @return このビルダー
		 */
		public Builder cacheDays(int days) {
			if (days < 0) {
				throw new IllegalArgumentException("cacheDays: " + days);
			}
			this.cacheDays = days;
			return this;
		}

		/**
		 * ダウンロードしたファイルの保存先（キャッシュ）ディレクトリーを設定します。
		 *
		 * @param dir 保存先ディレクトリー
		 * @return このビルダー
		 */
		public Builder cacheDir(Path dir) {
			this.cacheDir = Objects.requireNonNull(dir, "dir").toString();
			return this;
		}

		/**
		 * ダウンロードに失敗した場合に、期限切れのキャッシュを使用するかを設定します。
		 *
		 * @param fallback trueの場合は期限切れのキャッシュを使用、falseの場合は例外をスロー
		 * @return このビルダー
		 */
		public Builder fallbackToCache(boolean fallback) {
			this.fallbackToCache = fallback;
			return this;
		}

		/**
		 * 接続タイムアウトを設定します。
		 *
		 * @param timeout タイムアウト（0は無制限）
		 * @return このビルダー
		 */
		public Builder connectionTimeout(Duration timeout) {
			this.connectionTimeout = toMillis(timeout, "connectionTimeout");
			return this;
		}

		/**
		 * 読み込みタイムアウトを設定します。
		 *
		 * @param timeout タイムアウト（0は無制限）
		 * @return このビルダー
		 */
		public Builder readTimeout(Duration timeout) {
			this.readTimeout = toMillis(timeout, "readTimeout");
			return this;
		}

		private static int toMillis(Duration timeout, String name) {
			Objects.requireNonNull(timeout, name);
			if (timeout.isNegative() || timeout.toMillis() > Integer.MAX_VALUE) {
				throw new IllegalArgumentException(name + ": " + timeout);
			}
			return (int) timeout.toMillis();
		}

		/**
		 * 設定を作成します。
		 *
		 * @return 設定
		 */
		public ZipcodeCSVProperties build() {
			return new ZipcodeCSVProperties(this);
		}
	}
}
