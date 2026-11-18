package jp.try0.zipcode.japanpost.testutil;

import java.util.HashMap;
import java.util.Map;

/**
 * システムプロパティーを変更し、{@link #close()}で元に戻すテスト用ヘルパー
 */
public final class SystemProperties implements AutoCloseable {

	public static final String DOWNLOAD_URL = "jp.try0.zipcode.url.utf_ken_all";
	public static final String ENCODING = "jp.try0.zipcode.csv.encoding";
	public static final String CACHE_DAYS = "jp.try0.zipcode.csv.cache_days";
	public static final String CACHE_DIR = "jp.try0.zipcode.csv.cache_dir";
	public static final String FALLBACK_TO_CACHE = "jp.try0.zipcode.csv.fallback_to_cache";
	public static final String CONNECTION_TIMEOUT = "jp.try0.zipcode.http.connection_timeout";
	public static final String READ_TIMEOUT = "jp.try0.zipcode.http.read_timeout";

	private final Map<String, String> originals = new HashMap<>();

	/**
	 * システムプロパティーを設定します。nullの場合は削除します。
	 */
	public SystemProperties set(String key, String value) {
		if (!originals.containsKey(key)) {
			originals.put(key, System.getProperty(key));
		}
		apply(key, value);
		return this;
	}

	@Override
	public void close() {
		originals.forEach(SystemProperties::apply);
		originals.clear();
	}

	private static void apply(String key, String value) {
		if (value == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, value);
		}
	}
}
