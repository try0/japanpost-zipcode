package jp.try0.zipcode.japanpost.csv.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import jp.try0.zipcode.japanpost.csv.ZipcodeCSVException;
import jp.try0.zipcode.japanpost.csv.ZipcodeCSVProperties;
import jp.try0.zipcode.japanpost.util.ZipcodeFiles;

/**
 * ファイルダウンロード
 */
public class ZipcodeCSVDownloader {

	static class Holder {
		private static final ZipcodeCSVDownloader INSTANCE = new ZipcodeCSVDownloader();
	}

	public static ZipcodeCSVDownloader get() {
		return Holder.INSTANCE;
	}

	/**
	 * utf_ken_all.zipをキャッシュディレクトリーにダウンロードします。<br>
	 * 失敗した場合は途中までのファイルを残さず、前回ダウンロードしたファイルも変更しません。
	 *
	 * @return ダウンロードしたZipファイル
	 * @throws ZipcodeCSVException HTTPステータスが200以外の場合
	 * @throws IOException         通信・書き込みに失敗した場合、または受信が途中で終了した場合
	 */
	public File download() throws MalformedURLException, IOException {
		return download(ZipcodeCSVProperties.get().getDownloadUrl());
	}

	private File download(String url) throws MalformedURLException, IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		try {
			conn.setAllowUserInteraction(false);
			conn.setInstanceFollowRedirects(true);

			var props = ZipcodeCSVProperties.get();
			conn.setConnectTimeout(props.getConnectionTimeout());
			conn.setReadTimeout(props.getReadTimeout());
			conn.setRequestMethod("GET");

			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new ZipcodeCSVException("HTTP status " + responseCode + " " + url);
			}

			Path zipPath = ZipcodeFiles.getZipPath();
			try (InputStream in = conn.getInputStream()) {
				// HttpURLConnectionは受信が途中で切れても例外にならないため、Content-Lengthと照合する
				ZipcodeFiles.write(in, zipPath, conn.getContentLengthLong());
			}
			return zipPath.toFile();
		} finally {
			conn.disconnect();
		}
	}

	/**
	 * 指定ファイルが、日本郵便のWebサーバーに配置されているか判定します。<br>
	 * リンク切れチェック用
	 *
	 * @param fileType
	 * @return
	 */
	public boolean existsDownloadUrl() {
		HttpURLConnection conn = null;
		try {
			var props = ZipcodeCSVProperties.get();
			conn = (HttpURLConnection) new URL(props.getDownloadUrl()).openConnection();
			conn.setInstanceFollowRedirects(true);
			conn.setConnectTimeout(props.getConnectionTimeout());
			conn.setReadTimeout(props.getReadTimeout());
			conn.setRequestMethod("HEAD");
			return (conn.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception ignore) {
			return false;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

}
