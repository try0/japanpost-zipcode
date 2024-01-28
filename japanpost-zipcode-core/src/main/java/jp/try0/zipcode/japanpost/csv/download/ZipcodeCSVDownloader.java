package jp.try0.zipcode.japanpost.csv.download;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

	public static ZipcodeCSVDownloader getInstance() {
		return Holder.INSTANCE;
	}

	public static final Path TMP_FILE_PATH = Path.of(System.getProperty("java.io.tmpdir"), "jp.try0.zipcode",
			"utf_ken_all.zip");

	/**
	 * utf_ken_all.zipをダウンロードします。
	 *
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public File download() throws MalformedURLException, IOException {
		return download(ZipcodeCSVProperties.get().getDownloadUrl());
	}

	/**
	 *
	 * @param url
	 * @param fileName
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private File download(String url) throws MalformedURLException, IOException {

		File file = TMP_FILE_PATH.toFile();
		file.getParentFile().mkdirs();
		file.createNewFile();

		URL csvUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) csvUrl.openConnection();
		conn.setAllowUserInteraction(false);
		conn.setInstanceFollowRedirects(true);

		var props = ZipcodeCSVProperties.get();
		conn.setConnectTimeout(props.getConnectionTimeout());
		conn.setReadTimeout(props.getReadTimeout());
		conn.setRequestMethod("GET");
		conn.connect();

		int responseCode = conn.getResponseCode();
		if (responseCode != HttpURLConnection.HTTP_OK) {
			throw new ZipcodeCSVException("HTTP status " + responseCode + " " + url);
		}

		try (DataInputStream ins = new DataInputStream(conn.getInputStream())) {
			try (DataOutputStream outs = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
				byte[] b = new byte[ZipcodeFiles.BUFFER];
				int readByte = 0;

				while (-1 != (readByte = ins.read(b))) {
					outs.write(b, 0, readByte);
				}
			}
		}

		return file;
	}

	/**
	 * 指定ファイルが、日本郵便のWebサーバーに配置されているか判定します。<br>
	 * リンク切れチェック用
	 *
	 * @param fileType
	 * @return
	 */
	public boolean existsDownloadUrl() {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(ZipcodeCSVProperties.get().getDownloadUrl())
					.openConnection();
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod("HEAD");
			return (conn.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception ignore) {
			return false;
		}
	}

}
