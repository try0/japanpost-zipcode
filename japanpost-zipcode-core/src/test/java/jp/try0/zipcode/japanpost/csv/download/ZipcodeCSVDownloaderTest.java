package jp.try0.zipcode.japanpost.csv.download;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

import jp.try0.zipcode.japanpost.csv.data.ZipcodeCSVRow;
import jp.try0.zipcode.japanpost.csv.parser.ZipcodeCSVParser;

public class ZipcodeCSVDownloaderTest {

	@Test
	public void downloadTest() {

		try {

			var zipFile = ZipcodeCSVDownloader.get().download();
			ZipcodeCSVParser parser = new ZipcodeCSVParser(zipFile);
			for (ZipcodeCSVRow row : parser.parse()) {
				row.debugPrint();
				System.out.print("\n");
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
