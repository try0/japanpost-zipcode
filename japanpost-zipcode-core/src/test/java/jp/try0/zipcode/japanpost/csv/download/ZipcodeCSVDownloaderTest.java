package jp.try0.zipcode.japanpost.csv.download;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import jp.try0.zipcode.japanpost.csv.ZipcodeCSVProperties;
import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;
import jp.try0.zipcode.japanpost.csv.parser.ZipcodeCSVParser;

public class ZipcodeCSVDownloaderTest {

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
			for (ZipcodeCSVRow row : parser.parse()) {
				row.debugPrint();
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
