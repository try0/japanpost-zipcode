package jp.try0.zipcode.japanpost.csv.download;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import jp.try0.zipcode.japanpost.csv.ZipcodeCSVProperties;
import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;
import jp.try0.zipcode.japanpost.csv.parser.ZipcodeCSVParser;

public class ZipcodeCSVDownloaderTest {

	/**
	 * 接続は受け付けるが応答しないサーバーに対して、タイムアウトしてfalseを返すことを検証します。
	 */
	@Test
	public void existsDownloadUrlTimesOutWhenServerDoesNotRespond() throws Exception {
		Map<String, String> props = Map.of(
				"jp.try0.zipcode.http.connection_timeout", "500",
				"jp.try0.zipcode.http.read_timeout", "500");
		Map<String, String> originals = new HashMap<>();

		try (ServerSocket silentServer = new ServerSocket(0, 50, InetAddress.getLoopbackAddress())) {
			originals.put("jp.try0.zipcode.url.utf_ken_all", System.getProperty("jp.try0.zipcode.url.utf_ken_all"));
			System.setProperty("jp.try0.zipcode.url.utf_ken_all",
					"http://127.0.0.1:" + silentServer.getLocalPort() + "/utf_ken_all.zip");
			props.forEach((key, value) -> {
				originals.put(key, System.getProperty(key));
				System.setProperty(key, value);
			});

			Assertions.assertTimeoutPreemptively(Duration.ofSeconds(10),
					() -> Assertions.assertFalse(ZipcodeCSVDownloader.get().existsDownloadUrl()));
		} finally {
			originals.forEach((key, value) -> {
				if (value == null) {
					System.clearProperty(key);
				} else {
					System.setProperty(key, value);
				}
			});
		}
	}

	/**
	 * 日本郵便側でダウンロードURLが変更されていないか（リンク切れ）を検知します。
	 */
	@Test
	@Tag("network")
	public void downloadUrlExists() {
		Assertions.assertTrue(ZipcodeCSVDownloader.get().existsDownloadUrl(),
				"ダウンロードURLにアクセスできません: " + ZipcodeCSVProperties.get().getDownloadUrl());
	}

//	@Test
	public void downloadTest() {

		try {

			var zipFile = ZipcodeCSVDownloader.get().download();
			ZipcodeCSVParser parser = new ZipcodeCSVParser(zipFile);
			parser.forEachRow(ZipcodeCSVRow::debugPrint);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
