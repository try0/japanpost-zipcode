package jp.try0.zipcode.japanpost.csv;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jp.try0.zipcode.japanpost.util.ZipcodeFiles;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class ZipcodeCSVTest {

//	@Test
	public void cacheTest() throws IOException {
		{
			File file = ZipcodeFiles.CSV_PATH.toFile();
			if (file.exists()) {
				if (!file.delete()) {
					throw new RuntimeException();
				}
			}
		}

		ZipcodeCSV csvA = ZipcodeCSV.getOrDownload();
		var timeA = Files.getLastModifiedTime(csvA.getFile().toPath());
		Assertions.assertNotNull(csvA);

		ZipcodeCSV csvB = ZipcodeCSV.getOrDownload();
		var timeB = Files.getLastModifiedTime(csvB.getFile().toPath());
		Assertions.assertNotNull(csvB);

		Assertions.assertEquals(timeA, timeB, "一時ディレクトリーから取得できない");
	}

//	@Test
	public void deleteTest() throws IOException {
		ZipcodeCSV csv = ZipcodeCSV.getOrDownload();
		Assertions.assertTrue(csv.getFile().exists(), "削除テスト準備エラー");

		csv.delete();

		Assertions.assertFalse(csv.getFile().exists(), "削除できない");

	}

	@Test
	public void serializeTest() throws Exception {
		ZipcodeCSV csv = ZipcodeCSV.getOrDownload();
		serialize(csv);
	}

	private byte[] serialize(Object object) throws IOException {
		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);) {
			outputStream.writeObject(object);
			return byteStream.toByteArray();
		}
	}
}
